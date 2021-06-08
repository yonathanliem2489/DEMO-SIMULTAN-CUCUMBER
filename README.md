# DEMO-SIMULTAN-CUCUMBER

## Introdution
this demo is integration test using cucumber

## Requirement
1. Spring boot 2.4.x

## Configuring
### 1. Dependency
```xml
<dependency>
    <groupId>io.cucumber</groupId>
    <artifactId>cucumber-core</artifactId>
    <version>6.8.0</version>
    <scope>compile</scope>
</dependency>
<dependency>
    <groupId>io.cucumber</groupId>
    <artifactId>cucumber-java</artifactId>
    <version>6.8.0</version>
    <scope>compile</scope>
</dependency>
<dependency>
    <groupId>io.cucumber</groupId>
    <artifactId>cucumber-junit</artifactId>
    <version>6.8.0</version>
    <scope>compile</scope>
</dependency>
<dependency>
    <groupId>io.cucumber</groupId>
    <artifactId>cucumber-spring</artifactId>
    <version>6.8.0</version>
    <scope>compile</scope>
</dependency>
```

### 2. Setup Spring Configuration
#### 2.1 Setup Mongo
In this case, we want to test mongo

Setup Runner
```java
import static org.springframework.web.reactive.function.server.RequestPredicates.path;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import simultan.team.cucumber.mongo.EmployeeService;
import simultan.team.cucumber.rabbit.service.RabbitService;


@SpringBootApplication
@EnableConfigurationProperties(RestProperties.class)
public class DemoServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoServiceApplication.class, args);
	}

	@Autowired
	private RestProperties restProperties;

	@Bean
	RouterFunction<ServerResponse> endpointMongo(EmployeeService service) {
		return RouterFunctions
				.route(RequestPredicates.method(restProperties.getMongoEndpoint().getMethod())
								.and(path(restProperties.getMongoEndpoint().getPath())),
						serverRequest ->
								service.generateEmployee(buildEmployees(
										Objects.requireNonNull(serverRequest.queryParams().getFirst("employee"))))
										.flatMap(emp -> ServerResponse.ok()
												.bodyValue(emp))
				);
	}

	private List<String> buildEmployees(String employee) {
		return Arrays.asList(employee.split(","));
	}
}
```
 
#### 2.2 Setup Service
```java

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.util.Pair;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import simultan.team.cucumber.persist.Employee;

@Slf4j
public class EmployeeService {

  private final EmployeeRepository repository;

  private final ReactiveRedisTemplate<Object, Object> redisTemplate;

  private final ObjectMapper mapper;

  private Function<String, ByteBuffer> cacheKey = request ->
    ByteBuffer.wrap("employee.".concat(request).getBytes());

  public EmployeeService(ReactiveRedisTemplate reactiveRedisTemplate,
      ObjectMapper mapper, EmployeeRepository repository) {
    this.repository = repository;
    this.mapper = mapper;
    this.redisTemplate = reactiveRedisTemplate;
  }


  public Mono<List<Employee>> generateEmployee(List<String> request) {
    return Flux.fromIterable(request)
        .flatMap(employee -> repository.save(Employee.builder()
        .fullName(employee)
        .build()))
        .collectList()
        .flatMap(this::populateAndGet)
        .flatMap(employees -> repository.findAll()
            .collectList()
            .doOnNext(result -> log.info("employees {}", result))
            .flatMap(emp -> repository.deleteAll()
                .thenReturn(emp)));
  }

  private Mono<? extends List<Employee>> populateAndGet(List<Employee> employees) {
    Map<ByteBuffer, ByteBuffer> map = employees.stream()
        .map(employee -> {
          try {
            return Pair.of(cacheKey.apply(employee.getFullName()),
                ByteBuffer.wrap(mapper.writeValueAsString(employee).getBytes()));
          } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
          }
        })
        .collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));
      return
          // set cache
          redisTemplate.createMono(connection -> connection.stringCommands().mSet(map))
          // retrieve cache
          .then(redisTemplate.createMono(connection -> connection.stringCommands()
              .mGet(employees.stream().map(employee -> cacheKey.apply(employee.getFullName()))
                  .collect(Collectors.toList())))
              .flatMap(buffers -> Flux.fromIterable(buffers)
                  .flatMap(byteBuffer -> Mono.fromCallable(() ->
                      mapper.readValue(byteBuffer.array(), Employee.class)))
                  .collectList()
                  .doOnSuccess(data -> log.info("get cache employee {}", data))
                  .flatMap(data ->
                      // remove cache
                      redisTemplate.createMono(connection ->
                          connection.keyCommands().mDel(new ArrayList<>(map.keySet())))
                          .thenReturn(data))
              )
          );
  }

}
``` 

### 3. Setup Testing
#### 3.1 Setup Scenario
you must setup .feature in test resources
```
Feature: try to test mongo
  Scenario: client makes call test integration mongo
    When the client calls to test mongo
    Then the client mongo receives status code of 200
    And the client mongo receives server
```

#### 3.2 Setup Integration Test

```java
import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import io.cucumber.spring.CucumberContextConfiguration;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.TestPropertySource;
import simultan.team.cucumber.TestingConfiguration;
import simultan.team.cucumber.runner.DemoServiceApplication;

@RunWith(Cucumber.class)
@CucumberOptions(features = "src/test/resources/mongo")
@CucumberContextConfiguration
@SpringBootTest(classes = {DemoServiceApplication.class, TestingConfiguration.class},
    webEnvironment = WebEnvironment.DEFINED_PORT)
@TestPropertySource(locations = {"classpath:simultan-mongo.properties"})
public class MongoIntegrationIT {

}
```

#### 3.3 Setup Step Integration
```java
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;
import simultan.team.cucumber.persist.Employee;
import simultan.team.cucumber.runner.RestProperties;
import simultan.team.cucumber.runner.RestProperties.Endpoint;
import simultan.team.cucumber.utils.CollectionsTypeFactory;
import simultan.team.cucumber.utils.SpringIntegrationTest;
import simultan.team.cucumber.utils.TestUtils;


@EnableConfigurationProperties(RestProperties.class)
public class StepMongoIntegration extends SpringIntegrationTest {

    @Autowired
    private RestProperties restProperties;

    @Value("${server.port}")
    private String port;

    @Autowired
    private ObjectMapper mapper;

    private List<String> PERSIST_EMPLOYEE =
        Arrays.asList("yonathan","riska");

    @When("^the client calls to test mongo$")
    public void the_client_calls_to_test_mongo() throws Throwable {

        Endpoint endpoint = restProperties.getMongoEndpoint();
        URI setUpUri = TestUtils.setURI(endpoint.toBuilder().port(port).build());
        URI uri = UriComponentsBuilder.fromUri(setUpUri)
            .queryParam("employee",
                StringUtils.arrayToDelimitedString(PERSIST_EMPLOYEE.toArray(), ","))
            .build().toUri();
        executeGet(uri);
    }

    @Then("^the client mongo receives status code of (\\d+)$")
    public void the_client_mongo_receives_status_code_of(int statusCode) throws Throwable {
        final HttpStatus currentStatusCode = latestResponse.getTheResponse().getStatusCode();
        assertThat("status code is incorrect : " + latestResponse.getBody(), currentStatusCode.value(), is(statusCode));
    }

    @And("^the client mongo receives server$")
    public void the_client_mongo_receives_server_version_body() throws Throwable {

        List<Employee> response =
            mapper.readValue(latestResponse.getBody(),
                CollectionsTypeFactory.listOf(Employee.class));

        assertThat(response.stream()
            .filter(employee -> PERSIST_EMPLOYEE.stream()
                .anyMatch(employeeRequest ->
                    employeeRequest.equalsIgnoreCase(employee.getFullName())))
            .count(), is((long) PERSIST_EMPLOYEE.size()));
    }
}
```

#### 3.4 Setup properties test
```properties
server.port=9123

# Mongo
spring.data.mongodb.uri=mongodb://localhost:27017/demo-cucumber
spring.data.mongodb.database=demo-cucumber


# kafka
spring.kafka.consumer.group-id=simultan.cucumber.group
spring.kafka.consumer.enable-auto-commit=true
spring.kafka.consumer.auto-offset-reset=latest
spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer
spring.kafka.consumer.value-deserializer=org.apache.kafka.common.serialization.StringDeserializer
spring.kafka.listener.concurrency=2
```

Just Run MongoIntegrationIT and you can get do you want :)
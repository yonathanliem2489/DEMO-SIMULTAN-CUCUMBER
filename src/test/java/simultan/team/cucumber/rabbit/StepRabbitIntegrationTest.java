package simultan.team.cucumber.rabbit;

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
public class StepRabbitIntegrationTest extends SpringIntegrationTest {

    @Autowired
    private RestProperties restProperties;

    @Value("${server.port}")
    private String port;

    @Autowired
    private ObjectMapper mapper;

    private List<String> PERSIST_EMPLOYEE =
        Arrays.asList("yonathan", "riska");

    @When("^the client calls to test rabbit")
    public void the_client_calls_to_test_rabbit() throws Throwable {

        Endpoint endpoint = restProperties.getRabbitEndpoint();
        URI setUpUri = TestUtils.setURI(endpoint.toBuilder().port(port).build());
        URI uri = UriComponentsBuilder.fromUri(setUpUri)
            .queryParam("employee",
                StringUtils.arrayToDelimitedString(PERSIST_EMPLOYEE.toArray(), ","))
            .build().toUri();
        executeGet(uri);
    }

    @Then("^the client rabbit receives status code of (\\d+)$")
    public void the_client_rabbit_receives_status_code_of(int statusCode) throws Throwable {
        final HttpStatus currentStatusCode = latestResponse.getTheResponse().getStatusCode();
        assertThat("status code is incorrect : " + latestResponse.getBody(), currentStatusCode.value(), is(statusCode));
    }

    @And("^the client rabbit receives server$")
    public void the_client_rabbit_receives_server_version_body() throws Throwable {

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
package simultan.team.cucumber.runner;

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

	@Bean
	RouterFunction<ServerResponse> endpointRabbit(RabbitService service) {
		return RouterFunctions
				.route(RequestPredicates.method(restProperties.getRabbitEndpoint().getMethod())
								.and(path(restProperties.getRabbitEndpoint().getPath())),
						serverRequest ->
								service.handle(buildEmployees(
										Objects.requireNonNull(serverRequest.queryParams().getFirst("employee"))))
										.flatMap(emp -> ServerResponse.ok()
												.bodyValue(emp))
				);
	}

	private List<String> buildEmployees(String employee) {
		return Arrays.asList(employee.split(","));
	}
}

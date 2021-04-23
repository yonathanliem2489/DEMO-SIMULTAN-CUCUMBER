package simultan.team.cucumber.rabbit;

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
@CucumberOptions(features = "src/test/resources/rabbit")
@CucumberContextConfiguration
@SpringBootTest(classes = {DemoServiceApplication.class, TestingConfiguration.class},
    webEnvironment = WebEnvironment.DEFINED_PORT)
@TestPropertySource(locations = {"classpath:simultan-rabbit.properties"})
public class RabbitIntegrationIT {

}

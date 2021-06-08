package simultan.team.cucumber;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootConfiguration(proxyBeanMethods = false)
@ImportAutoConfiguration(JacksonAutoConfiguration.class)
public class TestingConfiguration {
  @Bean
  public RestTemplate getRestTemplate() {
    return new RestTemplate();
  }

}
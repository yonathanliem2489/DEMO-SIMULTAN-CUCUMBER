package simultan.team.cucumber.mongo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.data.redis.RedisReactiveAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.ReactiveRedisTemplate;

@Configuration
@AutoConfigureAfter({DataPersistenceAutoConfiguration.class,
    RedisReactiveAutoConfiguration.class})
public class MongoConfiguration {

  @Bean
  EmployeeService employeeService(EmployeeRepository repository,
      ObjectMapper mapper,
      ReactiveRedisTemplate reactiveRedisTemplate) {
    return new EmployeeService(reactiveRedisTemplate, mapper, repository);
  }
}

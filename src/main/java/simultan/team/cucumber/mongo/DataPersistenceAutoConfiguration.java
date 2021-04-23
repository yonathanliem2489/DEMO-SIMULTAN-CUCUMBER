package simultan.team.cucumber.mongo;


import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;


@Configuration
@EnableReactiveMongoRepositories
@EnableMongoAuditing(modifyOnCreate = false)
public class DataPersistenceAutoConfiguration {
}

package simultan.team.cucumber.kafka.publisher;


import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import simultan.team.cucumber.kafka.KafkaProperties;
import simultan.team.cucumber.persist.Employee;

@Slf4j
public class KafkaPublisher {


  private final ReactiveKafkaProducerTemplate<String, String> kafkaProducerTemplate;
  private final KafkaProperties kafkaProperties;
  private final ObjectMapper mapper;

  public KafkaPublisher(
      ReactiveKafkaProducerTemplate<String, String> kafkaProducerTemplate,
      ObjectMapper mapper, KafkaProperties kafkaProperties) {
    this.kafkaProducerTemplate = kafkaProducerTemplate;
    this.mapper = mapper;
    this.kafkaProperties = kafkaProperties;
  }

  public Mono<Void> send(List<String> request) {
    return Flux.fromIterable(request)
        .doOnNext(employee -> log.info("start send message {}", employee))
        .flatMap(employee -> Mono.fromCallable(() -> mapper.writeValueAsString(Employee.builder()
            .id(UUID.randomUUID().toString())
            .fullName(employee)
            .build())))
        .flatMap(employee -> Mono.fromRunnable(() ->
            kafkaProducerTemplate.send(kafkaProperties.getCucumber().getName(), employee)))
        .collectList().then();
  }
}

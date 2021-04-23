package simultan.team.cucumber.rabbit.publisher;


import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import simultan.team.cucumber.persist.Employee;
import simultan.team.cucumber.rabbit.utils.RabbitUtils;

@Slf4j
public class RabbitPublisher {

  private final RabbitTemplate rabbitTemplate;

  private ReactiveRedisTemplate<Object, Object> redisTemplate;

  public RabbitPublisher(RabbitTemplate rabbitTemplate,
      ReactiveRedisTemplate<Object, Object> redisTemplate) {
    this.rabbitTemplate = rabbitTemplate;
    this.redisTemplate = redisTemplate;
  }

  public Mono<Void> send(List<String> request) {
    return Flux.fromIterable(request)
        .doOnNext(employee -> log.info("start send message {}", employee))
        .flatMap(employee -> Mono.fromRunnable(() ->
            rabbitTemplate.convertAndSend(Employee.builder()
              .id(UUID.randomUUID().toString())
              .fullName(employee)
              .build())))
        .collectList().then();
  }
}

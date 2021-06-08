package simultan.team.cucumber.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.ByteBuffer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import reactor.core.publisher.Mono;
import simultan.team.cucumber.kafka.utils.KafkaUtils;
import simultan.team.cucumber.persist.Employee;

@Slf4j
public class KafkaConsumer {

  private final ReactiveRedisTemplate<Object, Object> redisTemplate;
  private final ObjectMapper objectMapper;


  public KafkaConsumer(ReactiveRedisTemplate<Object, Object> redisTemplate,
      ObjectMapper objectMapper) {
    this.redisTemplate = redisTemplate;
    this.objectMapper = objectMapper;
  }

  @KafkaListener(
      topics = {"${simultan.team.kafka.cucumber.name:simultan.team.cucumber.kafka}"},
      clientIdPrefix = "demo.cucumber")
  public void handleMessage(String message) {
    Mono.fromCallable(() -> objectMapper.readValue(message, Employee.class))
        .doOnNext(employee -> log.info("receive message queue {}", employee.getFullName()))
        .doOnError(throwable -> log.error("error parse caused, message {}",
            throwable.getMessage()))
        .flatMap(request -> Mono.fromCallable(() -> objectMapper.writeValueAsString(request))
            .flatMap(data -> redisTemplate.createMono(connection -> connection.stringCommands()
                .set(KafkaUtils.cacheKey.apply(request.getFullName()), ByteBuffer.wrap(data.getBytes()))))
        )
        .block();
  }

}

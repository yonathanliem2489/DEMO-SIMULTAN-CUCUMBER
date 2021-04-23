package simultan.team.cucumber.rabbit.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.ByteBuffer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import reactor.core.publisher.Mono;
import simultan.team.cucumber.persist.Employee;
import simultan.team.cucumber.rabbit.utils.RabbitUtils;

@Slf4j
public class RabbitConsumer {

  private final ReactiveRedisTemplate<Object, Object> redisTemplate;
  private final ObjectMapper objectMapper;

  public RabbitConsumer(ReactiveRedisTemplate<Object, Object> redisTemplate,
      ObjectMapper objectMapper) {
    this.redisTemplate = redisTemplate;
    this.objectMapper = objectMapper;
  }

  @RabbitListener(
      queues = {"${simultan.team.rabbit.cucumber.queue.name:simultan.team.cucumber.queue}"},
      ackMode = "AUTO")
  public void handleMessage(Message message) {
    Mono.fromCallable(() -> objectMapper.readValue(message.getBody(), Employee.class))
        .doOnNext(employee -> log.info("receive message queue {}", employee.getFullName()))
        .doOnError(throwable -> log.error("error parse caused, message {}",
            throwable.getMessage()))
        .flatMap(request -> Mono.fromCallable(() -> objectMapper.writeValueAsString(request))
            .flatMap(data -> redisTemplate.createMono(connection -> connection.stringCommands()
                .set(RabbitUtils.cacheKey.apply(request.getFullName()), ByteBuffer.wrap(data.getBytes()))))
        )
        .block();
  }

}

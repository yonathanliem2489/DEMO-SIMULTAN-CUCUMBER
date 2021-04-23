package simultan.team.cucumber.rabbit.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import reactor.core.publisher.Mono;
import simultan.team.cucumber.persist.Employee;
import simultan.team.cucumber.rabbit.consumer.RabbitConsumer;
import simultan.team.cucumber.rabbit.publisher.RabbitPublisher;
import simultan.team.cucumber.rabbit.utils.RabbitUtils;

public class RabbitService {

  private final RabbitPublisher publisher;

  private final RabbitConsumer consumer;

  private final ReactiveRedisTemplate<Object, Object> redisTemplate;

  private final ObjectMapper mapper;

  public RabbitService(RabbitPublisher publisher,
      RabbitConsumer consumer,
      ReactiveRedisTemplate<Object, Object> redisTemplate,
      ObjectMapper mapper) {
    this.publisher = publisher;
    this.consumer = consumer;
    this.redisTemplate = redisTemplate;
    this.mapper = mapper;
  }

  public Mono<List<Employee>> handle(List<String> request) {
    return publisher.send(request)
        .then(checkData(request));
  }

  private Mono<List<Employee>> checkData(List<String> request) {
    List<Employee> employees = new ArrayList<>();
    return Mono.fromCallable(() -> request.stream()
        .map(employee -> RabbitUtils.cacheKey.apply(employee))
        .collect(Collectors.toList()))
        // wait for get data from redis
        .delayElement(Duration.ofSeconds(5))
        .flatMap(buffers ->
            // get data
            redisTemplate
            .createMono(connection -> connection.stringCommands().mGet(buffers))
            .flatMap(byteBuffers ->
                // remove cache data
                redisTemplate
                .createMono(connection -> connection.keyCommands().mDel(buffers))
                .thenReturn(byteBuffers)))
        .handle((byteBuffers, sink) -> {
          byteBuffers.forEach(byteBuffer ->
          {
            try {
              employees.add(mapper.readValue(byteBuffer.array(), Employee.class));
            } catch (IOException e) {
              sink.error(new IllegalArgumentException("error parse object"));
            }
          });
          sink.complete();
        }).thenReturn(employees);
  }

}

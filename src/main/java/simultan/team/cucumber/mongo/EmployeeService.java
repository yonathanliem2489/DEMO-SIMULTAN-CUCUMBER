package simultan.team.cucumber.mongo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.util.Pair;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import simultan.team.cucumber.persist.Employee;

@Slf4j
public class EmployeeService {

  private final EmployeeRepository repository;

  private final ReactiveRedisTemplate<Object, Object> redisTemplate;

  private final ObjectMapper mapper;

  private Function<String, ByteBuffer> cacheKey = request ->
    ByteBuffer.wrap("employee.".concat(request).getBytes());

  public EmployeeService(ReactiveRedisTemplate reactiveRedisTemplate,
      ObjectMapper mapper, EmployeeRepository repository) {
    this.repository = repository;
    this.mapper = mapper;
    this.redisTemplate = reactiveRedisTemplate;
  }


  public Mono<List<Employee>> generateEmployee(List<String> request) {
    return Flux.fromIterable(request)
        .flatMap(employee -> repository.save(Employee.builder()
        .fullName(employee)
        .build()))
        .collectList()
        .flatMap(this::populateAndGet)
        .flatMap(employees -> repository.findAll()
            .collectList()
            .doOnNext(result -> log.info("employees {}", result))
            .flatMap(emp -> repository.deleteAll()
                .thenReturn(emp)));
  }

  private Mono<? extends List<Employee>> populateAndGet(List<Employee> employees) {
    Map<ByteBuffer, ByteBuffer> map = employees.stream()
        .map(employee -> {
          try {
            return Pair.of(cacheKey.apply(employee.getFullName()),
                ByteBuffer.wrap(mapper.writeValueAsString(employee).getBytes()));
          } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
          }
        })
        .collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));
      return
          // set cache
          redisTemplate.createMono(connection -> connection.stringCommands().mSet(map))
          // retrieve cache
          .then(redisTemplate.createMono(connection -> connection.stringCommands()
              .mGet(employees.stream().map(employee -> cacheKey.apply(employee.getFullName()))
                  .collect(Collectors.toList())))
              .flatMap(buffers -> Flux.fromIterable(buffers)
                  .flatMap(byteBuffer -> Mono.fromCallable(() ->
                      mapper.readValue(byteBuffer.array(), Employee.class)))
                  .collectList()
                  .doOnSuccess(data -> log.info("get cache employee {}", data))
                  .flatMap(data ->
                      // remove cache
                      redisTemplate.createMono(connection ->
                          connection.keyCommands().mDel(new ArrayList<>(map.keySet())))
                          .thenReturn(data))
              )
          );
  }

}

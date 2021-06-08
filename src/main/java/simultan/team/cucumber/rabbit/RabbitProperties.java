package simultan.team.cucumber.rabbit;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties(prefix = "simultan.team.rabbit")
public class RabbitProperties {

  private static final String BASE_NAME = "simultan.team.cucumber";
  private static final String QUEUE_NAME = BASE_NAME + ".queue";
  private static final String EXCHANGE_NAME = BASE_NAME + ".exchange";

  @NestedConfigurationProperty
  CucumberQueue cucumber = CucumberQueue.builder()
      .queue(Queue.builder()
          .durable(false)
          .name(QUEUE_NAME)
          .build())
      .exchangeName(EXCHANGE_NAME)
      .routingKey(BASE_NAME + ".key")
      .build();


  @Getter
  @Setter
  public static class Queue {
    String name;

    boolean durable;

    boolean exclusive;

    boolean autoDelete;

    @lombok.Builder(builderClassName = "Builder")
    Queue(String name, boolean durable, boolean exclusive, boolean autoDelete) {
      this.name = name;
      this.durable = durable;
      this.exclusive = exclusive;
      this.autoDelete = autoDelete;
    }
  }

  @Getter
  @Setter
  public static class CucumberQueue {
    Queue queue;
    String exchangeName;
    String routingKey;

    @lombok.Builder(builderClassName = "Builder")
    CucumberQueue(Queue queue, String exchangeName, String routingKey) {
      this.queue = queue;
      this.exchangeName = exchangeName;
      this.routingKey = routingKey;
    }
  }
}
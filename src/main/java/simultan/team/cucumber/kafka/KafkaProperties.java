package simultan.team.cucumber.kafka;

import java.io.Serializable;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties(prefix = "simultan.team.kafka")
public class KafkaProperties {

  @NestedConfigurationProperty
  private String producerServer = "localhost:9092";

  @NestedConfigurationProperty
  private String consumerServer = "localhost:9092";

  @NestedConfigurationProperty
  private String consumerGroupId = "simultan.cucumber";

  @NestedConfigurationProperty
  private String consumerClientId = "client1";


  @NestedConfigurationProperty
  private Topic cucumber = Topic.builder()
      .name("simultan.team.cucumber.kafka")
      .partitions(4)
      .build();

  @Getter
  @Setter
  public static class Topic implements Serializable {
    String name;
    int partitions;

    @lombok.Builder(builderClassName = "Builder")
    public Topic(String name, int partitions) {
      this.name = name;
      this.partitions = partitions;
    }
  }
}

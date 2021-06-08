package simultan.team.cucumber.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.protocol.types.Field.Str;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.sender.SenderOptions;
import simultan.team.cucumber.kafka.consumer.KafkaConsumer;
import simultan.team.cucumber.kafka.publisher.KafkaPublisher;

@Configuration
@EnableConfigurationProperties(KafkaProperties.class)
public class KafkaConfiguration {

  @Autowired
  private KafkaProperties kafkaProperties;

  @Bean
  NewTopic declareTopic() {
    return TopicBuilder.name(kafkaProperties.getCucumber().getName())
        .partitions(kafkaProperties.getCucumber().getPartitions())
        .build();
  }

//  class KafkaProducerConfiguration {
//    @Bean
//    public ReactiveKafkaProducerTemplate<String, String> reactiveKafkaProducerTemplate() {
//      Map<String, Object> configProps = new HashMap<>();
//      configProps.put(
//          ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
//          kafkaProperties.getProducerServer());
//      configProps.put(
//          ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
//          StringSerializer.class);
//      configProps.put(
//          ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
//          StringSerializer.class);
//      SenderOptions<String, String> senderOptions = SenderOptions.create(configProps);
//      return new ReactiveKafkaProducerTemplate<>(senderOptions);
//    }
//  }
//
//  class KafkaConsumerConfiguration {
//    @Bean
//    public ReactiveKafkaConsumerTemplate<String, String> reactiveKafkaConsumerTemplate() {
//      Map<String, Object> configProps = new HashMap<>();
//      configProps.put(
//          ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
//          kafkaProperties.getConsumerServer());
//      configProps.put(
//          ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
//          StringSerializer.class);
//      configProps.put(
//          ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
//          StringSerializer.class);
//      configProps.put(
//          ConsumerConfig.GROUP_ID_CONFIG,
//          kafkaProperties.getConsumerGroupId());
//      configProps.put(
//          ConsumerConfig.CLIENT_ID_CONFIG,
//          kafkaProperties.getConsumerClientId());
//
//      ReceiverOptions<String, String> receiverOptions = ReceiverOptions.create(configProps);
//      return new ReactiveKafkaConsumerTemplate<>(receiverOptions);
//    }
//  }
//
//  @Bean
//  KafkaPublisher kafkaPublisher(
//      ReactiveKafkaProducerTemplate<String, String> kafkaProducerTemplate, ObjectMapper mapper) {
//    return new KafkaPublisher(kafkaProducerTemplate, mapper, kafkaProperties);
//  }
//
//  @Bean
//  KafkaConsumer kafkaConsumer(ReactiveRedisTemplate<Object, Object> redisTemplate,
//      ObjectMapper mapper) {
//    return new KafkaConsumer(redisTemplate, mapper);
//  }
}

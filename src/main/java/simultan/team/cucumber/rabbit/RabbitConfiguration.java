package simultan.team.cucumber.rabbit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import simultan.team.cucumber.rabbit.consumer.RabbitConsumer;
import simultan.team.cucumber.rabbit.publisher.RabbitPublisher;
import simultan.team.cucumber.rabbit.service.RabbitService;

@Configuration
@AutoConfigureAfter(RabbitAutoConfiguration.class)
@EnableConfigurationProperties(RabbitProperties.class)
public class RabbitConfiguration {

  @Autowired
  private RabbitProperties rabbitProperties;

  @Bean
  Queue cucumberQueue() {
    return QueueBuilder.durable(rabbitProperties.getCucumber().getQueue().getName())
        .build();
  }

  @Bean
  TopicExchange cucumberExchange() {
    return ExchangeBuilder.topicExchange(rabbitProperties.getCucumber().getExchangeName()).build();
  }

  @Bean
  Binding cucumberBinding(Queue cucumberQueue, TopicExchange cucumberExchange) {
    return BindingBuilder.bind(cucumberQueue).to(cucumberExchange)
        .with(rabbitProperties.getCucumber().getRoutingKey());
  }

  @Bean
  RabbitPublisher rabbitPublisher(RabbitTemplate rabbitTemplate,
      ReactiveRedisTemplate<Object, Object> redisTemplate, ObjectMapper mapper) {
    rabbitTemplate.setDefaultReceiveQueue(rabbitProperties.getCucumber().getQueue().getName());
    rabbitTemplate.setExchange(rabbitProperties.getCucumber().getExchangeName());
    rabbitTemplate.setRoutingKey(rabbitProperties.getCucumber().getRoutingKey());
    rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter(mapper));
    return new RabbitPublisher(rabbitTemplate, redisTemplate);
  }

  @Bean
  RabbitConsumer rabbitConsumer(ReactiveRedisTemplate<Object, Object> redisTemplate,
      ObjectMapper mapper) {
    return new RabbitConsumer(redisTemplate, mapper);
  }

  @Bean
  RabbitService rabbitService(RabbitPublisher rabbitPublisher,
      RabbitConsumer rabbitConsumer, ReactiveRedisTemplate<Object, Object> redisTemplate,
      ObjectMapper mapper) {
    return new RabbitService(rabbitPublisher, rabbitConsumer, redisTemplate, mapper);
  }
}

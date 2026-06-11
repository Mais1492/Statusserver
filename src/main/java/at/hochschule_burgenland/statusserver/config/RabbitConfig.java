package at.hochschule_burgenland.statusserver.config;

import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.core.*;

@Configuration
public class RabbitConfig {

  @Value("${status.instance-id}")
  private String instanceId;

  // --- QUEUE ---
  @Bean
  public Queue statusQueue() {
    return new Queue("statusQueue-" + instanceId, true);
  }

  // --- EXCHANGE ---
  @Bean
  public FanoutExchange statusExchange() {
    return new FanoutExchange("statusExchange");
  }

  // --- BINDING ---
  @Bean
  public Binding binding(Queue statusQueue, FanoutExchange statusExchange) {
    return BindingBuilder.bind(statusQueue).to(statusExchange);
  }

  // --- JSON CONVERTER ---
  @Bean
  public Jackson2JsonMessageConverter messageConverter() {
    return new Jackson2JsonMessageConverter();
  }

  // --- RABBIT TEMPLATE (producer) ---
  @Bean
  public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                       MessageConverter messageConverter) {
    RabbitTemplate template = new RabbitTemplate(connectionFactory);
    template.setMessageConverter(messageConverter);
    return template;
  }

  // --- LISTENER FACTORY (consumer) ---
  @Bean
  public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
      ConnectionFactory connectionFactory,
      MessageConverter messageConverter) {

    SimpleRabbitListenerContainerFactory factory =
        new SimpleRabbitListenerContainerFactory();

    factory.setConnectionFactory(connectionFactory);
    factory.setMessageConverter(messageConverter);

    return factory;
  }
}
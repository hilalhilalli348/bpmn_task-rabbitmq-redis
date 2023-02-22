package com.example.bpmn_task.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.ArrayList;

@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmqProperties.queue-name}")
    private String queueName;
    @Value("${rabbitmqProperties.routing-name}")
    private String rountingName;
    @Value("${rabbitmqProperties.exchange-name}")
    private String exchangeName;

    @Bean
    public Queue queue(){
        return new Queue(queueName);
    }

    @Bean
    public TopicExchange topicExchange(){
        return new TopicExchange(exchangeName);
    }

    @Bean
    public Binding binding(){
        return  BindingBuilder.bind(queue()).to(topicExchange()).with(rountingName);
    }


}

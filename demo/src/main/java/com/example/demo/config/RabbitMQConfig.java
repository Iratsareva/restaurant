package com.example.demo.config;

import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;


@Configuration
public class RabbitMQConfig {
    public static final String EXCHANGE_NAME = "restaurant-exchange";
    public static final String ROUTING_KEY_RESERVATION_CREATED = "reservation.created";
    public static final String ROUTING_KEY_RESERVATION_DELETED = "reservation.deleted";
    public static final String FANOUT_EXCHANGE = "reservation-fanout";


    @Bean
    public TopicExchange restaurantExchange() {
        return new TopicExchange(EXCHANGE_NAME, true, false);  // durable=true
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (!ack) {
                System.out.println("NACK: Message delivery failed! " + cause);
            }
        });
        return rabbitTemplate;
    }

    @Bean
    public FanoutExchange reservationFanoutExchange() {
        return new FanoutExchange(FANOUT_EXCHANGE, true, false);
    }

}
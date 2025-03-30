package com.ticketmate.backend.object.mongo.rabbitmq;

public class RabbitMq {
    /**
     * 환경변수로 빼는거 고민
     */
    public static final String CHAT_QUEUE_NAME = "chat.queue";
    public static final String CHAT_EXCHANGE_NAME = "chat.exchange";
    public static final String ROUTING_KEY = "room.*";
}

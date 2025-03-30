package com.ticketmate.backend.test.service;

import com.ticketmate.backend.test.dto.request.ChatMessageRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import static com.ticketmate.backend.object.mongo.rabbitmq.RabbitMq.CHAT_EXCHANGE_NAME;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final RabbitTemplate rabbitTemplate;

    public void sendMessage(ChatMessageRequest message) {
        rabbitTemplate.convertAndSend(CHAT_EXCHANGE_NAME, "chat.room." + message.getRoomId(), message);
    }
}

package com.ticketmate.backend.global.config.beans;

import com.ticketmate.backend.global.config.properties.WebSocketProperties;
import com.ticketmate.backend.global.exception.stomp.StompExceptionHandler;
import com.ticketmate.backend.global.util.rabbit.StompChannelInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompReactorNettyCodec;
import org.springframework.messaging.tcp.reactor.ReactorNettyTcpClient;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import reactor.netty.tcp.TcpClient;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
@EnableConfigurationProperties(WebSocketProperties.class)
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

  private final StompChannelInterceptor channelInterceptor;
  private final StompExceptionHandler stompExceptionHandler;
  private final WebSocketProperties properties;


  @Override
  public void configureMessageBroker(MessageBrokerRegistry registry) {
    TcpClient tcpClient = TcpClient.create()
        .host(properties.getHost())
        .port(properties.getStompPort());

    ReactorNettyTcpClient<byte[]> client = new ReactorNettyTcpClient<>(tcpClient, new StompReactorNettyCodec());

    registry.enableStompBrokerRelay("/queue", "/topic", "/exchange", "/amq/queue")
        .setAutoStartup(true)
        .setTcpClient(client)  // RabbitMQ와 연결할 클라이언트
        .setRelayHost(properties.getHost())  // // RabbitMQ 서버 주소
        .setRelayPort(properties.getStompPort())
        .setClientLogin(properties.getUsername())  // 계정
        .setClientPasscode(properties.getPassword());  // 비빌번호

    registry.setApplicationDestinationPrefixes("/pub");  // 클라이언트에서 메시지 송신 시 프리픽스
    registry.setPathMatcher(new AntPathMatcher("."));  // url을 chat/room/3 -> chat.room.3으로 참조하기 위한 설정

    registry.setUserDestinationPrefix("/user"); // Error를 받기 위한 subscribe 주소
  }

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    // 웹소켓 연결을 위한 엔드포인트 등록 및 SockJS 폴백 지원
    registry.setErrorHandler(stompExceptionHandler)
        .addEndpoint("/chat")
        .setAllowedOriginPatterns("*")  // TODO CORS URL 설정
        .withSockJS();  // JS 라이브러리
  }

  @Override
  public void configureClientInboundChannel(ChannelRegistration registration) {
    registration.interceptors(channelInterceptor);
  }
}


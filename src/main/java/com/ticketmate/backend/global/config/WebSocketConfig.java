package com.ticketmate.backend.global.config;

import com.ticketmate.backend.global.exception.stomp.StompExceptionHandler;
import com.ticketmate.backend.global.util.rabbit.StompChannelInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
@Slf4j
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

  private final StompChannelInterceptor channelInterceptor;
  private final StompExceptionHandler stompExceptionHandler;
  @Value("${rabbitmq.host}")
  private String host;
  @Value("${rabbitmq.username}")
  private String username;
  @Value("${rabbitmq.password}")
  private String password;
  @Value("${rabbitmq.stomp-port}")
  private int stompPort;

  @Override
  public void configureMessageBroker(MessageBrokerRegistry registry) {
    TcpClient tcpClient = TcpClient.create()
        .host(host)
        .port(stompPort);

    ReactorNettyTcpClient<byte[]> client = new ReactorNettyTcpClient<>(tcpClient, new StompReactorNettyCodec());

    registry.enableStompBrokerRelay("/queue", "/topic", "/exchange", "/amq/queue")
        .setAutoStartup(true)
        .setTcpClient(client)  // RabbitMQ와 연결할 클라이언트
        .setRelayHost(host)  // // RabbitMQ 서버 주소
        .setRelayPort(stompPort)  // RabbitMQ 포트(5672), STOMP(61613)
        .setClientLogin(username)  // 계정
        .setClientPasscode(password);  // 비빌번호

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


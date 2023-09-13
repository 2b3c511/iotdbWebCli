package com.timecho.iotdbwebcli.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.HandshakeInterceptor;

@Configuration
@EnableWebSocket
public class WebSSHWebSocketConfig implements WebSocketConfigurer {
  @Autowired WebSocketHandler webSocketHandler;

  @Autowired HandshakeInterceptor interceptor;

  /**
   * 指定socket通道的处理器和路径
   *
   * @param registry
   */
  @Override
  public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
    registry
        .addHandler(webSocketHandler, "/webssh")
        .addInterceptors(interceptor)
        .setAllowedOrigins("*");
  }
}

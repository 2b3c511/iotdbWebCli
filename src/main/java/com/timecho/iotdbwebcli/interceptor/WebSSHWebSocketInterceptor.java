package com.timecho.iotdbwebcli.interceptor;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import com.timecho.iotdbwebcli.constant.CommonConstants;

import java.util.Map;
import java.util.UUID;

@Component
public class WebSSHWebSocketInterceptor implements HandshakeInterceptor {
  @Override
  public boolean beforeHandshake(
      ServerHttpRequest request,
      ServerHttpResponse response,
      WebSocketHandler wsHandler,
      Map<String, Object> attributes)
      throws Exception {
    if (request instanceof ServletServerHttpRequest) {
      String uuid = UUID.randomUUID().toString().replaceAll("-", "");
      attributes.put(CommonConstants.CONNECTION_KEY, uuid);
      return true;
    }
    return false;
  }

  @Override
  public void afterHandshake(
      ServerHttpRequest request,
      ServerHttpResponse response,
      WebSocketHandler wsHandler,
      Exception exception) {}
}

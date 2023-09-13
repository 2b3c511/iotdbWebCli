package com.timecho.iotdbwebcli.service;

import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

public interface WebSSHService {
  /**
   * @description: 初始化ssh连接
   * @param session
   */
  void initConnection(WebSocketSession session);

  void receiveHandler(WebSocketSession session, String buffer);

  void responseHandler(WebSocketSession session, byte[] buffer) throws IOException;

  void closeConnection(WebSocketSession session);
}

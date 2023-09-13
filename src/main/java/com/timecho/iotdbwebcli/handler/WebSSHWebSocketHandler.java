package com.timecho.iotdbwebcli.handler;

import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import com.timecho.iotdbwebcli.constant.CommonConstants;
import com.timecho.iotdbwebcli.service.WebSSHService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class WebSSHWebSocketHandler implements WebSocketHandler {
  @Autowired private WebSSHService sshService;
  /**
   * @Description:用户连接上WebSocket的回调
   *
   * @param session
   * @throws Exception
   */
  @Override
  public void afterConnectionEstablished(WebSocketSession session) throws Exception {
    log.info("用户:{}，连接webCli", session.getAttributes().get(CommonConstants.CONNECTION_KEY));
    // 初始化连接
    sshService.initConnection(session);
  }

  /**
   * @Description:收到消息的回调
   *
   * @param session
   * @param message
   * @throws Exception
   */
  @Override
  public void handleMessage(WebSocketSession session, WebSocketMessage<?> message)
      throws Exception {
    if (message instanceof TextMessage) {
      log.info(
          "用户:{},发送命令:{}",
          MapUtils.getString(session.getAttributes(), CommonConstants.CONNECTION_KEY),
          message.toString());
      // 接受消息
      sshService.receiveHandler(session, ((TextMessage) message).getPayload());
    } else if (message instanceof BinaryMessage) {

    } else if (message instanceof PongMessage) {

    } else {
      log.info("Unexpected WebSocket Message Type: " + message);
    }
  }

  /**
   * @Description:出现错误的回调
   *
   * @param session
   * @param exception
   * @throws Exception
   */
  @Override
  public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
    log.error("数据传输错误", exception);
  }

  /**
   * @Description: 连接关闭的回调
   *
   * @param session
   * @param closeStatus
   * @throws Exception
   */
  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus)
      throws Exception {
    log.info(
        "用户:{},断开webCli连接",
        MapUtils.getString(session.getAttributes(), CommonConstants.CONNECTION_KEY));
    // 关闭连接
    sshService.closeConnection(session);
  }

  @Override
  public boolean supportsPartialMessages() {
    return false;
  }
}

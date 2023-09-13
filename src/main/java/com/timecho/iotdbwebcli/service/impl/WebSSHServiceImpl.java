package com.timecho.iotdbwebcli.service.impl;

import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcraft.jsch.*;

import com.timecho.iotdbwebcli.constant.CommonConstants;
import com.timecho.iotdbwebcli.service.WebSSHService;
import com.timecho.iotdbwebcli.vo.SSHConnectionInfo;
import com.timecho.iotdbwebcli.vo.SSHDataVO;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
public class WebSSHServiceImpl implements WebSSHService {
  private ExecutorService executorService = Executors.newCachedThreadPool();

  private static Map<String, Object> sshInfoMap = new ConcurrentHashMap<>();

  @Override
  public void initConnection(WebSocketSession session) {
    JSch jSch = new JSch();
    SSHConnectionInfo connectionInfo =
        SSHConnectionInfo.builder().session(session).jSch(jSch).build();
    String connectionKey =
        MapUtils.getString(session.getAttributes(), CommonConstants.CONNECTION_KEY, "");
    sshInfoMap.put(connectionKey, connectionInfo);
  }

  @Override
  public void receiveHandler(WebSocketSession session, String buffer) {
    ObjectMapper objectMapper = new ObjectMapper();
    SSHDataVO dataVO = null;
    try {
      dataVO = objectMapper.readValue(buffer, SSHDataVO.class);
    } catch (JsonProcessingException e) {
      log.error("信息转换异常", e);
      return;
    }
    String connectionKey =
        MapUtils.getString(session.getAttributes(), CommonConstants.CONNECTION_KEY, "");
    if (CommonConstants.OPERATE_CONNECT.equals(dataVO.getOperate())) {
      SSHConnectionInfo connectionInfo = (SSHConnectionInfo) sshInfoMap.get(connectionKey);
      SSHDataVO finalDataVO = dataVO;
      executorService.execute(
          () -> {
            try {
              connectDevice(connectionInfo, finalDataVO, session);
            } catch (JSchException | IOException e) {
              log.error("ssh连接异常", e);
              closeConnection(session);
            }
          });
    } else if (CommonConstants.OPERATE_COMMAND.equals(dataVO.getOperate())) {
      String command = dataVO.getCommand();
      SSHConnectionInfo connectionInfo = (SSHConnectionInfo) sshInfoMap.get(connectionKey);
      if (connectionInfo != null) {
        try {
          sendCommand(connectionInfo.getChannel(), command);
        } catch (IOException e) {
          log.error("ssh连接异常", e);
          closeConnection(session);
        }
      }
    } else {
      log.error("不支持的操作：" + dataVO);
      closeConnection(session);
    }
  }

  @Override
  public void responseHandler(WebSocketSession session, byte[] buffer) throws IOException {
    session.sendMessage(new TextMessage(buffer));
  }

  @Override
  public void closeConnection(WebSocketSession session) {
    String connectionKey =
        MapUtils.getString(session.getAttributes(), CommonConstants.CONNECTION_KEY, "");
    SSHConnectionInfo connectionInfo = (SSHConnectionInfo) sshInfoMap.get(connectionKey);
    if (connectionInfo != null) {
      Channel channel = connectionInfo.getChannel();
      if (channel != null) {
        channel.disconnect();
      }
      sshInfoMap.remove(connectionKey);
    }
  }

  private void connectDevice(
      SSHConnectionInfo connectionInfo, SSHDataVO dataVO, WebSocketSession session)
      throws JSchException, IOException {
    Session jschSession =
        connectionInfo
            .getJSch()
            .getSession(dataVO.getUsername(), dataVO.getHost(), dataVO.getPort());
    Properties config = new Properties();
    config.put("StrictHostKeyChecking", "no");
    jschSession.setConfig(config);
    jschSession.setPassword(dataVO.getPassword());
    Channel channel = null;
    InputStream inputStream = null;
    try {
      jschSession.connect(CommonConstants.CONNECTION_TIMEOUT);
      channel = jschSession.openChannel("shell");
      channel.connect(CommonConstants.CHANNEL_TIMEOUT);

      connectionInfo.setChannel(channel);

      sendCommand(channel, "\r");

      inputStream = channel.getInputStream();
      byte[] buffer = new byte[1024];
      int i = 0;
      while ((i = inputStream.read(buffer)) != -1) {
        responseHandler(session, Arrays.copyOfRange(buffer, 0, i));
      }
    } finally {
      jschSession.disconnect();
      if (channel != null) {
        channel.disconnect();
      }
      if (inputStream != null) {
        inputStream.close();
      }
    }
  }

  private void sendCommand(Channel channel, String command) throws IOException {
    if (channel != null) {
      OutputStream outputStream = channel.getOutputStream();
      outputStream.write(command.getBytes());
      outputStream.flush();
    }
  }
}

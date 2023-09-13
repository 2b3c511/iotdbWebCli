package com.timecho.iotdbwebcli.vo;

import org.springframework.web.socket.WebSocketSession;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SSHConnectionInfo {

  private WebSocketSession session;

  private JSch jSch;

  private Channel channel;

  //  private IoTDBConnection connection;
}

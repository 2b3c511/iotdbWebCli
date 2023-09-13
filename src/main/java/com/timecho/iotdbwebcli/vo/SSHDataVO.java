package com.timecho.iotdbwebcli.vo;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"password"})
public class SSHDataVO {

  private String operate;

  private String host;

  private Integer port = 22;

  private String username;

  private String password;

  private String command = "";
}

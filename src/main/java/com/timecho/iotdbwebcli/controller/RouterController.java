package com.timecho.iotdbwebcli.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class RouterController {

  @RequestMapping("/cliPage")
  public String indexPage() {
    return "webssh";
  }
}

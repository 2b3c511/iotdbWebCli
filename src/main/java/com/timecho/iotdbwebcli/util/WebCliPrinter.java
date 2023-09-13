package com.timecho.iotdbwebcli.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import io.netty.handler.codec.string.LineSeparator;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

@Component
public class WebCliPrinter {
  private WebSocketSession session;

  private PrintStream getPrintStream() {
    return new PrintStream(System.out);
  }

  public void printf(String format, Object... args) {
    //        getPrintStream().printf(format, args);
    // TODO 补充printf逻辑

  }

  public void print(String msg) throws IOException {
    //        getPrintStream().print(msg);

    byte[] buffer = new byte[1024];
    int from = 0;
    int to = 1024;
    while (from < msg.length()) {
      to = Math.min(to, msg.length());
      session.sendMessage(new TextMessage(Arrays.copyOfRange(msg.getBytes(), from, to)));
      from += buffer.length;
      to += buffer.length;
    }
  }

  public void printException(Exception msg) throws IOException {
    //        getPrintStream().println(msg);
    print(msg.getMessage() + LineSeparator.DEFAULT.value());
  }

  public void println() throws IOException {
    //        getPrintStream().println();
    print(LineSeparator.DEFAULT.value());
  }

  public void println(String msg) throws IOException {
    //        getPrintStream().println(msg);
    print(msg + LineSeparator.DEFAULT.value());
  }

  public void printBlockLine(List<Integer> maxSizeList) throws IOException {
    StringBuilder blockLine = new StringBuilder();
    for (Integer integer : maxSizeList) {
      blockLine.append("+").append(StringUtils.repeat("-", integer));
    }
    blockLine.append("+");
    println(blockLine.toString());
  }

  public void printRow(List<List<String>> lists, int i, List<Integer> maxSizeList)
      throws IOException {
    printf("|");
    int count;
    int maxSize;
    String element;
    StringBuilder paddingStr;
    for (int j = 0; j < maxSizeList.size(); j++) {
      maxSize = maxSizeList.get(j);
      element = lists.get(j).get(i);
      count = computeHANCount(element);

      if (count > 0) {
        int remain = maxSize - (element.length() + count);
        if (remain > 0) {
          paddingStr = padding(remain);
          maxSize = maxSize - count;
          element = paddingStr.append(element).toString();
        } else if (remain == 0) {
          maxSize = maxSize - count;
        }
      }

      printf("%" + maxSize + "s|", element);
    }
    println();
  }

  public void printCount(int cnt) throws IOException {
    if (cnt == 0) {
      println("Empty set.");
    } else {
      println("Total line number = " + cnt);
    }
  }

  public static StringBuilder padding(int count) {
    StringBuilder sb = new StringBuilder();
    for (int k = 0; k < count; k++) {
      sb.append(' ');
    }

    return sb;
  }

  /** compute the number of Chinese characters included in the String */
  public static int computeHANCount(String s) {
    return (int)
        s.codePoints()
            .filter(
                codePoint -> Character.UnicodeScript.of(codePoint) == Character.UnicodeScript.HAN)
            .count();
  }
}

package com.timecho.iotdbwebcli.util;

import org.apache.iotdb.db.qp.sql.SqlLexer;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Token;
import org.jline.reader.Highlighter;
import org.jline.reader.LineReader;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;

import java.util.regex.Pattern;

import static org.jline.utils.AttributedStyle.DEFAULT;
import static org.jline.utils.AttributedStyle.GREEN;

public class IoTDBSyntexHighlighter implements Highlighter {

  private static final AttributedStyle KEYWORD_STYLE = DEFAULT.foreground(GREEN);

  @Override
  public AttributedString highlight(LineReader lineReader, String buffer) {
    CharStream stream = CharStreams.fromString(buffer);
    SqlLexer tokenSource = new SqlLexer(stream);
    tokenSource.removeErrorListeners();
    AttributedStringBuilder builder = new AttributedStringBuilder();
    while (true) {
      Token token = tokenSource.nextToken();
      int type = token.getType();
      if (type == Token.EOF) {
        break;
      }
      String text = token.getText();

      if (isKeyWord(text)) {
        builder.styled(KEYWORD_STYLE, text);
      } else {
        builder.append(text);
      }
    }
    return builder.toAttributedString();
  }

  @Override
  public void setErrorPattern(Pattern pattern) {}

  @Override
  public void setErrorIndex(int errorIndex) {}

  private boolean isKeyWord(String token) {
    return JlineUtils.SQL_KEYWORDS.contains(token.toUpperCase());
  }
}

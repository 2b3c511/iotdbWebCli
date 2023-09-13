package com.timecho.iotdbwebcli.util;

import org.apache.iotdb.db.qp.sql.SqlLexer;

import org.jline.reader.LineReader;
import org.jline.reader.LineReader.Option;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.impl.DefaultParser;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.terminal.Size;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.OSUtils;
import org.jline.widget.AutosuggestionWidgets;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class JlineUtils {
  private JlineUtils() {}

  public static final Pattern SQL_KEYWORD_PATTERN = Pattern.compile("([A-Z_]+)");
  public static final Set<String> SQL_KEYWORDS =
      IntStream.range(0, SqlLexer.VOCABULARY.getMaxTokenType())
          .mapToObj(SqlLexer.VOCABULARY::getDisplayName)
          .filter(Objects::nonNull)
          .filter(w -> SQL_KEYWORD_PATTERN.matcher(w).matches())
          .collect(Collectors.toSet());

  public static LineReader getLineReader(String username, String host, String port)
      throws IOException {
    // Defaulting to a dumb terminal when a supported terminal can not be correctly created
    // see https://github.com/jline/jline3/issues/291
    Terminal terminal = TerminalBuilder.builder().dumb(true).build();
    if (terminal.getWidth() == 0 || terminal.getHeight() == 0) {
      // Hard coded terminal size when redirecting
      terminal.setSize(new Size(120, 40));
    }
    Thread executeThread = Thread.currentThread();
    // Register signal handler. Instead of shutting down the process, interrupt the current thread
    // when signal INT is received (usually by pressing CTRL+C).
    terminal.handle(Terminal.Signal.INT, signal -> executeThread.interrupt());

    LineReaderBuilder builder = LineReaderBuilder.builder();
    builder.terminal(terminal);

    // Handle the command history. By default, the number of commands will not exceed 500 and the
    // size of the history fill will be less than 10KB. See:
    /// org.jline.reader.impl.history#DefaultHistory
    String historyFile = ".iotdb_history";
    String historyFilePath =
        System.getProperty("user.home")
            + File.separator
            + historyFile
            + "-"
            + host.hashCode()
            + "-"
            + port
            + "-"
            + username.hashCode();
    builder.variable(LineReader.HISTORY_FILE, new File(historyFilePath));

    // TODO: since the lexer doesn't produce tokens for quotation marks, disable the highlighter to
    // avoid incorrect inputs.
    //    builder.highlighter(new IoTDBSyntexHighlighter());

    builder.completer(new StringsCompleter(SQL_KEYWORDS));

    builder.option(Option.CASE_INSENSITIVE_SEARCH, true);
    builder.option(Option.CASE_INSENSITIVE, true);
    // See: https://www.gnu.org/software/bash/manual/html_node/Event-Designators.html
    builder.option(Option.DISABLE_EVENT_EXPANSION, true);

    DefaultParser parser = new DefaultParser();
    builder.parser(parser);
    LineReader lineReader = builder.build();
    if (OSUtils.IS_WINDOWS) {
      // If enabled cursor remains in begin parenthesis (gitbash).
      lineReader.setVariable(LineReader.BLINK_MATCHING_PAREN, 0);
    }

    // Create autosuggestion widgets
    AutosuggestionWidgets autosuggestionWidgets = new AutosuggestionWidgets(lineReader);
    // Enable autosuggestions
    autosuggestionWidgets.enable();
    return lineReader;
  }
}

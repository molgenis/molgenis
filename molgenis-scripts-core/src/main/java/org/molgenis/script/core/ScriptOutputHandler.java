package org.molgenis.script.core;

import java.util.function.Consumer;

public class ScriptOutputHandler {
  private final StringBuilder output = new StringBuilder();
  private final Consumer<String> outputConsumer = this::append;

  public void append(String line) {
    output.append(line);
  }

  public Consumer<String> getConsumer() {
    return outputConsumer;
  }

  @Override
  public String toString() {
    return output.toString();
  }
}

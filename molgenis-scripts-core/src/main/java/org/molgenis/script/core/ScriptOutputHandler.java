package org.molgenis.script.core;

public class ScriptOutputHandler {
  private final StringBuilder output = new StringBuilder();

  public void append(String line) {
    output.append(line);
  }

  @Override
  public String toString() {
    return output.toString();
  }
}

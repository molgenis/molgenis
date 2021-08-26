package org.molgenis.python;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.molgenis.script.core.ScriptOutputHandler;

public class PythonStreamHandler implements Runnable {
  private final InputStream in;
  private final ScriptOutputHandler outputHandler;
  private final Thread thread = new Thread(this);

  public PythonStreamHandler(InputStream in, ScriptOutputHandler outputHandler) {
    this.in = in;
    this.outputHandler = outputHandler;
  }

  public void start() {
    thread.start();
  }

  @Override
  public void run() {
    try {
      BufferedReader br = new BufferedReader(new InputStreamReader(in, UTF_8));
      String line;
      while ((line = br.readLine()) != null) {
        outputHandler.append(line + "\n");
      }
    } catch (IOException e) {
      throw new MolgenisPythonException("Error reading python outputstream", e);
    }
  }
}

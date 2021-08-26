package org.molgenis.python;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import org.molgenis.script.core.ScriptOutputHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Executes a Python script with the Python version installed on server executable in a new process.
 */
@Service
public class PythonScriptExecutor {

  private static final Logger LOG = LoggerFactory.getLogger(PythonScriptExecutor.class);
  private final String pythonScriptExecutable;

  public PythonScriptExecutor(
      @Value("${python_script_executable:/usr/bin/python}") String pythonScriptExecutable) {
    if (pythonScriptExecutable == null) {
      throw new IllegalArgumentException("pythonExecutable is null");
    }

    this.pythonScriptExecutable = pythonScriptExecutable;
  }

  /** Execute a python script and wait for it to finish */
  public void executeScript(String pythonScript, ScriptOutputHandler outputHandler) {
    // Check if Python is installed
    File file = new File(pythonScriptExecutable);
    if (!file.exists()) {
      throw new MolgenisPythonException("File [" + pythonScriptExecutable + "] does not exist");
    }

    // Check if Python has execution rights
    if (!file.canExecute()) {
      throw new MolgenisPythonException(
          "Can not execute [" + pythonScriptExecutable + "]. Does it have executable permissions?");
    }

    Path tempFile = null;
    try {
      tempFile = Files.createTempFile(null, ".py");
      Files.write(tempFile, pythonScript.getBytes(UTF_8), StandardOpenOption.WRITE);
      String tempScriptFilePath = tempFile.toAbsolutePath().toString();

      // Create Python process
      LOG.info("Running python script [{}]", tempScriptFilePath);
      var process =
          new ProcessBuilder()
              .command(getCommand(pythonScriptExecutable, tempScriptFilePath))
              .redirectErrorStream(true)
              .start();

      // Capture standard out and error
      new PythonStreamHandler(process.getInputStream(), outputHandler).start();

      // Wait until script is finished
      process.waitFor();

      // Check for errors
      if (process.exitValue() > 0) {
        throw new MolgenisPythonException(
            String.format(
                "Python script process had non-zero exit value: %s", process.exitValue()));
      }

      LOG.info("Script [{}] done", tempScriptFilePath);
    } catch (IOException e) {
      throw new MolgenisPythonException("Exception executing PythonScript.", e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new MolgenisPythonException("Exception waiting for PythonScript to finish", e);
    } finally {
      if (tempFile != null) {
        try {
          Files.delete(tempFile);
        } catch (IOException e) {
          LOG.error("", e);
        }
      }
    }
  }

  protected static String[] getCommand(String pythonScriptExecutable, String tempScriptFilePath) {
    String[] cmdArray = new String[2];
    cmdArray[0] = pythonScriptExecutable;
    cmdArray[1] = tempScriptFilePath;
    return cmdArray;
  }
}

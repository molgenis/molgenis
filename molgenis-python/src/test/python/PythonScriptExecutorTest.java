package org.molgenis.python;

import static org.testng.Assert.*;

import org.testng.annotations.Test;

public class PythonScriptExecutorTest {

  @Test
  public void testGetCommand() {
    assertEquals(
        PythonScriptExecutor.getCommand("python.exe", "Program Files/tomcat/temp/test.py"),
        new String[]{"python.exe","Program Files/tomcat/temp/test.py"});
  }
}

package org.molgenis.python;





public class PythonScriptExecutorTest {

  @Test
  public void testGetCommand() {
    assertEquals(
        PythonScriptExecutor.getCommand("python.exe", "Program Files/tomcat/temp/test.py"),
        new String[]{"python.exe","Program Files/tomcat/temp/test.py"});
  }
}

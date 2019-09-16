package org.molgenis.python;





public class PythonScriptExecutorTest {

  @Test
  public void testGetCommand() {
    asassertEquals(new String[]{"python.exe","Program Files/tomcat/temp/test.py"}, PythonScriptExecutor.getCommand("python.exe", "Program Files/tomcat/temp/test.py"));
  }
}

package org.molgenis.python;

public class StringPythonOutputHandler implements PythonOutputHandler
{
	private final StringBuilder sb = new StringBuilder();

	@Override
	public void outputReceived(String output)
	{
		sb.append(output).append("\n");
	}

	@Override
	public String toString()
	{
		return sb.toString();
	}

}

package org.molgenis.r;

public class StringROutputHandler implements ROutputHandler
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

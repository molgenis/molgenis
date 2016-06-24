package org.molgenis.util;

public class SshResult
{
	private String stdOut;
	private String stdErr;

	public SshResult(String out, String err)
	{
		this.stdOut = out;
		this.stdErr = err;
	}

	public String getStdOut()
	{
		return stdOut;
	}

	public void setStdOut(String stdOut)
	{
		this.stdOut = stdOut;
	}

	public String getStdErr()
	{
		return stdErr;
	}

	public void setStdErr(String stdErr)
	{
		this.stdErr = stdErr;
	}

}

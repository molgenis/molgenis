package org.molgenis.framework.server;

public class AuthStatus
{

	boolean showApi;
	String printMe;

	public AuthStatus(boolean showApi, String printMe)
	{
		super();
		this.showApi = showApi;
		this.printMe = printMe;
	}

	public boolean isShowApi()
	{
		return showApi;
	}

	public String getPrintMe()
	{
		return printMe;
	}
}

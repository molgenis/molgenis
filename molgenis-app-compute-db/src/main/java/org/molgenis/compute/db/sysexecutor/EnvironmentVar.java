package org.molgenis.compute.db.sysexecutor;


public class EnvironmentVar
{
	public String fName = null;
	public String fValue = null;

	public EnvironmentVar(String name, String value)
	{
		fName = name;
		fValue = value;
	}
}

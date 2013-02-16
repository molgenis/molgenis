package org.molgenis.compute5.model;

/** Output definition. The value can (and often is) a freemarker template*/
public class Output extends Input
{
	//value, can be a freemarker template
	private String value;

	public Output(String name)
	{
		super(name);
	}

	public String getValue()
	{
		return value;
	}

	public void setValue(String value)
	{
		this.value = value;
	}

}

package org.molgenis.ui;

public class Label extends MolgenisComponent<Label>
{
	private String value;

	public Label(String value)
	{
		super();
		this.value = value;
	}

	public String getValue()
	{
		return value;
	}

	public Label value(String value)
	{
		this.value = value;
		return this;
	}
}

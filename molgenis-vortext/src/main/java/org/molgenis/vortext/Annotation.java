package org.molgenis.vortext;

public class Annotation
{
	private String content;
	private String uuid;
	private String label;

	public String getContent()
	{
		return content;
	}

	public void setContent(String content)
	{
		this.content = content;
	}

	public String getUuid()
	{
		return uuid;
	}

	public void setUuid(String uuid)
	{
		this.uuid = uuid;
	}

	public String getLabel()
	{
		return label;
	}

	public void setLabel(String label)
	{
		this.label = label;
	}

	@Override
	public String toString()
	{
		return "Annoation [content=" + content + ", uuid=" + uuid + ", label=" + label + "]";
	}

}

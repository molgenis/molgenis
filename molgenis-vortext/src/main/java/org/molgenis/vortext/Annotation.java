package org.molgenis.vortext;

/**
 * Represents a piece of text in a marginalis (the text before the x)
 */
public class Annotation
{
	private String content;
	private String uuid;

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
}

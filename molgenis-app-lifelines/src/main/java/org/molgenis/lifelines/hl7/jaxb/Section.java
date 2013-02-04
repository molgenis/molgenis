package org.molgenis.lifelines.hl7.jaxb;

public class Section
{
	private Code code;
	private String title;
	private Text text;
	private Entry entry;

	public Code getCode()
	{
		return code;
	}

	public void setCode(Code code)
	{
		this.code = code;
	}

	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public Text getText()
	{
		return text;
	}

	public void setText(Text text)
	{
		this.text = text;
	}

	public Entry getEntry()
	{
		return entry;
	}

	public void setEntry(Entry entry)
	{
		this.entry = entry;
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("Section [code=").append(code).append(", title=").append(title).append(", text=").append(text)
				.append(", entry=").append(entry).append("]");
		return builder.toString();
	}
}

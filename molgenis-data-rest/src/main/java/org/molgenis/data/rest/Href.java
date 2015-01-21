package org.molgenis.data.rest;

public class Href
{
	private final String href;
	private final String hrefCollection;

	public Href(String href, String hrefCollection)
	{
		this.href = href;
		this.hrefCollection = hrefCollection;
	}

	public String getHref()
	{
		return href;
	}

	public String getHrefCollection()
	{
		return hrefCollection;
	}
}

package org.molgenis.util;

import java.io.File;

public class FileLink
{
	private File localpath;
	private String link;

	/**
	 * 
	 * @param localpath
	 *            to a file on the local file system
	 * @param link
	 *            the relative address underneath MOLGENIS server path, e.g.
	 *            http://host/molgenis/file'.
	 */
	public FileLink(File localpath, String link)
	{
		this.localpath = localpath;
		this.link = link;
	}

	public File getLocalpath()
	{
		return localpath;
	}

	public void setLocalpath(File localpath)
	{
		this.localpath = localpath;
	}

	public String getLink()
	{
		return link;
	}

	public void setLink(String link)
	{
		this.link = link;
	}
}

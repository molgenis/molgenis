package org.molgenis.omx.filemanager;

import org.molgenis.omx.core.MolgenisFile;

public class MolgenisFileManagerModel
{

	private MolgenisFile molgenisFile;
	private String db_path;
	private boolean hasFile;
	private String uploadTextAreaContent;
	private String ipURl;
	private Boolean showApplet;
	private long fileSize;

	public Boolean getShowApplet()
	{
		return showApplet;
	}

	public void setShowApplet(Boolean showApplet)
	{
		this.showApplet = showApplet;
	}

	public long getFileSize()
	{
		return fileSize;
	}

	public void setFileSize(long fileSize)
	{
		this.fileSize = fileSize;
	}

	public String getUploadTextAreaContent()
	{
		return uploadTextAreaContent;
	}

	public void setUploadTextAreaContent(String uploadTextAreaContent)
	{
		this.uploadTextAreaContent = uploadTextAreaContent;
	}

	public String getIpURl()
	{
		return ipURl;
	}

	public void setIpURl(String ipURl)
	{
		this.ipURl = ipURl;
	}

	public boolean isHasFile()
	{
		return hasFile;
	}

	public void setHasFile(boolean hasFile)
	{
		this.hasFile = hasFile;
	}

	public String getDb_path()
	{
		return db_path;
	}

	public void setDb_path(String dbPath)
	{
		db_path = dbPath;
	}

	public MolgenisFile getMolgenisFile()
	{
		return molgenisFile;
	}

	public void setMolgenisFile(MolgenisFile molgenisFile)
	{
		this.molgenisFile = molgenisFile;
	}

}

package org.molgenis.omx.services;

import java.io.File;

public class Report
{

	private Boolean FileStoragePropsPresent;
	private File fileStorage;
	private Boolean folderExists;
	private Boolean folderHasContent;
	private Boolean verified;

	public Boolean getFileStoragePropsPresent()
	{
		return FileStoragePropsPresent;
	}

	public void setFileStoragePropsPresent(Boolean fileStoragePropsPresent)
	{
		FileStoragePropsPresent = fileStoragePropsPresent;
	}

	public File getFileStorage()
	{
		return fileStorage;
	}

	public void setFileStorage(File fileStorage)
	{
		this.fileStorage = fileStorage;
	}

	public Boolean getVerified()
	{
		return verified;
	}

	public void setVerified(Boolean verified)
	{
		this.verified = verified;
	}

	public Boolean getFolderExists()
	{
		return folderExists;
	}

	public void setFolderExists(Boolean folderExists)
	{
		this.folderExists = folderExists;
	}

	public Boolean getFolderHasContent()
	{
		return folderHasContent;
	}

	public void setFolderHasContent(Boolean folderHasContent)
	{
		this.folderHasContent = folderHasContent;
	}

}
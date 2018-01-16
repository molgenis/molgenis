package org.molgenis.data.file;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static java.io.File.separator;

public class FileStore
{
	private final String storageDir;

	public FileStore(String storageDir)
	{
		if (storageDir == null) throw new IllegalArgumentException("storage dir is null");
		this.storageDir = storageDir;
	}

	public boolean createDirectory(String dirName)
	{
		return new File(storageDir + separator + dirName).mkdir();
	}

	public void deleteDirectory(String dirName) throws IOException
	{
		FileUtils.deleteDirectory(getFile(dirName));
	}

	public File store(InputStream is, String fileName) throws IOException
	{
		File file = new File(storageDir + separator + fileName);
		FileOutputStream fos = new FileOutputStream(file);
		try
		{
			IOUtils.copy(is, fos);
		}
		finally
		{
			IOUtils.closeQuietly(fos);
			IOUtils.closeQuietly(is);
		}
		return file;
	}

	public File getFile(String fileName)
	{
		return new File(storageDir + separator + fileName);
	}

	public boolean delete(String fileName)
	{
		File file = new File(storageDir + separator + fileName);
		return file.delete();
	}

	public String getStorageDir()
	{
		return storageDir;
	}

	public void writeToFile(InputStream inputStream, String fileName) throws IOException
	{
		FileUtils.copyInputStreamToFile(inputStream, getFile(fileName));
	}

}

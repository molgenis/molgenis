package org.molgenis.file;

import static java.io.File.separator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

public class FileStore
{
	private final String storageDir;

	public FileStore(String storageDir)
	{
		if (storageDir == null) throw new IllegalArgumentException("storage dir is null");
		this.storageDir = storageDir;
	}

	public boolean createDirectory(String dirName) throws IOException
	{
		return new File(storageDir + separator + dirName).mkdir();
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
			fos.flush();
			fos.close();
			is.close();
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

}

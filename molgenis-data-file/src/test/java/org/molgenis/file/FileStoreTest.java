package org.molgenis.file;

import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

public class FileStoreTest
{

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void FileStore()
	{
		new FileStore(null);
	}

	@Test
	public void createDirectory() throws IOException
	{
		FileStore fileStore = new FileStore(System.getProperty("java.io.tmpdir"));
		Assert.assertTrue(fileStore.createDirectory("testDir"));
		Assert.assertTrue(fileStore.getFile("testDir").isDirectory());
		fileStore.delete("testDir");
	}

	@Test
	public void store() throws IOException
	{
		FileStore fileStore = new FileStore(System.getProperty("java.io.tmpdir"));
		File file = fileStore.store(new ByteArrayInputStream(new byte[] { 1, 2, 3 }), "bytes.bin");
		Assert.assertEquals(FileUtils.readFileToByteArray(file), new byte[] { 1, 2, 3 });
	}

	@Test
	public void getFile() throws IOException
	{
		String fileName = "bytes.bin";
		FileStore fileStore = new FileStore(System.getProperty("java.io.tmpdir"));
		File file = fileStore.store(new ByteArrayInputStream(new byte[] { 1, 2, 3 }), fileName);

		Assert.assertEquals(fileStore.getFile(fileName).getAbsolutePath(), file.getAbsolutePath());
	}
}

package org.molgenis.data.file;

import com.google.common.io.Files;
import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

public class FileStoreTest
{

	private FileStore fileStore;

	@BeforeTest
	public void beforeTest() throws IOException
	{
		File tempDir = Files.createTempDir();
		fileStore = new FileStore(tempDir.getCanonicalPath());
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void FileStore()
	{
		new FileStore(null);
	}

	@Test
	public void createDirectory() throws IOException
	{
		Assert.assertTrue(fileStore.createDirectory("testDir"));
		Assert.assertTrue(fileStore.getFile("testDir").isDirectory());
		fileStore.delete("testDir");
	}

	@Test
	public void store() throws IOException
	{
		File file = fileStore.store(new ByteArrayInputStream(new byte[] { 1, 2, 3 }), "bytes.bin");
		Assert.assertEquals(FileUtils.readFileToByteArray(file), new byte[] { 1, 2, 3 });
	}

	@Test
	public void getFile() throws IOException
	{
		String fileName = "bytes.bin";
		File file = fileStore.store(new ByteArrayInputStream(new byte[] { 1, 2, 3 }), fileName);
		Assert.assertEquals(fileStore.getFile(fileName).getAbsolutePath(), file.getAbsolutePath());
	}
}

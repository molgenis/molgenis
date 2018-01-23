package org.molgenis.core.util;

import org.springframework.util.FileCopyUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.servlet.http.Part;
import java.io.*;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.*;

public class FileUploadUtilsTest
{
	private Part part;

	@BeforeMethod
	public void setUp()
	{
		part = mock(Part.class);
	}

	@Test
	public void getOriginalFileName()
	{
		when(part.getHeader("content-disposition")).thenReturn("form-data; name=\"upload\"; filename=\"example.xls\"");
		String filename = FileUploadUtils.getOriginalFileName(part);
		assertEquals(filename, "example.xls");
	}

	@Test
	public void getOriginalFileNameWithMissingHeader()
	{
		when(part.getHeader("content-disposition")).thenReturn(null);
		assertNull(FileUploadUtils.getOriginalFileName(part));
	}

	@Test
	public void getOriginalFileNameNoFileSelected()
	{
		when(part.getHeader("content-disposition")).thenReturn("form-data; name=\"upload\"; filename=\"\"");
		String filename = FileUploadUtils.getOriginalFileName(part);
		assertNull(filename);
	}

	@Test
	public void saveToTempFile() throws UnsupportedEncodingException, IOException
	{
		String fileContent = "Hey dude";
		when(part.getHeader("content-disposition")).thenReturn("form-data; name=\"upload\"; filename=\"example.txt\"");
		when(part.getInputStream()).thenReturn(new ByteArrayInputStream(fileContent.getBytes("UTF-8")));

		File tempFile = FileUploadUtils.saveToTempFile(part);

		try
		{
			assertNotNull(tempFile);
			assertTrue(tempFile.exists());
			assertEquals(FileCopyUtils.copyToString(new FileReader(tempFile)), fileContent);
		}
		finally
		{
			tempFile.delete();
		}

	}
}

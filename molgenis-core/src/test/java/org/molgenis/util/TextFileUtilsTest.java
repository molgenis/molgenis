package org.molgenis.util;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.commons.io.FileUtils;
import org.testng.annotations.Test;

public class TextFileUtilsTest
{
	@Test
	public void testGetNumberOfLines() throws Exception
	{
		File file0 = File.createTempFile("TextFileUtilsTest_file0", null);
		try
		{
			FileUtils.write(file0, "a\nb", Charset.forName("UTF-8"));
			assertEquals(2, TextFileUtils.getNumberOfLines(file0));
		}
		finally
		{
			file0.delete();
		}
	}

	@Test
	public void testGetNumberOfLines_endsWithNewline() throws Exception
	{
		File file0 = File.createTempFile("TextFileUtilsTest_file0", null);
		try
		{
			FileUtils.write(file0, "a\nb\n", Charset.forName("UTF-8"));
			assertEquals(3, TextFileUtils.getNumberOfLines(file0));
		}
		finally
		{
			file0.delete();
		}
	}

	@Test
	public void testFileEndsWithNewlineChar_false() throws Exception
	{
		File file0 = File.createTempFile("TextFileUtilsTest_file0", null);
		try
		{
			FileUtils.write(file0, "a\nb", Charset.forName("UTF-8"));
			assertFalse(TextFileUtils.fileEndsWithNewlineChar(file0));
		}
		finally
		{
			file0.delete();
		}
	}

	@Test
	public void testFileEndsWithNewlineChar_CR() throws Exception
	{
		// Mac
		File file0 = File.createTempFile("TextFileUtilsTest_file0", null);
		try
		{
			FileUtils.write(file0, "a\rb\r", Charset.forName("UTF-8"));
			assertTrue(TextFileUtils.fileEndsWithNewlineChar(file0));
		}
		finally
		{
			file0.delete();
		}
	}

	@Test
	public void testFileEndsWithNewlineChar_LF() throws Exception
	{
		// Unix
		File file0 = File.createTempFile("TextFileUtilsTest_file0", null);
		try
		{
			FileUtils.write(file0, "a\nb\n", Charset.forName("UTF-8"));
			assertTrue(TextFileUtils.fileEndsWithNewlineChar(file0));
		}
		finally
		{
			file0.delete();
		}
	}

	@Test
	public void testFileEndsWithNewlineChar_CRLF() throws Exception
	{
		// Windows
		File file0 = File.createTempFile("TextFileUtilsTest_file0", null);
		try
		{
			FileUtils.write(file0, "a\r\nb\r\n", Charset.forName("UTF-8"));
			assertTrue(TextFileUtils.fileEndsWithNewlineChar(file0));
		}
		finally
		{
			file0.delete();
		}
	}

	@Test
	public void testGetAmountOfNewlinesAtFileEnd() throws Exception
	{
		File file0 = File.createTempFile("TextFileUtilsTest_file0", null);
		try
		{
			FileUtils.write(file0, "a\nb\n\n\n\n", Charset.forName("UTF-8"));
			assertEquals(4, TextFileUtils.getAmountOfNewlinesAtFileEnd(file0));
		}
		finally
		{
			file0.delete();
		}
	}

	@Test
	public void testGetAmountOfNewlinesAtFileEnd_CR() throws Exception
	{
		// Mac
		File file0 = File.createTempFile("TextFileUtilsTest_file0", null);
		try
		{
			FileUtils.write(file0, "a\rb\r", Charset.forName("UTF-8"));
			assertEquals(1, TextFileUtils.getAmountOfNewlinesAtFileEnd(file0));
		}
		finally
		{
			file0.delete();
		}
	}

	@Test
	public void testGetAmountOfNewlinesAtFileEnd_LF() throws Exception
	{
		// Unix
		File file0 = File.createTempFile("TextFileUtilsTest_file0", null);
		try
		{
			FileUtils.write(file0, "a\nb\n", Charset.forName("UTF-8"));
			assertEquals(1, TextFileUtils.getAmountOfNewlinesAtFileEnd(file0));
		}
		finally
		{
			file0.delete();
		}
	}

	@Test
	public void testGetAmountOfNewlinesAtFileEnd_CRLF() throws Exception
	{
		// Windows
		File file0 = File.createTempFile("TextFileUtilsTest_file0", null);
		try
		{
			FileUtils.write(file0, "a\r\nb\r\n", Charset.forName("UTF-8"));
			assertEquals(1, TextFileUtils.getAmountOfNewlinesAtFileEnd(file0));
		}
		finally
		{
			file0.delete();
		}
	}

	@Test
	public void testGetNumberOfNonEmptyLines() throws IOException
	{
		File file0 = File.createTempFile("TextFileUtilsTest_file0", null);
		try
		{
			FileUtils.write(file0, "\nabc\n\ndef\nghi\n\n\n", Charset.forName("UTF-8"));
			assertEquals(3, TextFileUtils.getNumberOfNonEmptyLines(file0, Charset.forName("UTF-8")));
		}
		finally
		{
			file0.delete();
		}
	}
}

package org.molgenis.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;

public class TextFileUtils
{

	/**
	 * Count number of lines in the file. Add 1 extra because this only counts
	 * newlines, therefore 1 newline = 2 lines in the file. Consider using
	 * fileEndsWithNewlineChar() in combination with this function. See:
	 * http://stackoverflow
	 * .com/questions/453018/number-of-lines-in-a-file-in-java
	 * 
	 * @param inFile
	 * 
	 * @return
	 * @throws IOException
	 */
	public static int getNumberOfLines(File inFile) throws IOException
	{
		LineNumberReader lnr = new LineNumberReader(new InputStreamReader(new FileInputStream(inFile),
				Charset.forName("UTF-8")));
		try
		{
			lnr.skip(Long.MAX_VALUE);
			return lnr.getLineNumber() + 1;
		}
		finally
		{
			IOUtils.closeQuietly(lnr);
		}
	}

	/**
	 * Find out if the source file ends with a newline character. Useful in
	 * combination with getNumberOfLines().
	 * 
	 * @param inFile
	 * 
	 * @return
	 * @throws Exception
	 */
	public static boolean fileEndsWithNewlineChar(File inFile) throws Exception
	{
		RandomAccessFile raf = new RandomAccessFile(inFile, "r");
		try
		{
			raf.seek(raf.length() - 1);
			char c = (char) raf.readByte();
			if (c == '\n' || c == '\r')
			{
				return true;
			}
			else
			{
				return false;
			}
		}
		finally
		{
			raf.close();
		}
	}

	/**
	 * Get the amount of newline characters at the end of a file. Can be of
	 * great help when you want to judge the amount of elements in a file based
	 * on the number of lines, when the file might contain (many) empty trailing
	 * newlines. The amount of \r and \n terminators are counted. The
	 * combination \r\n is reduced to \n before counting. You will probably want
	 * to use this in combination with the more lightweight check of
	 * fileEndsWithNewlineChar().
	 * 
	 * @param inFile
	 * 
	 * @return
	 * @throws Exception
	 */
	public static int getAmountOfNewlinesAtFileEnd(File inFile) throws Exception
	{
		RandomAccessFile raf = new RandomAccessFile(inFile, "r");

		int nrOfNewLines = 1;
		boolean countingNewlines = true;
		StringBuilder terminatorSequenceBuilder = new StringBuilder();

		while (countingNewlines)
		{
			raf.seek(raf.length() - nrOfNewLines);
			char c = (char) raf.readByte();

			if (c == '\r')
			{
				terminatorSequenceBuilder.append('r');
				nrOfNewLines++;
			}
			else if (c == '\n')
			{
				terminatorSequenceBuilder.append('n');
				nrOfNewLines++;
			}
			else
			{
				countingNewlines = false;
			}
		}

		raf.close();

		// replace \r\n combinations with \n (note: separators are added in
		// reverse
		// order)
		String terminatorSequence = terminatorSequenceBuilder.toString().replaceAll("nr", "n");

		return terminatorSequence.length();

	}

	public static int getNumberOfNonEmptyLines(File file, Charset charset) throws IOException
	{
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), charset));
		try
		{
			int count = 0;
			String line;
			while ((line = reader.readLine()) != null)
				if (!line.isEmpty()) ++count;
			return count;
		}
		finally
		{
			reader.close();
		}
	}
}

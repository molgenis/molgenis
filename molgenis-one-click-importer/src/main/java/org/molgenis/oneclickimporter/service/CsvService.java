package org.molgenis.oneclickimporter.service;

import org.molgenis.oneclickimporter.exceptions.EmptyFileException;
import org.molgenis.oneclickimporter.exceptions.NoDataException;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface CsvService
{
	String CSV_SEPARATOR = ",";

	/**
	 * Creates a List containing the lines of a CSV file
	 * Including the header
	 *
	 * @param file
	 */
	List<String> buildLinesFromFile(File file) throws IOException, NoDataException, EmptyFileException;

	/**
	 * Splits a line from a file based on a separator.
	 * Takes into account values between '""'
	 * <p>
	 * e.g. "hello,world","21",43,"21,5" is split into ["hello,world", "21", "43", "21,5"]
	 *
	 * @param line
	 * @param separator
	 * @return A String array of values from the original string
	 */
	String[] splitLineOnSeparator(String line);
}

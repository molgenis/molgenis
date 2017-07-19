package org.molgenis.oneclickimporter.service;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface CsvService
{
	/**
	 * Creates a List containing the lines of a CSV file
	 * Including the header
	 *
	 * @param file
	 */
	List<String> buildLinesFromFile(File file) throws IOException;
}

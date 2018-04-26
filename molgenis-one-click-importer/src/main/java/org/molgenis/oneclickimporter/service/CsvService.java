package org.molgenis.oneclickimporter.service;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface CsvService
{

	Character CSV_SEPARATOR = ',';

	/**
	 * Creates a List with String[] containing the lines of a CSV file
	 * Including the header
	 * <p>
	 * Uses the {@link au.com.bytecode.opencsv.CSVReader} which is also used in the {@link org.molgenis.data.csv.CsvIterator}
	 *
	 * @param file can be a zip or a regular file
	 * @throws IOException if something goes wrong reading the file
	 * @throws org.molgenis.data.MolgenisDataException if validation of the file content fails
	 */
	List<String[]> buildLinesFromFile(File file) throws IOException;

}

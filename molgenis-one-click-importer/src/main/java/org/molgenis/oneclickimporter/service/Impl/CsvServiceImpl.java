package org.molgenis.oneclickimporter.service.Impl;

import org.molgenis.oneclickimporter.exceptions.EmptyFileException;
import org.molgenis.oneclickimporter.exceptions.NoDataException;
import org.molgenis.oneclickimporter.service.CsvService;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CsvServiceImpl implements CsvService
{
	@Override
	public List<String> buildLinesFromFile(File file) throws IOException, NoDataException, EmptyFileException
	{
		List<String> lines;

		try (BufferedReader bufferedReader = new BufferedReader(
				new InputStreamReader(new FileInputStream(file), "UTF-8")))
		{
			lines = bufferedReader.lines().collect(Collectors.toList());
			if (lines.size() == 0)
			{
				throw new EmptyFileException("File [" + file.getName() + "] is empty");
			}
			else if (lines.size() == 1)
			{
				throw new NoDataException("Header was found, but no data is present in file [" + file.getName() + "]");
			}
		}
		return lines;
	}

	@Override
	public String[] splitLineOnSeparator(String line)
	{
		return line.split(CSV_SEPARATOR + "(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
	}
}

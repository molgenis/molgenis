package org.molgenis.oneclickimporter.service.Impl;

import org.molgenis.oneclickimporter.exceptions.EmptyFileException;
import org.molgenis.oneclickimporter.exceptions.NoDataException;
import org.molgenis.oneclickimporter.service.CsvService;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CsvServiceImpl implements CsvService
{
	@Override
	public List<String> buildLinesFromFile(File file)
			throws IOException, NoDataException, EmptyFileException
	{
		FileReader fileReader = new FileReader(file);
		BufferedReader bufferedReader = new BufferedReader(fileReader);

		List<String> lines = bufferedReader.lines().collect(Collectors.toList());
		if (lines.size() == 0)
		{
			throw new EmptyFileException("File [" + file.getName() + "] is empty");
		}
		else if (lines.size() == 1)
		{
			throw new NoDataException("Header was found, but no data is present in file [" + file.getName() + "]");
		}
		else
		{
			return lines;
		}
	}
}

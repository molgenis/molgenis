package org.molgenis.oneclickimporter.service.Impl;

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
	public List<String> buildLinesFromFile(File file) throws IOException
	{
		FileReader fileReader = new FileReader(file);
		BufferedReader bufferedReader = new BufferedReader(fileReader);

		return bufferedReader.lines().collect(Collectors.toList());
	}
}

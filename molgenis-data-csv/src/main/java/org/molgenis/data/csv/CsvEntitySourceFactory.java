package org.molgenis.data.csv;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.molgenis.data.EntitySource;
import org.molgenis.data.processor.CellProcessor;
import org.molgenis.data.processor.LowerCaseProcessor;
import org.molgenis.data.processor.TrimProcessor;
import org.molgenis.data.support.AbstractFileBasedEntitySourceFactory;
import org.springframework.stereotype.Component;

@Component
public class CsvEntitySourceFactory extends AbstractFileBasedEntitySourceFactory
{
	public static final String CSV_ENTITYSOURCE_URL_PREFIX = "csv://";
	public static final List<String> FILE_EXTENSIONS = Arrays.asList("csv", "txt", "tsv", "zip");
	private static final List<CellProcessor> CELLPROCESSORS = Arrays.<CellProcessor> asList(new TrimProcessor(),
			new LowerCaseProcessor(true, false));

	public CsvEntitySourceFactory()
	{
		super(CSV_ENTITYSOURCE_URL_PREFIX, FILE_EXTENSIONS, CELLPROCESSORS);
	}

	@Override
	protected EntitySource createInternal(String url, List<CellProcessor> cellProcessors) throws IOException
	{
		return new CsvEntitySource(url, cellProcessors);
	}

	@Override
	protected EntitySource createInternal(File file, List<CellProcessor> cellProcessors) throws IOException
	{
		return new CsvEntitySource(file, cellProcessors);
	}
}

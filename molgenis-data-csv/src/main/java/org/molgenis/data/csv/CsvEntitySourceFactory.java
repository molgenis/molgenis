package org.molgenis.data.csv;

import org.molgenis.data.EntitySource;
import org.molgenis.data.EntitySourceFactory;

public class CsvEntitySourceFactory implements EntitySourceFactory
{
	public static final String CSV_ENTITYSOURCE_URL_PREFIX = "csv://";

	@Override
	public String getUrlPrefix()
	{
		return "csv";
	}

	@Override
	public EntitySource create(String url)
	{
		return new CsvEntitySource(url);
	}

}

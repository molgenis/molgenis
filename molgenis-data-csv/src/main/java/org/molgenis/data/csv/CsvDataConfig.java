package org.molgenis.data.csv;

import javax.annotation.PostConstruct;

import org.molgenis.data.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CsvDataConfig
{

	@Autowired
	private DataService dataService;

	/**
	 * Registers the CsvRepositorySource factory so it can be used by DataService.createFileRepositorySource(File file);
	 */
	@PostConstruct
	public void registerCsvRepositorySource()
	{
		dataService.addFileRepositorySourceClass(CsvRepositorySource.class, CsvRepositorySource.EXTENSIONS);
	}

}

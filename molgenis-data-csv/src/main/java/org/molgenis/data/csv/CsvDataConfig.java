package org.molgenis.data.csv;

import javax.annotation.PostConstruct;

import org.molgenis.data.FileRepositorySourceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CsvDataConfig
{

	@Autowired
	private FileRepositorySourceFactory fileRepositorySourceFactory;

	/**
	 * Registers the CsvRepositorySource factory so it can be used by DataService.createFileRepositorySource(File file);
	 */
	@PostConstruct
	public void registerCsvRepositorySource()
	{
		fileRepositorySourceFactory.addFileRepositorySourceClass(CsvRepositorySource.class,
				CsvRepositorySource.EXTENSIONS);
	}

}

package org.molgenis.data.excel;

import javax.annotation.PostConstruct;

import org.molgenis.data.FileRepositorySourceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ExcelDataConfig
{
	@Autowired
	private FileRepositorySourceFactory fileRepositorySourceFactory;

	/**
	 * Registers the ExcelRepositorySource factory so it can be used by DataService.createFileRepositorySource(File
	 * file);
	 */
	@PostConstruct
	public void registerExcelRepositorySource()
	{
		fileRepositorySourceFactory.addFileRepositorySourceClass(ExcelRepositorySource.class,
				ExcelRepositorySource.EXTENSIONS);
	}

}

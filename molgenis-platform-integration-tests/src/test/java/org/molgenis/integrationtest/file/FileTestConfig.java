package org.molgenis.integrationtest.file;

import org.molgenis.file.FileStore;
import org.molgenis.file.model.FileMetaFactory;
import org.molgenis.file.model.FileMetaMetaData;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.io.File;

@Configuration
@Import({ FileMetaFactory.class, FileMetaMetaData.class })
public class FileTestConfig
{
	@Bean
	public FileStore fileStore()
	{
		// get molgenis home directory
		String currentDir = System.getProperty("user.dir");

		if (!currentDir.endsWith(File.separator))
		{
			currentDir = currentDir + File.separator;
		}

		// create molgenis store directory in molgenis data directory if not exists
		String molgenisFileStoreDirStr = currentDir + "data" + File.separator + "filestore";
		File molgenisDataDir = new File(molgenisFileStoreDirStr);
		if (!molgenisDataDir.exists() && !molgenisDataDir.mkdirs())
		{
			throw new RuntimeException("failed to create directory: " + molgenisFileStoreDirStr);
		}
		
		return new FileStore(molgenisFileStoreDirStr);
	}
}

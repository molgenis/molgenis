package org.molgenis.integrationtest.config;

import org.molgenis.data.file.FileStore;
import org.molgenis.data.file.model.FileMetaFactory;
import org.molgenis.data.file.model.FileMetaMetaData;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.io.File;
import java.util.UUID;

@Configuration
@Import({ FileMetaFactory.class, FileMetaMetaData.class })
public class FileTestConfig
{
	
	@Bean
	public FileStore fileStore()
	{
		// get molgenis home directory
		String currentDir = System.getProperty("java.io.tmpdir") + File.separator + UUID.randomUUID()
																						.toString()
																						.replaceAll("-", "");

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

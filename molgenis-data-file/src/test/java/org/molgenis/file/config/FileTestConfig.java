package org.molgenis.file.config;

import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.file.model.FileMetaFactory;
import org.molgenis.file.model.FileMetaMetaData;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({ EntityBaseTestConfig.class, FileMetaMetaData.class, FileMetaFactory.class })
public class FileTestConfig
{
}

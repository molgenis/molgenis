package org.molgenis.data.file.config;

import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.file.model.FileMetaFactory;
import org.molgenis.data.file.model.FileMetaMetaData;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({ EntityBaseTestConfig.class, FileMetaMetaData.class, FileMetaFactory.class })
public class FileTestConfig
{
}

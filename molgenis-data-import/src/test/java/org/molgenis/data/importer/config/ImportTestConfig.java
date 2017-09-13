package org.molgenis.data.importer.config;

import org.molgenis.auth.SecurityPackage;
import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.importer.ImportRunFactory;
import org.molgenis.data.importer.ImportRunMetaData;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({ EntityBaseTestConfig.class, ImportRunMetaData.class, ImportRunFactory.class, SecurityPackage.class, })
public class ImportTestConfig
{
}

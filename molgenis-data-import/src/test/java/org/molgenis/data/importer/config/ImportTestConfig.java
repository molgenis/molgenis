package org.molgenis.data.importer.config;

import org.molgenis.data.security.model.SecurityPackage;
import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.importer.ImportRunFactory;
import org.molgenis.data.importer.ImportRunMetaData;
import org.molgenis.security.owned.OwnedEntityType;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({ EntityBaseTestConfig.class, ImportRunMetaData.class, ImportRunFactory.class, OwnedEntityType.class,
		SecurityPackage.class, })
public class ImportTestConfig
{
}

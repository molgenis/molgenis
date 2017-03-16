package org.molgenis.data.config;

import org.molgenis.data.meta.model.PackageFactory;
import org.molgenis.data.meta.model.PackageMetadata;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({ PackageMetadata.class, PackageFactory.class })
public class PackageTestConfig
{
}

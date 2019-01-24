package org.molgenis.beacon.config;

import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.genomebrowser.meta.GenomeBrowserAttributesMetadata;
import org.molgenis.genomebrowser.meta.GenomeBrowserPackage;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
  EntityBaseTestConfig.class,
  GenomeBrowserAttributesMetadata.class,
  GenomeBrowserPackage.class
})
public class BeaconTestConfig {}

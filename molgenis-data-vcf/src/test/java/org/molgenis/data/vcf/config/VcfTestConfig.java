package org.molgenis.data.vcf.config;

import org.molgenis.data.config.MetadataTestConfig;
import org.molgenis.data.vcf.model.VcfAttributes;
import org.molgenis.data.vcf.utils.VcfUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({ MetadataTestConfig.class, VcfAttributes.class, VcfUtils.class })
public class VcfTestConfig
{
}

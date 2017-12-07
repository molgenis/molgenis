package org.molgenis.data.vcf;

import org.molgenis.data.i18n.PropertiesMessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Vcf18nConfig
{

	public static final String NAMESPACE = "vcf";

	@Bean
	public PropertiesMessageSource vcfMessageSource()
	{
		return new PropertiesMessageSource(NAMESPACE);
	}
}

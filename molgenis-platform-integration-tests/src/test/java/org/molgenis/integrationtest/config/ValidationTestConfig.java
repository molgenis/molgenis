package org.molgenis.integrationtest.config;

import org.molgenis.data.validation.EntityAttributesValidator;
import org.molgenis.data.validation.ExpressionValidator;
import org.molgenis.data.validation.QueryValidator;
import org.molgenis.data.validation.meta.AttributeValidator;
import org.molgenis.data.validation.meta.EntityTypeValidator;
import org.molgenis.data.validation.meta.PackageValidator;
import org.molgenis.data.validation.meta.TagValidator;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({ ExpressionValidator.class, EntityAttributesValidator.class, AttributeValidator.class,
		EntityTypeValidator.class, PackageValidator.class, TagValidator.class, QueryValidator.class })
public class ValidationTestConfig
{
}

package org.molgenis.integrationtest.data.validation;

import org.molgenis.data.validation.data.EntityAttributesValidator;
import org.molgenis.data.validation.data.ExpressionValidator;
import org.molgenis.data.validation.meta.AttributeValidator;
import org.molgenis.data.validation.meta.EntityTypeValidator;
import org.molgenis.data.validation.meta.PackageValidator;
import org.molgenis.data.validation.meta.TagValidator;
import org.molgenis.data.validation.query.QueryValidator;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({ ExpressionValidator.class, EntityAttributesValidator.class, AttributeValidator.class,
		EntityTypeValidator.class, PackageValidator.class, TagValidator.class, QueryValidator.class })
public class ValidationTestConfig
{
}

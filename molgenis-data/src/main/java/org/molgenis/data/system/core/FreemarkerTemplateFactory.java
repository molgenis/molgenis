package org.molgenis.data.system.core;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FreemarkerTemplateFactory
		extends AbstractSystemEntityFactory<FreemarkerTemplate, FreemarkerTemplateMetaData, String>
{
	@Autowired
	FreemarkerTemplateFactory(FreemarkerTemplateMetaData freemarkerTemplate, EntityPopulator entityPopulator)
	{
		super(FreemarkerTemplate.class, freemarkerTemplate, entityPopulator);
	}
}

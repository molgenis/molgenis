package org.molgenis.ontology.core.meta;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SemanticTypeFactory extends AbstractSystemEntityFactory<SemanticTypeEntity, SemanticTypeMetaData, String>
{
	@Autowired
	SemanticTypeFactory(SemanticTypeMetaData systemEntityMeta, EntityPopulator entityPopulator)
	{
		super(SemanticTypeEntity.class, systemEntityMeta, entityPopulator);
	}
}

package org.molgenis.ontology.core.meta;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OntologyTermNodePathFactory
		extends AbstractSystemEntityFactory<OntologyTermNodePath, OntologyTermNodePathMetaData, String>
{
	@Autowired
	OntologyTermNodePathFactory(OntologyTermNodePathMetaData ontologyTermNodePathMeta, EntityPopulator entityPopulator)
	{
		super(OntologyTermNodePath.class, ontologyTermNodePathMeta, entityPopulator);
	}
}

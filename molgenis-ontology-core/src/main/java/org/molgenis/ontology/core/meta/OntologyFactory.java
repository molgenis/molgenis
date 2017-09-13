package org.molgenis.ontology.core.meta;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.stereotype.Component;

@Component
public class OntologyFactory extends AbstractSystemEntityFactory<Ontology, OntologyMetaData, String>
{
	OntologyFactory(OntologyMetaData ontologyMetaData, EntityPopulator entityPopulator)
	{
		super(Ontology.class, ontologyMetaData, entityPopulator);
	}
}

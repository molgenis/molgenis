package org.molgenis.ontology.core.meta;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OntologyTermFactory extends AbstractSystemEntityFactory<OntologyTerm, OntologyTermMetaData, String>
{
	@Autowired
	OntologyTermFactory(OntologyTermMetaData ontologyTermMeta, EntityPopulator entityPopulator)
	{
		super(OntologyTerm.class, ontologyTermMeta, entityPopulator);
	}
}

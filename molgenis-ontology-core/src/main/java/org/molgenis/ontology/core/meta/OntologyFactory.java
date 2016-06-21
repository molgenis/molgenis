package org.molgenis.ontology.core.meta;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OntologyFactory extends AbstractSystemEntityFactory<Ontology, OntologyMetaData, String>
{
	@Autowired
	OntologyFactory(OntologyMetaData ontologyMetaData)
	{
		super(Ontology.class, ontologyMetaData);
	}
}

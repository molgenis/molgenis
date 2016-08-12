package org.molgenis.ontology.core.meta;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OntologyTermFactory extends AbstractSystemEntityFactory<OntologyTerm, OntologyTermMetaData, String>
{
	@Autowired
	OntologyTermFactory(OntologyTermMetaData ontologyTermMeta)
	{
		super(OntologyTerm.class, ontologyTermMeta);
	}
}

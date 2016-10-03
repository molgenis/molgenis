package org.molgenis.ontology.core.meta;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TermFrequencyFactory extends AbstractSystemEntityFactory<TermFrequency, TermFrequencyMetaData, String>
{
	@Autowired
	TermFrequencyFactory(TermFrequencyMetaData termFrequencyMetaData, EntityPopulator entityPopulator)
	{
		super(TermFrequency.class, termFrequencyMetaData, entityPopulator);
	}
}

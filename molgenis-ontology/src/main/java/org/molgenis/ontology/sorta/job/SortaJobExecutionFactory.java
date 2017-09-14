package org.molgenis.ontology.sorta.job;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.molgenis.ontology.sorta.meta.SortaJobExecutionMetaData;
import org.springframework.stereotype.Component;

@Component
public class SortaJobExecutionFactory
		extends AbstractSystemEntityFactory<SortaJobExecution, SortaJobExecutionMetaData, String>
{
	SortaJobExecutionFactory(SortaJobExecutionMetaData sortaJobExecutionMetaData, EntityPopulator entityPopulator)
	{
		super(SortaJobExecution.class, sortaJobExecutionMetaData, entityPopulator);
	}
}

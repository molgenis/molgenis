package org.molgenis.ontology.sorta.job;

import org.molgenis.data.AbstractEntityFactory;
import org.molgenis.ontology.sorta.meta.SortaJobExecutionMetaData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SortaJobExecutionFactory
		extends AbstractEntityFactory<SortaJobExecution, SortaJobExecutionMetaData, String>
{
	@Autowired
	SortaJobExecutionFactory(SortaJobExecutionMetaData sortaJobExecutionMetaData)
	{
		super(SortaJobExecution.class, sortaJobExecutionMetaData, String.class);
	}
}

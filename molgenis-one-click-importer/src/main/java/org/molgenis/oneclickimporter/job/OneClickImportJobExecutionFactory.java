package org.molgenis.oneclickimporter.job;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.stereotype.Component;

@Component
public class OneClickImportJobExecutionFactory
		extends AbstractSystemEntityFactory<OneClickImportJobExecution, OneClickImportJobExecutionMetadata, String>
{
	protected OneClickImportJobExecutionFactory(OneClickImportJobExecutionMetadata oneClickImportJobExecutionMetadata,
			EntityPopulator entityPopulator)
	{
		super(OneClickImportJobExecution.class, oneClickImportJobExecutionMetadata, entityPopulator);
	}
}

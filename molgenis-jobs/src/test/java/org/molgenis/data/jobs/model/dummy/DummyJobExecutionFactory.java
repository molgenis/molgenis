package org.molgenis.data.jobs.model.dummy;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DummyJobExecutionFactory
		extends AbstractSystemEntityFactory<DummyJobExecution, DummyJobExecutionEntityType, String>
{
	@Autowired
	DummyJobExecutionFactory(DummyJobExecutionEntityType metaData, EntityPopulator entityPopulator)
	{
		super(DummyJobExecution.class, metaData, entityPopulator);
	}
}

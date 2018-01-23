package org.molgenis.jobs.model.hello;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
/**
 * Creates {@link HelloWorldJobExecution}s.
 */ public class HelloWorldJobExecutionFactory
		extends AbstractSystemEntityFactory<HelloWorldJobExecution, HelloWorldJobExecutionMetadata, String>
{
	@Autowired
	HelloWorldJobExecutionFactory(HelloWorldJobExecutionMetadata metaData, EntityPopulator entityPopulator)
	{
		super(HelloWorldJobExecution.class, metaData, entityPopulator);
	}
}

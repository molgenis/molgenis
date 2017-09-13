package org.molgenis.amazon.bucket.meta;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.stereotype.Component;

@Component
public class AmazonBucketJobExecutionFactory
		extends AbstractSystemEntityFactory<AmazonBucketJobExecution, AmazonBucketJobExecutionMetaData, String>
{
	AmazonBucketJobExecutionFactory(AmazonBucketJobExecutionMetaData fileIngestJobExecutionMetaData,
			EntityPopulator entityPopulator)
	{
		super(AmazonBucketJobExecution.class, fileIngestJobExecutionMetaData, entityPopulator);
	}
}
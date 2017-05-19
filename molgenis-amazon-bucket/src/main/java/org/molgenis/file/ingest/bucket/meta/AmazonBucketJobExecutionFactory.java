package org.molgenis.file.ingest.bucket.meta;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AmazonBucketJobExecutionFactory
		extends AbstractSystemEntityFactory<AmazonBucketJobExecution, AmazonBucketJobExecutionMetaData, String>
{
	@Autowired
	AmazonBucketJobExecutionFactory(AmazonBucketJobExecutionMetaData fileIngestJobExecutionMetaData,
			EntityPopulator entityPopulator)
	{
		super(AmazonBucketJobExecution.class, fileIngestJobExecutionMetaData, entityPopulator);
	}
}
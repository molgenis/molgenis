package org.molgenis.data.annotation.web.meta;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.stereotype.Component;

@Component
public class AnnotationJobExecutionFactory
		extends AbstractSystemEntityFactory<AnnotationJobExecution, AnnotationJobExecutionMetaData, String>
{
	AnnotationJobExecutionFactory(AnnotationJobExecutionMetaData annotationJobExecutionMetaData,
			EntityPopulator entityPopulator)
	{
		super(AnnotationJobExecution.class, annotationJobExecutionMetaData, entityPopulator);
	}
}

package org.molgenis.data.annotation.meta;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AnnotationJobExecutionFactory
		extends AbstractSystemEntityFactory<AnnotationJobExecution, AnnotationJobExecutionMetaData, String>
{
	@Autowired
	AnnotationJobExecutionFactory(AnnotationJobExecutionMetaData annotationJobExecutionMetaData)
	{
		super(AnnotationJobExecution.class, annotationJobExecutionMetaData);
	}
}

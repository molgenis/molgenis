package org.molgenis.data.annotation.meta;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.jobs.JobExecutionMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.springframework.stereotype.Component;

@Component
public class AnnotationJobExecutionMetaData extends DefaultEntityMetaData
{
	public AnnotationJobExecutionMetaData()
	{
		super(AnnotationJobExecution.ENTITY_NAME, AnnotationJobExecution.class);
		setExtends(new JobExecutionMetaData());

		addAttribute(AnnotationJobExecution.TARGET).setDataType(MolgenisFieldTypes.STRING)
				.setLabel("Entities being modified by this job").setNillable(true);
		addAttribute(AnnotationJobExecution.ANNOTATORS).setDataType(MolgenisFieldTypes.STRING)
				.setLabel("Annotators run by this job").setNillable(true);
	}
}
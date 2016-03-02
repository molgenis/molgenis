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
		addAttribute(AnnotationJobExecution.TARGET_NAME).setDataType(MolgenisFieldTypes.STRING).setLabel("target name")
				.setDescription("Fully qualified name of the entity that is being annotated.").setNillable(false);
		addAttribute(AnnotationJobExecution.ANNOTATORS).setDataType(MolgenisFieldTypes.STRING)
				.setLabel("Annotators run by this job").setNillable(false);
	}
}
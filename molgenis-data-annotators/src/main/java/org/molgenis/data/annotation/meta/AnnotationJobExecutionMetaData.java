package org.molgenis.data.annotation.meta;

import static java.util.Objects.requireNonNull;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.auth.MolgenisUserMetaData;
import org.molgenis.data.jobs.JobExecutionMetaData;
import org.molgenis.data.meta.EntityMetaDataImpl;
import org.molgenis.data.meta.SystemEntityMetaDataImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class AnnotationJobExecutionMetaData extends SystemEntityMetaDataImpl
{
	private JobExecutionMetaData jobExecutionMetaData;

	@Override
	public void init()
	{
		setName(AnnotationJobExecution.ENTITY_NAME);
		setExtends(jobExecutionMetaData);
		addAttribute(AnnotationJobExecution.TARGET_NAME).setDataType(MolgenisFieldTypes.STRING).setLabel("target name")
				.setDescription("Fully qualified name of the entity that is being annotated.").setNillable(false);
		addAttribute(AnnotationJobExecution.ANNOTATORS).setDataType(MolgenisFieldTypes.STRING)
				.setLabel("Annotators run by this job").setNillable(false);
	}

	// setter injection instead of constructor injection to avoid unresolvable circular dependencies
	@Autowired
	public void setJobExecutionMetaData(JobExecutionMetaData jobExecutionMetaData)
	{
		this.jobExecutionMetaData = requireNonNull(jobExecutionMetaData);
	}
}
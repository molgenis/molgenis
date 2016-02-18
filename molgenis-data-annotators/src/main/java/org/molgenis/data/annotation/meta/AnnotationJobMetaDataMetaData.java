package org.molgenis.data.annotation.meta;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.jobs.JobMetaData;
import org.molgenis.data.jobs.JobMetaDataMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.springframework.stereotype.Component;

import static com.google.common.collect.Lists.newArrayList;

@Component
public class AnnotationJobMetaDataMetaData extends DefaultEntityMetaData
{
	public AnnotationJobMetaDataMetaData()
	{
		super(AnnotationJobMetaData.ENTITY_NAME, AnnotationJobMetaData.class);
		setExtends(new JobMetaDataMetaData());

		addAttribute(AnnotationJobMetaData.TARGET).setDataType(MolgenisFieldTypes.STRING)
				.setLabel("Entities being modified by this job").setNillable(true);
		addAttribute(AnnotationJobMetaData.LOG).setDataType(MolgenisFieldTypes.TEXT)
				.setLabel("Log of the annotation progress").setNillable(true);
		addAttribute(AnnotationJobMetaData.ANNOTATORS).setDataType(MolgenisFieldTypes.STRING)
				.setLabel("Annotators ran by this job").setNillable(true);
	}
}
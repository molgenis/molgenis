package org.molgenis.data.jobs;

import static com.google.common.collect.Lists.newArrayList;
import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_ID;

import java.util.List;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.auth.MolgenisUserMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.fieldtypes.EnumField;
import org.springframework.stereotype.Component;

@Component
public class JobMetaDataMetaData extends DefaultEntityMetaData
{
	private List<String> jobStatusOptions = newArrayList("PENDING", "RUNNING", "SUCCESS", "FAILED", "CANCELED");

	public JobMetaDataMetaData()
	{
		super(JobMetaData.ENTITY_NAME, JobMetaData.class);
		addAttribute(JobMetaData.IDENTIFIER, ROLE_ID).setLabel("Job ID").setAuto(true).setNillable(false);
		addAttribute(JobMetaData.USER).setDataType(MolgenisFieldTypes.XREF).setRefEntity(new MolgenisUserMetaData())
				.setLabel("Job owner").setNillable(false);
		addAttribute(JobMetaData.STATUS).setDataType(new EnumField()).setEnumOptions(jobStatusOptions)
				.setLabel("Job status").setNillable(false);
		addAttribute(JobMetaData.TYPE).setDataType(MolgenisFieldTypes.STRING).setLabel("Job type").setNillable(false);
		addAttribute(JobMetaData.SUBMISSION_DATE).setDataType(MolgenisFieldTypes.DATETIME).setLabel("Job submission date")
				.setNillable(false);
		addAttribute(JobMetaData.START_DATE).setDataType(MolgenisFieldTypes.DATETIME).setLabel("Job start date")
				.setNillable(true);
		addAttribute(JobMetaData.END_DATE).setDataType(MolgenisFieldTypes.DATETIME).setLabel("Job end date").setNillable(true);
		addAttribute(JobMetaData.PROGRESS_INT).setDataType(MolgenisFieldTypes.INT).setLabel("Progress")
				.setNillable(true);
		addAttribute(JobMetaData.PROGRESS_MAX).setDataType(MolgenisFieldTypes.INT)
				.setLabel("Maximum progress").setNillable(true);
		addAttribute(JobMetaData.PROGRESS_MESSAGE).setDataType(MolgenisFieldTypes.TEXT).setLabel("Progress message")
				.setNillable(true);
	}

}
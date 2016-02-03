package org.molgenis.data.jobs;

import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_ID;
import static org.testng.collections.Lists.newArrayList;

import java.util.List;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.auth.MolgenisUserMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.springframework.stereotype.Component;

@Component
public class JobMetaData extends DefaultEntityMetaData
{
	private List<String> jobStatusOptions = newArrayList("Pending", "Running", "Success", "Failed", "Canceled");

	public JobMetaData()
	{
		super(Job.ENTITY_NAME, Job.class);
		addAttribute(Job.IDENTIFIER, ROLE_ID).setLabel("Job ID").setAuto(true).setNillable(false);
		addAttribute(Job.USER).setDataType(MolgenisFieldTypes.XREF).setRefEntity(new MolgenisUserMetaData())
				.setLabel("Job owner").setNillable(false);
		addAttribute(Job.STATUS).setDataType(MolgenisFieldTypes.ENUM).setEnumOptions(jobStatusOptions)
				.setLabel("Job status").setNillable(false);
		addAttribute(Job.TYPE).setDataType(MolgenisFieldTypes.STRING).setLabel("Job type").setNillable(false);
		addAttribute(Job.SUBMISSION_DATE).setDataType(MolgenisFieldTypes.DATETIME).setLabel("Job submission date")
				.setNillable(false);
		addAttribute(Job.START_DATE).setDataType(MolgenisFieldTypes.DATETIME).setLabel("Job start date")
				.setNillable(true);
		addAttribute(Job.END_DATE).setDataType(MolgenisFieldTypes.DATETIME).setLabel("Job end date").setNillable(true);
		addAttribute(Job.PROGRESS_INT).setDataType(MolgenisFieldTypes.INT).setLabel("Number of entities processed")
				.setNillable(true);
		addAttribute(Job.PROGRESS_MESSAGE).setDataType(MolgenisFieldTypes.STRING).setLabel("Progress message")
				.setNillable(true);
		addAttribute(Job.PROGRESS_MAX).setDataType(MolgenisFieldTypes.INT)
				.setLabel("Maximum number of entities to be processed").setNillable(true);
	}

}
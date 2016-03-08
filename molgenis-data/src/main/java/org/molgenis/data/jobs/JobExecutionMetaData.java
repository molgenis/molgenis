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
public class JobExecutionMetaData extends DefaultEntityMetaData
{
	private final List<String> jobStatusOptions = newArrayList("PENDING", "RUNNING", "SUCCESS", "FAILED", "CANCELED");

	public JobExecutionMetaData()
	{
		super(JobExecution.ENTITY_NAME, JobExecution.class);
		setAbstract(true);
		addAttribute(JobExecution.IDENTIFIER, ROLE_ID).setLabel("Job ID").setAuto(true).setNillable(false);
		addAttribute(JobExecution.USER).setDataType(MolgenisFieldTypes.XREF).setRefEntity(new MolgenisUserMetaData())
				.setLabel("Job owner").setNillable(false);
		addAttribute(JobExecution.STATUS).setDataType(new EnumField()).setEnumOptions(jobStatusOptions)
				.setLabel("Job status").setNillable(false);
		addAttribute(JobExecution.TYPE).setDataType(MolgenisFieldTypes.STRING).setLabel("Job type").setNillable(false);
		addAttribute(JobExecution.SUBMISSION_DATE).setDataType(MolgenisFieldTypes.DATETIME)
				.setLabel("Job submission date").setNillable(false);
		addAttribute(JobExecution.START_DATE).setDataType(MolgenisFieldTypes.DATETIME).setLabel("Job start date")
				.setNillable(true);
		addAttribute(JobExecution.END_DATE).setDataType(MolgenisFieldTypes.DATETIME).setLabel("Job end date")
				.setNillable(true);
		addAttribute(JobExecution.PROGRESS_INT).setDataType(MolgenisFieldTypes.INT).setLabel("Progress")
				.setNillable(true);
		addAttribute(JobExecution.PROGRESS_MAX).setDataType(MolgenisFieldTypes.INT).setLabel("Maximum progress")
				.setNillable(true);
		addAttribute(JobExecution.PROGRESS_MESSAGE).setDataType(MolgenisFieldTypes.STRING).setLabel("Progress message")
				.setNillable(true);
		addAttribute(JobExecution.LOG).setDataType(MolgenisFieldTypes.TEXT).setLabel("Log").setNillable(true);
		addAttribute(JobExecution.RESULT_URL).setDataType(MolgenisFieldTypes.HYPERLINK).setLabel("Result URL")
				.setNillable(true);
		addAttribute(JobExecution.FAILURE_EMAIL).setDataType(MolgenisFieldTypes.STRING).setLabel("Failure email")
				.setDescription("Comma-separated email addresses to send email to if execution fails or is canceled")
				.setNillable(true);
		addAttribute(JobExecution.SUCCESS_EMAIL).setDataType(MolgenisFieldTypes.STRING).setLabel("Success email")
				.setDescription("Comma-separated email addresses to send email to if execution succeeds")
				.setNillable(true);
	}

}
package org.molgenis.data.jobs;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Objects.requireNonNull;
import static org.molgenis.MolgenisFieldTypes.DATETIME;
import static org.molgenis.MolgenisFieldTypes.HYPERLINK;
import static org.molgenis.MolgenisFieldTypes.INT;
import static org.molgenis.MolgenisFieldTypes.STRING;
import static org.molgenis.MolgenisFieldTypes.TEXT;
import static org.molgenis.MolgenisFieldTypes.XREF;
import static org.molgenis.data.meta.EntityMetaData.AttributeRole.ROLE_ID;

import java.util.List;

import org.molgenis.auth.MolgenisUserMetaData;
import org.molgenis.data.meta.SystemEntityMetaDataImpl;
import org.molgenis.fieldtypes.EnumField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JobExecutionMetaData extends SystemEntityMetaDataImpl
{
	private final List<String> jobStatusOptions = newArrayList("PENDING", "RUNNING", "SUCCESS", "FAILED", "CANCELED");

	private MolgenisUserMetaData molgenisUserMetaData;

	@Override
	public void init()
	{
		setName(JobExecution.ENTITY_NAME);
		setAbstract(true);
		addAttribute(JobExecution.IDENTIFIER, ROLE_ID).setLabel("Job ID").setAuto(true).setNillable(false);
		addAttribute(JobExecution.USER).setDataType(XREF).setRefEntity(molgenisUserMetaData).setLabel("Job owner")
				.setNillable(false);
		addAttribute(JobExecution.STATUS).setDataType(new EnumField()).setEnumOptions(jobStatusOptions)
				.setLabel("Job status").setNillable(false);
		addAttribute(JobExecution.TYPE).setDataType(STRING).setLabel("Job type").setNillable(false);
		addAttribute(JobExecution.SUBMISSION_DATE).setDataType(DATETIME).setLabel("Job submission date")
				.setNillable(false);
		addAttribute(JobExecution.START_DATE).setDataType(DATETIME).setLabel("Job start date").setNillable(true);
		addAttribute(JobExecution.END_DATE).setDataType(DATETIME).setLabel("Job end date").setNillable(true);
		addAttribute(JobExecution.PROGRESS_INT).setDataType(INT).setLabel("Progress").setNillable(true);
		addAttribute(JobExecution.PROGRESS_MAX).setDataType(INT).setLabel("Maximum progress").setNillable(true);
		addAttribute(JobExecution.PROGRESS_MESSAGE).setDataType(STRING).setLabel("Progress message").setNillable(true);
		addAttribute(JobExecution.LOG).setDataType(TEXT).setLabel("Log").setNillable(true);
		addAttribute(JobExecution.RESULT_URL).setDataType(HYPERLINK).setLabel("Result URL").setNillable(true);
		addAttribute(JobExecution.FAILURE_EMAIL).setDataType(STRING).setLabel("Failure email")
				.setDescription("Comma-separated email addresses to send email to if execution fails or is canceled")
				.setNillable(true);
		addAttribute(JobExecution.SUCCESS_EMAIL).setDataType(STRING).setLabel("Success email")
				.setDescription("Comma-separated email addresses to send email to if execution succeeds")
				.setNillable(true);
	}

	// setter injection instead of constructor injection to avoid unresolvable circular dependencies
	@Autowired
	public void setMolgenisUserMetaData(MolgenisUserMetaData molgenisUserMetaData)
	{
		this.molgenisUserMetaData = requireNonNull(molgenisUserMetaData);
	}
}
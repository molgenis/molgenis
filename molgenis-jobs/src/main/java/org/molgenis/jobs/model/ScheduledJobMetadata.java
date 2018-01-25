package org.molgenis.jobs.model;

import org.molgenis.data.meta.SystemEntityType;
import org.molgenis.util.RegexUtils;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.AttributeType.*;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.*;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.jobs.model.JobPackage.PACKAGE_JOB;

@Component
public class ScheduledJobMetadata extends SystemEntityType
{
	private static final String SIMPLE_NAME = "ScheduledJob";
	public static final String SCHEDULED_JOB = PACKAGE_JOB + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public static final String ID = "id";
	public static final String NAME = "name";
	public static final String DESCRIPTION = "description";
	public static final String CRON_EXPRESSION = "cronExpression";
	public static final String ACTIVE = "active";
	public static final String FAILURE_EMAIL = "failureEmail";
	public static final String SUCCESS_EMAIL = "successEmail";
	public static final String TYPE = "type";
	public static final String PARAMETERS = "parameters";
	public static final String USER = "user";

	private ScheduledJobTypeMetadata scheduledJobTypeMetadata;
	private final JobPackage jobPackage;

	public ScheduledJobMetadata(ScheduledJobTypeMetadata scheduledJobTypeMetadata, JobPackage jobPackage)
	{
		super(SIMPLE_NAME, PACKAGE_JOB);
		this.scheduledJobTypeMetadata = requireNonNull(scheduledJobTypeMetadata);
		this.jobPackage = requireNonNull(jobPackage);
	}

	@Override
	public void init()
	{
		setLabel("Scheduled job");
		setPackage(jobPackage);
		addAttribute(ID, ROLE_ID).setAuto(true).setNillable(false);
		addAttribute(NAME, ROLE_LABEL, ROLE_LOOKUP).setLabel("Name").setNillable(false).setUnique(true);
		addAttribute(DESCRIPTION).setDataType(TEXT).setLabel("Description").setNillable(true);
		addAttribute(CRON_EXPRESSION).setLabel("Cron expression")
									 .setNillable(false)
									 .setDescription(
											 "Cron expression. A cron expression is a string comprised of 6 or 7 fields separated by white space. "
													 + "These fields are: Seconds, Minutes, Hours, Day of month, Month, Day of week, and optionally Year. "
													 + "An example input is 0 0 12 * * ? for a job that fires at noon every day. "
													 + "See http://www.quartz-scheduler.org/documentation/quartz-2.x/tutorials/crontrigger.html")
									 .setValidationExpression(buildValidationExpressionString(CRON_EXPRESSION,
											 RegexUtils.JAVA_SCRIPT_CRON_REGEX));
		addAttribute(ACTIVE).setDataType(BOOL).setLabel("Active").setNillable(false);
		addAttribute(USER).setLabel("Username")
						  .setDescription(
								  "Name of the user to run the job as. Will be automatically filled in by the system.")
						  .setNillable(true);
		addAttribute(FAILURE_EMAIL).setDataType(STRING)
								   .setLabel("Failure email")
								   .setDescription(
										   "Comma-separated list of emails. Leave blank if you don't want to receive emails if the jobs failed.")
								   .setNillable(true)
								   .setValidationExpression(buildValidationExpressionString(FAILURE_EMAIL,
										   RegexUtils.JAVA_SCRIPT_COMMA_SEPARATED_EMAIL_LIST_REGEX));
		addAttribute(SUCCESS_EMAIL).setDataType(STRING)
								   .setLabel("Success email")
								   .setDescription(
										   "Comma-separated list of emails. Leave blank if you don't want to receive emails if the jobs succeed.")
								   .setNillable(true)
								   .setValidationExpression(buildValidationExpressionString(SUCCESS_EMAIL,
										   RegexUtils.JAVA_SCRIPT_COMMA_SEPARATED_EMAIL_LIST_REGEX));
		addAttribute(TYPE).setDataType(CATEGORICAL).setRefEntity(scheduledJobTypeMetadata).setNillable(false);
		addAttribute(PARAMETERS).setDataType(TEXT).setLabel("Job parameters").setNillable(false);
	}

	private String buildValidationExpressionString(String field, String regex) {
		return  "$('" + field + "').matches(" + regex + ").value()";
	}
}

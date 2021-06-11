package org.molgenis.jobs.model;

import static com.github.jknack.handlebars.internal.text.StringEscapeUtils.escapeJava;
import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.AttributeType.BOOL;
import static org.molgenis.data.meta.AttributeType.CATEGORICAL;
import static org.molgenis.data.meta.AttributeType.STRING;
import static org.molgenis.data.meta.AttributeType.TEXT;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_LABEL;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_LOOKUP;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.jobs.model.JobPackage.PACKAGE_JOB;
import static org.molgenis.util.RegexUtils.COMMA_SEPARATED_EMAIL_LIST_REGEX;
import static org.molgenis.util.RegexUtils.CRON_REGEX;

import org.molgenis.data.meta.SystemEntityType;
import org.springframework.stereotype.Component;

@Component
public class ScheduledJobMetadata extends SystemEntityType {

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

  private final ScheduledJobTypeMetadata scheduledJobTypeMetadata;
  private final JobPackage jobPackage;

  public ScheduledJobMetadata(
      ScheduledJobTypeMetadata scheduledJobTypeMetadata, JobPackage jobPackage) {
    super(SIMPLE_NAME, PACKAGE_JOB);
    this.scheduledJobTypeMetadata = requireNonNull(scheduledJobTypeMetadata);
    this.jobPackage = requireNonNull(jobPackage);
  }

  @Override
  public void init() {
    setLabel("Scheduled job");
    setPackage(jobPackage);
    addAttribute(ID, ROLE_ID).setAuto(true).setNillable(false);
    addAttribute(NAME, ROLE_LABEL, ROLE_LOOKUP).setLabel("Name").setNillable(false).setUnique(true);
    addAttribute(DESCRIPTION).setDataType(TEXT).setLabel("Description").setNillable(true);
    addAttribute(CRON_EXPRESSION)
        .setLabel("Cron expression")
        .setNillable(false)
        .setDescription(
            "Cron expression. A cron expression is a string comprised of 6 or 7 fields separated by white space. "
                + "These fields are: Seconds, Minutes, Hours, Day of month, Month, Day of week, and optionally Year. "
                + "An example input is 0 0 12 * * ? for a job that fires at noon every day. "
                + "See http://www.quartz-scheduler.org/documentation/quartz-2.3.0/tutorials/tutorial-lesson-06.html")
        .setValidationExpression(
            String.format("regex('%s', {%s})", escapeJava(CRON_REGEX), CRON_EXPRESSION));
    addAttribute(ACTIVE).setDataType(BOOL).setLabel("Active").setNillable(false);
    addAttribute(USER)
        .setLabel("Username")
        .setDescription(
            "Name of the user to run the job as. Will be automatically filled in by the system.")
        .setNillable(true);
    addAttribute(FAILURE_EMAIL)
        .setDataType(STRING)
        .setLabel("Failure email")
        .setDescription(
            "Comma-separated list of emails. Leave blank if you don't want to receive emails if the jobs failed.")
        .setNillable(true)
        .setValidationExpression(
            String.format(
                "{%s} empty or regex('%s', {%s})",
                FAILURE_EMAIL, escapeJava(COMMA_SEPARATED_EMAIL_LIST_REGEX), FAILURE_EMAIL));
    addAttribute(SUCCESS_EMAIL)
        .setDataType(STRING)
        .setLabel("Success email")
        .setDescription(
            "Comma-separated list of emails. Leave blank if you don't want to receive emails if the jobs succeed.")
        .setNillable(true)
        .setValidationExpression(
            String.format(
                "{%s} empty or regex('%s', {%s})",
                SUCCESS_EMAIL, escapeJava(COMMA_SEPARATED_EMAIL_LIST_REGEX), SUCCESS_EMAIL));
    addAttribute(TYPE)
        .setDataType(CATEGORICAL)
        .setRefEntity(scheduledJobTypeMetadata)
        .setNillable(false);
    addAttribute(PARAMETERS).setDataType(TEXT).setLabel("Job parameters").setNillable(false);
  }
}

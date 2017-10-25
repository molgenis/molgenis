package org.molgenis.integrationtest.jobs;

import org.molgenis.data.jobs.JobExecutionConfig;
import org.molgenis.data.jobs.JobExecutionUpdaterImpl;
import org.molgenis.data.jobs.JobFactoryRegistrar;
import org.molgenis.data.jobs.model.JobPackage;
import org.molgenis.data.jobs.model.ScheduledJobTypeFactory;
import org.molgenis.data.jobs.model.ScheduledJobTypeMetadata;
import org.molgenis.data.jobs.schedule.JobScheduler;
import org.molgenis.scheduler.SchedulerConfig;
import org.molgenis.settings.mail.JavaMailPropertyType;
import org.molgenis.settings.mail.MailPackage;
import org.molgenis.settings.mail.MailSettingsImpl;
import org.molgenis.util.mail.JavaMailSenderFactory;
import org.molgenis.util.mail.MailSenderImpl;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({ SchedulerConfig.class, JobExecutionConfig.class, JobPackage.class, ScheduledJobTypeFactory.class,
		ScheduledJobTypeMetadata.class, JobExecutionUpdaterImpl.class, MailSenderImpl.class, MailSettingsImpl.class,
		JavaMailSenderFactory.class, JobScheduler.class, JobFactoryRegistrar.class, JavaMailPropertyType.class,
		MailPackage.class })
public class JobsTestConfig
{

}

package org.molgenis.integrationtest.config;

import org.molgenis.jobs.JobExecutionConfig;
import org.molgenis.jobs.JobExecutionUpdaterImpl;
import org.molgenis.jobs.JobFactoryRegistrar;
import org.molgenis.jobs.model.JobPackage;
import org.molgenis.jobs.model.ScheduledJobTypeFactory;
import org.molgenis.jobs.model.ScheduledJobTypeMetadata;
import org.molgenis.jobs.schedule.JobScheduler;
import org.molgenis.jobs.scheduler.SchedulerConfig;
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

package org.molgenis.jobs.scheduler;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

/**
 * Configure a scheduler factory based on a Quartz scheduler with jobs supporting autowiring.
 */
@Configuration
@EnableScheduling
public class SchedulerConfig
{
	private final ApplicationContext applicationContext;

	public SchedulerConfig(ApplicationContext applicationContext)
	{
		this.applicationContext = applicationContext;
	}

	@Bean
	public SchedulerFactoryBean schedulerFactoryBean()
	{
		SchedulerFactoryBean quartzScheduler = new SchedulerFactoryBean();
		AutowiringSpringBeanJobFactory jobFactory = new AutowiringSpringBeanJobFactory();
		jobFactory.setApplicationContext(applicationContext);
		quartzScheduler.setJobFactory(jobFactory);
		return quartzScheduler;
	}
}
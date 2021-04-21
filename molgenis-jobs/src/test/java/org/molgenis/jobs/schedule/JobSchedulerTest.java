package org.molgenis.jobs.schedule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.jobs.model.ScheduledJobMetadata.SCHEDULED_JOB;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.DataService;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.security.auth.SecurityPackage;
import org.molgenis.data.validation.MolgenisValidationException;
import org.molgenis.jobs.config.JobTestConfig;
import org.molgenis.jobs.model.ScheduledJob;
import org.molgenis.jobs.model.ScheduledJobFactory;
import org.molgenis.jobs.model.ScheduledJobMetadata;
import org.molgenis.jobs.model.ScheduledJobType;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = {JobSchedulerTest.Config.class})
class JobSchedulerTest extends AbstractMolgenisSpringTest {
  @Autowired private Config config;

  @Autowired private JobScheduler jobScheduler;

  @Autowired private DataService dataService;

  @Autowired private Scheduler quartzScheduler;

  @Autowired private ScheduledJobFactory scheduledJobFactory;

  @Mock private ScheduledJobType scheduledJobType;

  private String id = "id";
  private ScheduledJob scheduledJob;
  private JobKey jobKey = JobKey.jobKey(id, JobScheduler.SCHEDULED_JOB_GROUP);

  @BeforeEach
  void setUpBeforeMethod() {
    config.resetMocks();
    reset(scheduledJobType);
    scheduledJob = scheduledJobFactory.create();
    scheduledJob.setId(id);
    scheduledJob.setType(scheduledJobType);
  }

  @Test
  void runNow() throws SchedulerException {
    when(dataService.findOneById(SCHEDULED_JOB, id, ScheduledJob.class)).thenReturn(scheduledJob);
    when(quartzScheduler.checkExists(jobKey)).thenReturn(false);

    jobScheduler.runNow(id);

    verify(quartzScheduler)
        .scheduleJob(ArgumentMatchers.any(JobDetail.class), ArgumentMatchers.any(Trigger.class));
  }

  @Test
  void runNowUnknownEntity() {
    when(dataService.findOneById(SCHEDULED_JOB, id)).thenReturn(null);
    Exception exception = assertThrows(UnknownEntityException.class, () -> jobScheduler.runNow(id));
    assertThat(exception.getMessage())
        .containsPattern("type:sys_job_ScheduledJob id:id attribute:null");
  }

  @Test
  void runNowExists() throws SchedulerException {

    when(dataService.findOneById(SCHEDULED_JOB, id, ScheduledJob.class)).thenReturn(scheduledJob);
    when(quartzScheduler.checkExists(jobKey)).thenReturn(true);

    jobScheduler.runNow(id);

    verify(quartzScheduler).triggerJob(jobKey);
  }

  @Test
  void schedule() throws SchedulerException {
    ScheduledJob scheduledJob = scheduledJobFactory.create();
    scheduledJob.setId(id);
    scheduledJob.set(ScheduledJobMetadata.CRON_EXPRESSION, "	0/20 * * * * ?");
    scheduledJob.set(ScheduledJobMetadata.NAME, "name");
    scheduledJob.set(ScheduledJobMetadata.ACTIVE, true);
    scheduledJob.setType(scheduledJobType);

    when(quartzScheduler.checkExists(jobKey)).thenReturn(false);

    jobScheduler.schedule(scheduledJob);

    verify(quartzScheduler)
        .scheduleJob(ArgumentMatchers.any(JobDetail.class), ArgumentMatchers.any(Trigger.class));
  }

  @Test
  void scheduleInactive() throws SchedulerException {
    ScheduledJob scheduledJob = scheduledJobFactory.create();
    scheduledJob.setId(id);
    scheduledJob.set(ScheduledJobMetadata.CRON_EXPRESSION, "	0/20 * * * * ?");
    scheduledJob.set(ScheduledJobMetadata.NAME, "name");
    scheduledJob.set(ScheduledJobMetadata.ACTIVE, false);
    scheduledJob.setType(scheduledJobType);

    when(quartzScheduler.checkExists(jobKey)).thenReturn(false);

    jobScheduler.schedule(scheduledJob);

    verify(quartzScheduler, Mockito.never())
        .scheduleJob(ArgumentMatchers.any(JobDetail.class), ArgumentMatchers.any(Trigger.class));
  }

  @Test
  void scheduleInvalidCronExpression() throws SchedulerException {
    ScheduledJob scheduledJob = scheduledJobFactory.create();
    scheduledJob.setId(id);
    scheduledJob.set(ScheduledJobMetadata.CRON_EXPRESSION, "XXX");
    scheduledJob.set(ScheduledJobMetadata.NAME, "name");
    scheduledJob.set(ScheduledJobMetadata.ACTIVE, false);
    scheduledJob.setType(scheduledJobType);

    assertThrows(MolgenisValidationException.class, () -> jobScheduler.schedule(scheduledJob));
  }

  @Test
  void scheduleExisting() throws SchedulerException {
    ScheduledJob scheduledJob = scheduledJobFactory.create();
    scheduledJob.setId(id);
    scheduledJob.set(ScheduledJobMetadata.CRON_EXPRESSION, "	0/20 * * * * ?");
    scheduledJob.set(ScheduledJobMetadata.NAME, "name");
    scheduledJob.set(ScheduledJobMetadata.ACTIVE, true);
    scheduledJob.setType(scheduledJobType);

    when(quartzScheduler.checkExists(jobKey)).thenReturn(true);
    when(dataService.findOneById(SCHEDULED_JOB, id, ScheduledJob.class)).thenReturn(scheduledJob);

    jobScheduler.schedule(scheduledJob);

    verify(quartzScheduler).deleteJob((jobKey));
    verify(quartzScheduler)
        .scheduleJob(ArgumentMatchers.any(JobDetail.class), ArgumentMatchers.any(Trigger.class));
  }

  @Test
  void unschedule() throws SchedulerException {
    String id = "id";
    when(dataService.findOneById(SCHEDULED_JOB, id, ScheduledJob.class)).thenReturn(scheduledJob);
    jobScheduler.unschedule(id);
    verify(quartzScheduler).deleteJob((jobKey));
  }

  @SuppressWarnings("java:S5979") // mocks are initialized
  @Configuration
  @Import({SecurityPackage.class, JobTestConfig.class})
  static class Config {
    @Autowired private DataService dataService;

    @Mock private Scheduler quartzScheduler;

    Config() {
      org.mockito.MockitoAnnotations.initMocks(this);
    }

    void resetMocks() {
      reset(quartzScheduler);
    }

    @Bean
    JobScheduler jobScheduler() {
      return new JobScheduler(quartzScheduler(), dataService);
    }

    @Bean
    Scheduler quartzScheduler() {
      return quartzScheduler;
    }
  }
}

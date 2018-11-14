package org.molgenis.jobs;

import org.molgenis.jobs.model.JobExecution;

interface JobExecutionContextFactory {

  JobExecutionContext createJobExecutionContext(JobExecution jobExecution);
}

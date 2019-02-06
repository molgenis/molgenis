package org.molgenis.jobs;

import org.molgenis.jobs.model.JobExecution;

interface ProgressFactory {
  Progress create(JobExecution jobExecution);
}

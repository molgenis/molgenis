package org.molgenis.jobs;

import java.util.Locale;
import org.molgenis.jobs.model.JobExecution;

interface JobExecutorLocaleService {
  /** @return Locale, never <tt>null</tt> */
  Locale createLocale(JobExecution jobExecution);
}

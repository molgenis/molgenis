package org.molgenis.jobs;

import static java.util.Objects.requireNonNull;

import org.molgenis.jobs.model.JobExecution;
import org.springframework.mail.MailSender;
import org.springframework.stereotype.Component;

@Component
class ProgressFactoryImpl implements ProgressFactory {
  private final JobExecutionUpdater jobExecutionUpdater;
  private final MailSender mailSender;

  ProgressFactoryImpl(JobExecutionUpdater jobExecutionUpdater, MailSender mailSender) {
    this.jobExecutionUpdater = requireNonNull(jobExecutionUpdater);
    this.mailSender = requireNonNull(mailSender);
  }

  @Override
  public Progress create(JobExecution jobExecution) {
    Progress progress = new ProgressImpl(jobExecution, jobExecutionUpdater, mailSender);
    return new ProgressCancellationDecorator(progress);
  }
}

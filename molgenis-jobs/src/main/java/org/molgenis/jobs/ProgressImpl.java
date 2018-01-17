package org.molgenis.jobs;

import org.joda.time.Duration;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import org.molgenis.jobs.model.JobExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static java.util.Objects.requireNonNull;
import static org.molgenis.jobs.model.JobExecution.Status.*;

/**
 * Tracks progress and stores it in a {@link JobExecution} entity.
 * The entity may be a subclass of {@link JobExecution}.
 */
public class ProgressImpl implements Progress
{
	private static final Logger JOB_EXECUTION_LOG = LoggerFactory.getLogger(JobExecution.class);

	private final JobExecution jobExecution;
	private final JobExecutionUpdater updater;
	private final MailSender mailSender;

	public ProgressImpl(JobExecution jobExecution, JobExecutionUpdater updater, MailSender mailSender)
	{
		this.jobExecution = requireNonNull(jobExecution);
		this.mailSender = requireNonNull(mailSender);
		this.updater = requireNonNull(updater);
	}

	private void update()
	{
		updater.update(jobExecution);
	}

	@Override
	public void start()
	{
		JobExecutionContext.set(jobExecution);
		JOB_EXECUTION_LOG.info("Execution started.");
		jobExecution.setStartDate(Instant.now());
		jobExecution.setStatus(RUNNING);
		update();
	}

	@Override
	public void progress(int progress, String message)
	{
		jobExecution.setProgressInt(progress);
		jobExecution.setProgressMessage(message);
		JOB_EXECUTION_LOG.debug("progress ({}, {})", progress, message);
		update();
	}

	@Override
	public void increment(int amount)
	{
		jobExecution.setProgressInt(jobExecution.getProgressInt() + amount);
		update();
	}

	@Override
	public void success()
	{
		jobExecution.setEndDate(Instant.now());
		jobExecution.setStatus(SUCCESS);
		jobExecution.setProgressInt(jobExecution.getProgressMax());
		Duration yourDuration = Duration.millis(timeRunning());
		Period period = yourDuration.toPeriod();
		PeriodFormatter periodFormatter = new PeriodFormatterBuilder().appendDays()
																	  .appendSuffix("d ")
																	  .appendHours()
																	  .appendSuffix("h ")
																	  .appendMinutes()
																	  .appendSuffix("m ")
																	  .appendSeconds()
																	  .appendSuffix("s ")
																	  .appendMillis()
																	  .appendSuffix("ms ")
																	  .toFormatter();
		String timeSpent = periodFormatter.print(period);
		JOB_EXECUTION_LOG.info("Execution successful. Time spent: {}", timeSpent);
		sendEmail(jobExecution.getSuccessEmail(), jobExecution.getType() + " job succeeded.", jobExecution.getLog());
		update();
		JobExecutionContext.unset();
	}

	@Override
	public void failed(Exception ex)
	{
		JOB_EXECUTION_LOG.error("Failed. " + ex.getMessage(), ex);
		jobExecution.setEndDate(Instant.now());
		jobExecution.setStatus(FAILED);
		jobExecution.setProgressMessage(ex.getMessage());
		sendEmail(jobExecution.getFailureEmail(), jobExecution.getType() + " job failed.", jobExecution.getLog());
		update();
		JobExecutionContext.unset();
	}

	private void sendEmail(String[] to, String subject, String text)
	{
		if (to.length > 0)
		{
			try
			{
				SimpleMailMessage mailMessage = new SimpleMailMessage();
				mailMessage.setTo(to);
				mailMessage.setSubject(subject);
				mailMessage.setText(text);
				mailSender.send(mailMessage);
			}
			catch (RuntimeException e)
			{
				jobExecution.setProgressMessage(
						String.format("%s (Mail not sent: %s)", jobExecution.getProgressMessage(), e.getMessage()));
			}
		}
	}

	@Override
	public void canceled()
	{
		JOB_EXECUTION_LOG.warn("Canceled");
		jobExecution.setEndDate(Instant.now());
		jobExecution.setStatus(CANCELED);
		sendEmail(jobExecution.getFailureEmail(), jobExecution.getType() + " job failed.", jobExecution.getLog());
		update();
		JobExecutionContext.unset();
	}

	@Override
	public Long timeRunning()
	{
		Instant startDate = jobExecution.getStartDate();
		if (startDate == null)
		{
			return null;
		}
		return ChronoUnit.MILLIS.between(startDate, Instant.now());
	}

	@Override
	public void setProgressMax(int max)
	{
		jobExecution.setProgressMax(max);
		update();
	}

	@Override
	public void status(String message)
	{
		JOB_EXECUTION_LOG.info(message);
		jobExecution.setProgressMessage(message);
		update();
	}

	@Override
	public void setResultUrl(String string)
	{
		jobExecution.setResultUrl(string);
	}

	@Override
	public JobExecution getJobExecution()
	{
		return jobExecution;
	}
}

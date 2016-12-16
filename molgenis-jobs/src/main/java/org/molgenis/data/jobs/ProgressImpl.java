package org.molgenis.data.jobs;

import org.joda.time.Duration;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import org.molgenis.data.jobs.model.JobExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;

import java.util.Date;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.jobs.model.JobExecution.Status.*;

/**
 * Tracks progress and stores it in a {@link JobExecution} entity.
 * The entity may be a subclass of {@link JobExecution}.
 */
public class ProgressImpl implements Progress
{
	private static final Logger LOG = LoggerFactory.getLogger(ProgressImpl.class);
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
		jobExecution.setStartDate(new Date());
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
	public void success()
	{
		jobExecution.setEndDate(new Date());
		jobExecution.setStatus(SUCCESS);
		jobExecution.setProgressInt(jobExecution.getProgressMax());
		Duration yourDuration = Duration.millis(timeRunning());
		Period period = yourDuration.toPeriod();
		PeriodFormatter periodFormatter = new PeriodFormatterBuilder().appendDays().appendSuffix("d ").appendMinutes()
				.appendSuffix("m ").appendSeconds().appendSuffix("s ").appendMillis().appendSuffix("ms ").toFormatter();
		String timeSpent = periodFormatter.print(period);
		JOB_EXECUTION_LOG.info("Execution successful. Time spent: {}", timeSpent);
		sendEmail(jobExecution.getSuccessEmail(), jobExecution.getType() + " job succeeded.", jobExecution.getLog());
		update();
		JobExecutionContext.unset();
	}

	private void sendEmail(String[] to, String subject, String text) throws MailException
	{
		if (to.length > 0)
		{
			SimpleMailMessage mailMessage = new SimpleMailMessage();
			mailMessage.setTo(to);
			mailMessage.setSubject(subject);
			mailMessage.setText(text);
			mailSender.send(mailMessage);
		}
	}

	@Override
	public void failed(Exception ex)
	{
		JOB_EXECUTION_LOG.error("Failed. " + ex.getMessage(), ex);
		jobExecution.setEndDate(new Date());
		jobExecution.setStatus(FAILED);
		jobExecution.setProgressMessage(ex.getMessage());
		sendEmail(jobExecution.getFailureEmail(), jobExecution.getType() + " job failed.", jobExecution.getLog());
		update();
		JobExecutionContext.unset();
	}

	@Override
	public void canceled()
	{
		JOB_EXECUTION_LOG.warn("Canceled");
		jobExecution.setEndDate(new Date());
		jobExecution.setStatus(CANCELED);
		sendEmail(jobExecution.getFailureEmail(), jobExecution.getType() + " job failed.", jobExecution.getLog());
		update();
		JobExecutionContext.unset();
	}

	@Override
	public Long timeRunning()
	{
		Date startDate = jobExecution.getStartDate();
		if (startDate == null)
		{
			return null;
		}
		return System.currentTimeMillis() - startDate.getTime();
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

}

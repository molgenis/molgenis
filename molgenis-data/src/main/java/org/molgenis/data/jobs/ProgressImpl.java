package org.molgenis.data.jobs;

import static org.molgenis.data.jobs.JobExecution.Status.CANCELED;
import static org.molgenis.data.jobs.JobExecution.Status.FAILED;
import static org.molgenis.data.jobs.JobExecution.Status.RUNNING;
import static org.molgenis.data.jobs.JobExecution.Status.SUCCESS;
import static org.molgenis.security.core.runas.RunAsSystemProxy.runAsSystem;

import java.util.Date;

import org.joda.time.Duration;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import org.molgenis.data.DataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;

/**
 * Tracks progress and stores it in a {@link JobExecution} entity. The entity may be a subclass of {@link JobExecution}.
 */
public class ProgressImpl implements Progress
{
	private final JobExecution jobMetaData;
	private final DataService dataService;
	private ch.qos.logback.classic.Logger executionLogger;
	private final static Logger LOG = LoggerFactory.getLogger(ProgressImpl.class);
	private EntityLogAppender appender;

	public ProgressImpl(JobExecution jobMetaData, DataService dataService)
	{
		this.jobMetaData = jobMetaData;
		this.dataService = dataService;
		this.executionLogger = (ch.qos.logback.classic.Logger) LoggerFactory
				.getLogger("Job Execution[" + jobMetaData.getIdentifier() + "]");
		executionLogger.setLevel(Level.ALL);

		LoggerContext loggerContext = executionLogger.getLoggerContext();
		appender = new EntityLogAppender(jobMetaData, loggerContext);
		appender.start();
		appender.setContext(loggerContext);
		executionLogger.addAppender(appender);
	}

	private void update()
	{
		runAsSystem(() -> {
			dataService.update(jobMetaData.getEntityMetaData().getName(), jobMetaData);
		});
	}

	@Override
	public void start()
	{
		executionLogger.info("start ()");
		LOG.info("start()");
		jobMetaData.setStartDate(new Date());
		jobMetaData.setStatus(RUNNING);
		update();
	}

	@Override
	public void progress(int progress, String message)
	{
		jobMetaData.setProgressInt(progress);
		jobMetaData.setProgressMessage(message);
		executionLogger.info("progress ({}, {})", progress, message);
		update();
	}

	@Override
	public void success()
	{
		jobMetaData.setEndDate(new Date());
		jobMetaData.setStatus(SUCCESS);
		jobMetaData.setProgressInt(jobMetaData.getProgressMax());
		Duration yourDuration = Duration.millis(timeRunning());
		Period period = yourDuration.toPeriod();
		PeriodFormatter periodFormatter = new PeriodFormatterBuilder().printZeroAlways().minimumPrintedDigits(2)
				.appendHours().appendSeparator(":").appendMinutes().appendSeparator(":").appendSeconds()
				.appendSeparator(".").appendMillis3Digit().toFormatter();
		periodFormatter = new PeriodFormatterBuilder().appendDays().appendSuffix("d ").appendMinutes()
				.appendSuffix("m ").appendSeconds().appendSuffix("s ").appendMillis().appendSuffix("ms ").toFormatter();
		String timeSpent = periodFormatter.print(period);
		executionLogger.info("Execution successful. Time spent: {}", timeSpent);
		appender.stop();
		update();
	}

	@Override
	public void failed(Exception ex)
	{
		executionLogger.error("Failed", ex);
		jobMetaData.setEndDate(new Date());
		jobMetaData.setStatus(FAILED);
		appender.stop();
		update();
	}

	@Override
	public void canceled()
	{
		executionLogger.warn("Canceled");
		jobMetaData.setEndDate(new Date());
		jobMetaData.setStatus(CANCELED);
		appender.stop();
		update();
	}

	@Override
	public Long timeRunning()
	{
		Date startDate = jobMetaData.getStartDate();
		if (startDate == null)
		{
			return null;
		}
		return System.currentTimeMillis() - startDate.getTime();
	}

	@Override
	public void setProgressMax(int max)
	{
		jobMetaData.setProgressMax(max);
		update();
	}

	@Override
	public void status(String message)
	{
		executionLogger.info(message);
		jobMetaData.setProgressMessage(message);
		update();
	}

}

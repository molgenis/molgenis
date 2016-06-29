package org.molgenis.data.jobs;

import org.molgenis.data.jobs.model.JobExecution;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

/**
 * Appender that appends to the log attribute of a {@link JobExecution} entity.
 */
public class JobExecutionLogAppender extends AppenderBase<ILoggingEvent>
{
	private PatternLayout layout;

	private void createLayout()
	{
		layout = new PatternLayout();
		layout.setPattern("%d{HH:mm:ss.SSS} %-5level - %msg%n");
		layout.setContext(getContext());
		layout.start();
	}

	@Override
	public void start()
	{
		createLayout();
		super.start();
	}

	@Override
	protected void append(ILoggingEvent eventObject)
	{
		String formattedMessage = layout.doLayout(eventObject);
		JobExecution jobExecution = JobExecutionContext.get();
		String oldLog = jobExecution.getLog();
		String newLog = oldLog == null ? formattedMessage : oldLog + formattedMessage;
		jobExecution.setLog(newLog);
	}

}

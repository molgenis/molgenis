package org.molgenis.jobs.model;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import org.molgenis.jobs.JobExecutionContext;

/**
 * Appender that appends to the log attribute of a {@link JobExecution} entity.
 */
public class JobExecutionLogAppender extends AppenderBase<ILoggingEvent>
{
	private PatternLayout layout;

	private void createLayout()
	{
		layout = new PatternLayout();
		layout.setPattern("%d{HH:mm:ss.SSS} - %msg%n%nopex");
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
		jobExecution.appendLog(formattedMessage);
	}

}

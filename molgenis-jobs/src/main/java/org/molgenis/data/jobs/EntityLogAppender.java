package org.molgenis.data.jobs;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

public class EntityLogAppender extends AppenderBase<ILoggingEvent>
{
	private StringBuffer buffer = new StringBuffer();
	private final JobExecution jobExecution;
	private final PatternLayout layout;

	public EntityLogAppender(JobExecution jobMetaData, LoggerContext context)
	{
		this.jobExecution = jobMetaData;
		layout = new PatternLayout();
		layout.setPattern("%d{HH:mm:ss.SSS} %-5level - %msg%n");
		layout.setContext(context);
		layout.start();
	}

	@Override
	protected void append(ILoggingEvent eventObject)
	{
		String formattedMessage = layout.doLayout(eventObject);
		buffer.append(formattedMessage);
		jobExecution.set(JobExecution.LOG, buffer.toString());
	}

}

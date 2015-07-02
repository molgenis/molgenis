package org.molgenis.data.elasticsearch.logback;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.common.unit.TimeValue;
import org.molgenis.data.Entity;
import org.molgenis.data.elasticsearch.factory.EmbeddedElasticSearchServiceFactory;
import org.molgenis.data.elasticsearch.index.EntityToSourceConverter;
import org.molgenis.data.support.MapEntity;
import org.molgenis.util.ApplicationContextProvider;
import org.springframework.context.ApplicationContext;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.CoreConstants;

public class MolgenisAppender extends AppenderBase<ILoggingEvent>
{
	private static final String TRACE_PREFIX = CoreConstants.LINE_SEPARATOR + "    ";
	private static final TimeValue FLUSH_INTERVAL = TimeValue.timeValueSeconds(10);
	private static final String INDEX_NAME = "molgenis";

	private BulkProcessor bulkProcessor;

	@Override
	protected void append(ILoggingEvent eventObject)
	{
		try
		{
			if (bulkProcessor == null)
			{
				ApplicationContext ctx = ApplicationContextProvider.getApplicationContext();
				if (ctx == null) return;

				EmbeddedElasticSearchServiceFactory embeddedElasticSearchServiceFactory = ctx
						.getBean(EmbeddedElasticSearchServiceFactory.class);

				bulkProcessor = BulkProcessor
						.builder(embeddedElasticSearchServiceFactory.getClient(), new BulkProcessor.Listener()
						{
							@Override
							public void beforeBulk(long executionId, BulkRequest request)
							{
							}

							@Override
							public void afterBulk(long executionId, BulkRequest request, BulkResponse response)
							{
							}

							@Override
							public void afterBulk(long executionId, BulkRequest request, Throwable failure)
							{
							}
						}).setFlushInterval(FLUSH_INTERVAL).build();

			}

			Entity entity = toEntity(eventObject);
			Map<String, Object> source = new EntityToSourceConverter().convert(entity, LoggingEventMetaData.INSTANCE);

			bulkProcessor.add(new IndexRequest(INDEX_NAME, LoggingEventMetaData.INSTANCE.getName()).source(source));
		}
		catch (Throwable t)
		{
			t.printStackTrace();
		}
	}

	private Entity toEntity(ILoggingEvent eventObject)
	{
		Entity e = new MapEntity(LoggingEventMetaData.IDENTIFIER);
		e.set(LoggingEventMetaData.IDENTIFIER, UUID.randomUUID().toString());
		e.set(LoggingEventMetaData.THREAD, eventObject.getThreadName());
		if (eventObject.getLevel() != null)
		{
			e.set(LoggingEventMetaData.LEVEL, eventObject.getLevel().levelStr);
		}
		e.set(LoggingEventMetaData.LOGGER, eventObject.getLoggerName());
		e.set(LoggingEventMetaData.MESSAGE, eventObject.getMessage());
		e.set(LoggingEventMetaData.TIMESTAMP, new Date(eventObject.getTimeStamp()));
		if (eventObject.getThrowableProxy() != null)
		{
			e.set(LoggingEventMetaData.STACKTRACE, renderStacktrace(eventObject));
		}

		return e;
	}

	public String renderStacktrace(ILoggingEvent event)
	{
		StringBuilder sbuf = new StringBuilder();

		IThrowableProxy tp = event.getThrowableProxy();
		while (tp != null)
		{
			renderStacktrace(sbuf, tp);
			tp = tp.getCause();
		}

		return sbuf.toString();
	}

	private void renderStacktrace(StringBuilder sbuf, IThrowableProxy tp)
	{
		printFirstLine(sbuf, tp);

		int commonFrames = tp.getCommonFrames();
		StackTraceElementProxy[] stepArray = tp.getStackTraceElementProxyArray();

		for (int i = 0; i < stepArray.length - commonFrames; i++)
		{
			StackTraceElementProxy step = stepArray[i];
			sbuf.append(TRACE_PREFIX);
			sbuf.append(step.toString());
		}

		if (commonFrames > 0)
		{
			sbuf.append(TRACE_PREFIX);
			sbuf.append("\t... ").append(commonFrames).append(" common frames omitted");
		}
	}

	private void printFirstLine(StringBuilder sb, IThrowableProxy tp)
	{
		int commonFrames = tp.getCommonFrames();
		if (commonFrames > 0)
		{
			sb.append(CoreConstants.LINE_SEPARATOR).append(CoreConstants.CAUSED_BY);
		}
		sb.append(tp.getClassName()).append(": ").append(tp.getMessage());
	}

}

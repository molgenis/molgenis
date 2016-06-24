package org.molgenis.data.transaction;

import static org.molgenis.security.core.runas.RunAsSystemProxy.runAsSystem;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AsyncTransactionLog
{
	private static final int QUEUE_CAPACITY = 1000;
	private static final Logger LOG = LoggerFactory.getLogger(AsyncTransactionLog.class);
	private final DataService dataService;
	private final BlockingQueue<Entity> queue;
	private final QueueConsumer queueConsumer;
	private boolean run = true;

	public AsyncTransactionLog(DataService dataService)
	{
		this.dataService = dataService;
		this.queue = new ArrayBlockingQueue<>(QUEUE_CAPACITY);
		queueConsumer = new QueueConsumer();
	}

	@PostConstruct
	public void start()
	{
		new Thread(queueConsumer).start();
	}

	@PreDestroy
	public void stop()
	{
		run = false;
	}

	public void addLogEntry(Entity logEntry)
	{
		if (!queue.offer(logEntry))
		{
			LOG.warn("Could not add log entry. Queue is full.");
		}
	}

	public void logTransactionFinished(Entity transactionLog)
	{
		if (!queue.offer(transactionLog))
		{
			LOG.warn("Could not add transactionLog. Queue is full");
		}
	}

	private class QueueConsumer implements Runnable
	{
		@Override
		public void run()
		{
			while (run)
			{
				try
				{
					runAsSystem(() -> {
						Entity entity = queue.take();
						String type = entity.getEntityMetaData().getName();

						// Do not call dataService.add because that method is transactional resulting in an infinite
						// loop.
						if (type.equals(MolgenisTransactionLogEntryMetaData.ENTITY_NAME))
						{
							dataService.getRepository(MolgenisTransactionLogEntryMetaData.ENTITY_NAME).add(entity);
						}
						else if (type.equals(MolgenisTransactionLogMetaData.ENTITY_NAME))
						{
							dataService.getRepository(MolgenisTransactionLogMetaData.ENTITY_NAME).update(entity);
						}

						return null;
					});
				}
				catch (InterruptedException e)
				{
					LOG.error("InterruptedException consuming log entity from queue.", e);
				}
				catch (Exception e)
				{
					LOG.error("Exception consuming log entity from queue.", e);
				}
			}

		}

	}
}

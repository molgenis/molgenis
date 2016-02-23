package org.molgenis.data.jobs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Superclass for molgenis jobs that keeps track of their progress.
 */
public abstract class Job implements Runnable
{
	private static final Logger LOG = LoggerFactory.getLogger(Job.class);
	private final Progress progress;

	public Job(Progress progress)
	{
		this.progress = progress;
	}

	@Override
	public void run()
	{
		progress.start();
		try
		{
			run(progress);
			progress.success();
		}
		catch (Exception ex)
		{
			LOG.warn("Error running job", ex);
			progress.failed(ex);
		}
	};

	/**
	 * Executes this job. For concrete subclasses to implement.
	 * 
	 * @param progress
	 * @throws Exception
	 */
	public abstract void run(Progress progress) throws Exception;
}

package org.molgenis.jobs;

/**
 * Interface for molgenis jobs.
 */
@FunctionalInterface
public interface Job<T>
{
	/**
	 * Execute this job.
	 *
	 * @param progress the {@link Progress} to report progress to
	 * @throws Exception if something goes wrong. If an exception is thrown here, the job status will be set to failed.
	 */
	T call(Progress progress) throws Exception;
}
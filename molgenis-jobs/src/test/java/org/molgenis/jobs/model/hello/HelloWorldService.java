package org.molgenis.jobs.model.hello;

import org.molgenis.jobs.Progress;
import org.springframework.stereotype.Service;

import static java.lang.Thread.sleep;

@Service
public class HelloWorldService
{
	// This is a Service, so you can autowire any dependency you need

	/**
	 * Computes Hello World message.
	 *
	 * @param progress Progress to report job progress to
	 * @param who      Who shall we greet?
	 * @param delay    job duration in seconds
	 */
	@SuppressWarnings("squid:S2925")
	public String helloWorld(Progress progress, String who, int delay) throws InterruptedException
	{
		progress.setProgressMax(delay);
		progress.progress(0, "Wait for it...");
		for (int i = 0; i < delay; i++)
		{
			sleep(1000);
			progress.increment(1);
		}
		String helloWorld = "Hello " + who + "!";
		progress.status(helloWorld);
		return helloWorld;
	}
}

package org.molgenis.framework.server.services;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.server.MolgenisContext;
import org.molgenis.framework.server.MolgenisRequest;
import org.molgenis.framework.server.MolgenisResponse;
import org.molgenis.framework.server.MolgenisService;

/**
 * A MolgenisService to clean the tmp dir every hour. Files older than 12 hours
 * are attempted to be deleted. The handleRequest of this service should never
 * be called, instead it is just initialized and uses a sleeping thread to clean
 * up once in a while. This job will be triggered every hour.
 * 
 * @author joerivandervelde
 * 
 */
public class MolgenisCleanTmpDirService implements MolgenisService
{
	Logger logger = Logger.getLogger(MolgenisCleanTmpDirService.class);

	public MolgenisCleanTmpDirService(MolgenisContext mc)
	{
		new CleanTmpDirProcess(3600, 12);
	}

	@Override
	public void handleRequest(MolgenisRequest request, MolgenisResponse response) throws ParseException,
			DatabaseException, IOException
	{
		throw new IOException("This service does not accept requests.");
	}

}

class CleanTmpDirProcess implements Runnable
{
	private long mfa;
	private long hox;

	CleanTmpDirProcess(int howOftenExecutedInSeconds, long maxFileAgeInHours)
	{
		hox = howOftenExecutedInSeconds;
		mfa = maxFileAgeInHours;
		Thread t = new Thread(this);
		t.start();
	}

	@Override
	public void run()
	{
		boolean noExceptions = true;

		// 10 sec delay after starting the server
		try
		{
			Thread.sleep(10000);
		}
		catch (InterruptedException e)
		{
			System.out.println("SEVERE: CleanTmpDirProcess thread failed to wait on startup");
			e.printStackTrace();
			noExceptions = false;
		}

		while (noExceptions)
		{
			try
			{
				// delete all files in tmpdir older than 12 hours
				System.out.println("MolgenisCleanTmpDirService: executing cleaning job!");

				String tmpDirLoc = System.getProperty("java.io.tmpdir");
				File tmpDir = new File(tmpDirLoc);

				long curDate = new Date().getTime();
				long maxAge = 1000L * 60 * 60 * mfa;

				for (File f : tmpDir.listFiles())
				{
					// TODO: directory recursion..
					// if (!f.isDirectory())
					// {
					long lastMod = f.lastModified();
					long age = curDate - lastMod;

					// System.out.println("lastMod: " + lastMod);
					// System.out.println("curDate: " + curDate);
					// System.out.println("age: " + age);
					// System.out.println("maxAge: " + maxAge);

					if (age > maxAge)
					{
						System.out.println("MolgenisCleanTmpDirService: tmp file " + f.getName() + " is older than "
								+ mfa + " hours, deleting...");
						FileUtils.deleteQuietly(f);
					}
					else
					{
						// System.out.println(f.getAbsolutePath() +
						// " is younger than " + maxAge + " msec");
					}
					// }
				}

			}
			catch (Exception e)
			{
				System.out
						.println("SEVERE: Breaking execution of CleanTmpDirProcess! InterruptedException on file delete");
				e.printStackTrace();
				noExceptions = false;
			}

			long sleepTime = 1000 * hox;
			System.out.println("MolgenisCleanTmpDirService: going to sleep for " + hox + " seconds..");

			try
			{
				Thread.sleep(sleepTime);
			}
			catch (InterruptedException e)
			{
				System.out
						.println("SEVERE: Breaking execution of CleanTmpDirProcess! InterruptedException on thread sleep");
				e.printStackTrace();
				noExceptions = false;
			}
		}
	}
}

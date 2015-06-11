package org.molgenis.data.annotation.resources.impl;

import java.io.File;
import java.io.IOException;

import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.molgenis.data.annotation.resources.Resource;
import org.molgenis.data.annotator.tabix.TabixVcfRepository;
import org.molgenis.framework.server.MolgenisSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link TabixVcfRepository} that is potentially unavailable.
 */
public class TabixVcfResource implements Resource
{
	private final String name;
	private final String filenameKey;
	private final String defaultFilename;
	private final MolgenisSettings molgenisSettings;
	// the file the current repository works on
	private volatile File file;
	// the current repository
	private volatile TabixVcfRepository repository;

	private static final Logger LOG = LoggerFactory.getLogger(TabixVcfResource.class);

	public TabixVcfResource(String entityName, MolgenisSettings molgenisSettings, String filenameKey,
			String defaultFilename)
	{
		this.molgenisSettings = molgenisSettings;
		this.name = entityName;
		this.filenameKey = filenameKey;
		this.defaultFilename = defaultFilename;
	}

	/**
	 * Indicates if the repository is available.
	 * 
	 * Checks if the current fileName in MolgenisSettings still matches the file that the current repository was
	 * instantiated for, and removes a previously instantiated {@link Repository} if it no longer matches.
	 * 
	 * @return indication if this resource is available
	 */
	@Override
	public synchronized boolean isAvailable()
	{
		if (repository != null && repositoryNeedsUpdate())
		{
			repository = null;
			file = null;
		}
		return getFile() != null;
	}

	/**
	 * Searches the repository
	 * 
	 * @param q
	 *            the {@link Query} to use
	 * @return {@link Entity}s found
	 * @throws NullPointerException
	 *             if the repository is not available
	 */
	@Override
	public Iterable<Entity> findAll(Query q)
	{
		return getRepository().findAll(q);
	}

	private TabixVcfRepository getRepository()
	{
		if (repository == null && isAvailable())
		{
			initialize();
		}
		return repository;
	}

	private synchronized void initialize()
	{
		if (isAvailable() && repository == null)
		{
			try
			{
				file = getFile();
				if (file != null)
				{
					repository = new TabixVcfRepository(file, name);
				}
			}
			catch (IOException e)
			{
				LOG.warn("Failed to initialize TabixVcfRepository {} for file {}.", name, file, e);
			}
		}
	}

	private synchronized boolean repositoryNeedsUpdate()
	{
		if (repository == null)
		{
			return getFile() != null;
		}
		return !file.equals(getFile());
	}

	private synchronized File getFile()
	{
		try
		{
			String fileName = molgenisSettings.getProperty(filenameKey, defaultFilename);
			if (fileName == null)
			{
				return null;
			}
			File result = new File(fileName);
			if (result.exists())
			{
				return result;
			}
		}
		catch (Exception ex)
		{
			LOG.info("Resource file for {} unavailable", name, ex);
		}
		return null;
	}

	@Override
	public String getName()
	{
		return name;
	}

}

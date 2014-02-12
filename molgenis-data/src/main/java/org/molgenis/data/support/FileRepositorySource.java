package org.molgenis.data.support;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.molgenis.data.Repository;
import org.molgenis.data.RepositorySource;
import org.molgenis.data.processor.CellProcessor;

import com.google.common.collect.Lists;

public abstract class FileRepositorySource implements RepositorySource
{
	/** process cells after reading */
	protected List<CellProcessor> cellProcessors;
	private final Set<String> fileNameExtensions;

	public FileRepositorySource(Set<String> fileNameExtensions, CellProcessor... cellProcessors)
	{
		if (fileNameExtensions == null) throw new IllegalArgumentException("FileNameExtensions is null");
		this.fileNameExtensions = fileNameExtensions;

		if (cellProcessors != null)
		{
			this.cellProcessors = Arrays.asList(cellProcessors);
		}
	}

	public Set<String> getFileNameExtensions()
	{
		return fileNameExtensions;
	}

	@Override
	public abstract List<Repository> getRepositories();

	@Override
	public Repository getRepository(String name)
	{
		for (Repository repository : getRepositories())
		{
			if (repository.getName().equalsIgnoreCase(name))
			{
				return repository;
			}
		}

		return null;
	}

	public void addCellProcessor(CellProcessor cellProcessor)
	{
		if (cellProcessors == null)
		{
			cellProcessors = Lists.newArrayList();
		}

		cellProcessors.add(cellProcessor);
	}
}

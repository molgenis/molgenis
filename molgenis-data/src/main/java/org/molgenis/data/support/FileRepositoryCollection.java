package org.molgenis.data.support;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.processor.CellProcessor;

import com.google.common.collect.Lists;

public abstract class FileRepositoryCollection implements RepositoryCollection
{
	/** process cells after reading */
	protected List<CellProcessor> cellProcessors;
	private final Set<String> fileNameExtensions;

	public FileRepositoryCollection(Set<String> fileNameExtensions, CellProcessor... cellProcessors)
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

	public void addCellProcessor(CellProcessor cellProcessor)
	{
		if (cellProcessors == null)
		{
			cellProcessors = Lists.newArrayList();
		}

		cellProcessors.add(cellProcessor);
	}

	@Override
	public abstract Iterable<String> getEntityNames();

	@Override
	public abstract Repository getRepository(String name);
}

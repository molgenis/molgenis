package org.molgenis.data.file.support;

import com.google.common.collect.Lists;
import org.molgenis.data.file.processor.CellProcessor;
import org.molgenis.data.support.AbstractRepositoryCollection;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public abstract class FileRepositoryCollection extends AbstractRepositoryCollection
{
	/**
	 * process cells after reading
	 */
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

	/**
	 * Initialize this file repository collection
	 */
	public abstract void init() throws IOException;

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
}

package org.molgenis.data.support;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.molgenis.data.EntitySource;
import org.molgenis.data.FileBasedEntitySourceFactory;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.io.processor.CellProcessor;
import org.springframework.util.StringUtils;

public abstract class AbstractFileBasedEntitySourceFactory implements FileBasedEntitySourceFactory
{
	private final String urlPrefix;
	private final List<String> fileExtensions;
	private List<CellProcessor> cellProcessors;

	protected AbstractFileBasedEntitySourceFactory(String urlPrefix, List<String> fileExtensions,
			List<CellProcessor> cellProcessors)
	{
		this.urlPrefix = urlPrefix;
		this.fileExtensions = fileExtensions;
		this.cellProcessors = cellProcessors;
	}

	@Override
	public String getUrlPrefix()
	{
		return urlPrefix;
	}

	@Override
	public void addCellProcessor(CellProcessor cellProcessor)
	{
		if (cellProcessors == null) cellProcessors = new ArrayList<CellProcessor>();
		cellProcessors.add(cellProcessor);
	}

	@Override
	public EntitySource create(String url)
	{
		try
		{
			return createInternal(url, cellProcessors);
		}
		catch (IOException e)
		{
			throw new MolgenisDataException("Error creating EntitySource using url [" + url + "]", e);
		}
	}

	@Override
	public EntitySource create(File file)
	{
		System.out.println("file:" + file.getAbsolutePath());
		String extension = StringUtils.getFilenameExtension(file.getName());
		System.out.println("Extension:" + extension);
		if (!fileExtensions.contains(extension))
		{
			throw new MolgenisDataException("Unsupported file extension [" + extension + "]");
		}

		try
		{
			return createInternal(file, cellProcessors);
		}
		catch (IOException e)
		{
			throw new MolgenisDataException("Error creating EntitySource using file [" + file.getAbsolutePath() + "]",
					e);
		}
	}

	@Override
	public List<String> getFileExtensions()
	{
		return fileExtensions;
	}

	protected abstract EntitySource createInternal(String url, List<CellProcessor> cellProcessors) throws IOException;

	protected abstract EntitySource createInternal(File file, List<CellProcessor> cellProcessors) throws IOException;

}

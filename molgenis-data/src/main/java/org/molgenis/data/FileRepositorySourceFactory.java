package org.molgenis.data;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.Set;

import org.molgenis.data.support.FileRepositorySource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.google.common.collect.Maps;

/**
 * Factory for creating a RepositorySource from a file.
 * 
 * You can register a new FileRepositorySource class for a set of file extension by calling addFileRepositorySourceClass
 * 
 * For example ExcelFileRepositorySource class can be registered, then when you call 'createFileRepositorySource' for a
 * file with extension 'xls' a new ExcelFileRepositorySource is created for you.
 */
@Component
public class FileRepositorySourceFactory
{
	private final Map<String, Class<? extends FileRepositorySource>> fileRepositorySources;

	public FileRepositorySourceFactory()
	{
		this.fileRepositorySources = Maps.newHashMap();
	}

	/**
	 * Add a FileRepositorySource so it can be used by the 'createFileRepositySource' factory method
	 * 
	 * @param fileRepositorySource
	 */
	public void addFileRepositorySourceClass(Class<? extends FileRepositorySource> clazz, Set<String> fileExtensions)
	{
		for (String extension : fileExtensions)
		{
			fileRepositorySources.put(extension.toLowerCase(), clazz);
		}
	}

	/**
	 * Factory method for creating a new FileRepositorySource
	 * 
	 * For example an excel file
	 * 
	 * @param file
	 * @return
	 */
	public FileRepositorySource createFileRepositorySource(File file)
	{
		String extension = StringUtils.getFilenameExtension(file.getName());
		Class<? extends FileRepositorySource> clazz = fileRepositorySources.get(extension.toLowerCase());
		if (clazz == null)
		{
			throw new MolgenisDataException("Unknown extension '" + extension + "'");
		}

		Constructor<? extends FileRepositorySource> ctor;
		try
		{
			ctor = clazz.getConstructor(File.class);
		}
		catch (Exception e)
		{
			throw new MolgenisDataException("Exception creating [" + clazz
					+ "]  missing constructor FileRepositorySource(File file)");
		}

		return BeanUtils.instantiateClass(ctor, file);
	}

}

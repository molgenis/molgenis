package org.molgenis.data;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.molgenis.data.support.FileRepositoryCollection;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;

/**
 * Factory for creating a RepositoryCollections from a file.
 * 
 * You can register a new FileRepositoryCollection class for a set of file extension by calling
 * addFileRepositoryCollectionClass
 * 
 * For example ExcelFileRepositoryCollection class can be registered, then when you call
 * 'createFileRepositoryCollection' for a file with extension 'xls' a new ExcelFileRepositoryCollection is created for
 * you.
 */
@Component
public class FileRepositoryCollectionFactory
{
	private final Map<String, Class<? extends FileRepositoryCollection>> fileRepositoryCollection;

	public FileRepositoryCollectionFactory()
	{
		this.fileRepositoryCollection = Maps.newHashMap();
	}

	/**
	 * Add a FileRepositorySource so it can be used by the 'createFileRepositySource' factory method
	 * 
	 * @param fileRepositorySource
	 */
	public void addFileRepositoryCollectionClass(Class<? extends FileRepositoryCollection> clazz,
			Set<String> fileExtensions)
	{
		for (String extension : fileExtensions)
		{
			fileRepositoryCollection.put(extension.toLowerCase(), clazz);
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
	public FileRepositoryCollection createFileRepositoryCollection(File file)
	{
		String name = file.getName().toLowerCase();
		Class<? extends FileRepositoryCollection> clazz = null;
		
		List<String> possibleExtensions = new ArrayList<String>();
		for (Entry<String, Class<? extends FileRepositoryCollection>> entry : fileRepositoryCollection.entrySet())
		{
			if (name.endsWith('.' + entry.getKey()))
			{
				possibleExtensions.add(entry.getKey());
			}
		}

		String longestExtention = "";
		for (String possibleExtension : possibleExtensions)
		{
			if (longestExtention.length() < possibleExtension.length()) longestExtention = possibleExtension;
		}
		
		clazz = fileRepositoryCollection.get(longestExtention);

		if (clazz == null)
		{
			throw new MolgenisDataException("Unknown extension for file '" + file.getName() + "'");
		}

		Constructor<? extends FileRepositoryCollection> ctor;
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

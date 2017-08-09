package org.molgenis.data;

import com.google.common.collect.Maps;
import org.molgenis.data.support.FileRepositoryCollection;
import org.molgenis.util.FileExtensionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.Set;

import static java.util.Objects.requireNonNull;

/**
 * Factory for creating a RepositoryCollections from a file.
 * <p>
 * You can bootstrap a new FileRepositoryCollection class for a set of file extension by calling
 * addFileRepositoryCollectionClass
 * <p>
 * For example ExcelFileRepositoryCollection class can be registered, then when you call
 * 'createFileRepositoryCollection' for a file with extension 'xls' a new ExcelFileRepositoryCollection is created for
 * you.
 */
@Component
public class FileRepositoryCollectionFactory
{
	private final Map<String, Class<? extends FileRepositoryCollection>> fileRepositoryCollection;
	private final AutowireCapableBeanFactory autowireCapableBeanFactory;

	@Autowired
	public FileRepositoryCollectionFactory(AutowireCapableBeanFactory autowireCapableBeanFactory)
	{
		this.autowireCapableBeanFactory = requireNonNull(autowireCapableBeanFactory);
		this.fileRepositoryCollection = Maps.newHashMap();
	}

	/**
	 * Add a FileRepositorySource so it can be used by the 'createFileRepositySource' factory method
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
	 * <p>
	 * For example an excel file
	 *
	 * @param file
	 * @return
	 */
	public FileRepositoryCollection createFileRepositoryCollection(File file)
	{
		Class<? extends FileRepositoryCollection> clazz;

		String extension = FileExtensionUtils.findExtensionFromPossibilities(file.getName(),
				fileRepositoryCollection.keySet());

		clazz = fileRepositoryCollection.get(extension);

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
			throw new MolgenisDataException(
					"Exception creating [" + clazz + "]  missing constructor FileRepositorySource(File file)");
		}

		FileRepositoryCollection fileRepositoryCollection = BeanUtils.instantiateClass(ctor, file);
		autowireCapableBeanFactory.autowireBeanProperties(fileRepositoryCollection,
				AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE, false);
		try
		{
			fileRepositoryCollection.init();
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
		return fileRepositoryCollection;
	}

}

package org.molgenis.data.csv;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.IOUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Repository;
import org.molgenis.data.processor.CellProcessor;
import org.molgenis.data.support.FileRepositoryCollection;
import org.molgenis.data.support.GenericImporterExtensions;
import org.springframework.util.StringUtils;

import com.google.common.collect.Lists;

/**
 * Reads csv and tsv files. Can be bundled together in a zipfile.
 * 
 * The exposes the files as {@link org.molgenis.data.Repository}. The names of the repositories are the names of the
 * files without the extension
 */
public class CsvRepositoryCollection extends FileRepositoryCollection
{
	private static final String MAC_ZIP = "__MACOSX";
	private final File file;
	private List<String> entityNames;
	private List<String> entityNamesLowerCase;

	public CsvRepositoryCollection(File file) throws InvalidFormatException, IOException
	{
		this(file, (CellProcessor[]) null);
	}

	public CsvRepositoryCollection(File file, CellProcessor... cellProcessors) throws InvalidFormatException,
			IOException
	{
		super(GenericImporterExtensions.getCSV(), cellProcessors);
		this.file = file;

		loadEntityNames();
	}

	@Override
	public Iterable<String> getEntityNames()
	{
		return entityNames;
	}

	@Override
	public Repository getRepositoryByEntityName(String name)
	{
		if (!entityNamesLowerCase.contains(name.toLowerCase()))
		{
			return null;
		}

		return new CsvRepository(file, name, cellProcessors);
	}

	private void loadEntityNames()
	{
		String extension = StringUtils.getFilenameExtension(file.getName());
		entityNames = Lists.newArrayList();
		entityNamesLowerCase = Lists.newArrayList();

		if (extension.equalsIgnoreCase(GenericImporterExtensions.ZIP.toString()))
		{
			ZipFile zipFile = null;
			try
			{
				zipFile = new ZipFile(file);
				for (Enumeration<? extends ZipEntry> e = zipFile.entries(); e.hasMoreElements();)
				{
					ZipEntry entry = e.nextElement();
					if (!entry.getName().contains(MAC_ZIP))
					{
						String name = getRepositoryName(entry.getName());
						entityNames.add(name);
						entityNamesLowerCase.add(name.toLowerCase());
					}
				}
			}
			catch (Exception e)
			{
				throw new MolgenisDataException(e);
			}
			finally
			{
				IOUtils.closeQuietly(zipFile);
			}
		}
		else
		{
			String name = getRepositoryName(file.getName());
			entityNames.add(name);
			entityNamesLowerCase.add(name.toLowerCase());
		}
	}

	private String getRepositoryName(String fileName)
	{
		return StringUtils.stripFilenameExtension(StringUtils.getFilename(fileName));
	}

}

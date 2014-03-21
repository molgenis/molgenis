package org.molgenis.data.csv;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Repository;
import org.molgenis.data.processor.CellProcessor;
import org.molgenis.data.support.FileRepositoryCollection;
import org.springframework.util.StringUtils;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

/**
 * Reads csv and tsv files. Can be bundled together in a zipfile.
 * 
 * The exposes the files as {@link org.molgenis.data.Repository}. The names of the repositories are the names of the
 * files without the extension
 */
public class CsvRepositoryCollection extends FileRepositoryCollection
{
	private static final String EXTENSION_CSV = "csv";
	private static final String EXTENSION_TXT = "txt";
	private static final String EXTENSION_TSV = "tsv";
	private static final String EXTENSION_ZIP = "zip";

	public static final Set<String> EXTENSIONS = ImmutableSet.of(EXTENSION_CSV, EXTENSION_TXT, EXTENSION_TSV,
			EXTENSION_ZIP);

	private static final Charset CHARSET = Charset.forName("UTF-8");
	private static final String MAC_ZIP = "__MACOSX";
	private final File file;
	private ZipFile zipFile = null;

	public CsvRepositoryCollection(File file) throws InvalidFormatException, IOException
	{
		this(file, (CellProcessor[]) null);
	}

	public CsvRepositoryCollection(File file, CellProcessor... cellProcessors) throws InvalidFormatException, IOException
	{
		super(EXTENSIONS, cellProcessors);
		this.file = file;
	}

	private Repository getRepository(String fileName, InputStream in, List<CellProcessor> cellProcessors)
	{
		String name = StringUtils.stripFilenameExtension(StringUtils.getFilename(fileName));
		Reader reader = new InputStreamReader(in, CHARSET);

		if (fileName.toLowerCase().endsWith("." + EXTENSION_CSV)
				|| fileName.toLowerCase().endsWith("." + EXTENSION_TXT))
		{
			return new CsvRepository(fileName, reader, name, cellProcessors);
		}

		if (fileName.toLowerCase().endsWith("." + EXTENSION_TSV))
		{
			return new CsvRepository(fileName, reader, '\t', StringUtils.getFilename(fileName), cellProcessors);
		}

		throw new MolgenisDataException("Unknown file type: [" + fileName + "] for csv repository");
	}

	@Override
	public Iterable<String> getEntityNames()
	{
		String extension = StringUtils.getFilenameExtension(file.getName());
		List<String> repositories = Lists.newArrayList();

		if (extension.equalsIgnoreCase(EXTENSION_ZIP))
		{
			try
			{
				zipFile = new ZipFile(file);
				for (Enumeration<? extends ZipEntry> e = zipFile.entries(); e.hasMoreElements();)
				{
					ZipEntry entry = e.nextElement();
					if (!entry.getName().contains(MAC_ZIP))
					{
						repositories.add(StringUtils.stripFilenameExtension(StringUtils.getFilename(file.getName())));
					}
				}
			}
			catch (Exception e)
			{
				throw new MolgenisDataException(e);
			}
		}
		else
		{
			repositories.add(StringUtils.stripFilenameExtension(StringUtils.getFilename(file.getName())));
		}

		return repositories;
	}

	@Override
	public Repository getRepositoryByEntityName(String name)
	{
		String extension = StringUtils.getFilenameExtension(file.getName());

		if (extension.equalsIgnoreCase(EXTENSION_ZIP))
		{
			try
			{
				zipFile = new ZipFile(file);
				for (Enumeration<? extends ZipEntry> e = zipFile.entries(); e.hasMoreElements();)
				{
					ZipEntry entry = e.nextElement();
					if (StringUtils.stripFilenameExtension(StringUtils.getFilename(entry.getName())).equalsIgnoreCase(
							name))
					{
						return getRepository(file.getName() + "/" + entry.getName(), zipFile.getInputStream(entry),
								cellProcessors);
					}
				}
			}
			catch (Exception e)
			{
				throw new MolgenisDataException(e);
			}
		}
		else if (file.getName().equalsIgnoreCase(name))
		{
			try
			{
				return getRepository(StringUtils.stripFilenameExtension(StringUtils.getFilename(file.getName())),
						new FileInputStream(file), cellProcessors);
			}
			catch (FileNotFoundException e)
			{
				throw new MolgenisDataException(e);
			}
		}

		return null;
	}

}

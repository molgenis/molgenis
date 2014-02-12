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

import org.apache.commons.io.IOUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Repository;
import org.molgenis.data.processor.CellProcessor;
import org.molgenis.data.support.FileRepositorySource;
import org.springframework.util.StringUtils;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

/**
 * Reads csv and tsv files. Can be bundled together in a zipfile.
 * 
 * The exposes the files as {@link org.molgenis.data.Repository}. The names of the repositories are the names of the
 * files without the extension
 */
public class CsvRepositorySource extends FileRepositorySource
{
	public static final Set<String> EXTENSIONS = ImmutableSet.of("csv", "txt", "tsv", "zip");
	private static final Charset CHARSET = Charset.forName("UTF-8");
	private final File file;

	public CsvRepositorySource(File file) throws InvalidFormatException, IOException
	{
		this(file, (CellProcessor[]) null);
	}

	public CsvRepositorySource(File file, CellProcessor... cellProcessors) throws InvalidFormatException, IOException
	{
		super(EXTENSIONS, cellProcessors);
		this.file = file;
	}

	@Override
	public List<Repository> getRepositories()
	{
		String extension = StringUtils.getFilenameExtension(file.getName());
		List<Repository> repositories = Lists.newArrayList();

		if (extension.equalsIgnoreCase(".zip"))
		{
			ZipFile zipFile = null;
			try
			{
				zipFile = new ZipFile(file);
				for (Enumeration<? extends ZipEntry> e = zipFile.entries(); e.hasMoreElements();)
				{
					ZipEntry entry = e.nextElement();
					InputStream in = zipFile.getInputStream(entry);
					repositories.add(getRepository(file.getName() + "/" + entry.getName(), in, cellProcessors));
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
			try
			{
				repositories.add(getRepository(file.getName(), new FileInputStream(file), cellProcessors));
			}
			catch (FileNotFoundException e)
			{
				throw new MolgenisDataException(e);
			}
		}

		return repositories;
	}

	private Repository getRepository(String fileName, InputStream in, List<CellProcessor> cellProcessors)
	{
		String name = StringUtils.stripFilenameExtension(StringUtils.getFilename(fileName));
		Reader reader = new InputStreamReader(in, CHARSET);

		if (fileName.toLowerCase().endsWith(".csv") || fileName.toLowerCase().endsWith(".txt"))
		{
			return new CsvRepository(fileName, reader, name, cellProcessors);
		}

		if (fileName.toUpperCase().endsWith(".tsv"))
		{
			return new CsvRepository(fileName, reader, '\t', StringUtils.getFilename(fileName), cellProcessors);
		}

		throw new MolgenisDataException("Unknown file type: [" + fileName + "] for csv repository");
	}
}

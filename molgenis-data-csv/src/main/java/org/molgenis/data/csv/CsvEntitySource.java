package org.molgenis.data.csv;

import static org.molgenis.data.csv.CsvEntitySourceFactory.CSV_ENTITYSOURCE_URL_PREFIX;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.annotation.Nullable;

import org.apache.commons.io.IOUtils;
import org.molgenis.data.Entity;
import org.molgenis.data.EntitySource;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Repository;
import org.molgenis.data.processor.CellProcessor;

import com.google.common.collect.Lists;

/**
 * EntitySource implementation for csv.
 * 
 * This should be a zipfile containing csv files or a csv file or a tsv file
 * 
 * So this either contains one csv/tsv file or a zip containing multiple files
 * 
 * 
 * Example url: csv://Users/john/Documents/matrix.zip
 */
public class CsvEntitySource implements EntitySource
{
	private ZipFile zipFile;
	private File csvFile;
	private final Map<String, ZipEntry> zipEntryMap = new LinkedHashMap<String, ZipEntry>();
	private final String url;
	private final List<CellProcessor> cellProcessors;

	protected CsvEntitySource(File file, String url, @Nullable
	List<CellProcessor> cellProcessors)
	{
		if ((file == null) && (url == null)) throw new IllegalArgumentException("Please provide a file or an Url");
		if ((url != null) && !url.startsWith(CSV_ENTITYSOURCE_URL_PREFIX)) throw new IllegalArgumentException(
				"CsvEntitySource urls should start with " + CSV_ENTITYSOURCE_URL_PREFIX);

		if (url == null)
		{
			url = CSV_ENTITYSOURCE_URL_PREFIX + file.getAbsolutePath();
		}

		try
		{
			if (file == null)
			{
				// Get the file from the url, can be zip or csv, txt, tsv
				file = new File(url.substring(CSV_ENTITYSOURCE_URL_PREFIX.length()));
			}

			if (file.getName().endsWith(".zip"))
			{
				// It is a zipfile, get the zip entries
				zipFile = new ZipFile(file);

				for (Enumeration<? extends ZipEntry> e = zipFile.entries(); e.hasMoreElements();)
				{
					ZipEntry zipEntry = e.nextElement();
					zipEntryMap.put(getEntityName(zipEntry.getName()), zipEntry);
				}
			}
			else
			{
				csvFile = file;
			}

		}
		catch (IOException e)
		{
			throw new MolgenisDataException("Exception opening zip [" + url + "]", e);
		}

		this.url = url;
		this.cellProcessors = cellProcessors;
	}

	protected CsvEntitySource(String url, List<CellProcessor> cellProcessors)
	{
		this(null, url, cellProcessors);
	}

	protected CsvEntitySource(File file, List<CellProcessor> cellProcessors)
	{
		this(file, null, cellProcessors);
	}

	@Override
	public String getUrl()
	{
		return url;
	}

	@Override
	public Iterable<String> getEntityNames()
	{
		if (zipFile != null)
		{
			return zipEntryMap.keySet();
		}

		return Lists.newArrayList(getEntityName(csvFile.getName()));
	}

	@Override
	public Repository<? extends Entity> getRepositoryByEntityName(String entityName)
	{
		ZipEntry zipEntry = zipEntryMap.get(entityName);
		InputStream in = null;

		try
		{
			if (zipEntry != null)
			{
				in = zipFile.getInputStream(zipEntry);
				return toRepository(zipEntry.getName(), in, cellProcessors);
			}

			in = new FileInputStream(csvFile);
			return toRepository(csvFile.getName(), in, cellProcessors);
		}
		catch (IOException e)
		{
			IOUtils.closeQuietly(in);
			throw new MolgenisDataException("Error reading zipfile [" + url + "]", e);
		}
	}

	@Override
	public void close() throws IOException
	{
		// Nothing
	}

	private CsvRepository toRepository(String fileName, InputStream in, List<CellProcessor> cellProcessors)
			throws IOException
	{
		if (fileName.endsWith(".csv") || fileName.endsWith(".txt"))
		{
			Reader reader = new InputStreamReader(in, Charset.forName("UTF-8"));
			return new CsvRepository(reader, getEntityName(fileName), cellProcessors);
		}

		if (fileName.endsWith(".tsv"))
		{
			Reader reader = new InputStreamReader(in, Charset.forName("UTF-8"));
			return new CsvRepository(reader, '\t', getEntityName(fileName), cellProcessors);
		}

		throw new MolgenisDataException("unknown file type: [" + fileName + "] for csv repository");
	}

	private String getEntityName(String fileName)
	{
		// remove extension from filename
		int pos = fileName.lastIndexOf('.');
		return pos != -1 ? fileName.substring(0, pos) : fileName;
	}
}

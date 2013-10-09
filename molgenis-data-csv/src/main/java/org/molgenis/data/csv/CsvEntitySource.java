package org.molgenis.data.csv;

import static org.molgenis.data.csv.CsvEntitySourceFactory.CSV_ENTITYSOURCE_URL_PREFIX;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.molgenis.data.Entity;
import org.molgenis.data.EntitySource;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Repository;
import org.molgenis.data.UnknownEntityException;

/**
 * EntitySource implementation for csv.
 * 
 * This should be a zipfile containing csv files.
 * 
 * Example url: csv://Users/john/Documents/matrix.zip
 */
public class CsvEntitySource implements EntitySource
{
	private final ZipFile zipFile;
	private final Map<String, ZipEntry> zipEntryMap = new LinkedHashMap<String, ZipEntry>();
	private final String url;

	public CsvEntitySource(ZipFile zipFile, String url)
	{
		if ((zipFile == null) && (url == null)) throw new IllegalArgumentException("Please provide a ZipFile or an Url");
		if ((url != null) && !url.startsWith(CSV_ENTITYSOURCE_URL_PREFIX)) throw new IllegalArgumentException(
				"CsvEntitySource urls should start with " + CSV_ENTITYSOURCE_URL_PREFIX);

		if (url == null)
		{
			url = CSV_ENTITYSOURCE_URL_PREFIX + zipFile.getName();
		}

		if (zipFile == null)
		{
			try
			{
				zipFile = new ZipFile(new File(url.substring(CSV_ENTITYSOURCE_URL_PREFIX.length())));
			}
			catch (ZipException e)
			{
				throw new MolgenisDataException("Exception opening zipfile [" + url + "]", e);
			}
			catch (IOException e)
			{
				throw new MolgenisDataException("Exception opening zipfile [" + url + "]", e);
			}
		}

		this.url = url;
		this.zipFile = zipFile;

		// init zipEntryMap
		for (Enumeration<? extends ZipEntry> e = zipFile.entries(); e.hasMoreElements();)
		{
			ZipEntry zipEntry = e.nextElement();
			zipEntryMap.put(getEntityName(zipEntry), zipEntry);
		}
	}

	public CsvEntitySource(String url)
	{
		this(null, url);
	}

	public CsvEntitySource(ZipFile zipFile)
	{
		this(zipFile, null);
	}

	@Override
	public String getUrl()
	{
		return url;
	}

	@Override
	public Iterable<String> getEntityNames()
	{
		return zipEntryMap.keySet();
	}

	@Override
	public Repository<? extends Entity> getRepositoryByEntityName(String entityName)
	{
		ZipEntry zipEntry = zipEntryMap.get(entityName);
		if (zipEntry == null)
		{
			throw new UnknownEntityException("Unknown entity [" + entityName + "]");
		}

		try
		{
			return toRepository(zipEntry);
		}
		catch (IOException e)
		{
			throw new MolgenisDataException("Error reading zipfile [" + url + "]", e);
		}
	}

	@Override
	public void close() throws IOException
	{
		// Nothing
	}

	private CsvRepository toRepository(ZipEntry zipEntry) throws IOException
	{
		String name = zipEntry.getName();
		if (name.endsWith(".csv") || name.endsWith(".txt"))
		{
			Reader reader = new InputStreamReader(zipFile.getInputStream(zipEntry), Charset.forName("UTF-8"));
			return new CsvRepository(reader, getEntityName(zipEntry));
		}

		if (name.endsWith(".tsv"))
		{
			Reader reader = new InputStreamReader(zipFile.getInputStream(zipEntry), Charset.forName("UTF-8"));
			return new CsvRepository(reader, '\t', getEntityName(zipEntry));
		}

		throw new MolgenisDataException("unknown file type: [" + name + "] in zipfile [" + url + "]");
	}

	private String getEntityName(ZipEntry zipEntry)
	{
		// remove extension from filename
		int pos = zipEntry.getName().lastIndexOf('.');
		return pos != -1 ? zipEntry.getName().substring(0, pos) : zipEntry.getName();
	}
}

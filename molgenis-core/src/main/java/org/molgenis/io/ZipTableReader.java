package org.molgenis.io;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.annotation.Nullable;

import org.molgenis.io.csv.CsvReader;

import com.google.common.base.Function;
import com.google.common.collect.Iterators;

public class ZipTableReader implements TableReader
{
	private final ZipFile zipFile;
	private final Map<String, ZipEntry> tableNameMap;

	public ZipTableReader(ZipFile zipFile) throws IOException
	{
		if (zipFile == null) throw new IllegalArgumentException("zip file is null");
		this.zipFile = zipFile;

		// init table name map
		tableNameMap = new LinkedHashMap<String, ZipEntry>();
		for (Enumeration<? extends ZipEntry> e = zipFile.entries(); e.hasMoreElements();)
		{
			ZipEntry zipEntry = e.nextElement();

			// remove extension from filename
			int pos = zipEntry.getName().lastIndexOf('.');
			String tableName = pos != -1 ? zipEntry.getName().substring(0, pos) : zipEntry.getName();

			tableNameMap.put(tableName, zipEntry);
		}
	}

	@Override
	public Iterator<TupleReader> iterator()
	{
		return Iterators.transform(tableNameMap.values().iterator(), new Function<ZipEntry, TupleReader>()
		{
			@Override
			@Nullable
			public TupleReader apply(@Nullable
			ZipEntry zipEntry)
			{
				try
				{
					return toTupleReader(zipEntry);
				}
				catch (IOException e)
				{
					throw new RuntimeException(e);
				}
			}
		});
	}

	@Override
	public void close() throws IOException
	{
		// noop
	}

	@Override
	public TupleReader getTupleReader(String tableName) throws IOException
	{
		ZipEntry zipEntry = tableNameMap.get(tableName);
		return zipEntry != null ? toTupleReader(zipEntry) : null;
	}

	@Override
	public Iterable<String> getTableNames() throws IOException
	{
		return tableNameMap.keySet();
	}

	private TupleReader toTupleReader(ZipEntry zipEntry) throws IOException
	{
		String name = zipEntry.getName();
		if (name.endsWith(".csv") || name.endsWith(".txt"))
		{
			Reader reader = new InputStreamReader(zipFile.getInputStream(zipEntry), Charset.forName("UTF-8"));
			return new CsvReader(reader);
		}
		else if (name.endsWith(".tsv"))
		{
			Reader reader = new InputStreamReader(zipFile.getInputStream(zipEntry), Charset.forName("UTF-8"));
			return new CsvReader(reader, '\t');
		}
		else
		{
			throw new IOException("unknown file type: " + name);
		}
	}
}
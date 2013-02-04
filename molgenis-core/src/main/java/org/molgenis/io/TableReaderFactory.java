package org.molgenis.io;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipFile;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.molgenis.io.csv.CsvReader;
import org.molgenis.io.excel.ExcelReader;

public class TableReaderFactory
{
	private TableReaderFactory()
	{
	}

	public static TableReader create(File file) throws IOException
	{
		if (file == null) throw new IllegalArgumentException("file is null");
		if (!file.isFile()) throw new IllegalArgumentException("file is not a file: " + file.getName());
		return createTableReader(file);
	}

	public static TableReader create(List<File> files) throws IOException
	{
		if (files == null || files.isEmpty()) throw new IllegalArgumentException("files is null or empty");

		AggregateTableReader tableReader = new AggregateTableReader();
		for (File file : files)
			tableReader.addTableReader(createTableReader(file));

		return tableReader;
	}

	private static TableReader createTableReader(File file) throws IOException
	{
		if (file == null) throw new IllegalArgumentException("file is null");

		String name = file.getName();
		if (name.endsWith(".csv") || name.endsWith(".txt"))
		{
			String tableName = FilenameUtils.getBaseName(name);
			return new SingleTableReader(new CsvReader(file), tableName);
		}
		else if (name.endsWith(".tsv"))
		{
			String tableName = FilenameUtils.getBaseName(name);
			return new SingleTableReader(new CsvReader(file, '\t'), tableName);
		}
		else if (name.endsWith(".xls") || name.endsWith(".xlsx"))
		{
			return new ExcelReader(file);
		}
		else if (name.endsWith(".zip"))
		{
			return new ZipTableReader(new ZipFile(file));
		}
		else
		{
			throw new IOException("unknown file type: " + name);
		}
	}

	private static class SingleTableReader implements TableReader
	{
		private final TupleReader tupleReader;
		private final String tableName;

		public SingleTableReader(TupleReader tupleReader, String tableName)
		{
			if (tupleReader == null) throw new IllegalArgumentException("tuple reader is null");
			if (tableName == null) throw new IllegalArgumentException("table name is null");
			this.tupleReader = tupleReader;
			this.tableName = tableName;
		}

		@Override
		public Iterator<TupleReader> iterator()
		{
			return Collections.singletonList(tupleReader).iterator();
		}

		@Override
		public void close() throws IOException
		{
			tupleReader.close();
		}

		@Override
		public TupleReader getTupleReader(String tableName) throws IOException
		{
			return this.tableName.equals(tableName) ? tupleReader : null;
		}

		@Override
		public Iterable<String> getTableNames() throws IOException
		{
			return Collections.singletonList(tableName);
		}
	}

	private static class AggregateTableReader implements TableReader
	{
		private final List<TableReader> tableReaders;
		private final Map<String, TupleReader> tupleReaders;

		public AggregateTableReader()
		{
			tableReaders = new ArrayList<TableReader>();
			tupleReaders = new LinkedHashMap<String, TupleReader>();
		}

		@Override
		public Iterator<TupleReader> iterator()
		{
			return Collections.<TupleReader> unmodifiableCollection(tupleReaders.values()).iterator();
		}

		public void addTableReader(TableReader tableReader) throws IOException
		{
			tableReaders.add(tableReader);
			for (String tableName : tableReader.getTableNames())
				tupleReaders.put(tableName, tableReader.getTupleReader(tableName));
		}

		@Override
		public void close() throws IOException
		{
			for (TableReader tableReader : tableReaders)
				IOUtils.closeQuietly(tableReader);
		}

		@Override
		public TupleReader getTupleReader(String tableName) throws IOException
		{
			return tupleReaders.get(tableName);
		}

		@Override
		public Iterable<String> getTableNames() throws IOException
		{
			return Collections.unmodifiableSet(tupleReaders.keySet());
		}
	}
}

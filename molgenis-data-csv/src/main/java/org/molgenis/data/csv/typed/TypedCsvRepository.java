package org.molgenis.data.csv.typed;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.RepositoryCapability;
import org.molgenis.data.support.AbstractRepository;
import org.molgenis.util.CloseableIterator;

import au.com.bytecode.opencsv.CSVParser;
import au.com.bytecode.opencsv.CSVReader;

import com.google.common.collect.Iterables;

/**
 * Repository that contains typed entities. For csv/tsv files with known content.
 * 
 * You must provide a <code>LineMapper</code> so the <code>typedIterator()</code> function returns an iterator that
 * returns typed entities
 * 
 * @param <T>
 */
public class TypedCsvRepository<T extends Entity> extends AbstractRepository
{
	private final EntityMetaData entityMetaData;
	private final File file;
	private final char separatorChar;
	private final int skipLines;
	private final LineMapper<T> lineMapper;

	public TypedCsvRepository(File file, EntityMetaData entityMetaData, char separatorChar, int skipLines,
			LineMapper<T> lineMapper)
	{
		this.entityMetaData = entityMetaData;
		this.file = file;
		this.separatorChar = separatorChar;
		this.skipLines = skipLines;
		this.lineMapper = lineMapper;
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		return entityMetaData;
	}

	@Override
	public Iterator<Entity> iterator()
	{
		final CloseableIterator<T> it = typedIterator();

		return new CloseableIterator<Entity>()
		{
			@Override
			public boolean hasNext()
			{
				return it.hasNext();
			}

			@Override
			public Entity next()
			{
				return it.next();
			}

			@Override
			public void remove()
			{
				it.remove();
			}

			@Override
			public void close()
			{
				it.close();
			}

		};
	}

	public CloseableIterator<T> typedIterator()
	{
		try
		{
			return new CloseableIterator<T>()
			{
				private final CSVReader csvReader = new CSVReader(new BufferedReader(new InputStreamReader(
						new FileInputStream(file), Charset.forName("UTF-8"))), separatorChar,
						CSVParser.DEFAULT_QUOTE_CHARACTER, skipLines);
				private T next;
				private boolean getNext = true;
				private int lineNumber = 0;

				@Override
				public boolean hasNext()
				{
					boolean next = get() != null;
					if (!next)
					{
						close();
					}

					return next;
				}

				@Override
				public T next()
				{
					T entity = get();
					getNext = true;
					return entity;
				}

				@Override
				public void close()
				{
					IOUtils.closeQuietly(csvReader);
				}

				private T get()
				{
					if (getNext)
					{
						try
						{
							String[] values = csvReader.readNext();
							next = values != null ? lineMapper.mapLine(values, lineNumber++) : null;
							getNext = false;
						}
						catch (IOException e)
						{
							throw new MolgenisDataException("Exception reading line of csv file ["
									+ file.getAbsolutePath() + "]", e);
						}
					}

					return next;
				}

			};
		}
		catch (FileNotFoundException e)
		{
			throw new MolgenisDataException(e);
		}
	}

	@Override
	public Set<RepositoryCapability> getCapabilities()
	{
		return Collections.emptySet();
	}

	@Override
	public long count()
	{
		return Iterables.size(this);
	}

}

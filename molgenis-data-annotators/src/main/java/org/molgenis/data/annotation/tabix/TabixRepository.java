package org.molgenis.data.annotation.tabix;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

import net.sf.samtools.SAMFormatException;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataConverter;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Query;
import org.molgenis.data.RepositoryCapability;
import org.molgenis.data.support.AbstractRepository;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.vcf.VcfRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVParser;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

public class TabixRepository extends AbstractRepository
{
	private static final Logger LOG = LoggerFactory.getLogger(TabixRepository.class);

	private String fileName;
	private volatile TabixReader reader;
	private EntityMetaData entityMetaData;

	/**
	 * Creates a new {@link TabixRepository}
	 * 
	 * @param fileName
	 *            name of the Tabix file
	 * @param entityMetaData
	 *            {@link EntityMetaData} for the tabix file. Attributes should be in the order of the file's columns
	 * @throws IOException
	 *             if something goes wrong creating the {@link TabixReader} for the file
	 */
	public TabixRepository(String fileName, EntityMetaData entityMetaData) throws IOException
	{
		this.fileName = fileName;
		this.entityMetaData = entityMetaData;
	}

	private void checkTabixReader()
	{
		if (reader == null)
		{
			initTabixReader();
		}
	}

	private synchronized void initTabixReader()
	{
		if (reader == null)
		{
			try
			{
				reader = new TabixReader(fileName);
			}
			catch (IOException e)
			{
				LOG.error("Failed to initialize tabix reader for file " + fileName, e);
				throw new IllegalStateException("Failed to initialize tabix reader for file " + fileName, e);
			}
		}

	}

	@Override
	public Set<RepositoryCapability> getCapabilities()
	{
		return Collections.singleton(RepositoryCapability.QUERYABLE);
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		return entityMetaData;
	}

	@Override
	public Iterable<Entity> findAll(Query q)
	{
		String chromValue = getFirstEqualsValueFor(VcfRepository.CHROM, q).toString();
		long posValue = Long.parseLong(getFirstEqualsValueFor(VcfRepository.POS, q).toString());
		return query(chromValue, Long.valueOf(posValue));
	}

	/**
	 * Queries the tabix reader.
	 * 
	 * @param chrom
	 *            name of the chromosome
	 * @param pos
	 *            position
	 * @return {@link ImmutableList} of entities found
	 */
	private synchronized ImmutableList<Entity> query(String chrom, long pos)
	{
		checkTabixReader();
		String queryString = String.format("%s:%s-%2$s", chrom, pos);
		LOG.info("query({})", queryString);
		org.molgenis.data.annotation.tabix.TabixReader.Iterator iterator = reader.query(queryString);
		Builder<Entity> builder = ImmutableList.<Entity> builder();
		try
		{
			String line = iterator.next();
			while (line != null)
			{
				builder.add(toEntity(line));
				line = iterator.next();
			}
		}
		catch (SAMFormatException e)
		{
			LOG.error("Bad GZIP file: {}", fileName, e);
			throw e;
		}
		catch (IOException e)
		{
			LOG.error("Error reading from tabix reader {}", fileName, e);
		}
		return builder.build();
	}

	private static Object getFirstEqualsValueFor(String attributeName, Query q)
	{
		return q.getRules().stream().filter(rule -> attributeName.equals(rule.getField())).findFirst().get().getValue();
	}

	private Entity toEntity(String line) throws IOException
	{
		Entity result = new MapEntity(entityMetaData);
		CSVParser csvParser = new CSVParser('\t');
		String[] columns = csvParser.parseLine(line);
		int i = 0;
		for (AttributeMetaData amd : entityMetaData.getAtomicAttributes())
		{
			if (i < columns.length)
			{
				result.set(amd.getName(), DataConverter.convert(columns[i++], amd));
			}
		}
		return result;
	}

	private class TabixRepositoryIterator implements Iterator<Entity>
	{
		private String nextLine = null;

		@Override
		public boolean hasNext()
		{
			if (nextLine != null)
			{
				return true;
			}
			try
			{
				nextLine = reader.readLine();
				return nextLine != null;
			}
			catch (IOException e)
			{
				return false;
			}
		}

		@Override
		public Entity next()
		{
			if (!hasNext())
			{
				throw new NoSuchElementException();
			}
			try
			{
				return toEntity(nextLine);
			}
			catch (IOException e)
			{
				throw new NoSuchElementException();
			}
			finally
			{
				nextLine = null;
			}
		}
	}

	@Override
	public Iterator<Entity> iterator()
	{
		checkTabixReader();
		return new TabixRepositoryIterator();
	}

	public boolean fileExists()
	{
		return new File(fileName).exists();
	}
}

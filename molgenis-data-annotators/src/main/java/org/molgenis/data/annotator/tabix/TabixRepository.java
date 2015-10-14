package org.molgenis.data.annotator.tabix;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.molgenis.data.vcf.VcfRepository.CHROM;
import static org.molgenis.data.vcf.VcfRepository.POS;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataConverter;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Query;
import org.molgenis.data.RepositoryCapability;
import org.molgenis.data.support.AbstractRepository;
import org.molgenis.data.support.MapEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVParser;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

public class TabixRepository extends AbstractRepository
{
	private static final Logger LOG = LoggerFactory.getLogger(TabixRepository.class);

	private TabixReader reader;
	private EntityMetaData entityMetaData;
	private final String chromosomeAttributeName;
	private final String positionAttributeName;

	/**
	 * Creates a new {@link TabixRepository}
	 * 
	 * @param file
	 *            the Tabix file
	 * @param entityMetaData
	 *            {@link EntityMetaData} for the tabix file. Attributes should be in the order of the file's columns
	 * @throws IOException
	 *             if something goes wrong creating the {@link TabixReader} for the file
	 */
	public TabixRepository(File file, EntityMetaData entityMetaData) throws IOException
	{
		this(file, entityMetaData, CHROM, POS);
	}

	public TabixRepository(File file, EntityMetaData entityMetaData, String chromosomeAttributeName,
			String positionAttributeName) throws IOException
	{
		this.entityMetaData = entityMetaData;
		this.reader = new TabixReader(file.getAbsolutePath());
		this.chromosomeAttributeName = checkNotNull(chromosomeAttributeName);
		this.positionAttributeName = checkNotNull(positionAttributeName);
	}

	TabixRepository(TabixReader reader, EntityMetaData entityMetaData, String chromosomeAttributeName,
			String positionAttributeName)
	{
		this.reader = checkNotNull(reader);
		this.entityMetaData = checkNotNull(entityMetaData);
		this.chromosomeAttributeName = checkNotNull(chromosomeAttributeName);
		this.positionAttributeName = checkNotNull(positionAttributeName);
	}

	public static CSVParser getCsvParser()
	{
		return new CSVParser('\t');
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
		String chromValue = getFirstEqualsValueFor(chromosomeAttributeName, q).toString();
		long posValue = Long.parseLong(getFirstEqualsValueFor(positionAttributeName, q).toString());
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
		String queryString = String.format("%s:%s-%2$s", chrom, pos);
		LOG.debug("query({})", queryString);
		Builder<Entity> builder = ImmutableList.<Entity> builder();
		try
		{
			org.molgenis.data.annotator.tabix.TabixReader.Iterator iterator = reader.query(queryString);
			String line = iterator.next();
			while (line != null)
			{
				Entity entity = toEntity(line);
				if (entity.getLong(positionAttributeName) == pos)
				{
					builder.add(entity);
				}
				else
				{
					LOG.warn("TabixReader returns entity that does not match the query!");
				}
				line = iterator.next();
			}
		}
		catch (IOException e)
		{
			LOG.error("Error reading from tabix resource", e);
		}
		catch (NullPointerException e)
		{
			LOG.warn("Unable to read from tabix resource for query: " + queryString + " (Position not present in resource file?)");
			LOG.debug("", e);
		}
		catch (ArrayIndexOutOfBoundsException e)
		{
			LOG.warn("Unable to read from tabix resource for query: " + queryString + " (Chromosome not present in resource file?)");
			LOG.debug("", e);
		}
		return builder.build();
	}

	private static Object getFirstEqualsValueFor(String attributeName, Query q)
	{
		return q.getRules().stream().filter(rule -> attributeName.equals(rule.getField())).findFirst().get().getValue();
	}

	protected Entity toEntity(String line) throws IOException
	{
		Entity result = new MapEntity(entityMetaData);
		CSVParser csvParser = getCsvParser();
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
		return new TabixRepositoryIterator();
	}

}
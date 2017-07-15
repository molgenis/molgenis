package org.molgenis.data.annotation.core.resources.impl.tabix;

import au.com.bytecode.opencsv.CSVParser;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import org.molgenis.data.DataConverter;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.RepositoryCapability;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.AbstractRepository;
import org.molgenis.data.support.DynamicEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.vcf.model.VcfAttributes.CHROM;
import static org.molgenis.data.vcf.model.VcfAttributes.POS;

public class TabixRepository extends AbstractRepository
{
	private static final Logger LOG = LoggerFactory.getLogger(TabixRepository.class);

	private TabixReader reader;
	private EntityType entityType;
	private final String chromosomeAttributeName;
	private final String positionAttributeName;

	/**
	 * Creates a new {@link TabixRepository}
	 *
	 * @param file       the Tabix file
	 * @param entityType {@link EntityType} for the tabix file. Attributes should be in the order of the file's columns
	 * @throws IOException if something goes wrong creating the {@link TabixReader} for the file
	 */
	public TabixRepository(File file, EntityType entityType) throws IOException
	{
		this(file, entityType, CHROM, POS);
	}

	public TabixRepository(File file, EntityType entityType, String chromosomeAttributeName,
			String positionAttributeName) throws IOException
	{
		this.entityType = entityType;
		this.reader = new TabixReader(file.getAbsolutePath());
		this.chromosomeAttributeName = requireNonNull(chromosomeAttributeName);
		this.positionAttributeName = requireNonNull(positionAttributeName);
	}

	TabixRepository(TabixReader reader, EntityType entityType, String chromosomeAttributeName,
			String positionAttributeName)
	{
		this.reader = requireNonNull(reader);
		this.entityType = requireNonNull(entityType);
		this.chromosomeAttributeName = requireNonNull(chromosomeAttributeName);
		this.positionAttributeName = requireNonNull(positionAttributeName);
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

	public EntityType getEntityType()
	{
		return entityType;
	}

	@Override
	public Stream<Entity> findAll(Query<Entity> q)
	{
		Object posValue = getFirstEqualsValueFor(positionAttributeName, q);
		Object chromValue = getFirstEqualsValueFor(chromosomeAttributeName, q);
		List<Entity> result = new ArrayList<>();

		// if one of both required attributes is null, skip the query and return an empty list
		if (posValue != null && chromValue != null)
		{
			int posIntValue = Integer.parseInt(posValue.toString());
			String chromStringValue = chromValue.toString();
			result = query(chromStringValue, posIntValue);
		}
		return result.stream();
	}

	/**
	 * Queries the tabix reader.
	 *
	 * @param chrom name of the chromosome
	 * @param pos   position
	 * @return {@link ImmutableList} of entities found
	 */
	private synchronized ImmutableList<Entity> query(String chrom, int pos)
	{
		String queryString = String.format("%s:%s-%2$s", chrom, pos);
		LOG.debug("query({})", queryString);
		Builder<Entity> builder = ImmutableList.builder();
		try
		{
			org.molgenis.data.annotation.core.resources.impl.tabix.TabixReader.Iterator iterator = reader.query(
					queryString);
			if (iterator != null)
			{
				String line = iterator.next();
				while (line != null)
				{
					Entity entity = toEntity(line);
					if (entity.getInt(positionAttributeName) == pos)
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
			else
			{
				return ImmutableList.of(); // empty list
			}
		}
		catch (IOException e)
		{
			LOG.error("Error reading from tabix resource", e);
		}
		catch (NullPointerException e)
		{
			//FIXME: group the occurances of this exception and log once per annotation run
			LOG.trace("Unable to read from tabix resource for query: " + queryString
					+ " (Position not present in resource file?)");
			LOG.debug("", e);
		}
		catch (ArrayIndexOutOfBoundsException e)
		{
			//FIXME: group the occurances of this exception and log once per annotation run
			LOG.trace("Unable to read from tabix resource for query: " + queryString
					+ " (Chromosome not present in resource file?)");
			LOG.debug("", e);
		}
		return builder.build();
	}

	private static Object getFirstEqualsValueFor(String attributeName, Query<Entity> q)
	{
		return q.getRules().stream().filter(rule -> attributeName.equals(rule.getField())).findFirst().get().getValue();
	}

	protected Entity toEntity(String line) throws IOException
	{
		Entity result = new DynamicEntity(entityType);
		CSVParser csvParser = getCsvParser();
		String[] columns = csvParser.parseLine(line);
		int i = 0;
		for (Attribute amd : entityType.getAtomicAttributes())
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
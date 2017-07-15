package org.molgenis.data.annotation.core.resources.impl.tabix;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.QueryRule;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.RepositoryCapability;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.vcf.VcfReaderFactory;
import org.molgenis.data.vcf.VcfRepository;
import org.molgenis.data.vcf.model.VcfAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An indexed VCF Repository
 */
public class TabixVcfRepository extends VcfRepository
{
	private static final Logger LOG = LoggerFactory.getLogger(TabixVcfRepository.class);
	private final TabixReader tabixReader;

	public TabixVcfRepository(File file, String entityTypeId, VcfAttributes vcfAttributes,
			EntityTypeFactory entityTypeFactory, AttributeFactory attrMetaFactory) throws IOException
	{
		super(file, entityTypeId, vcfAttributes, entityTypeFactory, attrMetaFactory);
		tabixReader = new TabixReader(file.getCanonicalPath());
	}

	TabixVcfRepository(VcfReaderFactory readerFactory, TabixReader tabixReader, String entityTypeId,
			VcfAttributes vcfAttributes, EntityTypeFactory entityTypeFactory, AttributeFactory attrMetaFactory)
	{
		super(readerFactory, entityTypeId, vcfAttributes, entityTypeFactory, attrMetaFactory);
		this.tabixReader = tabixReader;
	}

	@Override
	public Set<RepositoryCapability> getCapabilities()
	{
		return Collections.emptySet();
	}

	/**
	 * Examines a {@link Query} and finds the first {@link QueryRule} with operator {@link Operator#EQUALS} whose field
	 * matches an attributeName. It returns the value of that first matching {@link QueryRule}.
	 *
	 * @param attributeName the query field name to match
	 * @param q             the query to search in
	 * @return the value from the first matching query rule
	 */
	private static Object getFirstEqualsValueFor(String attributeName, Query<Entity> q)
	{
		return q.getRules()
				.stream()
				.filter(rule -> attributeName.equals(rule.getField()) && rule.getOperator() == Operator.EQUALS)
				.findFirst()
				.get()
				.getValue();
	}

	@Override
	public Stream<Entity> findAll(Query<Entity> q)
	{
		Object posValue = getFirstEqualsValueFor(VcfAttributes.POS, q);
		Object chromValue = getFirstEqualsValueFor(VcfAttributes.CHROM, q);
		List<Entity> result = new ArrayList<>();

		// if one of both required attributes is null, skip the query and return an empty list
		if (posValue != null && chromValue != null)
		{
			int posIntValue = Integer.parseInt(posValue.toString());
			String chromStringValue = chromValue.toString();
			result = query(chromStringValue, posIntValue, posIntValue);
		}
		return result.stream();
	}

	/**
	 * Queries the tabix reader.
	 *
	 * @param chrom   Name of chromosome
	 * @param posFrom position lower bound (inclusive)
	 * @param posTo   position upper bound (inclusive)
	 * @return {@link ImmutableList} of entities found
	 */
	public synchronized List<Entity> query(String chrom, int posFrom, int posTo)
	{
		String queryString = String.format("%s:%s-%s", checkNotNull(chrom), checkNotNull(posFrom), checkNotNull(posTo));
		try
		{
			Collection<String> lines = getLines(tabixReader.query(queryString));
			return lines.stream()
						.map(line -> line.split("\t"))
						.map(vcfToEntitySupplier.get()::toEntity)
						.filter(entity -> positionMatches(entity, posFrom, posTo))
						.collect(Collectors.toList());
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

		return Collections.emptyList();
	}

	/**
	 * Tabix is not always so precise. For example, the cmdline query
	 * <p>
	 * <pre>
	 * tabix ExAC.r0.3.sites.vep.vcf.gz 1:1115548-1115548
	 * </pre>
	 * <p>
	 * returns 2 variants:
	 * <ul>
	 * <li>"1 1115547 . CG C,TG"</li>
	 * <li>"1 1115548 rs114390380 G A"</li>
	 * </ul>
	 * It is therefore needed to verify the position of the elements returned.
	 */
	private boolean positionMatches(Entity entity, int posFrom, int posTo)
	{
		int entityPos = entity.getInt(VcfAttributes.POS);
		return entityPos >= posFrom && entityPos <= posTo;
	}

	/**
	 * Collect the lines returned in a {@link TabixReader.Iterator}.
	 *
	 * @param iterator the iterator from which the lines are collected, may be null.
	 * @return {@link Collection} of lines, is empty if the iterator was null.
	 */
	protected Collection<String> getLines(
			org.molgenis.data.annotation.core.resources.impl.tabix.TabixReader.Iterator iterator)
	{
		Builder<String> builder = ImmutableList.builder();
		if (iterator != null)
		{
			try
			{
				String line = iterator.next();
				while (line != null)
				{
					builder.add(line);
					line = iterator.next();
				}
			}
			catch (IOException e)
			{
				LOG.error("Error reading from tabix reader.", e);
			}
		}
		return builder.build();
	}

}

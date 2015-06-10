package org.molgenis.data.annotator.tabix;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.QueryRule;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.RepositoryCapability;
import org.molgenis.data.vcf.VcfRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

/**
 * An indexed VCF Repository
 * 
 */
public class TabixVcfRepository extends VcfRepository
{
	private static final Logger LOG = LoggerFactory.getLogger(TabixVcfRepository.class);

	private TabixReader tabixReader;

	public TabixVcfRepository(File file, String entityName) throws IOException
	{
		super(file, entityName);
		tabixReader = new TabixReader(file.getCanonicalPath());
	}

	@Override
	public Set<RepositoryCapability> getCapabilities()
	{
		return Collections.singleton(RepositoryCapability.QUERYABLE);
	}

	/**
	 * Examines a {@link Query} and finds the first {@link QueryRule} with operator {@link Operator#EQUALS} whose field
	 * matches an attributeName. It returns the value of that first matching {@link QueryRule}.
	 * 
	 * @param attributeName
	 *            the query field name to match
	 * @param q
	 *            the query to search in
	 * @return the value from the first matching query rule
	 */
	private static Object getFirstEqualsValueFor(String attributeName, Query q)
	{
		return q.getRules().stream()
				.filter(rule -> attributeName.equals(rule.getField()) && rule.getOperator() == Operator.EQUALS)
				.findFirst().get().getValue();
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
		String queryString = String.format("%s:%s-%2$s", chrom, pos);
		LOG.info("query({})", queryString);
		org.molgenis.data.annotator.tabix.TabixReader.Iterator iterator = reader.query(queryString);
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
		catch (IOException e)
		{
			LOG.error("Error reading from tabix reader.", e);
		}
		return builder.build();
	}

}

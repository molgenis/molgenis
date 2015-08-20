package org.molgenis.data.annotator.tabix;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
	private final TabixReader tabixReader;

	public TabixVcfRepository(File file, String entityName) throws IOException
	{
		super(file, entityName);
		tabixReader = new TabixReader(file.getCanonicalPath());
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
	 * Queries the tabix reader. Uses tabix query syntax, for example "1:1115548-1115548".
	 * 
	 * @param chrom
	 *            Name of chromosome
	 * @param pos
	 *            position
	 * @return {@link ImmutableList} of entities found
	 */
	public synchronized List<Entity> query(String chrom, long pos)
	{
		String queryString = String.format("%s:%s-%2$s", chrom, pos);
		Collection<String> lines = getLines(tabixReader.query(queryString));
		return lines.stream().map(line -> line.split("\t")).map(vcfToEntitySupplier.get()::toEntity)
		// Tabix is not always so precise. For example, the cmdline query
		// "tabix ExAC.r0.3.sites.vep.vcf.gz 1:1115548-1115548"
		// returns 2 variants:
		// "1 1115547 . CG C,TG" and "1 1115548 rs114390380 G A".
		// It is therefore needed to verify the position of the elements returned.
				.filter(entity -> entity.getLong(VcfRepository.POS) == pos).collect(Collectors.toList());
	}

	/**
	 * Collect the lines returned in a {@link TabixReader.Iterator}.
	 * 
	 * @param iterator
	 *            the iterator from which the lines are collected, may be null.
	 * @return {@link Collection} of lines, is empty if the iterator was null.
	 */
	protected Collection<String> getLines(org.molgenis.data.annotator.tabix.TabixReader.Iterator iterator)
	{
		Builder<String> builder = ImmutableList.<String> builder();
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

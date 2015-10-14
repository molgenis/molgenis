package org.molgenis.data.annotator.tabix;

import static org.elasticsearch.common.base.Preconditions.checkNotNull;

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
import org.molgenis.data.vcf.VcfReaderFactory;
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

	TabixVcfRepository(VcfReaderFactory readerFactory, TabixReader tabixReader, String entityName)
	{
		super(readerFactory, entityName);
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
		return query(chromValue, Long.valueOf(posValue), Long.valueOf(posValue));
	}

	/**
	 * Queries the tabix reader.
	 * 
	 * @param chrom
	 *            Name of chromosome
	 * @param posFrom
	 *            position lower bound (inclusive)
	 * @param posTo
	 *            position upper bound (inclusive)
	 * @return {@link ImmutableList} of entities found
	 */
	public synchronized List<Entity> query(String chrom, long posFrom, long posTo)
	{
		String queryString = String.format("%s:%s-%s", checkNotNull(chrom), checkNotNull(posFrom), checkNotNull(posTo));
		Collection<String> lines = getLines(tabixReader.query(queryString));
		return lines.stream().map(line -> line.split("\t")).map(vcfToEntitySupplier.get()::toEntity)
				.filter(entity -> positionMatches(entity, posFrom, posTo)).collect(Collectors.toList());
	}

	/**
	 * Tabix is not always so precise. For example, the cmdline query
	 * 
	 * <pre>
	 * tabix ExAC.r0.3.sites.vep.vcf.gz 1:1115548-1115548
	 * </pre>
	 * 
	 * returns 2 variants:
	 * <ul>
	 * <li>"1 1115547 . CG C,TG"</li>
	 * <li>"1 1115548 rs114390380 G A"</li>
	 * </ul>
	 * It is therefore needed to verify the position of the elements returned.
	 */
	private boolean positionMatches(Entity entity, long posFrom, long posTo)
	{
		long entityPos = entity.getLong(VcfRepository.POS);
		return entityPos >= posFrom && entityPos <= posTo;
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

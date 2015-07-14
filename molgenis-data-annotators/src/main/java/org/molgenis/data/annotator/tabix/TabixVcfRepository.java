package org.molgenis.data.annotator.tabix;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.QueryRule;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.RepositoryCapability;
import org.molgenis.data.vcf.VcfRepository;
import org.molgenis.vcf.VcfReader;
import org.molgenis.vcf.VcfRecord;
import org.molgenis.vcf.meta.VcfMeta;
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
	private final VcfMeta vcfMeta;
	private final VcfReader vcfReader;

	private TabixReader tabixReader;

	public TabixVcfRepository(File file, String entityName) throws IOException
	{
		super(file, entityName);
		tabixReader = new TabixReader(file.getCanonicalPath());
		vcfReader = createVcfReader();
		vcfMeta = vcfReader.getVcfMeta();
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
		Builder<Entity> builder = ImmutableList.<Entity> builder();
		org.molgenis.data.annotator.tabix.TabixReader.Iterator iterator = tabixReader.query(queryString);
		
		//Tabix reader sometimes returns null. Does this mean that query doesn't return anything?
		// See also http://sourceforge.net/p/samtools/mailman/message/26113299/
		if (iterator == null) {
			return builder.build();
		}
		
		try
		{
			String line = iterator.next();
			while (line != null)
			{
				builder.add(toEntity(getEntityMetaData(), new VcfRecord(vcfMeta, line.split("\t")), vcfMeta));
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

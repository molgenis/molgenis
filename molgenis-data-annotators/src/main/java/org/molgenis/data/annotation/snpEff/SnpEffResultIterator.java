package org.molgenis.data.annotation.snpEff;

import static com.google.common.collect.Iterators.peekingIterator;

import java.util.Iterator;

import org.molgenis.data.Entity;
import org.molgenis.data.vcf.VcfRepository;

import com.google.common.collect.PeekingIterator;

public class SnpEffResultIterator
{
	private PeekingIterator<Entity> entityIterator;

	public SnpEffResultIterator(Iterator<Entity> entityIterator)
	{
		this.entityIterator = peekingIterator(entityIterator);
	}

	/**
	 * Returns the next entity containing SnpEff annotations if its Chrom and Pos match. This implementation works
	 * because SnpEff always returns output in the same order as the input
	 *
	 * @param chrom
	 * @param pos
	 * 
	 * @return {@link Entity}
	 */
	public Entity get(String chrom, long pos)
	{
		if (entityIterator.hasNext())
		{
			Entity entityCandidate = entityIterator.peek();
			if (chrom.equals(entityCandidate.getString(VcfRepository.CHROM))
					&& pos == entityCandidate.getLong(VcfRepository.POS))
			{
				entityIterator.next();
				return entityCandidate;
			}
		}
		return null;
	}
}

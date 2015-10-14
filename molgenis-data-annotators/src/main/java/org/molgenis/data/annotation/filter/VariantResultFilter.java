package org.molgenis.data.annotation.filter;

import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.annotation.entity.ResultFilter;
import org.molgenis.data.vcf.VcfRepository;

import com.google.common.collect.FluentIterable;

public class VariantResultFilter implements ResultFilter
{

	@Override
	public Collection<AttributeMetaData> getRequiredAttributes()
	{
		return Arrays.asList(VcfRepository.REF_META, VcfRepository.ALT_META);
	}

	@Override
	public com.google.common.base.Optional<Entity> filterResults(Iterable<Entity> results, Entity annotatedEntity)
	{
		return FluentIterable
				.from(results)
				.filter(result -> StringUtils.equals(result.getString(VcfRepository.REF),
						annotatedEntity.getString(VcfRepository.REF))
						&& StringUtils.equals(result.getString(VcfRepository.ALT),
								annotatedEntity.getString(VcfRepository.ALT))).first();
	}

}

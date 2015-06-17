package org.molgenis.data.annotation;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.vcf.VcfRepository;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jvelde on 2/13/14.
 */
public abstract class LocusAnnotator extends AbstractRepositoryAnnotator
{
	// TODO: needs genome build and possible organism !?

	@Override
	public List<AttributeMetaData> getInputMetaData()
	{
		List<AttributeMetaData> metadata = new ArrayList<>();
		metadata.add(VcfRepository.CHROM_META);
		metadata.add(VcfRepository.POS_META);

		return metadata;
	}

}

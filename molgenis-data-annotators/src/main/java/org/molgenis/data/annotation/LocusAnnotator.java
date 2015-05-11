package org.molgenis.data.annotation;

import org.molgenis.data.EntityMetaData;
import org.molgenis.data.annotation.cmd.AnnotatorInfo;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.vcf.VcfRepository;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * Created by jvelde on 2/13/14.
 */
public abstract class LocusAnnotator extends AbstractRepositoryAnnotator implements RepositoryAnnotator,
		ApplicationListener<ContextRefreshedEvent>, AnnotatorInfo
{
	// TODO: needs genome build and possible organism !?

	@Override
	public EntityMetaData getInputMetaData()
	{
		DefaultEntityMetaData metadata = new DefaultEntityMetaData(this.getClass().getName(), MapEntity.class);
		metadata.addAttributeMetaData(VcfRepository.CHROM_META);
		metadata.addAttributeMetaData(VcfRepository.POS_META);

		return metadata;
	}
	
	public enum status {READY, BETA, INDEV}
	public enum type {EFFECT_PREDICTION, AUTOMATED_PROTOCOL, PATHOGENICITY_ESTIMATE, POPULATION_REFERENCE, PHENOTYPE_ASSOCIATION, GENOMIC_FEATURE, UNUSED}
	String code; //e.g. 'cgd', 'gonl', 'cadd'

	@Override
	public AnnotatorInfo.status getStatus(){
		return AnnotatorInfo.status.INDEV;
	}

	@Override
	public AnnotatorInfo.type getType(){
		return AnnotatorInfo.type.UNUSED;
	}
	
	@Override
	public String getCode()
	{
		return "unknown";
	}
	
}

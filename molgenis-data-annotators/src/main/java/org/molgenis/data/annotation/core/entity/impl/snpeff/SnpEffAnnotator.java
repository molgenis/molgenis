package org.molgenis.data.annotation.core.entity.impl.snpeff;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.annotation.core.RepositoryAnnotator;
import org.molgenis.data.annotation.core.effects.EffectsMetaData;
import org.molgenis.data.annotation.core.entity.AnnotatorConfig;
import org.molgenis.data.vcf.model.VcfAttributes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SnpEff annotator
 * <p>
 * SnpEff is a genetic variant annotation and effect prediction toolbox. It annotates and predicts the effects of
 * variants on genes (such as amino acid changes). see http://snpeff.sourceforge.net/
 * <p>
 * For this annotator to work SnpEff.jar must be present on the filesystem at the location defined by the
 * RuntimeProperty 'snpeff_jar_location'
 * <p>
 * <p>
 * new ANN field replacing EFF:
 * <p>
 * ANN=A|missense_variant|MODERATE|NEXN|NEXN|transcript|NM_144573.3|Coding|8/13|c.733G>A|p.Gly245Arg|1030/3389|733/2028|
 * 245/675||
 * <p>
 * <p>
 * -lof doesnt seem to work? would be great... http://snpeff.sourceforge.net/snpEff_lof_nmd.pdfs
 */
@Configuration
public class SnpEffAnnotator implements AnnotatorConfig
{
	public static final String NAME = "snpEff";

	@Autowired
	private SnpEffRunner snpEffRunner;

	@Autowired
	private Entity snpEffAnnotatorSettings;

	@Autowired
	private VcfAttributes vcfAttributes;

	@Autowired
	DataService dataService;

	@Autowired
	private EffectsMetaData effectsMetaData;
	private SnpEffRepositoryAnnotator annotator;

	@Bean
	public RepositoryAnnotator snpEff()
	{
		annotator = new SnpEffRepositoryAnnotator(NAME);
		return annotator;
	}

	@Override
	public void init()
	{
		annotator.init(snpEffRunner, snpEffAnnotatorSettings, vcfAttributes, effectsMetaData, dataService);
	}
}

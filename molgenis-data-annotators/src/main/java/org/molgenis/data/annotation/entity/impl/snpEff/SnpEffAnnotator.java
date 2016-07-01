package org.molgenis.data.annotation.entity.impl.snpEff;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.annotation.AbstractRepositoryAnnotator;
import org.molgenis.data.annotation.CmdLineAnnotatorSettingsConfigurer;
import org.molgenis.data.annotation.RefEntityAnnotator;
import org.molgenis.data.annotation.RepositoryAnnotator;
import org.molgenis.data.annotation.cmd.cmdlineannotatorsettingsconfigurer.SingleFileLocationCmdLineAnnotatorSettingsConfigurer;
import org.molgenis.data.annotation.entity.AnnotatorConfig;
import org.molgenis.data.annotation.entity.AnnotatorInfo;
import org.molgenis.data.annotation.entity.AnnotatorInfo.Status;
import org.molgenis.data.annotation.entity.AnnotatorInfo.Type;
import org.molgenis.data.annotator.websettings.SnpEffAnnotatorSettings;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.support.DataServiceImpl;
import org.molgenis.data.support.EffectsMetaData;
import org.molgenis.data.vcf.model.VcfAttributes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
	private DataServiceImpl dataService;

	@Autowired
	private VcfAttributes vcfAttributes;

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
		annotator.init(snpEffRunner, snpEffAnnotatorSettings, dataService, vcfAttributes, effectsMetaData);
	}

	public class SnpEffRepositoryAnnotator extends AbstractRepositoryAnnotator implements RefEntityAnnotator
	{
		private final String name;
		private VcfAttributes vcfAttributes;
		private EffectsMetaData effectsMetaData;
		private AnnotatorInfo info;
		private SnpEffRunner snpEffRunner;
		private Entity snpEffAnnotatorSettings;
		private DataService dataService;

		public SnpEffRepositoryAnnotator(String name)
		{
			this.name = name;
		}

		public void init(SnpEffRunner snpEffRunner, Entity snpEffAnnotatorSettings,
				DataService dataService, VcfAttributes vcfAttributes, EffectsMetaData effectsMetaData){
			this.snpEffRunner = snpEffRunner;
			this.snpEffAnnotatorSettings = snpEffAnnotatorSettings;
			this.dataService = dataService;
			this.vcfAttributes = vcfAttributes;
			this.effectsMetaData = effectsMetaData;
			this.info = AnnotatorInfo.create(Status.READY, Type.EFFECT_PREDICTION, NAME,
					"Genetic variant annotation and effect prediction toolbox. "
							+ "It annotates and predicts the effects of variants on genes (such as amino acid changes). "
							+ "This annotator creates a new table with SnpEff output to be able to store mutli-allelic and multigenic results. "
							+ "Results are NOT added to your existing dataset. "
							+ "SnpEff results can found in the <your_dataset_name>_EFFECTS. ",
					effectsMetaData.getOrderedAttributes());
		}

		@Override
		public AnnotatorInfo getInfo()
		{
			return info;
		}

		@Override
		public Iterator<Entity> annotate(Iterable<Entity> source)
		{
			return snpEffRunner.getSnpEffects(source);
		}


		@Override
		public String canAnnotate(EntityMetaData repoMetaData)
		{
			if (dataService.hasRepository(repoMetaData.getName() + SnpEffRunner.ENTITY_NAME_SUFFIX))
			{
				return "already annotated with SnpEff";
			}
			else
			{
				return super.canAnnotate(repoMetaData);
			}
		}

		@Override
		public List<AttributeMetaData> getRequiredAttributes()
		{
			List<AttributeMetaData> attributes = new ArrayList<>();
			attributes.add(vcfAttributes.getChromAttribute());
			attributes.add(vcfAttributes.getPosAttribute());
			attributes.add(vcfAttributes.getRefAttribute());
			attributes.add(vcfAttributes.getAltAttribute());

			return attributes;
		}

		@Override
		public String getSimpleName()
		{
			return NAME;
		}

		@Override
		public boolean annotationDataExists()
		{
			return snpEffRunner.getSnpEffPath() != null;
		}

		@Override
		public CmdLineAnnotatorSettingsConfigurer getCmdLineAnnotatorSettingsConfigurer()
		{
			return new SingleFileLocationCmdLineAnnotatorSettingsConfigurer(
					SnpEffAnnotatorSettings.Meta.SNPEFF_JAR_LOCATION, snpEffAnnotatorSettings);
		}

		@Override
		public EntityMetaData getOutputAttributes(EntityMetaData sourceEMD)
		{
			return snpEffRunner.getOutputMetaData(sourceEMD);
		}

		@Override
		public List<AttributeMetaData> getOutputAttributes()
		{
			return effectsMetaData.getOrderedAttributes();
		}
	}

}

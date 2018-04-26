package org.molgenis.data.annotation.core.entity.impl.snpeff;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.annotation.core.AbstractRepositoryAnnotator;
import org.molgenis.data.annotation.core.EffectCreatingAnnotator;
import org.molgenis.data.annotation.core.effects.EffectsMetaData;
import org.molgenis.data.annotation.core.entity.AnnotatorInfo;
import org.molgenis.data.annotation.core.resources.CmdLineAnnotatorSettingsConfigurer;
import org.molgenis.data.annotation.web.settings.SingleFileLocationCmdLineAnnotatorSettingsConfigurer;
import org.molgenis.data.annotation.web.settings.SnpEffAnnotatorSettings;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.vcf.model.VcfAttributes;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SnpEffRepositoryAnnotator extends AbstractRepositoryAnnotator implements EffectCreatingAnnotator
{
	@SuppressWarnings("unused")
	private final String name; // for debug purposes
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

	public void init(SnpEffRunner snpEffRunner, Entity snpEffAnnotatorSettings, VcfAttributes vcfAttributes,
			EffectsMetaData effectsMetaData, DataService dataService)
	{
		this.snpEffRunner = snpEffRunner;
		this.snpEffAnnotatorSettings = snpEffAnnotatorSettings;
		this.vcfAttributes = vcfAttributes;
		this.effectsMetaData = effectsMetaData;
		this.dataService = dataService;

		this.info = AnnotatorInfo.create(AnnotatorInfo.Status.READY, AnnotatorInfo.Type.EFFECT_PREDICTION,
				SnpEffAnnotator.NAME, "Genetic variant annotation and effect prediction toolbox. "
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
	public String canAnnotate(EntityType repoMetaData)
	{
		if (dataService.hasRepository(repoMetaData.getId() + SnpEffRunner.ENTITY_NAME_SUFFIX))
		{
			return "already annotated with SnpEff";
		}
		else
		{
			return super.canAnnotate(repoMetaData);
		}
	}

	@Override
	public List<Attribute> getRequiredAttributes()
	{
		List<Attribute> attributes = new ArrayList<>();
		attributes.add(vcfAttributes.getChromAttribute());
		attributes.add(vcfAttributes.getPosAttribute());
		attributes.add(vcfAttributes.getRefAttribute());
		attributes.add(vcfAttributes.getAltAttribute());

		return attributes;
	}

	@Override
	public String getSimpleName()
	{
		return SnpEffAnnotator.NAME;
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
	public EntityType getTargetEntityType(EntityType sourceEntityType)
	{
		return snpEffRunner.getTargetEntityType(sourceEntityType);
	}

	@Override
	public List<Attribute> getOutputAttributes()
	{
		return effectsMetaData.getOrderedAttributes();
	}

	@Override
	public List<Attribute> createAnnotatorAttributes(AttributeFactory attributeFactory)
	{
		return effectsMetaData.getOrderedAttributes();
	}
}
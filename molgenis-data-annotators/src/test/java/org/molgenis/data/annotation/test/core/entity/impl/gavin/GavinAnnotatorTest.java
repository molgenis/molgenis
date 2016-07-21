package org.molgenis.data.annotation.test.core.entity.impl.gavin;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.annotation.core.RepositoryAnnotator;
import org.molgenis.data.annotation.core.effects.EffectsMetaData;
import org.molgenis.data.annotation.core.entity.AnnotatorConfig;
import org.molgenis.data.annotation.core.entity.impl.CaddAnnotator;
import org.molgenis.data.annotation.core.entity.impl.ExacAnnotator;
import org.molgenis.data.annotation.core.entity.impl.gavin.GavinAnnotator;
import org.molgenis.data.annotation.core.query.GeneNameQueryCreator;
import org.molgenis.data.annotation.core.resources.Resources;
import org.molgenis.data.annotation.core.resources.impl.ResourcesImpl;
import org.molgenis.data.annotation.web.AnnotationService;
import org.molgenis.data.annotation.web.settings.GavinAnnotatorSettings;
import org.molgenis.data.listeners.EntityListenersService;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.AttributeMetaDataFactory;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.meta.model.EntityMetaDataFactory;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.data.vcf.model.VcfAttributes;
import org.molgenis.test.data.AbstractMolgenisSpringTest;
import org.molgenis.util.ResourceUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.MolgenisFieldTypes.AttributeType.STRING;
import static org.molgenis.MolgenisFieldTypes.AttributeType.XREF;
import static org.molgenis.data.annotation.core.entity.impl.gavin.GavinAnnotator.*;
import static org.testng.Assert.*;

@ContextConfiguration(classes = { GavinAnnotatorTest.Config.class, GavinAnnotator.class })
public class GavinAnnotatorTest extends AbstractMolgenisSpringTest
{
	@Autowired
	ApplicationContext context;

	@Autowired
	AttributeMetaDataFactory attributeMetaDataFactory;

	@Autowired
	EntityMetaDataFactory entityMetaDataFactory;

	@Autowired
	VcfAttributes vcfAttributes;

	@Autowired
	RepositoryAnnotator annotator;

	@Autowired
	EffectsMetaData effectsMetaData;

	private EntityMetaData emd;
	private EntityMetaData entityMetaData;

	@BeforeClass
	public void beforeClass() throws IOException
	{
		AnnotatorConfig annotatorConfig = context.getBean(AnnotatorConfig.class);
		annotatorConfig.init();
		emd = entityMetaDataFactory.create().setName("gavin");
		entityMetaData = entityMetaDataFactory.create().setName("test_variant");
		List<AttributeMetaData> refAttributesList = Arrays
				.asList(CaddAnnotator.getCaddScaledAttr(attributeMetaDataFactory),
						ExacAnnotator.getExacAFAttr(attributeMetaDataFactory), vcfAttributes.getAltAttribute());
		entityMetaData.addAttributes(refAttributesList);
		AttributeMetaData refAttr = attributeMetaDataFactory.create().setName("test_variant").setDataType(XREF)
				.setRefEntity(entityMetaData).setDescription(
						"This annotator needs a references to an entity containing: " + StreamSupport
								.stream(refAttributesList.spliterator(), false).map(AttributeMetaData::getName)
								.collect(Collectors.joining(", ")));

		emd.addAttributes(
				Arrays.asList(effectsMetaData.getGeneNameAttr(), effectsMetaData.getPutativeImpactAttr(), refAttr,
						vcfAttributes.getAltAttribute()));

		AttributeMetaData idAttr = attributeMetaDataFactory.create().setName("idAttribute").setAuto(true);
		emd.addAttribute(idAttr);
		emd.setIdAttribute(idAttr);
		emd.addAttributes(effectsMetaData.getOrderedAttributes());
		emd.addAttribute(
				attributeMetaDataFactory.create().setName(EffectsMetaData.VARIANT).setNillable(false).setDataType(XREF)
						.setRefEntity(entityMetaData));
		AttributeMetaData classification = attributeMetaDataFactory.create().setName(CLASSIFICATION).setDataType(STRING)
				.setDescription(CLASSIFICATION).setLabel(CLASSIFICATION);
		AttributeMetaData confidence = attributeMetaDataFactory.create().setName(CONFIDENCE).setDataType(STRING)
				.setDescription(CONFIDENCE).setLabel(CONFIDENCE);
		AttributeMetaData reason = attributeMetaDataFactory.create().setName(REASON).setDataType(STRING)
				.setDescription(REASON).setLabel(REASON);
		emd.addAttributes(Arrays.asList(classification, confidence, reason));

		entityMetaData.addAttribute(idAttr);
		entityMetaData.setIdAttribute(idAttr);

	}

	@Test
	public void testAnnotateHighMafBenign()
	{
		Entity variant_entity = new DynamicEntity(entityMetaData);

		variant_entity.set(VcfAttributes.ALT, "A,T");
		variant_entity.set(CaddAnnotator.CADD_SCALED, "1,2");
		variant_entity.set(ExacAnnotator.EXAC_AF, "2,3");

		Entity effect_entity = new DynamicEntity(emd);
		effect_entity.set(EffectsMetaData.ALT, "A");
		effect_entity.set(EffectsMetaData.PUTATIVE_IMPACT, "HIGH");
		effect_entity.set(GavinAnnotator.VARIANT_ENTITY, variant_entity);

		Iterator<Entity> results = annotator.annotate(Collections.singletonList(effect_entity));
		assertTrue(results.hasNext());
		Entity resultEntity = results.next();
		assertFalse(results.hasNext());

		assertEquals(resultEntity.get(CLASSIFICATION), "Benign");
		assertEquals(resultEntity.get(CONFIDENCE), "genomewide");
		assertEquals(resultEntity.get(REASON), "MAF > 0.00474");

	}

	@Test
	public void testAnnotateLowCaddBenign()
	{
		Entity variant_entity = new DynamicEntity(entityMetaData);

		variant_entity.set(VcfAttributes.ALT, "A,T");
		variant_entity.set(CaddAnnotator.CADD_SCALED, "1,2");
		variant_entity.set(ExacAnnotator.EXAC_AF, "0.00001,0.00001");

		Entity effect_entity = new DynamicEntity(emd);
		effect_entity.set(EffectsMetaData.ALT, "A");
		effect_entity.set(EffectsMetaData.PUTATIVE_IMPACT, "HIGH");
		effect_entity.set(GavinAnnotator.VARIANT_ENTITY, variant_entity);

		Iterator<Entity> results = annotator.annotate(Collections.singletonList(effect_entity));
		assertTrue(results.hasNext());
		Entity resultEntity = results.next();
		assertFalse(results.hasNext());

		assertEquals(resultEntity.get(CLASSIFICATION), "Benign");
		assertEquals(resultEntity.get(CONFIDENCE), "genomewide");
		assertEquals(resultEntity.get(REASON), "CADDscore <= 15");

	}

	@Test
	public void testAnnotateNoCaddVOUS()
	{
		Entity variant_entity = new DynamicEntity(entityMetaData);

		variant_entity.set(VcfAttributes.ALT, "A,T");
		// variant_entity.set(CaddAnnotator.CADD_SCALED, "16,6");
		variant_entity.set(ExacAnnotator.EXAC_AF, "0.00001,0.00001");

		Entity effect_entity = new DynamicEntity(emd);
		effect_entity.set(EffectsMetaData.ALT, "A");
		effect_entity.set(EffectsMetaData.PUTATIVE_IMPACT, "HIGH");
		effect_entity.set(EffectsMetaData.GENE_NAME, "TFR2");
		effect_entity.set(GavinAnnotator.VARIANT_ENTITY, variant_entity);

		Iterator<Entity> results = annotator.annotate(Collections.singletonList(effect_entity));
		assertTrue(results.hasNext());
		Entity resultEntity = results.next();
		assertFalse(results.hasNext());

		assertEquals(resultEntity.get(CLASSIFICATION), "VOUS");
		assertEquals(resultEntity.get(CONFIDENCE), "genomewide");
		assertEquals(resultEntity.get(REASON),
				"Unable to classify variant as benign or pathogenic. The combination of HIGH impact, a CADD score [missing] and MAF of 1.0E-5 in TFR2 is inconclusive.");

	}

	@Test
	public void testAnnotateLowVariantCaddBenign()
	{
		Entity variant_entity = new DynamicEntity(entityMetaData);

		variant_entity.set(VcfAttributes.ALT, "A,T");
		variant_entity.set(CaddAnnotator.CADD_SCALED, "6,80");
		variant_entity.set(ExacAnnotator.EXAC_AF, "0.00001,0.00001");

		Entity effect_entity = new DynamicEntity(emd);
		effect_entity.set(EffectsMetaData.ALT, "A");
		effect_entity.set(EffectsMetaData.PUTATIVE_IMPACT, "HIGH");
		effect_entity.set(EffectsMetaData.GENE_NAME, "TFR2");
		effect_entity.set(GavinAnnotator.VARIANT_ENTITY, variant_entity);

		Iterator<Entity> results = annotator.annotate(Collections.singletonList(effect_entity));
		assertTrue(results.hasNext());
		Entity resultEntity = results.next();
		assertFalse(results.hasNext());

		assertEquals(resultEntity.get(CLASSIFICATION), "Benign");
		assertEquals(resultEntity.get(CONFIDENCE), "calibrated");
		assertEquals(resultEntity.get(REASON),
				"Variant CADD score of 6.0 is lesser than the 95% sensitivity threshold of 35.35 for this gene, although the variant MAF of 1.0E-5 is lesser than the pathogenic 95th percentile MAF of 1.9269599999999953E-4.");
	}

	@Test
	public void testAnnotateHighCaddPathogenic()
	{
		Entity variant_entity = new DynamicEntity(entityMetaData);

		variant_entity.set(VcfAttributes.ALT, "A,T");
		variant_entity.set(CaddAnnotator.CADD_SCALED, "6,80");
		variant_entity.set(ExacAnnotator.EXAC_AF, "0.00001,0.00001");

		Entity effect_entity = new DynamicEntity(emd);
		effect_entity.set(EffectsMetaData.ALT, "T");
		effect_entity.set(EffectsMetaData.PUTATIVE_IMPACT, "HIGH");
		effect_entity.set(EffectsMetaData.GENE_NAME, "TFR2");
		effect_entity.set(GavinAnnotator.VARIANT_ENTITY, variant_entity);

		Iterator<Entity> results = annotator.annotate(Collections.singletonList(effect_entity));
		assertTrue(results.hasNext());
		Entity resultEntity = results.next();
		assertFalse(results.hasNext());

		assertEquals(resultEntity.get(CLASSIFICATION), "Pathognic");
		assertEquals(resultEntity.get(CONFIDENCE), "calibrated");
		assertEquals(resultEntity.get(REASON),
				"Variant CADD score of 80.0 is greater than the 95% specificity threshold of 35.35 for this gene. Also, the variant MAF of 1.0E-5 is lesser than the pathogenic 95th percentile MAF of 1.9269599999999953E-4.");
	}

	@Configuration
	@ComponentScan({ "org.molgenis.data.vcf.model", "org.molgenis.data.annotation.core.effects" })
	public static class Config
	{
		@Bean
		public GavinAnnotatorSettings gavinAnnotatorSettings()
		{
			GavinAnnotatorSettings settings = mock(GavinAnnotatorSettings.class);
			when(settings.getString(GavinAnnotatorSettings.Meta.VARIANT_FILE_LOCATION))
					.thenReturn(ResourceUtils.getFile(getClass(), "/gavin/GAVIN_calibrations_r0.1.xlsx").getPath());

			return settings;
		}

		@Bean
		public DataService dataService()
		{
			return mock(DataService.class);
		}

		@Bean
		public AnnotationService annotationService()
		{
			return mock(AnnotationService.class);
		}

		@Bean
		public Resources resources()
		{
			return new ResourcesImpl();
		}

		@Bean
		public Entity caddAnnotatorSettings()
		{
			return mock(Entity.class);
		}

		@Bean
		public Entity exacAnnotatorSettings()
		{
			return mock(Entity.class);
		}

		@Bean
		public GeneNameQueryCreator geneNameQueryCreator()
		{
			return new GeneNameQueryCreator();
		}

		@Bean
		public EntityListenersService entityListenersService()
		{
			return new EntityListenersService();
		}
	}
}

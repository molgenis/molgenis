package org.molgenis.data.annotation.entity.impl;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.annotation.AnnotationService;
import org.molgenis.data.annotation.RepositoryAnnotator;
import org.molgenis.data.annotation.entity.impl.gavin.GavinAnnotator;
import org.molgenis.data.annotation.resources.Resources;
import org.molgenis.data.annotation.resources.impl.ResourcesImpl;
import org.molgenis.data.annotator.websettings.GavinAnnotatorSettings;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.EffectsMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.vcf.VcfRepository;
import org.molgenis.util.ResourceUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
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
import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_ID;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

@ContextConfiguration(classes =
{ GavinAnnotatorTest.Config.class, GavinAnnotator.class })
public class GavinAnnotatorTest extends AbstractTestNGSpringContextTests
{
	@Autowired
	RepositoryAnnotator annotator;
	private DefaultEntityMetaData emd;
	private DefaultEntityMetaData entityMetaData;

	@BeforeClass
	public void beforeClass() throws IOException
	{
		emd = new DefaultEntityMetaData("gavin");
		entityMetaData = new DefaultEntityMetaData("test_variant");
		List<AttributeMetaData> refAttributesList = Arrays.asList(CaddAnnotator.CADD_SCALED_ATTR,
				ExacAnnotator.EXAC_AF_ATTR, VcfRepository.ALT_META);
		entityMetaData.addAllAttributeMetaData(refAttributesList);
		AttributeMetaData refAttr = new DefaultAttributeMetaData("test_variant", MolgenisFieldTypes.FieldTypeEnum.XREF)
				.setRefEntity(entityMetaData)
				.setDescription("This annotator needs a references to an entity containing: "
						+ StreamSupport.stream(refAttributesList.spliterator(), false).map(AttributeMetaData::getName)
								.collect(Collectors.joining(", ")));

		emd.addAllAttributeMetaData(Arrays.asList(EffectsMetaData.GENE_NAME_ATTR, EffectsMetaData.PUTATIVE_IMPACT_ATTR,
				refAttr, VcfRepository.ALT_META));

		entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData("Identifier"), ROLE_ID);
		emd.addAttributeMetaData(new DefaultAttributeMetaData("Identifier"), ROLE_ID);

	}

	@Test
	public void testAnnotateHighMafBenign()
	{
		Entity variant_entity = new MapEntity(entityMetaData);

		variant_entity.set(VcfRepository.ALT, "A,T");
		variant_entity.set(CaddAnnotator.CADD_SCALED, "1,2");
		variant_entity.set(ExacAnnotator.EXAC_AF, "2,3");

		Entity effect_entity = new MapEntity(emd);
		effect_entity.set(EffectsMetaData.ALT, "A");
		effect_entity.set(EffectsMetaData.PUTATIVE_IMPACT, "HIGH");
		effect_entity.set(GavinAnnotator.VARIANT_ENTITY, variant_entity);

		Iterator<Entity> results = annotator.annotate(Collections.singletonList(effect_entity));
		assertTrue(results.hasNext());
		Entity resultEntity = results.next();
		assertFalse(results.hasNext());

		Entity expectedEntity = new MapEntity("expected");
		expectedEntity.set(GavinAnnotator.CLASSIFICATION, "Benign");
		expectedEntity.set(GavinAnnotator.CONFIDENCE, "genomewide");
		expectedEntity.set(GavinAnnotator.REASON, "Variant MAF of 2.0 is not rare enough to generally be considered pathogenic.");

		assertEquals(resultEntity.get(GavinAnnotator.CLASSIFICATION),
				expectedEntity.get(GavinAnnotator.CLASSIFICATION));
		assertEquals(resultEntity.get(GavinAnnotator.CONFIDENCE), expectedEntity.get(GavinAnnotator.CONFIDENCE));
		assertEquals(resultEntity.get(GavinAnnotator.REASON), expectedEntity.get(GavinAnnotator.REASON));

	}

	@Test
	public void testAnnotateLowCaddBenign()
	{
		Entity variant_entity = new MapEntity(entityMetaData);

		variant_entity.set(VcfRepository.ALT, "A,T");
		variant_entity.set(CaddAnnotator.CADD_SCALED, "1,2");
		variant_entity.set(ExacAnnotator.EXAC_AF, "0.00001,0.00001");

		Entity effect_entity = new MapEntity(emd);
		effect_entity.set(EffectsMetaData.ALT, "A");
		effect_entity.set(EffectsMetaData.PUTATIVE_IMPACT, "HIGH");
		effect_entity.set(GavinAnnotator.VARIANT_ENTITY, variant_entity);

		Iterator<Entity> results = annotator.annotate(Collections.singletonList(effect_entity));
		assertTrue(results.hasNext());
		Entity resultEntity = results.next();
		assertFalse(results.hasNext());

		Entity expectedEntity = new MapEntity("expected");
		expectedEntity.set(GavinAnnotator.CLASSIFICATION, "Benign");
		expectedEntity.set(GavinAnnotator.CONFIDENCE, "genomewide");
		expectedEntity.set(GavinAnnotator.REASON, "Variant CADD score of 1.0 is less than a global threshold of 15, although the variant MAF of 1.0E-5 is rare enough to be potentially pathogenic.");

		assertEquals(resultEntity.get(GavinAnnotator.CLASSIFICATION),
				expectedEntity.get(GavinAnnotator.CLASSIFICATION));
		assertEquals(resultEntity.get(GavinAnnotator.CONFIDENCE), expectedEntity.get(GavinAnnotator.CONFIDENCE));
		assertEquals(resultEntity.get(GavinAnnotator.REASON), expectedEntity.get(GavinAnnotator.REASON));

	}

	@Test
	public void testAnnotateNoCaddVOUS()
	{
		Entity variant_entity = new MapEntity(entityMetaData);

		variant_entity.set(VcfRepository.ALT, "A,T");
		// variant_entity.set(CaddAnnotator.CADD_SCALED, "16,6");
		variant_entity.set(ExacAnnotator.EXAC_AF, "0.00001,0.00001");

		Entity effect_entity = new MapEntity(emd);
		effect_entity.set(EffectsMetaData.ALT, "A");
		effect_entity.set(EffectsMetaData.PUTATIVE_IMPACT, "HIGH");
		effect_entity.set(EffectsMetaData.GENE_NAME, "TFR2");
		effect_entity.set(GavinAnnotator.VARIANT_ENTITY, variant_entity);

		Iterator<Entity> results = annotator.annotate(Collections.singletonList(effect_entity));
		assertTrue(results.hasNext());
		Entity resultEntity = results.next();
		assertFalse(results.hasNext());

		Entity expectedEntity = new MapEntity("expected");
		expectedEntity.set(GavinAnnotator.CLASSIFICATION, "VOUS");
		expectedEntity.set(GavinAnnotator.CONFIDENCE, "genomewide");
		expectedEntity.set(GavinAnnotator.REASON,
				"Unable to classify variant as benign or pathogenic. The combination of HIGH impact, a CADD score of null and MAF of 1.0E-5 in TFR2 is inconclusive.");

		assertEquals(resultEntity.get(GavinAnnotator.CLASSIFICATION),
				expectedEntity.get(GavinAnnotator.CLASSIFICATION));
		assertEquals(resultEntity.get(GavinAnnotator.CONFIDENCE), expectedEntity.get(GavinAnnotator.CONFIDENCE));
		assertEquals(resultEntity.get(GavinAnnotator.REASON), expectedEntity.get(GavinAnnotator.REASON));

	}

	@Test
	public void testAnnotateLowVariantCaddBenign()
	{
		Entity variant_entity = new MapEntity(entityMetaData);

		variant_entity.set(VcfRepository.ALT, "A,T");
		variant_entity.set(CaddAnnotator.CADD_SCALED, "6,80");
		variant_entity.set(ExacAnnotator.EXAC_AF, "0.00001,0.00001");

		Entity effect_entity = new MapEntity(emd);
		effect_entity.set(EffectsMetaData.ALT, "A");
		effect_entity.set(EffectsMetaData.PUTATIVE_IMPACT, "HIGH");
		effect_entity.set(EffectsMetaData.GENE_NAME, "TFR2");
		effect_entity.set(GavinAnnotator.VARIANT_ENTITY, variant_entity);

		Iterator<Entity> results = annotator.annotate(Collections.singletonList(effect_entity));
		assertTrue(results.hasNext());
		Entity resultEntity = results.next();
		assertFalse(results.hasNext());

		Entity expectedEntity = new MapEntity("expected");
		expectedEntity.set(GavinAnnotator.CLASSIFICATION, "Benign");
		expectedEntity.set(GavinAnnotator.CONFIDENCE, "calibrated");
		expectedEntity.set(GavinAnnotator.REASON,
				"Variant CADD score of 6.0 is less than 30.35 for this gene.");

		assertEquals(resultEntity.get(GavinAnnotator.CLASSIFICATION),
				expectedEntity.get(GavinAnnotator.CLASSIFICATION));
		assertEquals(resultEntity.get(GavinAnnotator.CONFIDENCE), expectedEntity.get(GavinAnnotator.CONFIDENCE));
		assertEquals(resultEntity.get(GavinAnnotator.REASON), expectedEntity.get(GavinAnnotator.REASON));
	}

	@Test
	public void testAnnotateHighCaddPathogenic()
	{
		Entity variant_entity = new MapEntity(entityMetaData);

		variant_entity.set(VcfRepository.ALT, "A,T");
		variant_entity.set(CaddAnnotator.CADD_SCALED, "6,80");
		variant_entity.set(ExacAnnotator.EXAC_AF, "0.00001,0.00001");

		Entity effect_entity = new MapEntity(emd);
		effect_entity.set(EffectsMetaData.ALT, "T");
		effect_entity.set(EffectsMetaData.PUTATIVE_IMPACT, "HIGH");
		effect_entity.set(EffectsMetaData.GENE_NAME, "TFR2");
		effect_entity.set(GavinAnnotator.VARIANT_ENTITY, variant_entity);

		Iterator<Entity> results = annotator.annotate(Collections.singletonList(effect_entity));
		assertTrue(results.hasNext());
		Entity resultEntity = results.next();
		assertFalse(results.hasNext());

		Entity expectedEntity = new MapEntity("expected");
		expectedEntity.set(GavinAnnotator.CLASSIFICATION, "Pathogenic");
		expectedEntity.set(GavinAnnotator.CONFIDENCE, "calibrated");
		expectedEntity.set(GavinAnnotator.REASON,
				"Variant CADD score of 80.0 is greater than 30.35 for this gene.");

		assertEquals(resultEntity.get(GavinAnnotator.CLASSIFICATION),
				expectedEntity.get(GavinAnnotator.CLASSIFICATION));
		assertEquals(resultEntity.get(GavinAnnotator.CONFIDENCE), expectedEntity.get(GavinAnnotator.CONFIDENCE));
		assertEquals(resultEntity.get(GavinAnnotator.REASON), expectedEntity.get(GavinAnnotator.REASON));
	}

	public static class Config
	{
		@Autowired
		private DataService dataService;

		@Bean
		public Entity VariantClassificationAnnotatorSettings()
		{
			Entity settings = new MapEntity();

			settings.set(GavinAnnotatorSettings.Meta.VARIANT_FILE_LOCATION,
					ResourceUtils.getFile(getClass(), "/gavin/GAVIN_calibrations_r0.1.xlsx").getPath());

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
	}
}

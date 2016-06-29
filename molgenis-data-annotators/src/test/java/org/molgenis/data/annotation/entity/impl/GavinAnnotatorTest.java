package org.molgenis.data.annotation.entity.impl;

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
import java.util.Iterator;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.StreamSupport.stream;
import static org.mockito.Mockito.mock;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.XREF;
import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.annotation.entity.impl.gavin.GavinAnnotator.*;
import static org.molgenis.data.support.EffectsMetaData.GENE_NAME_ATTR;
import static org.molgenis.data.support.EffectsMetaData.PUTATIVE_IMPACT_ATTR;
import static org.molgenis.data.vcf.VcfRepository.ALT_META;
import static org.testng.Assert.*;

@ContextConfiguration(classes = { GavinAnnotatorTest.Config.class, GavinAnnotator.class })
public class GavinAnnotatorTest extends AbstractTestNGSpringContextTests
{
	private static final String BENIGN = "Benign";
	private static final String GENOMEWIDE = "genomewide";
	private static final String VOUS = "VOUS";
	private static final String CALIBRATED = "calibrated";
	private static final String PATHOGENIC = "Pathogenic";
	
	@Autowired
	RepositoryAnnotator annotator;

	private DefaultEntityMetaData effectEntityMetaData;
	private DefaultEntityMetaData variantEntityMetaData;

	@BeforeClass
	public void beforeClass() throws IOException
	{
		effectEntityMetaData = new DefaultEntityMetaData("gavin");
		variantEntityMetaData = new DefaultEntityMetaData("test_variant");

		List<AttributeMetaData> refAttributesList = newArrayList(CaddAnnotator.CADD_SCALED_ATTR,
				ExacAnnotator.EXAC_AF_ATTR, ALT_META);

		variantEntityMetaData.addAllAttributeMetaData(refAttributesList);

		AttributeMetaData refAttr = new DefaultAttributeMetaData("test_variant", XREF)
				.setRefEntity(variantEntityMetaData).setDescription(
						"This annotator needs a references to an entity containing: " + stream(
								refAttributesList.spliterator(), false).map(AttributeMetaData::getName)
								.collect(joining(", ")));

		effectEntityMetaData
				.addAllAttributeMetaData(newArrayList(GENE_NAME_ATTR, PUTATIVE_IMPACT_ATTR, refAttr, ALT_META));

		variantEntityMetaData.addAttributeMetaData(new DefaultAttributeMetaData("Identifier"), ROLE_ID);
		effectEntityMetaData.addAttributeMetaData(new DefaultAttributeMetaData("Identifier"), ROLE_ID);
	}

	@Test
	public void testAnnotateHighMafBenign()
	{
		Entity variantEntity = getVariantEntity("A,T", "1,2", "2,3");
		Entity effectEntity = getEffectEntity("A", "HIGH", null, variantEntity);

		Iterator<Entity> results = annotator.annotate(singletonList(effectEntity));
		assertTrue(results.hasNext());
		Entity resultEntity = results.next();
		assertFalse(results.hasNext());

		assertEquals(resultEntity.get(CLASSIFICATION), BENIGN);
		assertEquals(resultEntity.get(CONFIDENCE), GENOMEWIDE);
		assertEquals(resultEntity.get(REASON),
				"Variant MAF of 2.0 is not rare enough to generally be considered pathogenic.");
	}

	@Test
	public void testAnnotateLowCaddBenign()
	{
		Entity variantEntity = getVariantEntity("A,T", "1,2", "0.00001,0.00001");
		Entity effectEntity = getEffectEntity("A", "HIGH", null, variantEntity);

		Iterator<Entity> results = annotator.annotate(singletonList(effectEntity));
		assertTrue(results.hasNext());
		Entity resultEntity = results.next();
		assertFalse(results.hasNext());

		assertEquals(resultEntity.get(CLASSIFICATION), BENIGN);
		assertEquals(resultEntity.get(CONFIDENCE), GENOMEWIDE);
		assertEquals(resultEntity.get(REASON),
				"Variant CADD score of 1.0 is less than a global threshold of 15, although the variant MAF of 1.0E-5 is rare enough to be potentially pathogenic.");
	}

	@Test
	public void testAnnotateNoCaddVOUS()
	{
		Entity variantEntity = getVariantEntity("A,T", null, "0.00001,0.00001");
		Entity effectEntity = getEffectEntity("A", "HIGH", "TFR2", variantEntity);

		Iterator<Entity> results = annotator.annotate(singletonList(effectEntity));
		assertTrue(results.hasNext());
		Entity resultEntity = results.next();
		assertFalse(results.hasNext());

		assertEquals(resultEntity.get(CLASSIFICATION), VOUS);
		assertEquals(resultEntity.get(CONFIDENCE), GENOMEWIDE);
		assertEquals(resultEntity.get(REASON),
				"Unable to classify variant as benign or pathogenic. The combination of HIGH impact, a CADD score of null and MAF of 1.0E-5 in TFR2 is inconclusive.");
	}

	@Test
	public void testAnnotateLowVariantCaddBenign()
	{
		Entity variantEntity = getVariantEntity("A,T", "6,80", "0.00001,0.00001");
		Entity effectEntity = getEffectEntity("A", "HIGH", "TFR2", variantEntity);

		Iterator<Entity> results = annotator.annotate(singletonList(effectEntity));
		assertTrue(results.hasNext());
		Entity resultEntity = results.next();
		assertFalse(results.hasNext());

		assertEquals(resultEntity.get(CLASSIFICATION), BENIGN);
		assertEquals(resultEntity.get(CONFIDENCE), CALIBRATED);
		assertEquals(resultEntity.get(REASON),
				"Variant CADD score of 6.0 is less than 13.329999999999998 for this gene.");
	}

	@Test
	public void testAnnotateHighCaddPathogenic()
	{
		Entity variantEntity = getVariantEntity("A,T", "6,80", "0.00001,0.00001");
		Entity effectEntity = getEffectEntity("T", "HIGH", "TFR2", variantEntity);

		Iterator<Entity> results = annotator.annotate(singletonList(effectEntity));
		assertTrue(results.hasNext());
		Entity resultEntity = results.next();
		assertFalse(results.hasNext());

		assertEquals(resultEntity.get(CLASSIFICATION), PATHOGENIC);
		assertEquals(resultEntity.get(CONFIDENCE), CALIBRATED);
		assertEquals(resultEntity.get(REASON), "Variant CADD score of 80.0 is greater than 30.35 for this gene.");
	}

	@Test
	public void testGeneWithNullFields()
	{
		Entity variantEntity = getVariantEntity("A,T", "6,80", "0.00001,0.00001");
		Entity effectEntity = getEffectEntity("T", "HIGH", "ABCD1", variantEntity);

		Iterator<Entity> results = annotator.annotate(singletonList(effectEntity));
		assertTrue(results.hasNext());
		Entity resultEntity = results.next();
		assertFalse(results.hasNext());

		assertEquals(resultEntity.get(CLASSIFICATION), BENIGN);
		assertEquals(resultEntity.get(CONFIDENCE), CALIBRATED);
		assertEquals(resultEntity.get(REASON), "Variant MAF of 1.0E-5 is greater than 0.0.");
	}

	private Entity getEffectEntity(String alt, String putativeImpact, String geneName, Entity variantEntity)
	{
		Entity effectEntity = new MapEntity(effectEntityMetaData);
		effectEntity.set(EffectsMetaData.ALT, alt);
		effectEntity.set(EffectsMetaData.PUTATIVE_IMPACT, putativeImpact);
		effectEntity.set(EffectsMetaData.GENE_NAME, geneName);
		effectEntity.set(GavinAnnotator.VARIANT_ENTITY, variantEntity);
		return effectEntity;
	}

	private Entity getVariantEntity(String alt, String caddScaled, String exacAF)
	{
		Entity variantEntity = new MapEntity(variantEntityMetaData);
		variantEntity.set(VcfRepository.ALT, alt);
		variantEntity.set(CaddAnnotator.CADD_SCALED, caddScaled);
		variantEntity.set(ExacAnnotator.EXAC_AF, exacAF);
		return variantEntity;
	}

	public static class Config
	{
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

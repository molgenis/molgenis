package org.molgenis.data.annotation.core.entity.impl;

import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.Entity;
import org.molgenis.data.annotation.core.RepositoryAnnotator;
import org.molgenis.data.annotation.core.entity.AnnotatorConfig;
import org.molgenis.data.annotation.core.resources.Resources;
import org.molgenis.data.annotation.core.resources.impl.ResourcesImpl;
import org.molgenis.data.annotation.web.AnnotationService;
import org.molgenis.data.annotation.web.settings.GoNLAnnotatorSettings;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.data.vcf.config.VcfTestConfig;
import org.molgenis.data.vcf.model.VcfAttributes;
import org.molgenis.util.ResourceUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.annotation.core.entity.impl.GoNLAnnotator.*;
import static org.molgenis.data.meta.AttributeType.STRING;
import static org.testng.Assert.*;

@ContextConfiguration(classes = { GoNLAnnotatorTest.Config.class, GoNLAnnotator.class })
public class GoNLAnnotatorTest extends AbstractMolgenisSpringTest
{
	@Autowired
	ApplicationContext context;

	@Autowired
	AttributeFactory attributeFactory;

	@Autowired
	EntityTypeFactory entityTypeFactory;

	@Autowired
	VcfAttributes vcfAttributes;

	private final static String GONL_TEST_PATTERN = "gonl.chr%s.snps_indels.r5.vcf.gz";
	private final static String GONL_TEST_ROOT_DIRECTORY = "/gonl";
	private final static String GONL_TEST_CHROMOSOMES = "1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,X";
	private final static String GONL_TEST_OVERRIDE_CHROMOSOME_FILES_PROPERTY = "X:gonl.chrX.release4.gtc.vcf.gz";

	@Autowired
	RepositoryAnnotator annotator;
	private EntityType emd;

	@BeforeClass
	public void beforeClass() throws IOException
	{
		AnnotatorConfig annotatorConfig = context.getBean(AnnotatorConfig.class);
		annotatorConfig.init();

		emd = entityTypeFactory.create("gonl");
		emd.addAttribute(vcfAttributes.getChromAttribute());
		emd.addAttribute(vcfAttributes.getPosAttribute());
		emd.addAttribute(vcfAttributes.getRefAttribute());
		emd.addAttribute(vcfAttributes.getAltAttribute());
		emd.addAttribute(attributeFactory.create()
										 .setName(GONL_GENOME_AF)
										 .setDataType(STRING)
										 .setDescription(
												 "The allele frequency for variants seen in the population used for the GoNL project")
										 .setLabel(GONL_AF_LABEL));
		emd.addAttribute(attributeFactory.create()
										 .setName(GONL_GENOME_GTC)
										 .setDataType(STRING)
										 .setDescription(
												 "GenoType Counts. For each ALT allele in the same order as listed = 0/0,0/1,1/1,0/2,1/2,2/2,0/3,1/3,2/3,3/3,etc. Phasing is ignored; hence 1/0, 0|1 and 1|0 are all counted as 0/1. When one or more alleles is not called for a genotype in a specific sample (./., ./0, ./1, ./2, etc.), that sample's genotype is completely discarded for calculating GTC.")
										 .setLabel(GONL_GTC_LABEL));

	}

	// 14 tests below are test cases from the "test-edgecases.vcf"
	@Test
	public void testAnnotate1()
	{
		Entity entity1 = new DynamicEntity(emd);
		entity1.set(VcfAttributes.CHROM, "1");
		entity1.set(VcfAttributes.POS, 13380);
		entity1.set(VcfAttributes.REF, "C");
		entity1.set(VcfAttributes.ALT, "G");

		Iterator<Entity> results = annotator.annotate(Collections.singletonList(entity1));
		assertTrue(results.hasNext());
		Entity resultEntity = results.next();
		assertFalse(results.hasNext());

		assertNull(resultEntity.get(GONL_GENOME_AF));
		assertNull(resultEntity.get(GONL_GENOME_GTC));
	}

	@Test
	public void testAnnotate2()
	{
		Entity entity2 = new DynamicEntity(emd);
		entity2.set(VcfAttributes.CHROM, "1");
		entity2.set(VcfAttributes.POS, 13980);
		entity2.set(VcfAttributes.REF, "T");
		entity2.set(VcfAttributes.ALT, "C");

		Iterator<Entity> results = annotator.annotate(Collections.singletonList(entity2));
		assertTrue(results.hasNext());
		Entity resultEntity = results.next();
		assertFalse(results.hasNext());

		assertEquals(resultEntity.get(GONL_GENOME_AF), "0.013052208835341365");
		assertEquals(resultEntity.get(GONL_GENOME_GTC), "485|13|0");
	}

	@Test
	public void testAnnotate3()
	{
		Entity entity3 = new DynamicEntity(emd);
		entity3.set(VcfAttributes.CHROM, "1");
		entity3.set(VcfAttributes.POS, 78383467);
		entity3.set(VcfAttributes.REF, "G");
		entity3.set(VcfAttributes.ALT, "A");

		Iterator<Entity> results = annotator.annotate(Collections.singletonList(entity3));
		assertTrue(results.hasNext());
		Entity resultEntity = results.next();
		assertFalse(results.hasNext());

		assertEquals(resultEntity.get(GONL_GENOME_AF), "0.8674698795180723");
		assertEquals(resultEntity.get(GONL_GENOME_GTC), "6|120|372");
	}

	@Test
	public void testAnnotate4()
	{
		Entity entity4 = new DynamicEntity(emd);
		entity4.set(VcfAttributes.CHROM, "1");
		entity4.set(VcfAttributes.POS, 231094050);
		entity4.set(VcfAttributes.REF, "GAA");
		entity4.set(VcfAttributes.ALT, "G,GAAA,GA");

		Iterator<Entity> results = annotator.annotate(Collections.singletonList(entity4));
		assertTrue(results.hasNext());
		Entity resultEntity = results.next();
		assertFalse(results.hasNext());

		assertNull(resultEntity.get(GONL_GENOME_AF));
		assertNull(resultEntity.get(GONL_GENOME_GTC));
	}

	@Test
	public void testAnnotate5()
	{
		Entity entity5 = new DynamicEntity(emd);
		entity5.set(VcfAttributes.CHROM, "2");
		entity5.set(VcfAttributes.POS, 171570151);
		entity5.set(VcfAttributes.REF, "C");
		entity5.set(VcfAttributes.ALT, "T");

		Iterator<Entity> results = annotator.annotate(Collections.singletonList(entity5));
		assertTrue(results.hasNext());
		Entity resultEntity = results.next();
		assertFalse(results.hasNext());

		assertEquals(resultEntity.get(GONL_GENOME_AF), "0.30823293172690763");
		assertEquals(resultEntity.get(GONL_GENOME_GTC), "241|207|50");
	}

	@Test
	public void testAnnotate6()
	{
		Entity entity6 = new DynamicEntity(emd);
		entity6.set(VcfAttributes.CHROM, "4");
		entity6.set(VcfAttributes.POS, 69964234);
		entity6.set(VcfAttributes.REF, "CT");
		entity6.set(VcfAttributes.ALT, "CTT,CTTT,C");

		Iterator<Entity> results = annotator.annotate(Collections.singletonList(entity6));
		assertTrue(results.hasNext());
		Entity resultEntity = results.next();
		assertFalse(results.hasNext());

		assertNull(resultEntity.get(GONL_GENOME_AF));
		assertNull(resultEntity.get(GONL_GENOME_GTC));
	}

	@Test
	public void testAnnotate7()
	{
		Entity entity7 = new DynamicEntity(emd);
		entity7.set(VcfAttributes.CHROM, "15");
		entity7.set(VcfAttributes.POS, 66641732);
		entity7.set(VcfAttributes.REF, "G");
		entity7.set(VcfAttributes.ALT, "A,C,T");

		Iterator<Entity> results = annotator.annotate(Collections.singletonList(entity7));
		assertTrue(results.hasNext());
		Entity resultEntity = results.next();
		assertFalse(results.hasNext());

		assertEquals(resultEntity.get(GONL_GENOME_AF), ".,0.09538152610441768,.");
		assertEquals(resultEntity.get(GONL_GENOME_GTC), ".,412|77|9,.");
	}

	@Test
	public void testAnnotate8()
	{
		Entity entity8 = new DynamicEntity(emd);
		entity8.set(VcfAttributes.CHROM, "21");
		entity8.set(VcfAttributes.POS, 46924425);
		entity8.set(VcfAttributes.REF, "CGGCCCCCCA");
		entity8.set(VcfAttributes.ALT, "C");

		Iterator<Entity> results = annotator.annotate(Collections.singletonList(entity8));
		assertTrue(results.hasNext());
		Entity resultEntity = results.next();
		assertFalse(results.hasNext());

		assertNull(resultEntity.get(GONL_GENOME_AF));
		assertNull(resultEntity.get(GONL_GENOME_GTC));
	}

	@Test
	public void testAnnotate9()
	{
		Entity entity9 = new DynamicEntity(emd);
		entity9.set(VcfAttributes.CHROM, "X");
		entity9.set(VcfAttributes.POS, 79943569);
		entity9.set(VcfAttributes.REF, "T");
		entity9.set(VcfAttributes.ALT, "C");

		Iterator<Entity> results = annotator.annotate(Collections.singletonList(entity9));
		assertTrue(results.hasNext());
		Entity resultEntity = results.next();
		assertFalse(results.hasNext());

		assertEquals(resultEntity.get(GONL_GENOME_AF), "0.9989939637826962");
		assertEquals(resultEntity.get(GONL_GENOME_GTC), "0|1|496");
	}

	@Test
	public void testAnnotate10()
	{
		Entity entity10 = new DynamicEntity(emd);
		entity10.set(VcfAttributes.CHROM, "2");
		entity10.set(VcfAttributes.POS, 191904021);
		entity10.set(VcfAttributes.REF, "G");
		entity10.set(VcfAttributes.ALT, "T");

		Iterator<Entity> results = annotator.annotate(Collections.singletonList(entity10));
		assertTrue(results.hasNext());
		Entity resultEntity = results.next();
		assertFalse(results.hasNext());

		assertEquals(resultEntity.get(GONL_GENOME_AF), "0.0030120481927710845");
		assertEquals(resultEntity.get(GONL_GENOME_GTC), "495|3|0");
	}

	@Test
	public void testAnnotate11()
	{
		Entity entity11 = new DynamicEntity(emd);
		entity11.set(VcfAttributes.CHROM, "3");
		entity11.set(VcfAttributes.POS, 53219680);
		entity11.set(VcfAttributes.REF, "G");
		entity11.set(VcfAttributes.ALT, "C");

		Iterator<Entity> results = annotator.annotate(Collections.singletonList(entity11));
		assertTrue(results.hasNext());
		Entity resultEntity = results.next();
		assertFalse(results.hasNext());

		assertNull(resultEntity.get(GONL_GENOME_AF));
		assertNull(resultEntity.get(GONL_GENOME_GTC));
	}

	@Test
	public void testAnnotate12()
	{
		Entity entity12 = new DynamicEntity(emd);
		entity12.set(VcfAttributes.CHROM, "2");
		entity12.set(VcfAttributes.POS, 219142023);
		entity12.set(VcfAttributes.REF, "G");
		entity12.set(VcfAttributes.ALT, "A");

		Iterator<Entity> results = annotator.annotate(Collections.singletonList(entity12));
		assertTrue(results.hasNext());
		Entity resultEntity = results.next();
		assertFalse(results.hasNext());

		assertEquals(resultEntity.get(GONL_GENOME_AF), "0.9969879518072289");
		assertEquals(resultEntity.get(GONL_GENOME_GTC), "0|3|495");
	}

	@Test
	public void testAnnotate13()
	{
		Entity entity13 = new DynamicEntity(emd);
		entity13.set(VcfAttributes.CHROM, "1");
		entity13.set(VcfAttributes.POS, 1115548);
		entity13.set(VcfAttributes.REF, "G");
		entity13.set(VcfAttributes.ALT, "A");

		Iterator<Entity> results = annotator.annotate(Collections.singletonList(entity13));
		assertTrue(results.hasNext());
		Entity resultEntity = results.next();
		assertFalse(results.hasNext());

		assertEquals(resultEntity.get(GONL_GENOME_AF), "0.02610441767068273");
		assertEquals(resultEntity.get(GONL_GENOME_GTC), "472|26|0");
	}

	@Test
	public void testAnnotate14()
	{
		Entity entity14 = new DynamicEntity(emd);
		entity14.set(VcfAttributes.CHROM, "21");
		entity14.set(VcfAttributes.POS, 45650009);
		entity14.set(VcfAttributes.REF, "T");
		entity14.set(VcfAttributes.ALT, "TG, A, G");

		Iterator<Entity> results = annotator.annotate(Collections.singletonList(entity14));
		assertTrue(results.hasNext());
		Entity resultEntity = results.next();
		assertFalse(results.hasNext());

		assertEquals(resultEntity.get(GONL_GENOME_AF), "0.22188755020080322,.,.");
		assertEquals(resultEntity.get(GONL_GENOME_GTC), "306|163|29,.,.");
	}

	/**
	 * Test for bugfix where alt allele in resource was trimmed too much causing index out of bounds.
	 */
	@Test
	public void testAnnotate15()
	{
		Entity entity14 = new DynamicEntity(emd);
		entity14.set(VcfAttributes.CHROM, "1");
		entity14.set(VcfAttributes.POS, 28227207);
		entity14.set(VcfAttributes.REF, "A");
		entity14.set(VcfAttributes.ALT, "G");

		Iterator<Entity> results = annotator.annotate(Collections.singletonList(entity14));
		assertTrue(results.hasNext());
		Entity resultEntity = results.next();
		assertFalse(results.hasNext());

		assertNull(resultEntity.get(GONL_GENOME_AF));
		assertNull(resultEntity.get(GONL_GENOME_GTC));
	}

	@Configuration
	@Import({ VcfTestConfig.class })
	public static class Config
	{
		@Bean
		public Entity goNLAnnotatorSettings()
		{
			Entity settings = mock(Entity.class);
			when(settings.getString(GoNLAnnotatorSettings.Meta.ROOT_DIRECTORY)).thenReturn(
					ResourceUtils.getFile(getClass(), GONL_TEST_ROOT_DIRECTORY).getPath());
			when(settings.getString(GoNLAnnotatorSettings.Meta.CHROMOSOMES)).thenReturn(GONL_TEST_CHROMOSOMES);
			when(settings.getString(GoNLAnnotatorSettings.Meta.FILEPATTERN)).thenReturn(GONL_TEST_PATTERN);
			when(settings.getString(GoNLAnnotatorSettings.Meta.OVERRIDE_CHROMOSOME_FILES)).thenReturn(
					GONL_TEST_OVERRIDE_CHROMOSOME_FILES_PROPERTY);

			return settings;
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

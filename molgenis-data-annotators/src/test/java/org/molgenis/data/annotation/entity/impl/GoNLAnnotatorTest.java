package org.molgenis.data.annotation.entity.impl;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.annotation.AnnotationService;
import org.molgenis.data.annotation.RepositoryAnnotator;
import org.molgenis.data.annotation.resources.Resources;
import org.molgenis.data.annotation.resources.impl.ResourcesImpl;
import org.molgenis.data.annotator.websettings.GoNLAnnotatorSettings;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.vcf.VcfRepository;
import org.molgenis.util.ResourceUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@ContextConfiguration(classes =
{ GoNLAnnotatorTest.Config.class, GoNLAnnotator.class })
public class GoNLAnnotatorTest extends AbstractTestNGSpringContextTests
{
	private final static String GONL_TEST_PATTERN = "gonl.chr%s.snps_indels.r5.vcf.gz";
	private final static String GONL_TEST_ROOT_DIRECTORY = "/gonl";
	private final static String GONL_TEST_CHROMOSOMES = "1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,X";
	private final static String GONL_TEST_OVERRIDE_CHROMOSOME_FILES_PROPERTY = "X:gonl.chrX.release4.gtc.vcf.gz";

	@Autowired
	RepositoryAnnotator annotator;
	private DefaultEntityMetaData emd;

	@BeforeClass
	public void beforeClass() throws IOException
	{
		emd = new DefaultEntityMetaData("gonl");
		emd.addAttribute(VcfRepository.CHROM).setIdAttribute(true).setNillable(false);
		emd.addAttributeMetaData(VcfRepository.POS_META);
		emd.addAttributeMetaData(VcfRepository.REF_META);
		emd.addAttributeMetaData(VcfRepository.ALT_META);
	}

	// 14 tests below are test cases from the "test-edgecases.vcf"
	@Test
	public void testAnnotate1()
	{
		Entity entity1 = new MapEntity(emd);
		entity1.set(VcfRepository.CHROM, "1");
		entity1.set(VcfRepository.POS, 13380);
		entity1.set(VcfRepository.REF, "C");
		entity1.set(VcfRepository.ALT, "G");

		Iterator<Entity> results = annotator.annotate(Collections.singletonList(entity1));
		assertTrue(results.hasNext());
		Entity resultEntity = results.next();
		assertFalse(results.hasNext());

		Entity expectedEntity = new MapEntity("expected");
		expectedEntity.set(GoNLAnnotator.GONL_GENOME_AF, null);
		expectedEntity.set(GoNLAnnotator.GONL_GENOME_GTC, null);

		assertEquals(resultEntity.get(GoNLAnnotator.GONL_GENOME_AF), expectedEntity.get(GoNLAnnotator.GONL_GENOME_AF));
		assertEquals(resultEntity.get(GoNLAnnotator.GONL_GENOME_GTC),
				expectedEntity.get(GoNLAnnotator.GONL_GENOME_GTC));
	}

	@Test
	public void testAnnotate2()
	{
		Entity entity2 = new MapEntity(emd);
		entity2.set(VcfRepository.CHROM, "1");
		entity2.set(VcfRepository.POS, 13980);
		entity2.set(VcfRepository.REF, "T");
		entity2.set(VcfRepository.ALT, "C");

		Iterator<Entity> results = annotator.annotate(Collections.singletonList(entity2));
		assertTrue(results.hasNext());
		Entity resultEntity = results.next();
		assertFalse(results.hasNext());

		Entity expectedEntity = new MapEntity("expected");
		expectedEntity.set(GoNLAnnotator.GONL_GENOME_AF, "0.013052208835341365");
		expectedEntity.set(GoNLAnnotator.GONL_GENOME_GTC, "485,13,0");

		assertEquals(resultEntity.get(GoNLAnnotator.GONL_GENOME_AF), expectedEntity.get(GoNLAnnotator.GONL_GENOME_AF));
		assertEquals(resultEntity.get(GoNLAnnotator.GONL_GENOME_GTC),
				expectedEntity.get(GoNLAnnotator.GONL_GENOME_GTC));
	}

	@Test
	public void testAnnotate3()
	{
		Entity entity3 = new MapEntity(emd);
		entity3.set(VcfRepository.CHROM, "1");
		entity3.set(VcfRepository.POS, 78383467);
		entity3.set(VcfRepository.REF, "G");
		entity3.set(VcfRepository.ALT, "A");

		Iterator<Entity> results = annotator.annotate(Collections.singletonList(entity3));
		assertTrue(results.hasNext());
		Entity resultEntity = results.next();
		assertFalse(results.hasNext());

		Entity expectedEntity = new MapEntity("expected");
		expectedEntity.set(GoNLAnnotator.GONL_GENOME_AF, "0.8674698795180723");
		expectedEntity.set(GoNLAnnotator.GONL_GENOME_GTC, "6,120,372");

		assertEquals(resultEntity.get(GoNLAnnotator.GONL_GENOME_AF), expectedEntity.get(GoNLAnnotator.GONL_GENOME_AF));
		assertEquals(resultEntity.get(GoNLAnnotator.GONL_GENOME_GTC),
				expectedEntity.get(GoNLAnnotator.GONL_GENOME_GTC));
	}

	@Test
	public void testAnnotate4()
	{
		Entity entity4 = new MapEntity(emd);
		entity4.set(VcfRepository.CHROM, "1");
		entity4.set(VcfRepository.POS, 231094050);
		entity4.set(VcfRepository.REF, "GAA");
		entity4.set(VcfRepository.ALT, "G,GAAA,GA");

		Iterator<Entity> results = annotator.annotate(Collections.singletonList(entity4));
		assertTrue(results.hasNext());
		Entity resultEntity = results.next();
		assertFalse(results.hasNext());

		Entity expectedEntity = new MapEntity("expected");
		expectedEntity.set(GoNLAnnotator.GONL_GENOME_AF, "||0.015136226034308779");
		expectedEntity.set(GoNLAnnotator.GONL_GENOME_GTC, "||7,11,33");

		assertEquals(resultEntity.get(GoNLAnnotator.GONL_GENOME_AF), expectedEntity.get(GoNLAnnotator.GONL_GENOME_AF));
		assertEquals(resultEntity.get(GoNLAnnotator.GONL_GENOME_GTC),
				expectedEntity.get(GoNLAnnotator.GONL_GENOME_GTC));
	}

	@Test
	public void testAnnotate5()
	{
		Entity entity5 = new MapEntity(emd);
		entity5.set(VcfRepository.CHROM, "2");
		entity5.set(VcfRepository.POS, 171570151);
		entity5.set(VcfRepository.REF, "C");
		entity5.set(VcfRepository.ALT, "T");

		Iterator<Entity> results = annotator.annotate(Collections.singletonList(entity5));
		assertTrue(results.hasNext());
		Entity resultEntity = results.next();
		assertFalse(results.hasNext());

		Entity expectedEntity = new MapEntity("expected");
		expectedEntity.set(GoNLAnnotator.GONL_GENOME_AF, "0.30823293172690763");
		expectedEntity.set(GoNLAnnotator.GONL_GENOME_GTC, "241,207,50");

		assertEquals(resultEntity.get(GoNLAnnotator.GONL_GENOME_AF), expectedEntity.get(GoNLAnnotator.GONL_GENOME_AF));
		assertEquals(resultEntity.get(GoNLAnnotator.GONL_GENOME_GTC),
				expectedEntity.get(GoNLAnnotator.GONL_GENOME_GTC));
	}

	@Test
	public void testAnnotate6()
	{
		Entity entity6 = new MapEntity(emd);
		entity6.set(VcfRepository.CHROM, "4");
		entity6.set(VcfRepository.POS, 69964234);
		entity6.set(VcfRepository.REF, "CT");
		entity6.set(VcfRepository.ALT, "CTT,CTTT,C");

		Iterator<Entity> results = annotator.annotate(Collections.singletonList(entity6));
		assertTrue(results.hasNext());
		Entity resultEntity = results.next();
		assertFalse(results.hasNext());

		Entity expectedEntity = new MapEntity("expected");
		expectedEntity.set(GoNLAnnotator.GONL_GENOME_AF, "0.9969909729187563||");
		expectedEntity.set(GoNLAnnotator.GONL_GENOME_GTC, "1,4,496||");

		assertEquals(resultEntity.get(GoNLAnnotator.GONL_GENOME_AF), expectedEntity.get(GoNLAnnotator.GONL_GENOME_AF));
		assertEquals(resultEntity.get(GoNLAnnotator.GONL_GENOME_GTC),
				expectedEntity.get(GoNLAnnotator.GONL_GENOME_GTC));
	}

	@Test
	public void testAnnotate7()
	{
		Entity entity7 = new MapEntity(emd);
		entity7.set(VcfRepository.CHROM, "15");
		entity7.set(VcfRepository.POS, 66641732);
		entity7.set(VcfRepository.REF, "G");
		entity7.set(VcfRepository.ALT, "A,C,T");

		Iterator<Entity> results = annotator.annotate(Collections.singletonList(entity7));
		assertTrue(results.hasNext());
		Entity resultEntity = results.next();
		assertFalse(results.hasNext());

		Entity expectedEntity = new MapEntity("expected");
		expectedEntity.set(GoNLAnnotator.GONL_GENOME_AF, "|0.09538152610441768|");
		expectedEntity.set(GoNLAnnotator.GONL_GENOME_GTC, "|412,77,9|");

		assertEquals(resultEntity.get(GoNLAnnotator.GONL_GENOME_AF), expectedEntity.get(GoNLAnnotator.GONL_GENOME_AF));
		assertEquals(resultEntity.get(GoNLAnnotator.GONL_GENOME_GTC),
				expectedEntity.get(GoNLAnnotator.GONL_GENOME_GTC));
	}

	@Test
	public void testAnnotate8()
	{
		Entity entity8 = new MapEntity(emd);
		entity8.set(VcfRepository.CHROM, "21");
		entity8.set(VcfRepository.POS, 46924425);
		entity8.set(VcfRepository.REF, "CGGCCCCCCA");
		entity8.set(VcfRepository.ALT, "C");

		Iterator<Entity> results = annotator.annotate(Collections.singletonList(entity8));
		assertTrue(results.hasNext());
		Entity resultEntity = results.next();
		assertFalse(results.hasNext());

		Entity expectedEntity = new MapEntity("expected");
		expectedEntity.set(GoNLAnnotator.GONL_GENOME_AF, null);
		expectedEntity.set(GoNLAnnotator.GONL_GENOME_GTC, null);

		assertEquals(resultEntity.get(GoNLAnnotator.GONL_GENOME_AF), expectedEntity.get(GoNLAnnotator.GONL_GENOME_AF));
		assertEquals(resultEntity.get(GoNLAnnotator.GONL_GENOME_GTC),
				expectedEntity.get(GoNLAnnotator.GONL_GENOME_GTC));
	}

	@Test
	public void testAnnotate9()
	{
		Entity entity9 = new MapEntity(emd);
		entity9.set(VcfRepository.CHROM, "X");
		entity9.set(VcfRepository.POS, 79943569);
		entity9.set(VcfRepository.REF, "T");
		entity9.set(VcfRepository.ALT, "C");

		Iterator<Entity> results = annotator.annotate(Collections.singletonList(entity9));
		assertTrue(results.hasNext());
		Entity resultEntity = results.next();
		assertFalse(results.hasNext());

		Entity expectedEntity = new MapEntity("expected");
		expectedEntity.set(GoNLAnnotator.GONL_GENOME_AF, "0.9989939637826962");
		expectedEntity.set(GoNLAnnotator.GONL_GENOME_GTC, "0,1,496");

		assertEquals(resultEntity.get(GoNLAnnotator.GONL_GENOME_AF), expectedEntity.get(GoNLAnnotator.GONL_GENOME_AF));
		assertEquals(resultEntity.get(GoNLAnnotator.GONL_GENOME_GTC),
				expectedEntity.get(GoNLAnnotator.GONL_GENOME_GTC));
	}

	@Test
	public void testAnnotate10()
	{
		Entity entity10 = new MapEntity(emd);
		entity10.set(VcfRepository.CHROM, "2");
		entity10.set(VcfRepository.POS, 191904021);
		entity10.set(VcfRepository.REF, "G");
		entity10.set(VcfRepository.ALT, "T");

		Iterator<Entity> results = annotator.annotate(Collections.singletonList(entity10));
		assertTrue(results.hasNext());
		Entity resultEntity = results.next();
		assertFalse(results.hasNext());

		Entity expectedEntity = new MapEntity("expected");
		expectedEntity.set(GoNLAnnotator.GONL_GENOME_AF, "0.0030120481927710845");
		expectedEntity.set(GoNLAnnotator.GONL_GENOME_GTC, "495,3,0");

		assertEquals(resultEntity.get(GoNLAnnotator.GONL_GENOME_AF), expectedEntity.get(GoNLAnnotator.GONL_GENOME_AF));
		assertEquals(resultEntity.get(GoNLAnnotator.GONL_GENOME_GTC),
				expectedEntity.get(GoNLAnnotator.GONL_GENOME_GTC));
	}

	@Test
	public void testAnnotate11()
	{
		Entity entity11 = new MapEntity(emd);
		entity11.set(VcfRepository.CHROM, "3");
		entity11.set(VcfRepository.POS, 53219680);
		entity11.set(VcfRepository.REF, "G");
		entity11.set(VcfRepository.ALT, "C");

		Iterator<Entity> results = annotator.annotate(Collections.singletonList(entity11));
		assertTrue(results.hasNext());
		Entity resultEntity = results.next();
		assertFalse(results.hasNext());

		Entity expectedEntity = new MapEntity("expected");
		expectedEntity.set(GoNLAnnotator.GONL_GENOME_AF, null);
		expectedEntity.set(GoNLAnnotator.GONL_GENOME_GTC, null);

		assertEquals(resultEntity.get(GoNLAnnotator.GONL_GENOME_AF), expectedEntity.get(GoNLAnnotator.GONL_GENOME_AF));
		assertEquals(resultEntity.get(GoNLAnnotator.GONL_GENOME_GTC),
				expectedEntity.get(GoNLAnnotator.GONL_GENOME_GTC));
	}

	@Test
	public void testAnnotate12()
	{
		Entity entity12 = new MapEntity(emd);
		entity12.set(VcfRepository.CHROM, "2");
		entity12.set(VcfRepository.POS, 219142023);
		entity12.set(VcfRepository.REF, "G");
		entity12.set(VcfRepository.ALT, "A");

		Iterator<Entity> results = annotator.annotate(Collections.singletonList(entity12));
		assertTrue(results.hasNext());
		Entity resultEntity = results.next();
		assertFalse(results.hasNext());

		Entity expectedEntity = new MapEntity("expected");
		expectedEntity.set(GoNLAnnotator.GONL_GENOME_AF, "0.9969879518072289");
		expectedEntity.set(GoNLAnnotator.GONL_GENOME_GTC, "0,3,495");

		assertEquals(resultEntity.get(GoNLAnnotator.GONL_GENOME_AF), expectedEntity.get(GoNLAnnotator.GONL_GENOME_AF));
		assertEquals(resultEntity.get(GoNLAnnotator.GONL_GENOME_GTC),
				expectedEntity.get(GoNLAnnotator.GONL_GENOME_GTC));
	}

	@Test
	public void testAnnotate13()
	{
		Entity entity13 = new MapEntity(emd);
		entity13.set(VcfRepository.CHROM, "1");
		entity13.set(VcfRepository.POS, 1115548);
		entity13.set(VcfRepository.REF, "G");
		entity13.set(VcfRepository.ALT, "A");

		Iterator<Entity> results = annotator.annotate(Collections.singletonList(entity13));
		assertTrue(results.hasNext());
		Entity resultEntity = results.next();
		assertFalse(results.hasNext());

		Entity expectedEntity = new MapEntity("expected");
		expectedEntity.set(GoNLAnnotator.GONL_GENOME_AF, "0.02610441767068273");
		expectedEntity.set(GoNLAnnotator.GONL_GENOME_GTC, "472,26,0");

		assertEquals(resultEntity.get(GoNLAnnotator.GONL_GENOME_AF), expectedEntity.get(GoNLAnnotator.GONL_GENOME_AF));
		assertEquals(resultEntity.get(GoNLAnnotator.GONL_GENOME_GTC),
				expectedEntity.get(GoNLAnnotator.GONL_GENOME_GTC));
	}

	@Test
	public void testAnnotate14()
	{
		Entity entity14 = new MapEntity(emd);
		entity14.set(VcfRepository.CHROM, "21");
		entity14.set(VcfRepository.POS, 45650009);
		entity14.set(VcfRepository.REF, "T");
		entity14.set(VcfRepository.ALT, "TG, A, G");

		Iterator<Entity> results = annotator.annotate(Collections.singletonList(entity14));
		assertTrue(results.hasNext());
		Entity resultEntity = results.next();
		assertFalse(results.hasNext());

		Entity expectedEntity = new MapEntity("expected");
		expectedEntity.set(GoNLAnnotator.GONL_GENOME_AF, "0.22188755020080322||");
		expectedEntity.set(GoNLAnnotator.GONL_GENOME_GTC, "306,163,29||");

		assertEquals(resultEntity.get(GoNLAnnotator.GONL_GENOME_AF), expectedEntity.get(GoNLAnnotator.GONL_GENOME_AF));
		assertEquals(resultEntity.get(GoNLAnnotator.GONL_GENOME_GTC),
				expectedEntity.get(GoNLAnnotator.GONL_GENOME_GTC));
	}

	public static class Config
	{
		@Autowired
		private DataService dataService;

		@Bean
		public Entity goNLAnnotatorSettings()
		{
			Entity settings = new MapEntity();

			settings.set(GoNLAnnotatorSettings.Meta.ROOT_DIRECTORY,
					ResourceUtils.getFile(getClass(), GONL_TEST_ROOT_DIRECTORY).getPath());
			settings.set(GoNLAnnotatorSettings.Meta.CHROMOSOMES, GONL_TEST_CHROMOSOMES);
			settings.set(GoNLAnnotatorSettings.Meta.FILEPATTERN, GONL_TEST_PATTERN);
			settings.set(GoNLAnnotatorSettings.Meta.OVERRIDE_CHROMOSOME_FILES,
					GONL_TEST_OVERRIDE_CHROMOSOME_FILES_PROPERTY);

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

package org.molgenis.data.annotation.entity.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.annotation.AnnotationService;
import org.molgenis.data.annotation.RepositoryAnnotator;
import org.molgenis.data.annotation.resources.Resources;
import org.molgenis.data.annotation.resources.impl.MultiResourceConfigImpl;
import org.molgenis.data.annotation.resources.impl.ResourcesImpl;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.vcf.VcfRepository;
import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.util.ResourceUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@ContextConfiguration(classes =
{ GoNLAnnotatorTest.Config.class, GoNLAnnotator.class })
public class GoNLAnnotatorTest extends AbstractTestNGSpringContextTests
{
	private final static String GONL_TEST_PATTERN = "gonl.chr%s.snps_indels.r5.vcf.gz";
	private final static String GONL_TEST_ROOT_DIRECTORY = "/gonl";
	private final static String GONL_TEST_CHROMOSOMES = "1";
	
	@Autowired
	RepositoryAnnotator annotator;
	
	@BeforeMethod
	public void beforeMethod() throws IOException
	{
		// ENTITY1.SET(VCFREPOSITORY.CHROM, "1");
		// ENTITY1.SET(VCFREPOSITORY.POS, 126108);
		// ENTITY1.SET(VCFREPOSITORY.REF, "G");
		// ENTITY1.SET(VCFREPOSITORY.ALT, "A");
		// INPUT1.ADD(ENTITY1);
		//
		// ENTITY2.SET(VCFREPOSITORY.CHROM, "1");
		// ENTITY2.SET(VCFREPOSITORY.POS, 123456);
		// ENTITY2.SET(VCFREPOSITORY.REF, "G");
		// ENTITY2.SET(VCFREPOSITORY.ALT, "A");
		// INPUT2.ADD(ENTITY2);
	}

	@Test
	public void testAnnotate()
	{
		// TEST FILE
		//
		// #CHROM POS ID REF ALT QUAL FILTER INFO
		// 1 126108 . G A . Inaccessible AC=3;AN=996;GTC=495,3,0;set=SNP
		// 1 126118 . G A . Inaccessible AC=1;AN=996;GTC=497,1,0;set=SNP
		//
		
		DefaultEntityMetaData emdIn = new DefaultEntityMetaData("gonl");
		emdIn.addAttribute(VcfRepository.CHROM).setIdAttribute(true).setNillable(false);
		emdIn.addAttributeMetaData(VcfRepository.POS_META);
		emdIn.addAttributeMetaData(VcfRepository.REF_META);
		emdIn.addAttributeMetaData(VcfRepository.ALT_META);

		Entity inputEntity = new MapEntity(emdIn);
		inputEntity.set(VcfRepository.CHROM, "1");
		inputEntity.set(VcfRepository.POS, 126108);
		inputEntity.set(VcfRepository.REF, "G");
		inputEntity.set(VcfRepository.ALT, "A");

		Iterator<Entity> results = annotator.annotate(Collections.singletonList(inputEntity));
		assertTrue(results.hasNext());
		Entity resultEntity = results.next();
		assertFalse(results.hasNext());

		Map<String, Object> expectedMap = new LinkedHashMap<String, Object>();
		expectedMap.put(VcfRepository.CHROM, "1");
		expectedMap.put(VcfRepository.POS, 126108);
		expectedMap.put(VcfRepository.REF, "G");
		expectedMap.put(VcfRepository.ALT, "A");
		expectedMap.put(GoNLAnnotator.GONL_GENOME_AF, "0.0030120481927710845");
		expectedMap.put(GoNLAnnotator.GONL_GENOME_GTC, "495,3,0");
		Entity expectedEntity = new MapEntity(expectedMap);

		assertEquals(resultEntity.get(VcfRepository.CHROM), expectedEntity.get(VcfRepository.CHROM));
		assertEquals(resultEntity.get(VcfRepository.POS), expectedEntity.get(VcfRepository.POS));
		assertEquals(resultEntity.get(VcfRepository.REF), expectedEntity.get(VcfRepository.REF));
		assertEquals(resultEntity.get(VcfRepository.ALT), expectedEntity.get(VcfRepository.ALT));
		assertEquals(resultEntity.get(GoNLAnnotator.GONL_GENOME_AF), expectedEntity.get(GoNLAnnotator.GONL_GENOME_AF));
		assertEquals(resultEntity.get(GoNLAnnotator.GONL_GENOME_GTC), expectedEntity.get(GoNLAnnotator.GONL_GENOME_GTC));
	}

	@Configuration
	public static class Config
	{
		@Autowired
		private DataService dataService;

		@Bean
		public MolgenisSettings molgenisSettings()
		{
			MolgenisSettings settings = mock(MolgenisSettings.class);
			when(
					settings.getProperty(GoNLAnnotator.GONL_ROOT_DIRECTORY_PROPERTY,
							MultiResourceConfigImpl.DEFAULT_ROOT_DIRECTORY)).thenReturn(
					ResourceUtils.getFile(getClass(), GoNLAnnotatorTest.GONL_TEST_ROOT_DIRECTORY).getPath());
			when(
					settings.getProperty(GoNLAnnotator.GONL_CHROMOSOME_PROPERTY,
							MultiResourceConfigImpl.DEFAULT_CHROMOSOMES)).thenReturn(
					GoNLAnnotatorTest.GONL_TEST_CHROMOSOMES);
			when(
					settings.getProperty(GoNLAnnotator.GONL_FILE_PATTERN_PROPERTY,
							MultiResourceConfigImpl.DEFAULT_PATTERN)).thenReturn(GoNLAnnotatorTest.GONL_TEST_PATTERN);
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

package org.molgenis.data.annotation.entity.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

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

	@Test
	public void testAnnotate()
	{
		DefaultEntityMetaData emdIn = new DefaultEntityMetaData("gonl");
		emdIn.addAttribute(VcfRepository.CHROM).setIdAttribute(true).setNillable(false);
		emdIn.addAttributeMetaData(VcfRepository.POS_META);
		emdIn.addAttributeMetaData(VcfRepository.REF_META);
		emdIn.addAttributeMetaData(VcfRepository.ALT_META);

		Entity inputEntity = new MapEntity(emdIn);
		inputEntity.set(VcfRepository.CHROM, "1");
		inputEntity.set(VcfRepository.POS, 249239510);
		inputEntity.set(VcfRepository.REF, "C");
		inputEntity.set(VcfRepository.ALT, "G");

		Iterator<Entity> results = annotator.annotate(Collections.singletonList(inputEntity));
		assertTrue(results.hasNext());
		Entity resultEntity = results.next();
		assertFalse(results.hasNext());

		Map<String, Object> expectedMap = new LinkedHashMap<String, Object>();
		expectedMap.put(VcfRepository.CHROM, "1");
		expectedMap.put(VcfRepository.POS, 249239510);
		expectedMap.put(VcfRepository.REF, "C");
		expectedMap.put(VcfRepository.ALT, "G");
		expectedMap.put(GoNLAnnotator.GONL_GENOME_AF, "0.010040160642570281");
		expectedMap.put(GoNLAnnotator.GONL_GENOME_GTC, "488,10,0");
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
					ResourceUtils.getFile(getClass(), GONL_TEST_ROOT_DIRECTORY).getPath());

			when(
					settings.getProperty(GoNLAnnotator.GONL_CHROMOSOME_PROPERTY,
							MultiResourceConfigImpl.DEFAULT_CHROMOSOMES)).thenReturn(GONL_TEST_CHROMOSOMES);

			when(
					settings.getProperty(GoNLAnnotator.GONL_FILE_PATTERN_PROPERTY,
							MultiResourceConfigImpl.DEFAULT_PATTERN)).thenReturn(GONL_TEST_PATTERN);

			when(settings.getProperty(GoNLAnnotator.GONL_OVERRIDE_CHROMOSOME_FILES_PROPERTY)).thenReturn(
					GoNLAnnotatorTest.GONL_TEST_OVERRIDE_CHROMOSOME_FILES_PROPERTY);

			when(settings.propertyExists(GoNLAnnotator.GONL_OVERRIDE_CHROMOSOME_FILES_PROPERTY)).thenReturn(true);

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

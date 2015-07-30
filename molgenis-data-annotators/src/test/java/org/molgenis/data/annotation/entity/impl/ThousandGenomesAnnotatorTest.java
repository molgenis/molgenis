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
{ ThousandGenomesAnnotatorTest.Config.class, ThousandGenomesAnnotator.class })
public class ThousandGenomesAnnotatorTest extends AbstractTestNGSpringContextTests
{
	private final static String THOUSAND_GENOME_TEST_PATTERN = "ALL.chr%s.phase3_shapeit2_mvncall_integrated_v5.20130502.genotypes.vcf.gz";
	private final static String THOUSAND_GENOME_TEST_FOLDER_PROPERTY = "/1000g";
	private final static String THOUSAND_GENOME_TEST_CHROMOSOMES = "1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,Y,X";
	private final static String THOUSAND_GENOME_TEST_OVERRIDE_CHROMOSOME_FILES_PROPERTY = "X:ALL.chrX.phase3_shapeit2_mvncall_integrated.20130502.genotypes.vcf.gz,Y:ALL.chrY.phase3_integrated.20130502.genotypes.vcf.gz";

	@Autowired
	RepositoryAnnotator annotator;

	@Test
	public void testAnnotate()
	{
		DefaultEntityMetaData emdIn = new DefaultEntityMetaData("test");
		emdIn.addAttribute(VcfRepository.CHROM).setIdAttribute(true).setNillable(false);
		emdIn.addAttributeMetaData(VcfRepository.POS_META);
		emdIn.addAttributeMetaData(VcfRepository.REF_META);
		emdIn.addAttributeMetaData(VcfRepository.ALT_META);

		Entity inputEntity = new MapEntity(emdIn);
		inputEntity.set(VcfRepository.CHROM, "1");
		inputEntity.set(VcfRepository.POS, 249240543);
		inputEntity.set(VcfRepository.REF, "AGG");
		inputEntity.set(VcfRepository.ALT, "A");

		Iterator<Entity> results = annotator.annotate(Collections.singletonList(inputEntity));
		assertTrue(results.hasNext());
		Entity resultEntity = results.next();
		assertFalse(results.hasNext());

		Map<String, Object> expectedMap = new LinkedHashMap<String, Object>();
		expectedMap.put(VcfRepository.CHROM, "1");
		expectedMap.put(VcfRepository.POS, 249240543);
		expectedMap.put(VcfRepository.REF, "AGG");
		expectedMap.put(VcfRepository.ALT, "A");
		expectedMap.put(ThousandGenomesAnnotator.THOUSAND_GENOME_AF, "0.61861");
		Entity expectedEntity = new MapEntity(expectedMap);

		assertEquals(resultEntity.get(VcfRepository.CHROM), expectedEntity.get(VcfRepository.CHROM));
		assertEquals(resultEntity.get(VcfRepository.POS), expectedEntity.get(VcfRepository.POS));
		assertEquals(resultEntity.get(VcfRepository.REF), expectedEntity.get(VcfRepository.REF));
		assertEquals(resultEntity.get(VcfRepository.ALT), expectedEntity.get(VcfRepository.ALT));
		assertEquals(resultEntity.get(ThousandGenomesAnnotator.THOUSAND_GENOME_AF),
				expectedEntity.get(ThousandGenomesAnnotator.THOUSAND_GENOME_AF));
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
					settings.getProperty(ThousandGenomesAnnotator.THOUSAND_GENOME_FOLDER_PROPERTY,
							MultiResourceConfigImpl.DEFAULT_ROOT_DIRECTORY)).thenReturn(
					ResourceUtils.getFile(getClass(), THOUSAND_GENOME_TEST_FOLDER_PROPERTY).getPath());

			when(
					settings.getProperty(ThousandGenomesAnnotator.THOUSAND_GENOME_CHROMOSOME_PROPERTY,
							MultiResourceConfigImpl.DEFAULT_CHROMOSOMES)).thenReturn(THOUSAND_GENOME_TEST_CHROMOSOMES);

			when(
					settings.getProperty(ThousandGenomesAnnotator.THOUSAND_GENOME_FILE_PATTERN_PROPERTY,
							MultiResourceConfigImpl.DEFAULT_PATTERN)).thenReturn(THOUSAND_GENOME_TEST_PATTERN);

			when(settings.getProperty(ThousandGenomesAnnotator.THOUSAND_GENOME_OVERRIDE_CHROMOSOME_FILES_PROPERTY))
					.thenReturn(THOUSAND_GENOME_TEST_OVERRIDE_CHROMOSOME_FILES_PROPERTY);

			when(settings.propertyExists(ThousandGenomesAnnotator.THOUSAND_GENOME_OVERRIDE_CHROMOSOME_FILES_PROPERTY))
					.thenReturn(true);

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

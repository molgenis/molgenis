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
		// entity1.set(VcfRepository.CHROM, "1");
		// entity1.set(VcfRepository.POS, 126108);
		// entity1.set(VcfRepository.REF, "G");
		// entity1.set(VcfRepository.ALT, "A");
		// input1.add(entity1);
		//
		// entity2.set(VcfRepository.CHROM, "1");
		// entity2.set(VcfRepository.POS, 123456);
		// entity2.set(VcfRepository.REF, "G");
		// entity2.set(VcfRepository.ALT, "A");
		// input2.add(entity2);
	}


	public void testAnnotate()
	{
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
		expectedMap.put(GoNLAnnotator.GONL_AF_RESOURCE_ATTRIBUTE_NAME, 0.03714859437751004);
		Entity expectedEntity = new MapEntity(expectedMap);

		assertEquals(resultEntity.get(VcfRepository.CHROM), expectedEntity.get(VcfRepository.CHROM));
		assertEquals(resultEntity.get(VcfRepository.POS), expectedEntity.get(VcfRepository.POS));
		assertEquals(resultEntity.get(VcfRepository.REF), expectedEntity.get(VcfRepository.REF));
		assertEquals(resultEntity.get(VcfRepository.ALT), expectedEntity.get(VcfRepository.ALT));
		assertEquals(resultEntity.get(GoNLAnnotator.GONL_AF_RESOURCE_ATTRIBUTE_NAME),
				expectedEntity.get(GoNLAnnotator.GONL_AF_RESOURCE_ATTRIBUTE_NAME));
	}

	@Test
	public void testAnnotateNoResult()
	{
		// DefaultEntityMetaData annotatedMetadata = new DefaultEntityMetaData("test");
		// annotatedMetadata.addAttributeMetaData(attributeMetaDataCantAnnotateChrom);
		// annotatedMetadata.addAttributeMetaData(pos);
		// annotatedMetadata.addAttributeMetaData(ref);
		// annotatedMetadata.addAttributeMetaData(alt);
		// annotatedMetadata.setIdAttribute(attributeMetaDataCantAnnotateChrom.getName());
		// annotatedMetadata.addAttributeMetaData(new DefaultAttributeMetaData(
		// GoNLAnnotator.GONL_AF_RESOURCE_ATTRIBUTE_NAME,
		// FieldTypeEnum.DECIMAL));
		//
		// Iterator<Entity> results = annotator.annotate(input2);
		//
		// MapEntity mapEntity = new MapEntity(entity2, annotatedMetadata);
		// mapEntity.set(GoNLAnnotator.GONL_AF_RESOURCE_ATTRIBUTE_NAME, null);
		//
		// assertEquals(results.next(), mapEntity);
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

package org.molgenis.data.annotation.entity.impl;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.annotation.AnnotationService;
import org.molgenis.data.annotation.RepositoryAnnotator;
import org.molgenis.data.annotation.resources.Resources;
import org.molgenis.data.annotation.resources.impl.ResourcesImpl;
import org.molgenis.data.annotator.websettings.DannAnnotatorSettings;
import org.molgenis.data.support.DefaultAttributeMetaData;
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
{ DannAnnotatorTest.Config.class, DannAnnotator.class })
public class DannAnnotatorTest extends AbstractTestNGSpringContextTests
{
	@Autowired
	RepositoryAnnotator annotator;

	@Autowired
	Resources resourcess;

	// Can annotate
	public DefaultEntityMetaData metaDataCanAnnotate = new DefaultEntityMetaData("test");

	// Negative test cannot annotate
	public DefaultEntityMetaData metaDataCantAnnotate = new DefaultEntityMetaData("test");

	public ArrayList<Entity> input1;
	public ArrayList<Entity> input2;
	public ArrayList<Entity> input3;
	public ArrayList<Entity> input4;
	public static Entity entity;
	public static Entity entity1;
	public static Entity entity2;
	public static Entity entity3;
	public static Entity entity4;

	public ArrayList<Entity> entities;

	public void setValues()
	{
		AttributeMetaData attributeMetaDataChrom = new DefaultAttributeMetaData(VcfRepository.CHROM,
				MolgenisFieldTypes.FieldTypeEnum.STRING);
		AttributeMetaData attributeMetaDataPos = new DefaultAttributeMetaData(VcfRepository.POS,
				MolgenisFieldTypes.FieldTypeEnum.LONG);
		AttributeMetaData attributeMetaDataRef = new DefaultAttributeMetaData(VcfRepository.REF,
				MolgenisFieldTypes.FieldTypeEnum.TEXT);
		AttributeMetaData attributeMetaDataAlt = new DefaultAttributeMetaData(VcfRepository.ALT,
				MolgenisFieldTypes.FieldTypeEnum.TEXT);

		metaDataCanAnnotate.addAttributeMetaData(attributeMetaDataChrom);
		metaDataCanAnnotate.setIdAttribute(attributeMetaDataChrom.getName());
		metaDataCanAnnotate.addAttributeMetaData(attributeMetaDataPos);
		metaDataCanAnnotate.addAttributeMetaData(attributeMetaDataRef);
		metaDataCanAnnotate.addAttributeMetaData(attributeMetaDataAlt);

		AttributeMetaData attributeMetaDataCantAnnotateChrom = new DefaultAttributeMetaData(VcfRepository.CHROM,
				MolgenisFieldTypes.FieldTypeEnum.LONG);

		metaDataCantAnnotate.addAttributeMetaData(attributeMetaDataCantAnnotateChrom);
		metaDataCantAnnotate.addAttributeMetaData(attributeMetaDataPos);
		metaDataCantAnnotate.addAttributeMetaData(attributeMetaDataRef);
		metaDataCantAnnotate.addAttributeMetaData(attributeMetaDataAlt);

		entity = new MapEntity(metaDataCanAnnotate);
		entity1 = new MapEntity(metaDataCanAnnotate);
		entity2 = new MapEntity(metaDataCanAnnotate);
		entity3 = new MapEntity(metaDataCanAnnotate);
		entity4 = new MapEntity(metaDataCanAnnotate);

		entities = new ArrayList<>();
		entities.add(entity);
	}

	@BeforeClass
	public void beforeClass() throws IOException
	{
		// Test file
		// 1 10001 T A 0.164613914
		// 1 10001 T C 0.439699405
		// 1 10001 T G 0.381086294

		input1 = new ArrayList<>();
		input2 = new ArrayList<>();
		input3 = new ArrayList<>();
		input4 = new ArrayList<>();

		setValues();

		entity1.set(VcfRepository.CHROM, "1");
		entity1.set(VcfRepository.POS, 10001L);
		entity1.set(VcfRepository.REF, "T");
		entity1.set(VcfRepository.ALT, "A");

		input1.add(entity1);

		entity2.set(VcfRepository.CHROM, "1");
		entity2.set(VcfRepository.POS, 10001L);
		entity2.set(VcfRepository.REF, "T");
		entity2.set(VcfRepository.ALT, "X");

		input2.add(entity2);

		entity3.set(VcfRepository.CHROM, "3");
		entity3.set(VcfRepository.POS, 10001L);
		entity3.set(VcfRepository.REF, "T");
		entity3.set(VcfRepository.ALT, "G");

		input3.add(entity3);

		entity4.set(VcfRepository.CHROM, "1");
		entity4.set(VcfRepository.POS, 10001L);
		entity4.set(VcfRepository.REF, "T");
		entity4.set(VcfRepository.ALT, "G");

		input4.add(entity4);
	}

	@Test
	public void testThreeOccurencesOneMatchEntity1()
	{
		this.testMatch(input1, 0.16461391399220135);
	}

	@Test
	public void testThreeOccurencesNoMatchEntity2()
	{
		this.testNoMatch(input2);

	}

	@Test
	public void testNoOccurencesNoMatchEntity3()
	{
		this.testNoMatch(input3);
	}

	@Test
	public void testThreeOccurencesOneMatchEntity4()
	{
		this.testMatch(input4, 0.38108629377072734);
	}

	private void testMatch(List<Entity> inputToAnnotate, Object dannScore)
	{
		List<Entity> expectedList = new ArrayList<Entity>();
		Map<String, Object> resultMap = new LinkedHashMap<String, Object>();

		resultMap.put(DannAnnotator.DANN_SCORE, dannScore);

		Entity expectedEntity = new MapEntity(resultMap);
		expectedList.add(expectedEntity);

		Iterator<Entity> results = annotator.annotate(inputToAnnotate);

		Entity resultEntity = results.next();

		assertEquals(resultEntity.get(DannAnnotator.DANN_SCORE), expectedEntity.get(DannAnnotator.DANN_SCORE));
	}

	private void testNoMatch(List<Entity> inputToAnnotate)
	{
		Iterator<Entity> results = annotator.annotate(inputToAnnotate);
		Entity resultEntity = results.next();
		assertEquals(resultEntity.get(DannAnnotator.DANN_SCORE), null);
	}

	@Test
	public void canAnnotateTrueTest()
	{
		assertEquals(annotator.canAnnotate(metaDataCanAnnotate), "true");
	}

	@Test
	public void canAnnotateFalseTest()
	{
		assertEquals(annotator.canAnnotate(metaDataCantAnnotate), "a required attribute has the wrong datatype");
	}

	public static class Config
	{
		@Autowired
		private DataService dataService;

		@Bean
		public Entity dannAnnotatorSettings()
		{
			Entity settings = new MapEntity();
			settings.set(DannAnnotatorSettings.Meta.DANN_LOCATION,
					ResourceUtils.getFile(getClass(), "/dann/DANN_test_set.tsv.bgz").getPath());
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

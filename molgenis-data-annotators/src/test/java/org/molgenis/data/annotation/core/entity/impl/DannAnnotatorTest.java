package org.molgenis.data.annotation.core.entity.impl;

import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.annotation.core.RepositoryAnnotator;
import org.molgenis.data.annotation.core.entity.AnnotatorConfig;
import org.molgenis.data.annotation.core.resources.Resources;
import org.molgenis.data.annotation.core.resources.impl.ResourcesImpl;
import org.molgenis.data.annotation.web.AnnotationService;
import org.molgenis.data.annotation.web.settings.DannAnnotatorSettings;
import org.molgenis.data.meta.model.Attribute;
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
import java.util.ArrayList;
import java.util.Iterator;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.AttributeType.*;
import static org.molgenis.data.vcf.model.VcfAttributes.*;
import static org.testng.Assert.assertEquals;

@ContextConfiguration(classes = { DannAnnotatorTest.Config.class, DannAnnotator.class })
public class DannAnnotatorTest extends AbstractMolgenisSpringTest
{
	@Autowired
	ApplicationContext context;

	@Autowired
	AttributeFactory attributeFactory;

	@Autowired
	EntityTypeFactory entityTypeFactory;

	@Autowired
	VcfAttributes vcfAttributes;
	@Autowired
	RepositoryAnnotator annotator;

	@Autowired
	Resources resourcess;

	// Can annotate
	public EntityType metaDataCanAnnotate;

	// Negative test cannot annotate
	public EntityType metaDataCantAnnotate;

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
		Attribute attributeChrom = attributeFactory.create().setName(CHROM).setDataType(STRING);
		Attribute attributePos = attributeFactory.create().setName(POS).setDataType(INT);
		Attribute attributeRef = attributeFactory.create().setName(REF).setDataType(TEXT);
		Attribute attributeAlt = attributeFactory.create().setName(ALT).setDataType(TEXT);

		metaDataCanAnnotate.addAttribute(attributeChrom);
		metaDataCanAnnotate.addAttribute(attributePos);
		metaDataCanAnnotate.addAttribute(attributeRef);
		metaDataCanAnnotate.addAttribute(attributeAlt);
		metaDataCanAnnotate.addAttribute(attributeFactory.create().setName("DANN_SCORE").setDataType(STRING));

		Attribute attributeCantAnnotateChrom = attributeFactory.create().setName(CHROM).setDataType(LONG);

		metaDataCantAnnotate.addAttribute(attributeCantAnnotateChrom);
		metaDataCantAnnotate.addAttribute(attributePos);
		metaDataCantAnnotate.addAttribute(attributeRef);
		metaDataCantAnnotate.addAttribute(attributeAlt);

		entity = new DynamicEntity(metaDataCanAnnotate);
		entity1 = new DynamicEntity(metaDataCanAnnotate);
		entity2 = new DynamicEntity(metaDataCanAnnotate);
		entity3 = new DynamicEntity(metaDataCanAnnotate);
		entity4 = new DynamicEntity(metaDataCanAnnotate);

		entities = new ArrayList<>();
		entities.add(entity);
	}

	@BeforeClass
	public void beforeClass() throws IOException
	{
		// Can annotate
		metaDataCanAnnotate = entityTypeFactory.create("test");

		// Negative test cannot annotate
		metaDataCantAnnotate = entityTypeFactory.create("test");

		AnnotatorConfig annotatorConfig = context.getBean(AnnotatorConfig.class);
		annotatorConfig.init();
		// Test file
		// 1 10001 T A 0.164613914
		// 1 10001 T C 0.439699405
		// 1 10001 T G 0.381086294

		input1 = new ArrayList<>();
		input2 = new ArrayList<>();
		input3 = new ArrayList<>();
		input4 = new ArrayList<>();

		setValues();

		entity1.set(VcfAttributes.CHROM, "1");
		entity1.set(VcfAttributes.POS, 10001);
		entity1.set(VcfAttributes.REF, "T");
		entity1.set(VcfAttributes.ALT, "A");

		input1.add(entity1);

		entity2.set(VcfAttributes.CHROM, "1");
		entity2.set(VcfAttributes.POS, 10001);
		entity2.set(VcfAttributes.REF, "T");
		entity2.set(VcfAttributes.ALT, "X");

		input2.add(entity2);

		entity3.set(VcfAttributes.CHROM, "3");
		entity3.set(VcfAttributes.POS, 10001);
		entity3.set(VcfAttributes.REF, "T");
		entity3.set(VcfAttributes.ALT, "G");

		input3.add(entity3);

		entity4.set(VcfAttributes.CHROM, "1");
		entity4.set(VcfAttributes.POS, 10001);
		entity4.set(VcfAttributes.REF, "T");
		entity4.set(VcfAttributes.ALT, "G");

		input4.add(entity4);
	}

	@Test
	public void testThreeOccurencesOneMatchEntity1()
	{
		Iterator<Entity> results = annotator.annotate(input1);
		Entity resultEntity = results.next();
		assertEquals(resultEntity.get(DannAnnotator.DANN_SCORE), "0.16461391399220135");
	}

	@Test
	public void testThreeOccurencesNoMatchEntity2()
	{
		Iterator<Entity> results = annotator.annotate(input2);
		Entity resultEntity = results.next();
		assertEquals(resultEntity.get(DannAnnotator.DANN_SCORE), null);

	}

	@Test
	public void testNoOccurencesNoMatchEntity3()
	{
		Iterator<Entity> results = annotator.annotate(input3);
		Entity resultEntity = results.next();
		assertEquals(resultEntity.get(DannAnnotator.DANN_SCORE), null);
	}

	@Test
	public void testThreeOccurencesOneMatchEntity4()
	{
		Iterator<Entity> results = annotator.annotate(input4);
		Entity resultEntity = results.next();

		assertEquals(resultEntity.get(DannAnnotator.DANN_SCORE), "0.38108629377072734");
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

	@Configuration
	@Import({ VcfTestConfig.class })
	public static class Config
	{
		@Autowired
		private DataService dataService;

		@Bean
		public Entity dannAnnotatorSettings()
		{
			Entity settings = mock(Entity.class);
			when(settings.getString(DannAnnotatorSettings.Meta.DANN_LOCATION)).thenReturn(
					ResourceUtils.getFile(getClass(), "/dann/DANN_test_set.tsv.bgz").getPath());
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

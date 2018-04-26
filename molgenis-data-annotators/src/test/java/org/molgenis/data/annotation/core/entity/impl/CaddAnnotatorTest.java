package org.molgenis.data.annotation.core.entity.impl;

import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.Entity;
import org.molgenis.data.annotation.core.RepositoryAnnotator;
import org.molgenis.data.annotation.core.entity.AnnotatorConfig;
import org.molgenis.data.annotation.core.resources.Resources;
import org.molgenis.data.annotation.core.resources.impl.ResourcesImpl;
import org.molgenis.data.annotation.web.AnnotationService;
import org.molgenis.data.annotation.web.settings.CaddAnnotatorSettings;
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

@ContextConfiguration(classes = { CaddAnnotatorTest.Config.class, CaddAnnotator.class })
public class CaddAnnotatorTest extends AbstractMolgenisSpringTest
{
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

	@Autowired
	ApplicationContext context;

	@Autowired
	CaddAnnotator caddAnnotator;

	public EntityType metaDataCanAnnotate;
	public EntityType metaDataCantAnnotate;

	public ArrayList<Entity> input;
	public ArrayList<Entity> input1;
	public ArrayList<Entity> input2;
	public ArrayList<Entity> input3;
	public ArrayList<Entity> input4;
	public ArrayList<Entity> input5;
	public ArrayList<Entity> input6;
	public ArrayList<Entity> input7;
	public static Entity entity;
	public static Entity entity1;
	public static Entity entity2;
	public static Entity entity3;
	public static Entity entity4;
	public static Entity entity5;
	public static Entity entity6;
	public static Entity entity7;

	public void setValues()
	{
		metaDataCanAnnotate = entityTypeFactory.create("test");
		metaDataCantAnnotate = entityTypeFactory.create("test");

		Attribute attributeChrom = attributeFactory.create().setName(CHROM).setDataType(STRING);
		Attribute attributePos = attributeFactory.create().setName(POS).setDataType(INT);
		Attribute attributeRef = attributeFactory.create().setName(REF).setDataType(TEXT);
		Attribute attributeAlt = attributeFactory.create().setName(ALT).setDataType(TEXT);
		Attribute attributeCantAnnotateChrom = attributeFactory.create().setName(CHROM).setDataType(LONG);

		metaDataCanAnnotate.addAttribute(attributeChrom);
		metaDataCanAnnotate.addAttribute(attributePos);
		metaDataCanAnnotate.addAttribute(attributeRef);
		metaDataCanAnnotate.addAttribute(attributeAlt);
		metaDataCanAnnotate.addAttribute(caddAnnotator.createCaddAbsAttr(attributeFactory));
		metaDataCanAnnotate.addAttribute(caddAnnotator.createCaddScaledAttr(attributeFactory));

		metaDataCantAnnotate.addAttribute(attributeCantAnnotateChrom);
		metaDataCantAnnotate.addAttribute(attributePos);
		metaDataCantAnnotate.addAttribute(attributeRef);
		metaDataCantAnnotate.addAttribute(attributeAlt);

		entity = new DynamicEntity(metaDataCanAnnotate);
		entity1 = new DynamicEntity(metaDataCanAnnotate);
		entity2 = new DynamicEntity(metaDataCanAnnotate);
		entity3 = new DynamicEntity(metaDataCanAnnotate);
		entity4 = new DynamicEntity(metaDataCanAnnotate);
		entity5 = new DynamicEntity(metaDataCanAnnotate);
		entity6 = new DynamicEntity(metaDataCanAnnotate);
		entity7 = new DynamicEntity(metaDataCanAnnotate);
	}

	@BeforeClass
	public void beforeClass() throws IOException
	{
		AnnotatorConfig annotatorConfig = context.getBean(AnnotatorConfig.class);
		annotatorConfig.init();

		input = new ArrayList<>();
		input1 = new ArrayList<>();
		input2 = new ArrayList<>();
		input3 = new ArrayList<>();
		input4 = new ArrayList<>();
		input5 = new ArrayList<>();
		input6 = new ArrayList<>();
		input7 = new ArrayList<>();

		setValues();

		entity1.set(CHROM, "1");
		entity1.set(VcfAttributes.POS, 100);
		entity1.set(VcfAttributes.REF, "C");
		entity1.set(VcfAttributes.ALT, "T");

		input1.add(entity1);

		entity2.set(CHROM, "2");
		entity2.set(VcfAttributes.POS, 200);
		entity2.set(VcfAttributes.REF, "A");
		entity2.set(VcfAttributes.ALT, "C");

		input2.add(entity2);

		entity3.set(CHROM, "3");
		entity3.set(VcfAttributes.POS, 300);
		entity3.set(VcfAttributes.REF, "G");
		entity3.set(VcfAttributes.ALT, "C");

		input3.add(entity3);

		entity4.set(CHROM, "3");
		entity4.set(VcfAttributes.POS, 300);
		entity4.set(VcfAttributes.REF, "G");
		entity4.set(VcfAttributes.ALT, "T,A,C");

		input4.add(entity4);

		entity5.set(CHROM, "3");
		entity5.set(VcfAttributes.POS, 300);
		entity5.set(VcfAttributes.REF, "GC");
		entity5.set(VcfAttributes.ALT, "T,A");

		input5.add(entity5);

		entity6.set(CHROM, "3");
		entity6.set(VcfAttributes.POS, 300);
		entity6.set(VcfAttributes.REF, "C");
		entity6.set(VcfAttributes.ALT, "GX,GC");

		input6.add(entity6);

		entity7.set(CHROM, "3");
		entity7.set(VcfAttributes.POS, 300);
		entity7.set(VcfAttributes.REF, "C");
		entity7.set(VcfAttributes.ALT, "GC");

		input7.add(entity7);
	}

	@Test
	public void testThreeOccurencesOneMatch()
	{
		Iterator<Entity> results = annotator.annotate(input1);
		Entity resultEntity = results.next();

		assertEquals(resultEntity.get(CaddAnnotator.CADD_ABS), "-0.03");
		assertEquals(resultEntity.get(CaddAnnotator.CADD_SCALED), "2.003");
	}

	@Test
	public void testTwoOccurencesNoMatch()
	{
		Iterator<Entity> results = annotator.annotate(input2);
		Entity resultEntity = results.next();

		assertEquals(resultEntity.get(CaddAnnotator.CADD_ABS), null);
		assertEquals(resultEntity.get(CaddAnnotator.CADD_SCALED), null);
	}

	@Test
	public void testFourOccurences()
	{
		Iterator<Entity> results = annotator.annotate(input3);
		Entity resultEntity = results.next();

		assertEquals(resultEntity.get(CaddAnnotator.CADD_ABS), "0.5");
		assertEquals(resultEntity.get(CaddAnnotator.CADD_SCALED), "14.5");
	}

	@Test
	public void testFiveMultiAllelic()
	{
		Iterator<Entity> results = annotator.annotate(input4);
		Entity resultEntity = results.next();

		assertEquals(resultEntity.get(CaddAnnotator.CADD_ABS), "-2.4,0.2,0.5");
		assertEquals(resultEntity.get(CaddAnnotator.CADD_SCALED), "0.123,23.1,14.5");
	}

	@Test
	public void testSixMultiAllelicDel()
	{
		Iterator<Entity> results = annotator.annotate(input5);
		Entity resultEntity = results.next();

		assertEquals(resultEntity.get(CaddAnnotator.CADD_ABS), "-3.4,1.2");
		assertEquals(resultEntity.get(CaddAnnotator.CADD_SCALED), "1.123,24.1");
	}

	@Test
	public void testSevenMultiAllelicIns()
	{
		Iterator<Entity> results = annotator.annotate(input6);
		Entity resultEntity = results.next();

		assertEquals(resultEntity.get(CaddAnnotator.CADD_ABS), "-1.002,1.5");
		assertEquals(resultEntity.get(CaddAnnotator.CADD_SCALED), "3.3,15.5");
	}

	@Test
	public void testEightSingleAllelicIns()
	{
		Iterator<Entity> results = annotator.annotate(input7);
		Entity resultEntity = results.next();

		assertEquals(resultEntity.get(CaddAnnotator.CADD_ABS), "1.5");
		assertEquals(resultEntity.get(CaddAnnotator.CADD_SCALED), "15.5");
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
		@Bean
		public Entity caddAnnotatorSettings()
		{
			Entity settings = mock(Entity.class);
			when(settings.getString(CaddAnnotatorSettings.Meta.CADD_LOCATION)).thenReturn(
					ResourceUtils.getFile(getClass(), "/cadd_test.vcf.gz").getPath());
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

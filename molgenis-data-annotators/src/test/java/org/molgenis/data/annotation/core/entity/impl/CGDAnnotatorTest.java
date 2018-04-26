package org.molgenis.data.annotation.core.entity.impl;

import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.annotation.core.RepositoryAnnotator;
import org.molgenis.data.annotation.core.entity.AnnotatorConfig;
import org.molgenis.data.annotation.core.resources.Resources;
import org.molgenis.data.annotation.core.resources.impl.ResourcesImpl;
import org.molgenis.data.annotation.web.AnnotationService;
import org.molgenis.data.annotation.web.settings.CGDAnnotatorSettings;
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
import static org.molgenis.data.annotation.core.entity.impl.CGDAnnotator.*;
import static org.molgenis.data.annotation.core.entity.impl.CGDAnnotator.CGDAttributeName.*;
import static org.molgenis.data.meta.AttributeType.STRING;
import static org.molgenis.data.meta.AttributeType.TEXT;
import static org.testng.Assert.*;

/***
 * Clinical Genomics Database Test
 */
@ContextConfiguration(classes = { CGDAnnotatorTest.Config.class, CGDAnnotator.class })
public class CGDAnnotatorTest extends AbstractMolgenisSpringTest
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

	@BeforeClass
	public void beforeClass() throws IOException
	{
		AnnotatorConfig annotatorConfig = context.getBean(AnnotatorConfig.class);
		annotatorConfig.init();
	}

	@Test
	public void annotateTestMatch()
	{
		EntityType emdIn = entityTypeFactory.create("Test");
		emdIn.addAttribute(attributeFactory.create().setName(GENE.getAttributeName()));
		emdIn.addAttribute(attributeFactory.create().setName(HGNC_ID.getAttributeName()).setDataType(STRING));
		emdIn.addAttribute(attributeFactory.create().setName(ENTREZ_GENE_ID.getAttributeName()).setDataType(TEXT));
		emdIn.addAttribute(attributeFactory.create()
										   .setName(CONDITION.getAttributeName())
										   .setDataType(TEXT)
										   .setLabel(CONDITION_LABEL));
		emdIn.addAttribute(attributeFactory.create()
										   .setName(INHERITANCE.getAttributeName())
										   .setDataType(TEXT)
										   .setLabel(INHERITANCE_LABEL));
		emdIn.addAttribute(attributeFactory.create()
										   .setName(GENERALIZED_INHERITANCE.getAttributeName())
										   .setDataType(TEXT)
										   .setLabel(GENERALIZED_INHERITANCE_LABEL));
		emdIn.addAttribute(attributeFactory.create()
										   .setName(AGE_GROUP.getAttributeName())
										   .setDataType(TEXT)
										   .setLabel(AGE_GROUP_LABEL));
		emdIn.addAttribute(attributeFactory.create().setName(ALLELIC_CONDITIONS.getAttributeName()).setDataType(TEXT));
		emdIn.addAttribute(
				attributeFactory.create().setName(MANIFESTATION_CATEGORIES.getAttributeName()).setDataType(TEXT));
		emdIn.addAttribute(
				attributeFactory.create().setName(INTERVENTION_CATEGORIES.getAttributeName()).setDataType(TEXT));
		emdIn.addAttribute(attributeFactory.create().setName(COMMENTS.getAttributeName()).setDataType(TEXT));
		emdIn.addAttribute(
				attributeFactory.create().setName(INTERVENTION_RATIONALE.getAttributeName()).setDataType(TEXT));
		emdIn.addAttribute(attributeFactory.create().setName(REFERENCES.getAttributeName()).setDataType(TEXT));

		Entity inputEntity = new DynamicEntity(emdIn);
		inputEntity.set(GENE.getAttributeName(), "LEPR");

		Iterator<Entity> results = annotator.annotate(Collections.singletonList(inputEntity));
		assertTrue(results.hasNext());
		Entity resultEntity = results.next();
		assertFalse(results.hasNext());

		assertEquals(resultEntity.get(GENE.getAttributeName()), "LEPR");
		assertEquals(resultEntity.get(ENTREZ_GENE_ID.getAttributeName()), "3953");
		assertEquals(resultEntity.get(CONDITION.getAttributeName()), "Leptin receptor deficiency");
		assertEquals(resultEntity.get(INHERITANCE.getAttributeName()), "AR");
		assertEquals(resultEntity.get(AGE_GROUP.getAttributeName()), "Pediatric");
		assertEquals(resultEntity.get(ALLELIC_CONDITIONS.getAttributeName()), null);
		assertEquals(resultEntity.get(MANIFESTATION_CATEGORIES.getAttributeName()),
				"Allergy/Immunology/Infectious; Endocrine");
		assertEquals(resultEntity.get(INTERVENTION_CATEGORIES.getAttributeName()),
				"Allergy/Immunology/Infectious; Endocrine");
		assertEquals(resultEntity.get(COMMENTS.getAttributeName()),
				"Standard treatments for obesity, such as gastric surgery, have been described as beneficial");
		assertEquals(resultEntity.get(INTERVENTION_RATIONALE.getAttributeName()),
				"In addition to endocrine manifestations, individuals may be susceptible to infections (eg, respiratory infections), which, coupled with other manifestations (eg, severe obesity) can have severe sequelae such that prophylaxis and rapid treatment may be beneficial");
		assertEquals(resultEntity.get(REFERENCES.getAttributeName()),
				"8666155; 9537324; 17229951; 21306929; 23275530; 23616257");
	}

	@Test
	public void annotateTestNoMatch()
	{
		EntityType emdIn = entityTypeFactory.create("Test");
		emdIn.addAttribute(attributeFactory.create().setName(GENE.getAttributeName()));

		Entity inputEntity = new DynamicEntity(emdIn);
		inputEntity.set(GENE.getAttributeName(), "BOGUS");

		Iterator<Entity> results = annotator.annotate(Collections.singletonList(inputEntity));
		assertTrue(results.hasNext());
		Entity resultEntity = results.next();
		assertFalse(results.hasNext());

		assertEquals(resultEntity.get(GENE.getAttributeName()), "BOGUS");
		assertEquals(resultEntity.get(ENTREZ_GENE_ID.getAttributeName()), null);
		assertEquals(resultEntity.get(CONDITION.getAttributeName()), null);
		assertEquals(resultEntity.get(INHERITANCE.getAttributeName()), null);
		assertEquals(resultEntity.get(AGE_GROUP.getAttributeName()), null);
		assertEquals(resultEntity.get(ALLELIC_CONDITIONS.getAttributeName()), null);
		assertEquals(resultEntity.get(MANIFESTATION_CATEGORIES.getAttributeName()), null);
		assertEquals(resultEntity.get(INTERVENTION_CATEGORIES.getAttributeName()), null);
		assertEquals(resultEntity.get(COMMENTS.getAttributeName()), null);
		assertEquals(resultEntity.get(INTERVENTION_RATIONALE.getAttributeName()), null);
		assertEquals(resultEntity.get(REFERENCES.getAttributeName()), null);
	}

	@Configuration
	@Import({ VcfTestConfig.class })
	public static class Config
	{
		@Autowired
		private DataService dataService;

		@Bean
		public Entity CGDAnnotatorSettings()
		{
			Entity settings = mock(Entity.class);
			when(settings.getString(CGDAnnotatorSettings.Meta.CGD_LOCATION)).thenReturn(
					ResourceUtils.getFile(getClass(), "/cgd_example.txt").getPath());
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

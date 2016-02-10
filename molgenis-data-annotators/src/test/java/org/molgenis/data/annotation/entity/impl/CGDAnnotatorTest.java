package org.molgenis.data.annotation.entity.impl;

import static org.mockito.Mockito.mock;
import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.annotation.entity.impl.CGDAnnotator.CGDAttributeName.AGE_GROUP;
import static org.molgenis.data.annotation.entity.impl.CGDAnnotator.CGDAttributeName.ALLELIC_CONDITIONS;
import static org.molgenis.data.annotation.entity.impl.CGDAnnotator.CGDAttributeName.COMMENTS;
import static org.molgenis.data.annotation.entity.impl.CGDAnnotator.CGDAttributeName.CONDITION;
import static org.molgenis.data.annotation.entity.impl.CGDAnnotator.CGDAttributeName.ENTREZ_GENE_ID;
import static org.molgenis.data.annotation.entity.impl.CGDAnnotator.CGDAttributeName.GENE;
import static org.molgenis.data.annotation.entity.impl.CGDAnnotator.CGDAttributeName.HGNC_ID;
import static org.molgenis.data.annotation.entity.impl.CGDAnnotator.CGDAttributeName.INHERITANCE;
import static org.molgenis.data.annotation.entity.impl.CGDAnnotator.CGDAttributeName.INTERVENTION_CATEGORIES;
import static org.molgenis.data.annotation.entity.impl.CGDAnnotator.CGDAttributeName.INTERVENTION_RATIONALE;
import static org.molgenis.data.annotation.entity.impl.CGDAnnotator.CGDAttributeName.MANIFESTATION_CATEGORIES;
import static org.molgenis.data.annotation.entity.impl.CGDAnnotator.CGDAttributeName.REFERENCES;
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
import org.molgenis.data.annotation.resources.impl.ResourcesImpl;
import org.molgenis.data.annotator.websettings.CGDAnnotatorSettings;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.util.ResourceUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

/***
 * Clinical Genomics Database Test
 */
@ContextConfiguration(classes =
{ CGDAnnotatorTest.Config.class, CGDAnnotator.class })
public class CGDAnnotatorTest extends AbstractTestNGSpringContextTests
{
	@Autowired
	RepositoryAnnotator annotator;

	@Test
	public void annotateTestMatch()
	{
		DefaultEntityMetaData emdIn = new DefaultEntityMetaData("Test");
		emdIn.addAttribute(GENE.getAttributeName(), ROLE_ID);
		Entity inputEntity = new MapEntity(emdIn);
		inputEntity.set(GENE.getAttributeName(), "LEPR");

		Iterator<Entity> results = annotator.annotate(Collections.singletonList(inputEntity));
		assertTrue(results.hasNext());
		Entity resultEntity = results.next();
		assertFalse(results.hasNext());

		Map<String, Object> resultMap = new LinkedHashMap<String, Object>();
		resultMap.put(GENE.getAttributeName(), "LEPR");
		resultMap.put(HGNC_ID.getAttributeName(), "6554");
		resultMap.put(ENTREZ_GENE_ID.getAttributeName(), "3953");
		resultMap.put(CONDITION.getAttributeName(), "Leptin receptor deficiency");
		resultMap.put(INHERITANCE.getAttributeName(), "AR");
		resultMap.put(AGE_GROUP.getAttributeName(), "Pediatric");
		resultMap.put(MANIFESTATION_CATEGORIES.getAttributeName(), "Allergy/Immunology/Infectious; Endocrine");
		resultMap.put(INTERVENTION_CATEGORIES.getAttributeName(), "Allergy/Immunology/Infectious; Endocrine");
		resultMap.put(COMMENTS.getAttributeName(),
				"Standard treatments for obesity, such as gastric surgery, have been described as beneficial");
		resultMap.put(INTERVENTION_RATIONALE.getAttributeName(),
				"In addition to endocrine manifestations, individuals may be susceptible to infections (eg, respiratory infections), which, coupled with other manifestations (eg, severe obesity) can have severe sequelae such that prophylaxis and rapid treatment may be beneficial");
		resultMap.put(REFERENCES.getAttributeName(), "8666155; 9537324; 17229951; 21306929; 23275530; 23616257");
		Entity expectedEntity = new MapEntity(resultMap);

		assertEquals(resultEntity.get(GENE.getAttributeName()), expectedEntity.get(GENE.getAttributeName()));
		assertEquals(resultEntity.get(ENTREZ_GENE_ID.getAttributeName()),
				expectedEntity.get(ENTREZ_GENE_ID.getAttributeName()));
		assertEquals(resultEntity.get(CONDITION.getAttributeName()), expectedEntity.get(CONDITION.getAttributeName()));
		assertEquals(resultEntity.get(INHERITANCE.getAttributeName()),
				expectedEntity.get(INHERITANCE.getAttributeName()));
		assertEquals(resultEntity.get(AGE_GROUP.getAttributeName()), expectedEntity.get(AGE_GROUP.getAttributeName()));
		assertEquals(resultEntity.get(ALLELIC_CONDITIONS.getAttributeName()),
				expectedEntity.get(ALLELIC_CONDITIONS.getAttributeName()));
		assertEquals(resultEntity.get(MANIFESTATION_CATEGORIES.getAttributeName()),
				expectedEntity.get(MANIFESTATION_CATEGORIES.getAttributeName()));
		assertEquals(resultEntity.get(INTERVENTION_CATEGORIES.getAttributeName()),
				expectedEntity.get(INTERVENTION_CATEGORIES.getAttributeName()));
		assertEquals(resultEntity.get(COMMENTS.getAttributeName()), expectedEntity.get(COMMENTS.getAttributeName()));
		assertEquals(resultEntity.get(INTERVENTION_RATIONALE.getAttributeName()),
				expectedEntity.get(INTERVENTION_RATIONALE.getAttributeName()));
		assertEquals(resultEntity.get(REFERENCES.getAttributeName()),
				expectedEntity.get(REFERENCES.getAttributeName()));
	}

	@Test
	public void annotateTestNoMatch()
	{
		DefaultEntityMetaData emdIn = new DefaultEntityMetaData("Test");
		emdIn.addAttribute(GENE.getAttributeName(), ROLE_ID);

		Entity inputEntity = new MapEntity(emdIn);
		inputEntity.set(GENE.getAttributeName(), "BOGUS");

		Iterator<Entity> results = annotator.annotate(Collections.singletonList(inputEntity));
		assertTrue(results.hasNext());
		Entity resultEntity = results.next();
		assertFalse(results.hasNext());

		Map<String, Object> resultMap = new LinkedHashMap<String, Object>();
		resultMap.put(GENE.getAttributeName(), "BOGUS");
		Entity expectedEntity = new MapEntity(resultMap);

		assertEquals(resultEntity.get(GENE.getAttributeName()), expectedEntity.get(GENE.getAttributeName()));
		assertEquals(resultEntity.get(ENTREZ_GENE_ID.getAttributeName()),
				expectedEntity.get(ENTREZ_GENE_ID.getAttributeName()));
		assertEquals(resultEntity.get(CONDITION.getAttributeName()), expectedEntity.get(CONDITION.getAttributeName()));
		assertEquals(resultEntity.get(INHERITANCE.getAttributeName()),
				expectedEntity.get(INHERITANCE.getAttributeName()));
		assertEquals(resultEntity.get(AGE_GROUP.getAttributeName()), expectedEntity.get(AGE_GROUP.getAttributeName()));
		assertEquals(resultEntity.get(ALLELIC_CONDITIONS.getAttributeName()),
				expectedEntity.get(ALLELIC_CONDITIONS.getAttributeName()));
		assertEquals(resultEntity.get(MANIFESTATION_CATEGORIES.getAttributeName()),
				expectedEntity.get(MANIFESTATION_CATEGORIES.getAttributeName()));
		assertEquals(resultEntity.get(INTERVENTION_CATEGORIES.getAttributeName()),
				expectedEntity.get(INTERVENTION_CATEGORIES.getAttributeName()));
		assertEquals(resultEntity.get(COMMENTS.getAttributeName()), expectedEntity.get(COMMENTS.getAttributeName()));
		assertEquals(resultEntity.get(INTERVENTION_RATIONALE.getAttributeName()),
				expectedEntity.get(INTERVENTION_RATIONALE.getAttributeName()));
		assertEquals(resultEntity.get(REFERENCES.getAttributeName()),
				expectedEntity.get(REFERENCES.getAttributeName()));
	}

	public static class Config
	{
		@Autowired
		private DataService dataService;

		@Bean
		public Entity CGDAnnotatorSettings()
		{
			Entity settings = new MapEntity();
			settings.set(CGDAnnotatorSettings.Meta.CGD_LOCATION,
					ResourceUtils.getFile(getClass(), "/cgd_example.txt").getPath());
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

package org.molgenis.data.annotation.entity.impl;

import static org.elasticsearch.common.collect.Lists.newArrayList;
import static org.mockito.Mockito.mock;
import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_ID;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Iterator;
import java.util.List;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.annotation.AnnotationService;
import org.molgenis.data.annotation.RepositoryAnnotator;
import org.molgenis.data.annotation.resources.Resources;
import org.molgenis.data.annotation.resources.impl.ResourcesImpl;
import org.molgenis.data.annotator.websettings.OmimAnnotatorSettings;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

@ContextConfiguration(classes =
{ OmimAnnotatorTest.Config.class, OmimAnnotator.class })
public class OmimAnnotatorTest extends AbstractTestNGSpringContextTests
{
	@Autowired
	RepositoryAnnotator annotator;

	@Autowired
	Resources resourcess;

	// Can annotate
	public DefaultEntityMetaData metaDataCanAnnotate = new DefaultEntityMetaData("test");

	// Negative test cannot annotate
	public DefaultEntityMetaData metaDataCantAnnotate = new DefaultEntityMetaData("test");

	@Test
	public void testOmimAnnotation()
	{
		List<Entity> entitiesToAnnotate = newArrayList();

		DefaultEntityMetaData entityMetaData = new DefaultEntityMetaData("Test");
		entityMetaData.addAttribute(SnpEffAnnotator.GENE_NAME, ROLE_ID);

		Entity inputEntity = new MapEntity(entityMetaData);
		inputEntity.set(SnpEffAnnotator.GENE_NAME, "CYP17A1");

		entitiesToAnnotate.add(inputEntity);

		Iterator<Entity> results = annotator.annotate(entitiesToAnnotate);
		assertTrue(results.hasNext());

		Entity resultEntity = results.next();
		System.out.println(resultEntity);
		// assertFalse(results.hasNext());

		// TODO
		// - Create meta data for a start entity
		// - Annotate start entity
		// - Create meta data for annotated entity
		// - Assert the created entity equals what we expect
	}

	public static class Config
	{
		@Autowired
		@SuppressWarnings("unused")
		private DataService dataService;

		@Bean
		public Entity OmimAnnotatorSettings()
		{
			Entity settings = new MapEntity();
			settings.set(OmimAnnotatorSettings.Meta.OMIM_LOCATION, "/src/test/resources/omim/omim.txt");
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

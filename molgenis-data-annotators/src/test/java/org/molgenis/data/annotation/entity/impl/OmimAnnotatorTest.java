package org.molgenis.data.annotation.entity.impl;

import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.Mockito.mock;
import static org.molgenis.MolgenisFieldTypes.TEXT;
import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.annotation.entity.impl.OmimAnnotator.CYTO_LOCATIONS;
import static org.molgenis.data.annotation.entity.impl.OmimAnnotator.MIM_NUMBER;
import static org.molgenis.data.annotation.entity.impl.OmimAnnotator.PHENOTYPE;
import static org.molgenis.data.annotation.entity.impl.SnpEffAnnotator.GENE_NAME;
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
import org.molgenis.util.ResourceUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.Test;

@ContextConfiguration(classes =
{ OmimAnnotatorTest.Config.class, OmimAnnotator.class })
public class OmimAnnotatorTest extends AbstractTestNGSpringContextTests
{
	@Autowired
	RepositoryAnnotator annotator;

	@Autowired
	Resources resources;

	@Test
	public void testAvailability()
	{
		assertTrue(resources.hasRepository(OmimAnnotator.OMIM_RESOURCE));
	}

	@Test
	public void testOmimAnnotation()
	{
		List<Entity> entitiesToAnnotate = newArrayList();

		DefaultEntityMetaData inputEntityMetaData = new DefaultEntityMetaData("Test");
		inputEntityMetaData.addAttribute(SnpEffAnnotator.GENE_NAME, ROLE_ID);

		Entity inputEntity = new MapEntity(inputEntityMetaData);
		inputEntity.set(GENE_NAME, "CYP17A1");

		entitiesToAnnotate.add(inputEntity);
		Iterator<Entity> results = annotator.annotate(entitiesToAnnotate);

		DefaultEntityMetaData expectedEntityMetaData = new DefaultEntityMetaData("Test");
		expectedEntityMetaData.addAttribute(GENE_NAME, ROLE_ID);
		expectedEntityMetaData.addAttribute(PHENOTYPE).setDataType(TEXT);
		expectedEntityMetaData.addAttribute(MIM_NUMBER).setDataType(TEXT);
		expectedEntityMetaData.addAttribute(CYTO_LOCATIONS).setDataType(TEXT);

		Entity expectedEntity = new MapEntity(expectedEntityMetaData);
		expectedEntity.set(GENE_NAME, "CYP17A1");
		expectedEntity.set(PHENOTYPE, newArrayList("17,20-lyase deficiency, isolated, 202110 (3)",
				"17-alpha-hydroxylase/17,20-lyase deficiency, 202110 (3)"));
		expectedEntity.set(MIM_NUMBER, newArrayList("609300", "609300"));
		expectedEntity.set(CYTO_LOCATIONS, newArrayList("10q24.32", "10q24.32"));

		assertTrue(results.hasNext());

		Entity resultEntity = results.next();

		assertFalse(results.hasNext());

		Assert.assertEquals(resultEntity, expectedEntity);
	}

	public static class Config
	{
		@Autowired
		@SuppressWarnings("unused")
		private DataService dataService;

		@Bean
		public Entity omimAnnotatorSettings()
		{
			Entity settings = new MapEntity();
			settings.set(OmimAnnotatorSettings.Meta.OMIM_LOCATION,
					ResourceUtils.getFile(getClass(), "/omim/omim.txt").getPath());
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

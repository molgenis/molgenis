package org.molgenis.data.annotation.entity.impl;

import static com.google.common.collect.Lists.newArrayList;
import static org.apache.commons.lang.StringUtils.join;
import static org.mockito.Mockito.mock;
import static org.molgenis.MolgenisFieldTypes.TEXT;
import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.annotation.entity.impl.omim.OmimAnnotator.OMIM_DISORDER;
import static org.molgenis.data.annotation.entity.impl.omim.OmimAnnotator.OMIM_ENTRY;
import static org.molgenis.data.annotation.entity.impl.omim.OmimAnnotator.OMIM_TYPE;
import static org.molgenis.data.annotation.entity.impl.omim.OmimAnnotator.OMIM_CAUSAL_IDENTIFIER;
import static org.molgenis.data.annotation.entity.impl.omim.OmimAnnotator.OMIM_CYTO_LOCATIONS;
import static org.molgenis.data.support.EffectsMetaData.GENE_NAME;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.annotation.AnnotationService;
import org.molgenis.data.annotation.RepositoryAnnotator;
import org.molgenis.data.annotation.entity.impl.omim.OmimAnnotator;
import org.molgenis.data.annotation.entity.impl.snpEff.SnpEffAnnotator;
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
		inputEntityMetaData.addAttribute(GENE_NAME, ROLE_ID);

		Entity inputEntity = new MapEntity(inputEntityMetaData);
		inputEntity.set(GENE_NAME, "CYP17A1");

		entitiesToAnnotate.add(inputEntity);
		Iterator<Entity> results = annotator.annotate(entitiesToAnnotate);

		DefaultEntityMetaData expectedEntityMetaData = new DefaultEntityMetaData("Test");
		expectedEntityMetaData.addAttribute(GENE_NAME, ROLE_ID);
		expectedEntityMetaData.addAttribute(OMIM_DISORDER).setDataType(TEXT);
		expectedEntityMetaData.addAttribute(OMIM_CAUSAL_IDENTIFIER).setDataType(TEXT);
		expectedEntityMetaData.addAttribute(OMIM_CYTO_LOCATIONS).setDataType(TEXT);
		expectedEntityMetaData.addAttribute(OMIM_ENTRY).setDataType(TEXT);
		expectedEntityMetaData.addAttribute(OMIM_TYPE).setDataType(TEXT);

		Entity expectedEntity = new MapEntity(expectedEntityMetaData);
		expectedEntity.set(GENE_NAME, "CYP17A1");
		expectedEntity.set(OMIM_DISORDER, join(
				newArrayList("17,20-lyase deficiency, isolated", "17-alpha-hydroxylase/17,20-lyase deficiency"), ","));
		expectedEntity.set(OMIM_CAUSAL_IDENTIFIER, join(newArrayList("609300", "609300"), ","));
		expectedEntity.set(OMIM_CYTO_LOCATIONS, join(newArrayList("10q24.32", "10q24.32"), ","));
		expectedEntity.set(OMIM_ENTRY, join(newArrayList("202110", "202110"), ","));
		expectedEntity.set(OMIM_TYPE, join(newArrayList("3", "3"), ","));

		assertTrue(results.hasNext());
		Entity resultEntity = results.next();
		Assert.assertEquals(resultEntity, expectedEntity);
		assertFalse(results.hasNext());
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

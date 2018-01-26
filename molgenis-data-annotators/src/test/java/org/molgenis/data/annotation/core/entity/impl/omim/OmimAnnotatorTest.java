package org.molgenis.data.annotation.core.entity.impl.omim;

import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.annotation.core.RepositoryAnnotator;
import org.molgenis.data.annotation.core.entity.AnnotatorConfig;
import org.molgenis.data.annotation.core.query.GeneNameQueryCreator;
import org.molgenis.data.annotation.core.resources.Resources;
import org.molgenis.data.annotation.core.resources.impl.ResourcesImpl;
import org.molgenis.data.annotation.web.AnnotationService;
import org.molgenis.data.annotation.web.settings.OmimAnnotatorSettings;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.data.util.EntityUtils;
import org.molgenis.data.vcf.config.VcfTestConfig;
import org.molgenis.data.vcf.model.VcfAttributes;
import org.molgenis.util.ResourceUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.apache.commons.lang3.StringUtils.join;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.annotation.core.effects.EffectsMetaData.GENE_NAME;
import static org.molgenis.data.annotation.core.entity.impl.omim.OmimAnnotator.*;
import static org.molgenis.data.annotation.core.entity.impl.omim.OmimRepository.OMIM_GENE_SYMBOLS_COL_NAME;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

@ContextConfiguration(classes = { OmimAnnotatorTest.Config.class, OmimAnnotator.class, GeneNameQueryCreator.class })
public class OmimAnnotatorTest extends AbstractMolgenisSpringTest
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
	Resources resources;

	@Autowired
	OmimAnnotator omimAnnotator;

	@BeforeClass
	public void beforeClass() throws IOException
	{
		AnnotatorConfig annotatorConfig = context.getBean(AnnotatorConfig.class);
		annotatorConfig.init();
	}

	@Test
	public void testAvailability()
	{
		assertTrue(resources.hasRepository(OmimAnnotator.OMIM_RESOURCE));
	}

	@Test
	public void testOmimAnnotation()
	{
		List<Entity> entitiesToAnnotate = newArrayList();

		EntityType inputEntityType = entityTypeFactory.create("Test");
		inputEntityType.addAttribute(attributeFactory.create().setName(OMIM_GENE_SYMBOLS_COL_NAME));
		inputEntityType.addAttributes(Arrays.asList(omimAnnotator.getPhenotypeAttr(), omimAnnotator.getMimNumberAttr(),
				omimAnnotator.getOmimLocationAttr(), omimAnnotator.getEntryAttr(), omimAnnotator.getTypeAttr()));

		Entity inputEntity = new DynamicEntity(inputEntityType);
		inputEntity.set(GENE_NAME, "CYP17A1");

		entitiesToAnnotate.add(inputEntity);
		Iterator<Entity> results = annotator.annotate(entitiesToAnnotate);

		EntityType expectedEntityType = entityTypeFactory.create("Test");
		expectedEntityType.addAttribute(attributeFactory.create().setName(OMIM_GENE_SYMBOLS_COL_NAME));
		expectedEntityType.addAttribute(omimAnnotator.getPhenotypeAttr());
		expectedEntityType.addAttribute(omimAnnotator.getMimNumberAttr());
		expectedEntityType.addAttribute(omimAnnotator.getOmimLocationAttr());
		expectedEntityType.addAttribute(omimAnnotator.getEntryAttr());
		expectedEntityType.addAttribute(omimAnnotator.getTypeAttr());

		Entity expectedEntity = new DynamicEntity(expectedEntityType);
		expectedEntity.set(GENE_NAME, "CYP17A1");
		expectedEntity.set(OMIM_DISORDER, join(newArrayList("17,20-lyase deficiency, isolated"), ","));
		expectedEntity.set(OMIM_CAUSAL_IDENTIFIER, join(newArrayList("609300"), ","));
		expectedEntity.set(OMIM_CYTO_LOCATIONS, join(newArrayList("10q24.32"), ","));
		expectedEntity.set(OMIM_ENTRY, join(newArrayList("202110"), ","));
		expectedEntity.set(OMIM_TYPE, join(newArrayList("3"), ","));

		assertTrue(results.hasNext());
		Entity resultEntity = results.next();
		Assert.assertTrue(EntityUtils.equals(resultEntity, expectedEntity));
		assertFalse(results.hasNext());
	}

	@Configuration
	@Import({ VcfTestConfig.class })
	public static class Config
	{
		@Autowired
		@SuppressWarnings("unused")
		private DataService dataService;

		@Bean
		public Entity omimAnnotatorSettings()
		{
			Entity settings = mock(Entity.class);
			when(settings.getString(OmimAnnotatorSettings.Meta.OMIM_LOCATION)).thenReturn(
					ResourceUtils.getFile(getClass(), "/omim/omim.txt").getPath());
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

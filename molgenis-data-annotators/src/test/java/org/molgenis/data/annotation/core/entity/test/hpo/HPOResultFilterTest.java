package org.molgenis.data.annotation.core.entity.test.hpo;

import com.google.common.base.Optional;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.annotation.core.entity.impl.hpo.HPOAnnotator;
import org.molgenis.data.annotation.core.entity.impl.hpo.HpoResultFilter;
import org.molgenis.data.annotation.core.query.GeneNameQueryCreator;
import org.molgenis.data.annotation.core.resources.Resources;
import org.molgenis.data.annotation.core.resources.impl.ResourcesImpl;
import org.molgenis.data.annotation.web.AnnotationService;
import org.molgenis.data.meta.model.AttributeMetaDataFactory;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.meta.model.EntityMetaDataFactory;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.test.data.AbstractMolgenisSpringTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

import java.util.Arrays;

import static org.mockito.Mockito.mock;
import static org.molgenis.data.annotation.core.entity.impl.hpo.HPORepository.*;
import static org.molgenis.data.meta.model.EntityMetaData.AttributeRole.ROLE_ID;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@ContextConfiguration(classes = { HPOAnnotator.class, HPOResultFilterTest.Config.class, GeneNameQueryCreator.class })
public class HPOResultFilterTest extends AbstractMolgenisSpringTest
{
	@Autowired
	ApplicationContext context;

	@Autowired
	AttributeMetaDataFactory attributeMetaDataFactory;

	@Autowired
	EntityMetaDataFactory entityMetaDataFactory;

	@Autowired
	HPOAnnotator hpoAnnotator;

	@Test
	public void filterResults()
	{
		HpoResultFilter filter = new HpoResultFilter(entityMetaDataFactory, attributeMetaDataFactory, hpoAnnotator);

		EntityMetaData resultEntityMeta = entityMetaDataFactory.create().setSimpleName("result");
		resultEntityMeta.addAttribute(hpoAnnotator.getIdsAttr());
		resultEntityMeta.addAttribute(hpoAnnotator.getTermsAttr());

		EntityMetaData entityMeta = entityMetaDataFactory.create().setSimpleName("HPO");
		entityMeta.addAttribute(attributeMetaDataFactory.create().setName(HPO_DISEASE_ID_COL_NAME));
		entityMeta.addAttribute(attributeMetaDataFactory.create().setName(HPO_GENE_SYMBOL_COL_NAME));
		entityMeta.addAttribute(attributeMetaDataFactory.create().setName(HPO_ID_COL_NAME), ROLE_ID);
		entityMeta.addAttribute(attributeMetaDataFactory.create().setName(HPO_TERM_COL_NAME));

		Entity e1 = new DynamicEntity(entityMeta);
		e1.set(HPO_ID_COL_NAME, "id1");
		e1.set(HPO_TERM_COL_NAME, "term1");

		Entity e2 = new DynamicEntity(entityMeta);
		e2.set(HPO_ID_COL_NAME, "id2");
		e2.set(HPO_TERM_COL_NAME, "term2");

		Optional<Entity> result = filter.filterResults(Arrays.asList(e1, e2), new DynamicEntity(resultEntityMeta),
				false);
		assertTrue(result.isPresent());
		assertEquals(result.get().getString(HPOAnnotator.HPO_IDS), "id1/id2");
		assertEquals(result.get().getString(HPOAnnotator.HPO_TERMS), "term1/term2");
	}

	@Configuration
	@ComponentScan({ "org.molgenis.data.vcf.model" })
	public static class Config
	{
		@Bean
		public Entity HPOAnnotatorSettings()
		{
			Entity settings = mock(Entity.class);
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

package org.molgenis.data.annotation.core.entity.impl.hpo;

import com.google.common.base.Optional;
import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.Entity;
import org.molgenis.data.annotation.core.query.GeneNameQueryCreator;
import org.molgenis.data.annotation.core.resources.Resources;
import org.molgenis.data.annotation.core.resources.impl.ResourcesImpl;
import org.molgenis.data.annotation.web.AnnotationService;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.data.vcf.config.VcfTestConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

import java.util.Arrays;

import static org.mockito.Mockito.mock;
import static org.molgenis.data.annotation.core.entity.impl.hpo.HPORepository.*;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_ID;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@ContextConfiguration(classes = { HPOAnnotator.class, HPOResultFilterTest.Config.class, GeneNameQueryCreator.class })
public class HPOResultFilterTest extends AbstractMolgenisSpringTest
{
	@Autowired
	ApplicationContext context;

	@Autowired
	AttributeFactory attributeFactory;

	@Autowired
	EntityTypeFactory entityTypeFactory;

	@Autowired
	HPOAnnotator hpoAnnotator;

	@Test
	public void filterResults()
	{
		HpoResultFilter filter = new HpoResultFilter(entityTypeFactory, hpoAnnotator);

		EntityType resultEntityType = entityTypeFactory.create("result");
		resultEntityType.addAttribute(hpoAnnotator.getIdsAttr());
		resultEntityType.addAttribute(hpoAnnotator.getTermsAttr());

		EntityType entityType = entityTypeFactory.create("HPO");
		entityType.addAttribute(attributeFactory.create().setName(HPO_DISEASE_ID_COL_NAME));
		entityType.addAttribute(attributeFactory.create().setName(HPO_GENE_SYMBOL_COL_NAME));
		entityType.addAttribute(attributeFactory.create().setName(HPO_ID_COL_NAME), ROLE_ID);
		entityType.addAttribute(attributeFactory.create().setName(HPO_TERM_COL_NAME));

		Entity e1 = new DynamicEntity(entityType);
		e1.set(HPO_ID_COL_NAME, "id1");
		e1.set(HPO_TERM_COL_NAME, "term1");

		Entity e2 = new DynamicEntity(entityType);
		e2.set(HPO_ID_COL_NAME, "id2");
		e2.set(HPO_TERM_COL_NAME, "term2");

		Optional<Entity> result = filter.filterResults(Arrays.asList(e1, e2), new DynamicEntity(resultEntityType),
				false);
		assertTrue(result.isPresent());
		assertEquals(result.get().getString(HPOAnnotator.HPO_IDS), "id1/id2");
		assertEquals(result.get().getString(HPOAnnotator.HPO_TERMS), "term1/term2");
	}

	@Configuration
	@Import({ VcfTestConfig.class })
	public static class Config
	{
		@Bean
		public Entity HPOAnnotatorSettings()
		{
			Entity settings = mock(Entity.class);
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

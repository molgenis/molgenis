package org.molgenis.data.annotation.core.entity.impl;

import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.annotation.core.RepositoryAnnotator;
import org.molgenis.data.annotation.core.entity.AnnotatorConfig;
import org.molgenis.data.annotation.core.resources.Resources;
import org.molgenis.data.annotation.core.resources.impl.ResourcesImpl;
import org.molgenis.data.annotation.web.AnnotationService;
import org.molgenis.data.annotation.web.settings.ExacAnnotatorSettings;
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
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_ID;
import static org.testng.Assert.*;

@ContextConfiguration(classes = { ExacAnnotatorTest.Config.class, ExacAnnotator.class })
public class ExacAnnotatorTest extends AbstractMolgenisSpringTest
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
	ExacAnnotator exacAnnotator;

	@BeforeClass
	public void beforeClass() throws IOException
	{
		AnnotatorConfig annotatorConfig = context.getBean(AnnotatorConfig.class);
		annotatorConfig.init();
	}

	@Test
	public void testAnnotate()
	{
		EntityType emdIn = entityTypeFactory.create("exac");
		emdIn.addAttribute(vcfAttributes.getChromAttribute(), ROLE_ID);
		emdIn.addAttribute(vcfAttributes.getPosAttribute());
		emdIn.addAttribute(vcfAttributes.getRefAttribute());
		emdIn.addAttribute(vcfAttributes.getAltAttribute());
		emdIn.addAttribute(exacAnnotator.getExacAFAttr(attributeFactory));
		emdIn.addAttribute(exacAnnotator.getExacAcHomAttr(attributeFactory));
		emdIn.addAttribute(exacAnnotator.getExacAcHetAttr(attributeFactory));

		Entity inputEntity = new DynamicEntity(emdIn);
		inputEntity.set(VcfAttributes.CHROM, "1");
		inputEntity.set(VcfAttributes.POS, 13372);
		inputEntity.set(VcfAttributes.REF, "G");
		inputEntity.set(VcfAttributes.ALT, "C");

		Iterator<Entity> results = annotator.annotate(Collections.singletonList(inputEntity));
		assertTrue(results.hasNext());
		Entity resultEntity = results.next();
		assertFalse(results.hasNext());

		assertEquals(resultEntity.get(VcfAttributes.CHROM), "1");
		assertEquals(resultEntity.get(VcfAttributes.POS), 13372);
		assertEquals(resultEntity.get(VcfAttributes.REF), "G");
		assertEquals(resultEntity.get(VcfAttributes.ALT), "C");
		assertEquals(resultEntity.get(ExacAnnotator.EXAC_AF), "6.998E-5");
	}

	@Configuration
	@Import({ VcfTestConfig.class })
	public static class Config
	{
		@Autowired
		private DataService dataService;

		@Bean
		public Entity exacAnnotatorSettings()
		{
			Entity settings = mock(Entity.class);
			when(settings.getString(ExacAnnotatorSettings.Meta.EXAC_LOCATION)).thenReturn(
					ResourceUtils.getFile(getClass(), "/exac/exac_test_set.vcf.gz").getPath());
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

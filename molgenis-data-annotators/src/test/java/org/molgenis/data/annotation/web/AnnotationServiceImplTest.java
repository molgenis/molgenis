package org.molgenis.data.annotation.web;

import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.annotation.core.RepositoryAnnotator;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.vcf.config.VcfTestConfig;
import org.molgenis.data.vcf.model.VcfAttributes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.*;

@ContextConfiguration(classes = { AnnotationServiceImplTest.Config.class })
public class AnnotationServiceImplTest extends AbstractMolgenisSpringTest
{
	@Autowired
	AttributeFactory attributeFactory;

	@Autowired
	EntityTypeFactory entityTypeFactory;

	@Autowired
	VcfAttributes vcfAttributes;

	@Autowired
	private AnnotationServiceImpl annotationService;

	@Test
	public void getAllAnnotators()
	{
		assertNotNull(annotationService.getAllAnnotators());
	}

	@Test
	public void getRepositoryByEntityName()
	{
		assertEquals(annotationService.getAnnotatorByName("annotator1").getSimpleName(), "annotator1");
		assertEquals(annotationService.getAnnotatorByName("annotator2").getSimpleName(), "annotator2");
		assertEquals(annotationService.getAnnotatorByName("annotator3").getSimpleName(), "annotator3");
	}

	@Test
	public void get()
	{
		assertEquals(annotationService.getAnnotatorsByMetaData(Config.metaData).size(), 2);
		assertTrue(annotationService.getAnnotatorsByMetaData(Config.metaData)
									.contains(annotationService.getAnnotatorByName("annotator2")));
		assertTrue(annotationService.getAnnotatorsByMetaData(Config.metaData)
									.contains(annotationService.getAnnotatorByName("annotator3")));
	}

	@Configuration
	@Import({ VcfTestConfig.class })
	static class Config
	{
		public static EntityType metaData = mock(EntityType.class);

		@Bean
		public RepositoryAnnotator annotator1()
		{
			RepositoryAnnotator annotator = mock(RepositoryAnnotator.class);
			when(annotator.getSimpleName()).thenReturn("annotator1");
			when(annotator.canAnnotate(metaData)).thenReturn("no way, this entity is unsuitable!");
			return annotator;
		}

		@Bean
		public RepositoryAnnotator annotator2()
		{
			RepositoryAnnotator annotator = mock(RepositoryAnnotator.class);
			when(annotator.getSimpleName()).thenReturn("annotator2");
			when(annotator.canAnnotate(metaData)).thenReturn("true");
			return annotator;
		}

		@Bean
		public RepositoryAnnotator annotator3()
		{
			RepositoryAnnotator annotator = mock(RepositoryAnnotator.class);
			when(annotator.getSimpleName()).thenReturn("annotator3");
			when(annotator.canAnnotate(metaData)).thenReturn("true");
			return annotator;
		}

		@Bean
		public AnnotationService annotationService()
		{
			return new AnnotationServiceImpl();
		}

		@Bean
		public MetaDataService metaDataService()
		{
			return mock(MetaDataService.class);
		}
	}
}

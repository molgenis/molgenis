package org.molgenis.data;

import org.molgenis.data.annotation.core.RepositoryAnnotator;
import org.molgenis.data.annotation.web.AnnotationService;
import org.molgenis.data.annotation.web.AnnotationServiceImpl;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.meta.model.EntityMetaDataFactory;
import org.molgenis.data.vcf.model.VcfAttributes;
import org.molgenis.test.data.AbstractMolgenisSpringTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
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
	EntityMetaDataFactory entityMetaDataFactory;

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
	@ComponentScan({ "org.molgenis.data.vcf.model" })
	static class Config
	{
		public static EntityMetaData metaData = mock(EntityMetaData.class);

		@Bean
		public DataService dataService()
		{
			return mock(DataService.class);
		}

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

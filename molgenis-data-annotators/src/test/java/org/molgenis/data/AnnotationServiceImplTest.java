package org.molgenis.data;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import org.molgenis.data.annotation.AnnotationService;
import org.molgenis.data.annotation.RepositoryAnnotator;
import org.molgenis.data.support.AnnotationServiceImpl;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

@ContextConfiguration(classes =
{ AnnotationServiceImplTest.Config.class })
public class AnnotationServiceImplTest extends AbstractTestNGSpringContextTests
{
	@Autowired
	private AnnotationServiceImpl annotationService;
	private final EntityMetaData metaData = new DefaultEntityMetaData("test");

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
		assertEquals(annotationService.getAnnotatorsByMetaData(metaData).size(), 2);
		assertTrue(annotationService.getAnnotatorsByMetaData(metaData).contains(
				annotationService.getAnnotatorByName("annotator2")));
		assertTrue(annotationService.getAnnotatorsByMetaData(metaData).contains(
				annotationService.getAnnotatorByName("annotator3")));
	}

	public static class Config
	{
		private final EntityMetaData metaData = new DefaultEntityMetaData("test");

		@Bean
		public RepositoryAnnotator annotator1()
		{
			RepositoryAnnotator annotator = mock(RepositoryAnnotator.class);
			when(annotator.getSimpleName()).thenReturn("annotator1");
			when(annotator.canAnnotate(metaData)).thenReturn("no");
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
	}
}

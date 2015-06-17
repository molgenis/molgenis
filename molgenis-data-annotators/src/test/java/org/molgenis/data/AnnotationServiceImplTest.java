package org.molgenis.data;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.molgenis.data.annotation.RepositoryAnnotator;
import org.molgenis.data.support.AnnotationServiceImpl;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class AnnotationServiceImplTest
{
	private RepositoryAnnotator annotator1;
	private RepositoryAnnotator annotator2;
	private RepositoryAnnotator annotator3;
	private AnnotationServiceImpl annotationService;
	private EntityMetaData metaData;

	@BeforeMethod
	public void beforeMethod()
	{
		annotationService = new AnnotationServiceImpl();
		metaData = mock(EntityMetaData.class);

		annotator1 = mock(RepositoryAnnotator.class);
		when(annotator1.getSimpleName()).thenReturn("annotator1");
		when(annotator1.canAnnotate(metaData)).thenReturn("no");

		annotator2 = mock(RepositoryAnnotator.class);
		when(annotator2.getSimpleName()).thenReturn("annotator2");
		when(annotator2.canAnnotate(metaData)).thenReturn("true");

		annotator3 = mock(RepositoryAnnotator.class);
		when(annotator3.getSimpleName()).thenReturn("annotator3");
		when(annotator3.canAnnotate(metaData)).thenReturn("true");
	}

	@Test
	public void getAllAnnotators()
	{
		//TODO: mock application context somehow and reimplement test
	}

	@Test
	public void getRepositoryByEntityName()
	{
		//TODO: mock application context somehow and reimplement test
		//assertEquals(annotationService.getAnnotatorByName("annotator1"), annotator1);
		//assertEquals(annotationService.getAnnotatorByName("annotator2"), annotator2);
		//assertEquals(annotationService.getAnnotatorByName("annotator3"), annotator3);
	}

	@Test
	public void get()
	{
		//TODO: mock application context somehow and reimplement test
		//List<RepositoryAnnotator> expected = new ArrayList<RepositoryAnnotator>();
		//expected.add(annotator2);
		//expected.add(annotator3);
		//assertEquals(annotationService.getAnnotatorsByMetaData(metaData), expected);
	}

}

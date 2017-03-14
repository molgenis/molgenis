package org.molgenis.dataexplorer;

import com.google.common.collect.ImmutableList;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.annotation.core.RepositoryAnnotator;
import org.molgenis.data.annotation.web.CrudRepositoryAnnotator;
import org.molgenis.data.jobs.JobExecutionException;
import org.molgenis.data.jobs.Progress;
import org.molgenis.data.mem.InMemoryRepository;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.dataexplorer.controller.AnnotationJob;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

public class AnnotationJobTest extends AbstractMockitoTest
{
	private AnnotationJob annotationJob;

	@Mock
	private CrudRepositoryAnnotator crudRepositoryAnnotator;
	@Mock
	private RepositoryAnnotator exac;
	@Mock
	private RepositoryAnnotator cadd;
	private Repository<Entity> repository;
	@Mock
	private Progress progress;

	@Mock
	private Authentication authentication;
	@Mock
	private PlatformTransactionManager transactionManager;

	@BeforeMethod
	public void beforeMethod()
	{
		EntityType emd = when(mock(EntityType.class).getFullyQualifiedName()).thenReturn("repo").getMock();
		when(emd.getLabel()).thenReturn("My repo");

		repository = new InMemoryRepository(emd);
		annotationJob = new AnnotationJob(crudRepositoryAnnotator, "fdlk", ImmutableList.of(exac, cadd), repository,
				progress, authentication, new TransactionTemplate(transactionManager));
	}

	@Test
	public void testHappyPath() throws IOException
	{
		when(exac.getSimpleName()).thenReturn("exac");
		when(cadd.getSimpleName()).thenReturn("cadd");

		annotationJob.call();

		Mockito.verify(crudRepositoryAnnotator).annotate(exac, repository);
		Mockito.verify(crudRepositoryAnnotator).annotate(cadd, repository);

		Mockito.verify(progress).start();
		Mockito.verify(progress).setProgressMax(2);
		Mockito.verify(progress)
				.progress(0, "Annotating \"My repo\" with exac (annotator 1 of 2, started by \"fdlk\")");
		Mockito.verify(progress)
				.progress(1, "Annotating \"My repo\" with cadd (annotator 2 of 2, started by \"fdlk\")");
		Mockito.verify(progress).success();
	}

	@Test
	public void testFirstAnnotatorFails() throws IOException
	{
		when(exac.getSimpleName()).thenReturn("exac");
		when(cadd.getSimpleName()).thenReturn("cadd");

		IOException exception = new IOException("error");
		doThrow(exception).when(crudRepositoryAnnotator).annotate(exac, repository);
		try
		{
			annotationJob.call();
			fail("Should throw exception");
		}
		catch (JobExecutionException actual)
		{
			assertEquals(actual.getCause(), exception);
		}

		Mockito.verify(progress).start();
		Mockito.verify(progress).setProgressMax(2);
		Mockito.verify(progress)
				.progress(0, "Annotating \"My repo\" with exac (annotator 1 of 2, started by \"fdlk\")");
		Mockito.verify(progress)
				.progress(1, "Annotating \"My repo\" with cadd (annotator 2 of 2, started by \"fdlk\")");
		Mockito.verify(progress).status("Failed annotators: exac. Successful annotators: cadd");
		Mockito.verify(progress).failed(exception);
	}
}

package org.molgenis.dataexplorer;

import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_ID;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import java.io.IOException;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.molgenis.data.Repository;
import org.molgenis.data.annotation.CrudRepositoryAnnotator;
import org.molgenis.data.annotation.RepositoryAnnotator;
import org.molgenis.data.jobs.JobExecutionException;
import org.molgenis.data.jobs.Progress;
import org.molgenis.data.mem.InMemoryRepository;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.vcf.VcfRepository;
import org.molgenis.dataexplorer.controller.AnnotationJob;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import autovalue.shaded.com.google.common.common.collect.ImmutableList;

public class AnnotationJobTest
{
	private AnnotationJob annotationJob;

	@Mock
	private CrudRepositoryAnnotator crudRepositoryAnnotator;
	private String username = "fdlk";
	@Mock
	private RepositoryAnnotator exac;
	@Mock
	private RepositoryAnnotator cadd;
	private Repository repository;
	@Mock
	private Progress progress;
	private DefaultEntityMetaData emd = new DefaultEntityMetaData("repo");

	private Authentication authentication;

	@Mock
	private PlatformTransactionManager transactionManager;

	@BeforeMethod
	public void beforeMethod()
	{
		MockitoAnnotations.initMocks(this);
		emd.addAttribute("id", ROLE_ID);
		emd.addAttributeMetaData(VcfRepository.CHROM_META);
		emd.addAttributeMetaData(VcfRepository.POS_META);
		emd.addAttribute("description");
		emd.setLabel("My repo");
		authentication = null;

		repository = new InMemoryRepository(emd);
		annotationJob = new AnnotationJob(crudRepositoryAnnotator, username, ImmutableList.of(exac, cadd), repository,
				progress, authentication, new TransactionTemplate(transactionManager));
	}

	@Test
	public void testHappyPath() throws IOException
	{
		Mockito.when(exac.getSimpleName()).thenReturn("exac");
		Mockito.when(cadd.getSimpleName()).thenReturn("cadd");

		annotationJob.call();

		Mockito.verify(crudRepositoryAnnotator).annotate(exac, repository);
		Mockito.verify(crudRepositoryAnnotator).annotate(cadd, repository);

		Mockito.verify(progress).start();
		Mockito.verify(progress).setProgressMax(2);
		Mockito.verify(progress).progress(0,
				"Annotating \"My repo\" with exac (annotator 1 of 2, started by \"fdlk\")");
		Mockito.verify(progress).progress(1,
				"Annotating \"My repo\" with cadd (annotator 2 of 2, started by \"fdlk\")");
		Mockito.verify(progress).success();
	}

	@Test
	public void testFirstAnnotatorFails() throws IOException
	{
		Mockito.when(exac.getSimpleName()).thenReturn("exac");
		Mockito.when(cadd.getSimpleName()).thenReturn("cadd");

		IOException exception = new IOException("error");
		Mockito.when(crudRepositoryAnnotator.annotate(exac, repository)).thenThrow(exception);
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
		Mockito.verify(progress).progress(0,
				"Annotating \"My repo\" with exac (annotator 1 of 2, started by \"fdlk\")");
		Mockito.verify(progress).progress(1,
				"Annotating \"My repo\" with cadd (annotator 2 of 2, started by \"fdlk\")");
		Mockito.verify(progress).status("Failed annotators: exac. Successful annotators: cadd");
		Mockito.verify(progress).failed(exception);
	}
}

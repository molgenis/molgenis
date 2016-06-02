package org.molgenis.dataexplorer;

public class AnnotationJobTest
{
	//	private AnnotationJob annotationJob;
	//
	//	@Mock
	//	private CrudRepositoryAnnotator crudRepositoryAnnotator;
	//	private String username = "fdlk";
	//	@Mock
	//	private RepositoryAnnotator exac;
	//	@Mock
	//	private RepositoryAnnotator cadd;
	//	private Repository<Entity> repository;
	//	@Mock
	//	private Progress progress;
	//	private EntityMetaDataImpl emd = new EntityMetaDataImpl("repo");
	//
	//	private Authentication authentication;
	//
	//	@Mock
	//	private PlatformTransactionManager transactionManager;
	//
	//	@BeforeMethod
	//	public void beforeMethod()
	//	{
	//		MockitoAnnotations.initMocks(this);
	//		emd.addAttribute("id", ROLE_ID);
	//		emd.addAttribute(VcfAttributes.CHROM_META);
	//		emd.addAttribute(VcfAttributes.POS_META);
	//		emd.addAttribute("description");
	//		emd.setLabel("My repo");
	//		authentication = null;
	//
	//		repository = new InMemoryRepository(emd);
	//		annotationJob = new AnnotationJob(crudRepositoryAnnotator, username, ImmutableList.of(exac, cadd), repository,
	//				progress, authentication, new TransactionTemplate(transactionManager));
	//	}
	//
	//	@Test
	//	public void testHappyPath() throws IOException
	//	{
	//		Mockito.when(exac.getSimpleName()).thenReturn("exac");
	//		Mockito.when(cadd.getSimpleName()).thenReturn("cadd");
	//
	//		annotationJob.call();
	//
	//		Mockito.verify(crudRepositoryAnnotator).annotate(exac, repository);
	//		Mockito.verify(crudRepositoryAnnotator).annotate(cadd, repository);
	//
	//		Mockito.verify(progress).start();
	//		Mockito.verify(progress).setProgressMax(2);
	//		Mockito.verify(progress).progress(0,
	//				"Annotating \"My repo\" with exac (annotator 1 of 2, started by \"fdlk\")");
	//		Mockito.verify(progress).progress(1,
	//				"Annotating \"My repo\" with cadd (annotator 2 of 2, started by \"fdlk\")");
	//		Mockito.verify(progress).success();
	//	}
	//
	//	@Test
	//	public void testFirstAnnotatorFails() throws IOException
	//	{
	//		Mockito.when(exac.getSimpleName()).thenReturn("exac");
	//		Mockito.when(cadd.getSimpleName()).thenReturn("cadd");
	//
	//		IOException exception = new IOException("error");
	//		Mockito.when(crudRepositoryAnnotator.annotate(exac, repository)).thenThrow(exception);
	//		try
	//		{
	//			annotationJob.call();
	//			fail("Should throw exception");
	//		}
	//		catch (JobExecutionException actual)
	//		{
	//			assertEquals(actual.getCause(), exception);
	//		}
	//
	//		Mockito.verify(progress).start();
	//		Mockito.verify(progress).setProgressMax(2);
	//		Mockito.verify(progress).progress(0,
	//				"Annotating \"My repo\" with exac (annotator 1 of 2, started by \"fdlk\")");
	//		Mockito.verify(progress).progress(1,
	//				"Annotating \"My repo\" with cadd (annotator 2 of 2, started by \"fdlk\")");
	//		Mockito.verify(progress).status("Failed annotators: exac. Successful annotators: cadd");
	//		Mockito.verify(progress).failed(exception);
	//	}
}

package org.molgenis.data.elasticsearch.config;

public class EmbeddedElasticSearchConfigTest
{
	// FIXME
	//	private AnnotationConfigApplicationContext context;
	//
	//	private File molgenisHomeDir;
	//
	//	@BeforeMethod
	//	public void beforeMethod()
	//	{
	//		molgenisHomeDir = Files.createTempDir();
	//		molgenisHomeDir.deleteOnExit();
	//
	//		System.setProperty("molgenis.home", molgenisHomeDir.getAbsolutePath());
	//		context = new AnnotationConfigApplicationContext(MetadataTestConfig.class, DataServiceImpl.class,
	//				EmbeddedElasticSearchConfig.class,
	//				ElasticsearchEntityFactory.class, Config.class);
	//	}
	//
	//	@AfterMethod
	//	public void tearDownAfterMethod() throws IOException
	//	{
	//		context.destroy();
	//		FileUtils.deleteDirectory(molgenisHomeDir);
	//	}
	//
	//	@Configuration
	//	public static class Config
	//	{
	//		@Bean
	//		public EntityManager entityManager()
	//		{
	//			return mock(EntityManager.class);
	//		}
	//
	//		@Bean
	//		public SourceToEntityConverter sourceToEntityConverter()
	//		{
	//			return mock(SourceToEntityConverter.class);
	//		}
	//
	//		@Bean
	//		public TransactionManager molgenisTransactionManager()
	//		{
	//			return mock(TransactionManager.class);
	//		}
	//
	//		@Bean
	//		public JobExecutionUpdater jobExecutionUpdater()
	//		{
	//			return mock(JobExecutionUpdater.class);
	//		}
	//
	//		@Bean
	//		public MailSender mailSender()
	//		{
	//			return mock(MailSender.class);
	//		}
	//
	//		@Bean
	//		public IndexActionRegisterService indexActionRegisterService()
	//		{
	//			return mock(IndexActionRegisterServiceImpl.class);
	//		}
	//
	//		@Bean
	//		public DocumentIdGenerator documentIdGenerator()
	//		{
	//			return mock(DocumentIdGenerator.class);
	//		}
	//	}
	//
	//	@Test
	//	public void embeddedElasticSearchServiceFactory()
	//	{
	//		EmbeddedElasticSearchServiceFactory factory = context.getBean(EmbeddedElasticSearchServiceFactory.class);
	//		assertNotNull(factory);
	//	}
	//
	//	@Test
	//	public void searchService()
	//	{
	//		SearchService searchService = context.getBean(SearchService.class);
	//		assertNotNull(searchService);
	//		assertTrue(searchService instanceof ElasticsearchService);
	//	}
}

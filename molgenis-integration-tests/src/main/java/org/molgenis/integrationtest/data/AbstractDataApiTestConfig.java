package org.molgenis.integrationtest.data;

//@EnableTransactionManagement(proxyTargetClass = true)
//@ComponentScan(
//{ "org.molgenis.data.meta", "org.molgenis.data.elasticsearch.index", "org.molgenis.auth" })
//@Import(
//{TransactionConfig.class,
//		RunAsSystemBeanPostProcessor.class, FileMetaMetaData.class,
//		OwnedEntityMetaData.class, RhinoConfig.class, ExpressionValidator.class, LanguageService.class,
//		DatabaseConfig.class, UuidGenerator.class
//})
public abstract class AbstractDataApiTestConfig
{
	//	@Autowired
	//	private TransactionLogService transactionLogService;
	//
	//	@Autowired
	//	public ExpressionValidator expressionValidator;
	//
	//	@Autowired
	//	public DataSource dataSource;
	//
	//	@Autowired
	//	public IdGenerator idGenerator;
	//
	//	@Autowired
	//	public SystemEntityMetaDataRegistry systemEntityMetaDataRegistry;
	//
	//	@Autowired
	//	public LanguageService languageService;
	//
	//	@Autowired
	//	public UserAuthorityFactory userAuthorityFactory;
	//
	//	@Autowired
	//	public MolgenisUserFactory molgenisUserFactory;
	//
	//	protected AbstractDataApiTestConfig()
	//	{
	//		System.setProperty("molgenis.home", Files.createTempDir().getAbsolutePath());
	//		setUp();
	//	}
	//
	//	@PostConstruct
	//	public void init()
	//	{
	//		SecuritySupport.login();
	//		dataService().setMetaDataService(metaDataService());
	//		metaDataService().setDefaultBackend(getBackend());
	//	}
	//
	//	protected abstract void setUp();
	//	protected abstract RepositoryCollection getBackend();
	//
	//	@Bean
	//	public MetaDataService metaDataService()
	//	{
	//		return new MetaDataServiceImpl(dataService());
	//	}
	//
	//	@Bean
	//	public LanguageService languageService()
	//	{
	//		return new LanguageService(dataService(), appSettings());
	//	}
	//
	//	@Bean
	//	public IdGenerator idGenerator()
	//	{
	//		return new UuidGenerator();
	//	}
	//
	//	@Bean
	//	public DataServiceImpl dataService()
	//	{
	//		return new DataServiceImpl(); // FIXME
	//	}
	//
	//	@Bean
	//	public EntityManager entityManager()
	//	{
	//		return new EntityManagerImpl(dataService());
	//	}
	//
	//	@Bean
	//	public PermissionSystemService permissionSystemService()
	//	{
	//		return new PermissionSystemService(dataService(), userAuthorityFactory);
	//	}
	//
	//	@Bean
	//	public AppSettings appSettings()
	//	{
	//		return new TestAppSettings();
	//	}
	//
	//	@Bean
	//	public EntityAttributesValidator entityAttributesValidator()
	//	{
	//		return new EntityAttributesValidator();
	//	}
	//
	//	@Bean
	//	public RepositoryDecoratorRegistry repositoryDecoratorRegistry()
	//	{
	//		return new RepositoryDecoratorRegistry();
	//	}
	//
	//	@Bean
	//	public RepositoryDecoratorFactory repositoryDecoratorFactory()
	//	{
	//		return new RepositoryDecoratorFactory()
	//		{
	//			@Override
	//			public Repository<Entity> createDecoratedRepository(Repository<Entity> repository)
	//			{
	//				return new MolgenisRepositoryDecoratorFactory(entityManager(), entityAttributesValidator(),
	//						idGenerator(), appSettings(), dataService(), expressionValidator, repositoryDecoratorRegistry(),
	//						systemEntityMetaDataRegistry, molgenisUserFactory, userAuthorityFactory, )
	//						.createDecoratedRepository(repository);
	//			}
	//
	//			@Override
	//			public <E extends Entity> Repository<E> createDecoratedRepository(Repository<E> repository, Class<E> clazz)
	//			{
	//				Repository<Entity> decoratedRepository = createDecoratedRepository((Repository<Entity>) repository);
	//				return new TypedRepositoryDecorator<>(decoratedRepository, clazz);
	//			}
	//		};
	//	}
	//
	//	@Bean
	//	public FreeMarkerConfigurer freeMarkerConfigurer()
	//	{
	//		return new FreeMarkerConfigurer();
	//	}
	//
	//	@Bean
	//	public ConversionService conversionService()
	//	{
	//		return new DefaultConversionService();
	//	}
	//
	//	@Bean
	//	public ApplicationContextProvider applicationContextProvider()
	//	{
	//		return new ApplicationContextProvider();
	//	}
	//
	//	@Bean
	//	public PasswordEncoder passwordEncoder()
	//	{
	//		return new MolgenisPasswordEncoder(new BCryptPasswordEncoder());
	//	}
	//
}

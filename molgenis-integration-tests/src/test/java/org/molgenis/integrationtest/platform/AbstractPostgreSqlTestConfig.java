package org.molgenis.integrationtest.platform;

import org.molgenis.data.postgresql.PostgreSqlConfiguration;
import org.molgenis.data.postgresql.PostgreSqlEntityFactory;
import org.molgenis.integrationtest.data.AbstractDataApiTestConfig;
import org.springframework.context.annotation.Import;

@Import({ PostgreSqlEntityFactory.class, PostgreSqlConfiguration.class })
public abstract class AbstractPostgreSqlTestConfig extends AbstractDataApiTestConfig
{
	private static final Logger LOG = LoggerFactory.getLogger(AbstractPostgreSqlTestConfig.class);
	public static final String INTEGRATION_DATABASE = "molgenis_integration_test";

	@Autowired
	DataService dataService;

	@Autowired
	PostgreSqlEntityFactory postgreSqlEntityFactory;

	@Autowired
	JdbcTemplate jdbcTemplate;

	@Override
	protected ManageableRepositoryCollection getBackend()
	{
		return new PostgreSqlRepositoryCollection(dataSource)
		{
			@Override
			protected PostgreSqlRepository createPostgreSqlRepository()
			{
				return new PostgreSqlRepository(postgreSqlEntityFactory, jdbcTemplate, dataSource);
			}

			@Override
			public boolean hasRepository(String name)
			{
				throw new UnsupportedOperationException();
			}
		};
	}

	@Override
	public void setUp()
	{
		try
		{
			Connection conn = getConnection();

			Statement statement = conn.createStatement();
			statement.executeUpdate("DROP DATABASE IF EXISTS \"" + INTEGRATION_DATABASE + "\"");
			statement.executeUpdate("CREATE DATABASE \"" + INTEGRATION_DATABASE + "\"");

			conn.close();
		}
		catch (Exception e)
		{
			throw new RuntimeException(e.getMessage());
		}
	}

	private Connection getConnection() throws IOException, SQLException
	{
		Properties properties = new Properties();
		File file = ResourceUtils.getFile(getClass(), "/postgresql/molgenis.properties");
		properties.load(new FileInputStream(file));

		String db_uri = properties.getProperty("db_uri");
		int slashIndex = db_uri.lastIndexOf('/');

		// remove the, not yet created, database from the connection url
		return DriverManager.getConnection(db_uri.substring(0, slashIndex + 1), properties.getProperty("db_user"),
				properties.getProperty("db_password"));
	}

	@PostConstruct
	public void init()
	{
		super.init();
	}

	@PreDestroy
	public void cleanup()
	{
		try
		{
			((ComboPooledDataSource) dataSource).close();

			Connection conn = getConnection();
			Statement statement = conn.createStatement();
			statement.executeUpdate("DROP database if exists \"" + INTEGRATION_DATABASE + "\"");
			conn.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Bean
	public static PropertySourcesPlaceholderConfigurer properties()
	{
		PropertySourcesPlaceholderConfigurer pspc = new PropertySourcesPlaceholderConfigurer();
		Resource[] resources = new Resource[]
		{ new FileSystemResource(System.getProperty("molgenis.home") + "/molgenis-server.properties"),
				new ClassPathResource("/postgresql/molgenis.properties") };
		pspc.setLocations(resources);
		pspc.setFileEncoding("UTF-8");
		pspc.setIgnoreUnresolvablePlaceholders(true);
		pspc.setIgnoreResourceNotFound(true);
		pspc.setNullValue("@null");
		return pspc;
	}
}

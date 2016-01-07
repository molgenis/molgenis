package org.molgenis.integrationtest.data;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.molgenis.data.EntityManager;
import org.molgenis.data.EntityManagerImpl;
import org.molgenis.data.ManageableRepositoryCollection;
import org.molgenis.data.i18n.LanguageService;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.MetaDataServiceImpl;
import org.molgenis.data.settings.AppSettings;
import org.molgenis.data.support.DataServiceImpl;
import org.molgenis.framework.ui.MolgenisPluginRegistry;
import org.molgenis.framework.ui.MolgenisPluginRegistryImpl;
import org.molgenis.mysql.embed.EmbeddedMysqlDatabaseBuilder;
import org.molgenis.security.permission.PermissionSystemService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableTransactionManagement(proxyTargetClass = true)
@ComponentScan("org.molgenis.data.meta")
public abstract class AbstractDataApiTestConfig
{
	@PostConstruct
	public void init()
	{
		dataService().setMeta(metaDataService());

		// Entity english = new DefaultEntity(LanguageMetaData.INSTANCE, dataService());
		// english.set(LanguageMetaData.CODE, "en");
		// english.set(LanguageMetaData.NAME, "English");
		// dataService().add(LanguageMetaData.ENTITY_NAME, english);

		metaDataService().setDefaultBackend(getBackend());
	}

	protected abstract ManageableRepositoryCollection getBackend();

	@Bean
	public LanguageService languageService()
	{
		return new LanguageService(dataService(), appSettings());
	}

	@Bean
	public PlatformTransactionManager transactionManager()
	{
		return new DataSourceTransactionManager(dataSource());
	}

	@Bean
	public DataServiceImpl dataService()
	{
		return new DataServiceImpl();
	}

	@Bean
	public EntityManager entityResolver()
	{
		return new EntityManagerImpl(dataService());
	}

	@Bean
	public PermissionSystemService permissionSystemService()
	{
		return new PermissionSystemService(dataService());
	}

	@Bean
	public MetaDataService metaDataService()
	{
		return new MetaDataServiceImpl(dataService());
	}

	@Bean
	public MolgenisPluginRegistry molgenisPluginRegistry()
	{
		return new MolgenisPluginRegistryImpl();
	}

	@Bean
	public AppSettings appSettings()
	{
		return new TestAppSettings();
	}

	@Bean(destroyMethod = "shutdown")
	public DataSource dataSource()
	{
		return new EmbeddedMysqlDatabaseBuilder().build();
	}
}

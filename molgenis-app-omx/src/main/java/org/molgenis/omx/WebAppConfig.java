package org.molgenis.omx;

import java.io.IOException;
import java.util.Map;

import org.molgenis.DatabaseConfig;
import org.molgenis.catalogmanager.CatalogManagerService;
import org.molgenis.data.DataService;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.IndexedCrudRepository;
import org.molgenis.data.IndexedCrudRepositorySecurityDecorator;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryDecoratorFactory;
import org.molgenis.data.elasticsearch.ElasticsearchRepositoryDecorator;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.elasticsearch.config.EmbeddedElasticSearchConfig;
import org.molgenis.data.elasticsearch.meta.ElasticsearchAttributeMetaDataRepository;
import org.molgenis.data.elasticsearch.meta.ElasticsearchEntityMetaDataRepository;
import org.molgenis.data.meta.AttributeMetaDataRepository;
import org.molgenis.data.meta.AttributeMetaDataRepositoryDecoratorFactory;
import org.molgenis.data.meta.EntityMetaDataRepository;
import org.molgenis.data.meta.EntityMetaDataRepositoryDecoratorFactory;
import org.molgenis.data.validation.EntityAttributesValidator;
import org.molgenis.data.validation.IndexedRepositoryValidationDecorator;
import org.molgenis.dataexplorer.freemarker.DataExplorerHyperlinkDirective;
import org.molgenis.omx.catalogmanager.OmxCatalogManagerService;
import org.molgenis.omx.config.DataExplorerConfig;
import org.molgenis.omx.core.FreemarkerTemplateRepository;
import org.molgenis.omx.studymanager.OmxStudyManagerService;
import org.molgenis.security.user.MolgenisUserService;
import org.molgenis.studymanager.StudyManagerService;
import org.molgenis.ui.MolgenisWebAppConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import freemarker.template.TemplateException;

@Configuration
@EnableTransactionManagement
@EnableWebMvc
@EnableAsync
@ComponentScan("org.molgenis")
@Import(
{ WebAppSecurityConfig.class, DatabaseConfig.class, OmxConfig.class, EmbeddedElasticSearchConfig.class,
		DataExplorerConfig.class })
public class WebAppConfig extends MolgenisWebAppConfig
{
	@Autowired
	private DataService dataService;
	@Autowired
	private MolgenisUserService molgenisUserService;
	@Autowired
	private FreemarkerTemplateRepository freemarkerTemplateRepository;
	@Autowired
	private SearchService elasticSearchService;

	@Bean
	@Qualifier("catalogService")
	public CatalogManagerService catalogManagerService()
	{
		return new OmxCatalogManagerService(dataService);
	}

	@Bean
	public StudyManagerService studyDefinitionManagerService()
	{
		return new OmxStudyManagerService(dataService, molgenisUserService);
	}

	@Override
	protected void addFreemarkerVariables(Map<String, Object> freemarkerVariables)
	{
		freemarkerVariables.put("dataExplorerLink", new DataExplorerHyperlinkDirective(molgenisPluginRegistry(),
				dataService));
	}

	@Override
	public FreeMarkerConfigurer freeMarkerConfigurer() throws IOException, TemplateException
	{
		FreeMarkerConfigurer result = super.freeMarkerConfigurer();
		// Look up unknown templates in the FreemarkerTemplate repository
		result.setPostTemplateLoaders(new RepositoryTemplateLoader(freemarkerTemplateRepository));
		return result;
	}

	// temporary workaround for module dependencies
	@Bean
	public RepositoryDecoratorFactory repositoryDecoratorFactory()
	{
		return new RepositoryDecoratorFactory()
		{
			@Override
			public Repository createDecoratedRepository(Repository repository)
			{
				// do not index an indexed repository
				if (repository instanceof IndexedCrudRepository)
				{
					// 1. security decorator
					// 2. validation decorator
					// 3. indexed repository
					return new IndexedCrudRepositorySecurityDecorator(new IndexedRepositoryValidationDecorator(
							(IndexedCrudRepository) repository, new EntityAttributesValidator()));
				}
				else
				{
					// create indexing meta data if meta data does not exist
					EntityMetaData entityMetaData = repository.getEntityMetaData();
					if (!elasticSearchService.hasMapping(entityMetaData))
					{
						try
						{
							elasticSearchService.createMappings(entityMetaData);
						}
						catch (IOException e)
						{
							throw new MolgenisDataException(e);
						}
					}

					// 1. security decorator
					// 2. validation decorator
					// 3. indexing decorator
					// 4. repository
					return new IndexedCrudRepositorySecurityDecorator(new IndexedRepositoryValidationDecorator(
							new ElasticsearchRepositoryDecorator(repository, elasticSearchService),
							new EntityAttributesValidator()));
				}
			}
		};
	}

	// temporary workaround for module dependencies
	@Bean
	public AttributeMetaDataRepositoryDecoratorFactory attributeMetaDataRepositoryDecoratorFactory()
	{
		return new AttributeMetaDataRepositoryDecoratorFactory()
		{
			@Override
			public AttributeMetaDataRepository createDecoratedRepository(AttributeMetaDataRepository repository)
			{
				// 1. indexing decorator
				return new ElasticsearchAttributeMetaDataRepository(repository, dataService, elasticSearchService);
			}
		};
	}

	// temporary workaround for module dependencies
	@Bean
	public EntityMetaDataRepositoryDecoratorFactory entityMetaDataRepositoryDecoratorFactory()
	{
		return new EntityMetaDataRepositoryDecoratorFactory()
		{
			@Override
			public EntityMetaDataRepository createDecoratedRepository(EntityMetaDataRepository repository)
			{
				// 1. indexing decorator
				return new ElasticsearchEntityMetaDataRepository(repository, elasticSearchService);
			}
		};
	}
}

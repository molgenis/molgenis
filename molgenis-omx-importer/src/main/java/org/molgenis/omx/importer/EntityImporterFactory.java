package org.molgenis.omx.importer;

import java.util.Set;

import org.molgenis.data.DataService;
import org.molgenis.data.FileRepositoryCollectionFactory;
import org.molgenis.data.Repository;
import org.molgenis.data.elasticsearch.ElasticsearchRepositoryDecorator;
import org.molgenis.data.jpa.JpaRepository;
import org.molgenis.data.support.FileRepositoryCollection;
import org.molgenis.data.support.QueryResolver;
import org.molgenis.data.validation.DefaultEntityValidator;
import org.molgenis.data.validation.EntityAttributesValidator;
import org.molgenis.data.validation.EntityValidator;
import org.molgenis.elasticsearch.ElasticSearchService;
import org.molgenis.framework.server.MolgenisSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by charbonb on 22/05/14.
 */
@Component
public class EntityImporterFactory
{
	private final MolgenisSettings molgenisSettings;
	private final FileRepositoryCollectionFactory fileRepositoryCollectionFactory;
	private final ElasticSearchService elasticSearchService;
	private final EntityValidator validator;
	private final QueryResolver queryResolver;

	@Autowired
	public EntityImporterFactory(ElasticSearchService elasticSearchService, DataService dataService,
			FileRepositoryCollectionFactory fileRepositoryCollectionFactory, MolgenisSettings molgenisSettings)
	{
		this.validator = new DefaultEntityValidator(dataService, new EntityAttributesValidator());
		this.queryResolver = new QueryResolver(dataService);
		this.molgenisSettings = molgenisSettings;
		this.fileRepositoryCollectionFactory = fileRepositoryCollectionFactory;
		this.elasticSearchService = elasticSearchService;
	}

	Repository getOutRepository(Repository inRepository)
	{
		String outRepositoryClass = molgenisSettings.getProperty("plugin.importers.out.repository."
				+ inRepository.getClass().getSimpleName());
		switch (outRepositoryClass)
		{
			case "elasticSearch":
				return new ElasticsearchRepositoryDecorator(elasticSearchService, inRepository);
			case "mysql":
				return null;// new MysqlRepository();
			case "jpa":
				return new JpaRepository(inRepository.getEntityMetaData(), validator, queryResolver);
			case "omx":
				return null;// new OmxRepository();
			default:
				throw new IllegalArgumentException();
		}
	}

	void registerRepository(Set<String> fileExtensions, Class<? extends FileRepositoryCollection> repositoryCollection)
	{
		fileRepositoryCollectionFactory.addFileRepositoryCollectionClass(repositoryCollection, fileExtensions);
	}
}

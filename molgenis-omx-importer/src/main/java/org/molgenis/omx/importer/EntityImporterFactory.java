package org.molgenis.omx.importer;

import java.util.Set;

import org.elasticsearch.client.Client;
import org.molgenis.data.DataService;
import org.molgenis.data.FileRepositoryCollectionFactory;
import org.molgenis.data.Repository;
import org.molgenis.data.elasticsearch.ElasticsearchRepository;
import org.molgenis.data.support.FileRepositoryCollection;
import org.molgenis.data.support.QueryResolver;
import org.molgenis.data.validation.DefaultEntityValidator;
import org.molgenis.data.validation.EntityAttributesValidator;
import org.molgenis.data.validation.EntityValidator;
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
	private final EntityValidator validator;
	private final QueryResolver queryResolver;
	private final Client client;
	private final DataService dataService;

	@Autowired
	public EntityImporterFactory(Client client, DataService dataService,
			FileRepositoryCollectionFactory fileRepositoryCollectionFactory, MolgenisSettings molgenisSettings)
	{
		this.validator = new DefaultEntityValidator(dataService, new EntityAttributesValidator());
		this.queryResolver = new QueryResolver(dataService);
		this.molgenisSettings = molgenisSettings;
		this.fileRepositoryCollectionFactory = fileRepositoryCollectionFactory;
		this.client = client;
		this.dataService = dataService;
	}

	Repository getOutRepository(Repository inRepository)
	{
		return new ElasticsearchRepository(client, "molgenis", inRepository.getEntityMetaData());
		// String outRepositoryClass = molgenisSettings.getProperty("plugin.importers.out.repository."
		// + inRepository.getClass().getSimpleName());
		// switch (outRepositoryClass)
		// {
		// case "elasticSearch":
		// return new ElasticsearchRepositoryDecorator(elasticSearchService, inRepository);
		// case "mysql":
		// return null;// new MysqlRepository();
		// case "jpa":
		// return new JpaRepository(inRepository.getEntityMetaData(), validator, queryResolver);
		// case "omx":
		// return null;// new OmxRepository();
		// default:
		// throw new IllegalArgumentException();
		// }
	}

	void registerRepository(Set<String> fileExtensions, Class<? extends FileRepositoryCollection> repositoryCollection)
	{
		fileRepositoryCollectionFactory.addFileRepositoryCollectionClass(repositoryCollection, fileExtensions);
	}
}

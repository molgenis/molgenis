package org.molgenis.data.meta.system;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toSet;

import java.util.Set;

import org.molgenis.data.DataService;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.meta.AttributeMetaDataMetaData;
import org.molgenis.data.meta.DefaultPackage;
import org.molgenis.data.meta.EntityMetaData;
import org.molgenis.data.meta.EntityMetaDataMetaData;
import org.molgenis.data.meta.PackageMetaData;
import org.molgenis.data.meta.TagMetaData;
import org.molgenis.util.DependencyResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Persists {@link org.molgenis.data.meta.SystemEntityMetaData} in the metadata {@link org.molgenis.data.RepositoryCollection}
 */
@Component
public class SystemEntityMetaDataPersister
{
	private final RepositoryCollection repositoryCollection;
	private final DataService dataService;
	private final DefaultPackage defaultPackage;

	@Autowired
	public SystemEntityMetaDataPersister(
			@Value("#{PostgreSqlRepositoryCollection}") RepositoryCollection repositoryCollection, DataService dataService, DefaultPackage defaultPackage)
	{
		this.repositoryCollection = requireNonNull(repositoryCollection);
		this.dataService = requireNonNull(dataService);
		this.defaultPackage = requireNonNull(defaultPackage);
	}

	public void persist()
	{
		SystemEntityMetaDataRegistrySingleton systemEntityMetaDataRegistry = SystemEntityMetaDataRegistrySingleton.INSTANCE;

		// create meta entity tables
		repositoryCollection
				.createRepository(systemEntityMetaDataRegistry.getSystemEntityMetaData(TagMetaData.ENTITY_NAME));
		repositoryCollection.createRepository(
				systemEntityMetaDataRegistry.getSystemEntityMetaData(AttributeMetaDataMetaData.ENTITY_NAME));
		repositoryCollection
				.createRepository(systemEntityMetaDataRegistry.getSystemEntityMetaData(PackageMetaData.ENTITY_NAME));
		repositoryCollection.createRepository(
				systemEntityMetaDataRegistry.getSystemEntityMetaData(EntityMetaDataMetaData.ENTITY_NAME));

		// add default meta entities
		dataService.add(PackageMetaData.ENTITY_NAME, defaultPackage);

		// persist entity meta data
		Set<EntityMetaData> metaEntityMetaDataSet = systemEntityMetaDataRegistry.getSystemEntityMetaDatas().collect(toSet());
		DependencyResolver.resolve(metaEntityMetaDataSet).stream().filter(this::isNotPersisted).forEach(this::persist);
	}

	private boolean isNotPersisted(EntityMetaData entityMetaData)
	{
		return repositoryCollection.hasRepository(entityMetaData);
	}

	private void persist(EntityMetaData entityMetaData)
	{
		dataService.add(EntityMetaDataMetaData.ENTITY_NAME, entityMetaData);
	}
}

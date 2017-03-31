package org.molgenis.data.support;

import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.populate.IdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.model.EntityType.AttributeCopyMode.DEEP_COPY_ATTRS;

@Component
public class RepositoryCopier
{
	private static final Logger LOG = LoggerFactory.getLogger(RepositoryCopier.class);

	private final MetaDataService metaDataService;
	private final AttributeFactory attrFactory;
	private final IdGenerator idGenerator;

	@Autowired
	public RepositoryCopier(MetaDataService metaDataService, AttributeFactory attrFactory, IdGenerator idGenerator)
	{
		this.metaDataService = requireNonNull(metaDataService);
		this.attrFactory = requireNonNull(attrFactory);
		this.idGenerator = idGenerator;
	}

	@Transactional
	public Repository<Entity> copyRepository(Repository<Entity> repository, String simpleName, Package pack,
			String newRepositoryLabel)
	{
		return copyRepository(repository, simpleName, pack, newRepositoryLabel, new QueryImpl<>());
	}

	private Repository<Entity> copyRepository(Repository<Entity> repository, String simpleName, Package pack,
			String label, Query<Entity> query)
	{
		LOG.info("Creating a copy of {} repository, with simpleName: {}, package: {} and label: {}",
				repository.getName(), simpleName, pack, label);

		// create copy of entity meta data
		EntityType emd = EntityType.newInstance(repository.getEntityType(), DEEP_COPY_ATTRS, attrFactory);
		emd.setPackage(pack);
		emd.setLabel(label);
		emd.setId(idGenerator.generateId());
		// create repository for copied entity meta data
		Repository<Entity> repositoryCopy = metaDataService.createRepository(emd);

		// copy data to new repository
		repositoryCopy.add(repository.findAll(query));
		return repositoryCopy;
	}
}

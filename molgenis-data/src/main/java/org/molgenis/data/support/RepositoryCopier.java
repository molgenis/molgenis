package org.molgenis.data.support;

import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.Package;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

	public RepositoryCopier(MetaDataService metaDataService, AttributeFactory attrFactory)
	{
		this.metaDataService = requireNonNull(metaDataService);
		this.attrFactory = requireNonNull(attrFactory);
	}

	@Transactional
	public Repository<Entity> copyRepository(Repository<Entity> repository, String entityTypeId, Package pack,
			String entityTypeLabel)
	{
		LOG.info("Creating a copy of {} repository, with id: {}, package: {} and label: {}", repository.getName(),
				entityTypeId, pack, entityTypeLabel);

		// create copy of entity meta data
		EntityType emd = EntityType.newInstance(repository.getEntityType(), DEEP_COPY_ATTRS, attrFactory);
		emd.setId(entityTypeId);
		emd.setLabel(entityTypeLabel);
		emd.setPackage(pack);
		// create repository for copied entity meta data
		Repository<Entity> repositoryCopy = metaDataService.createRepository(emd);

		// copy data to new repository
		repositoryCopy.add(repository.query().findAll());
		return repositoryCopy;
	}
}

package org.molgenis.data.security.owned;

import org.molgenis.data.AbstractRepositoryDecorator;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.security.EntityIdentity;
import org.molgenis.data.security.meta.RowLevelSecuredMetadata;
import org.molgenis.data.security.meta.RowLevelSecurityConfiguration;
import org.springframework.security.acls.model.MutableAclService;

import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

//This is the decorator that decorates the RowLevelSecurityConfiguration entity -> the administration of row level secured stuff
public class RowLevelSecurityConfigurationEntityDecorator
		extends AbstractRepositoryDecorator<RowLevelSecurityConfiguration>
{
	private final MutableAclService mutableAclService;
	private final DataService dataService;

	//TODO: discuss
	// create: add acls
	// update: leave acls as they are but enable/disable RLS
	// delete: remove acls

	public RowLevelSecurityConfigurationEntityDecorator(Repository<RowLevelSecurityConfiguration> delegateRepository,
			MutableAclService mutableAclService, DataService dataService)
	{
		super(delegateRepository);
		this.mutableAclService = requireNonNull(mutableAclService);
		this.dataService = requireNonNull(dataService);
	}

	@Override
	public void delete(RowLevelSecurityConfiguration entity)
	{
		deleteEntityAcls(entity);
		super.delete(entity);
	}

	@Override
	public void deleteById(Object id)
	{
		Entity entity = dataService.findOneById(RowLevelSecuredMetadata.ROW_LEVEL_SECURED, id);
		deleteEntityAcls((RowLevelSecurityConfiguration) entity);
		super.deleteById(id);
	}

	@Override
	public void deleteAll()
	{
		dataService.findAll(RowLevelSecuredMetadata.ROW_LEVEL_SECURED, RowLevelSecurityConfiguration.class)
				   .forEach(this::deleteEntityAcls);
		super.deleteAll();
	}

	@Override
	public void add(RowLevelSecurityConfiguration entity)
	{
		createEntityAcls(entity);
		super.add(entity);
	}

	@Override
	public Integer add(Stream<RowLevelSecurityConfiguration> entities)
	{
		entities.forEach(this::createEntityAcls);
		return super.add(entities);
	}

	@Override
	public void delete(Stream<RowLevelSecurityConfiguration> entities)
	{
		entities.forEach(this::deleteEntityAcls);
		super.delete(entities);
	}

	@Override
	public void deleteAll(Stream<Object> ids)
	{
		ids.forEach(id ->
		{
			RowLevelSecurityConfiguration rowLevelSecurityConfiguration = dataService.findOneById(
					RowLevelSecuredMetadata.ROW_LEVEL_SECURED, id, RowLevelSecurityConfiguration.class);
			deleteEntityAcls(rowLevelSecurityConfiguration);
		});
		super.deleteAll(ids);
	}

	private void createEntityAcls(RowLevelSecurityConfiguration rowLevelSecurityConfiguration)
	{
		dataService.findAll(getEntityType().getId())
				   .forEach(entity -> createEntityAcl(rowLevelSecurityConfiguration.getEntityTypeId(),
						   entity.getIdValue()));
	}

	private void deleteEntityAcls(RowLevelSecurityConfiguration rowLevelSecurityConfiguration)
	{
		dataService.findAll(getEntityType().getId())
				   .forEach(entity -> deleteEntityAcl(rowLevelSecurityConfiguration.getEntityTypeId(),
						   entity.getIdValue()));
	}

	private void createEntityAcl(String entityTypeId, Object entityId)
	{
		EntityIdentity identity = new EntityIdentity(entityTypeId, entityId);
		mutableAclService.createAcl(identity);
	}

	private void deleteEntityAcl(String entityTypeId, Object entityId)
	{
		EntityIdentity identity = new EntityIdentity(entityTypeId, entityId);
		mutableAclService.deleteAcl(identity, true);
	}
}
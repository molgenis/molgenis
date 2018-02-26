package org.molgenis.data.security.owned;

import org.molgenis.data.AbstractRepositoryDecorator;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.security.EntityIdentity;
import org.molgenis.data.security.meta.RowLevelSecured;
import org.molgenis.data.security.meta.RowLevelSecuredMetadata;
import org.molgenis.data.security.user.UserService;
import org.springframework.security.acls.model.MutableAclService;

import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

//FIXME: names of the rowlevel decorators are unclear
//This is the decorator that decorates the RowLevelSecured entity -> the administration of row level secured stuff
public class RowLevelSecurityEntityDecorator extends AbstractRepositoryDecorator<RowLevelSecured>
{
	private final MutableAclService mutableAclService;
	private final DataService dataService;
	private final UserService userService;

	//TODO: discuss
	// create: add acls
	// update: leave acls as they are but enable/disable RLS
	// delete: remove acls

	public RowLevelSecurityEntityDecorator(Repository<RowLevelSecured> delegateRepository,
			MutableAclService mutableAclService, DataService dataService, UserService userService)
	{
		super(delegateRepository);
		this.mutableAclService = requireNonNull(mutableAclService);
		this.dataService = requireNonNull(dataService);
		this.userService = requireNonNull(userService);
	}

	@Override
	public void delete(RowLevelSecured entity)
	{
		deleteEntityAcls(entity);
		super.delete(entity);
	}

	@Override
	public void deleteById(Object id)
	{
		Entity entity = dataService.findOneById(RowLevelSecuredMetadata.ROW_LEVEL_SECURED, id);
		deleteEntityAcls((RowLevelSecured) entity);
		super.deleteById(id);
	}

	@Override
	public void deleteAll()
	{
		dataService.findAll(RowLevelSecuredMetadata.ROW_LEVEL_SECURED, RowLevelSecured.class)
				   .forEach(this::deleteEntityAcls);
		super.deleteAll();
	}

	@Override
	public void add(RowLevelSecured entity)
	{
		createEntityAcls(entity);
		super.add(entity);
	}

	@Override
	public Integer add(Stream<RowLevelSecured> entities)
	{
		entities.forEach(entity ->
		{
			createEntityAcls(entity);
		});
		return super.add(entities);
	}

	@Override
	public void delete(Stream<RowLevelSecured> entities)
	{
		entities.forEach(entity ->
		{
			deleteEntityAcls(entity);
		});

		super.delete(entities);
	}

	@Override
	public void deleteAll(Stream<Object> ids)
	{
		ids.forEach(id ->
		{
			RowLevelSecured rowLevelSecured = dataService.findOneById(RowLevelSecuredMetadata.ROW_LEVEL_SECURED, id,
					RowLevelSecured.class);
			deleteEntityAcls(rowLevelSecured);
		});
		super.deleteAll(ids);
	}

	private void createEntityAcls(RowLevelSecured rowLevelSecured)
	{
		dataService.findAll(getEntityType().getId())
				   .forEach(entity -> createEntityAcl(rowLevelSecured.getEntity(), entity));
	}

	private void deleteEntityAcls(RowLevelSecured rowLevelSecured)
	{
		dataService.findAll(getEntityType().getId())
				   .forEach(entity -> deleteEntityAcl(rowLevelSecured.getEntity(), entity));
	}

	private void createEntityAcl(EntityType entityType, Entity entity)
	{
		EntityIdentity identity = new EntityIdentity(entityType, entity);
		mutableAclService.createAcl(identity);
	}

	private void deleteEntityAcl(EntityType entityType, Entity entity)
	{
		EntityIdentity identity = new EntityIdentity(entityType, entity);
		mutableAclService.deleteAcl(identity, true);
	}
}
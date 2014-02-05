package org.molgenis.data.support;

import java.util.Arrays;
import java.util.List;

import org.molgenis.data.CrudRepository;
import org.molgenis.data.DatabaseAction;
import org.molgenis.data.Entity;
import org.molgenis.data.validation.EntityValidator;
import org.springframework.transaction.annotation.Transactional;

public abstract class AbstractCrudRepository extends AbstractRepository implements CrudRepository
{
	private final EntityValidator validator;

	public AbstractCrudRepository(EntityValidator validator)
	{
		this.validator = validator;
	}

	@Override
	@Transactional
	public final Integer add(Entity entity)
	{
		validator.validate(Arrays.asList(entity), this);
		return addInternal(entity);
	}

	@Override
	@Transactional
	public final void add(Iterable<? extends Entity> entities)
	{
		validator.validate(entities, this);
		addInternal(entities);
	}

	@Override
	@Transactional
	public final void update(Entity entity)
	{
		validator.validate(Arrays.asList(entity), this);
		updateInternal(entity);
	}

	@Override
	@Transactional
	public void update(Iterable<? extends Entity> entities)
	{
		validator.validate(entities, this);
		updateInternal(entities);
	}

	@Override
	@Transactional
	public void update(List<? extends Entity> entities, DatabaseAction dbAction, String... keyName)
	{
		validator.validate(entities, this);
		updateInternal(entities, dbAction, keyName);
	}

	protected abstract void addInternal(Iterable<? extends Entity> entities);

	protected abstract Integer addInternal(Entity entity);

	protected abstract void updateInternal(Entity entity);

	protected abstract void updateInternal(Iterable<? extends Entity> entities);

	protected abstract void updateInternal(List<? extends Entity> entities, DatabaseAction dbAction, String... keyName);

}

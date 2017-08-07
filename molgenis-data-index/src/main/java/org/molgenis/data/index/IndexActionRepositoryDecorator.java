package org.molgenis.data.index;

import org.molgenis.data.AbstractRepositoryDecorator;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCapability;
import org.molgenis.data.meta.model.EntityType;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * {@link Repository} decorator that registers changes with a {@link IndexActionRegisterServiceImpl}.
 */
public class IndexActionRepositoryDecorator extends AbstractRepositoryDecorator<Entity>
{
	private final IndexActionRegisterService indexActionRegisterService;

	public IndexActionRepositoryDecorator(Repository<Entity> delegateRepository,
			IndexActionRegisterService indexActionRegisterService)
	{
		super(delegateRepository);
		this.indexActionRegisterService = requireNonNull(indexActionRegisterService);
	}

	@Override
	public Set<RepositoryCapability> getCapabilities()
	{
		Set<RepositoryCapability> capabilities = new HashSet<>();
		capabilities.add(RepositoryCapability.INDEXABLE);
		capabilities.addAll(delegate().getCapabilities());
		return capabilities;
	}

	@Override
	public void update(Entity entity)
	{
		delegate().update(entity);
		indexActionRegisterService.register(getEntityType(), entity.getIdValue());
		registerRefEntityIndexActions();
	}

	@Override
	public void delete(Entity entity)
	{
		indexActionRegisterService.register(getEntityType(), entity.getIdValue());
		registerRefEntityIndexActions(entity);
		delegate().delete(entity);
	}

	@Override
	public void deleteById(Object id)
	{
		indexActionRegisterService.register(getEntityType(), id);
		registerRefEntityIndexActions();
		delegate().deleteById(id);
	}

	@Override
	public void deleteAll()
	{
		indexActionRegisterService.register(getEntityType(), null);
		registerRefEntityIndexActions();
		delegate().deleteAll();
	}

	@Override
	public void add(Entity entity)
	{
		delegate().add(entity);
		indexActionRegisterService.register(getEntityType(), entity.getIdValue());
		registerRefEntityIndexActions(entity);
	}

	@Override
	public Integer add(Stream<Entity> entities)
	{
		indexActionRegisterService.register(getEntityType(), null);
		registerRefEntityIndexActions();
		return delegate().add(entities);
	}

	@Override
	public void update(Stream<Entity> entities)
	{
		indexActionRegisterService.register(getEntityType(), null);
		registerRefEntityIndexActions();
		delegate().update(entities);
	}

	@Override
	public void delete(Stream<Entity> entities)
	{
		indexActionRegisterService.register(getEntityType(), null);
		registerRefEntityIndexActions();
		delegate().delete(entities);
	}

	@Override
	public void deleteAll(Stream<Object> ids)
	{
		indexActionRegisterService.register(getEntityType(), null);
		registerRefEntityIndexActions();
		delegate().deleteAll(ids);
	}

	/**
	 * Register index actions for entity types with bidirectional attribute values.
	 */
	private void registerRefEntityIndexActions()
	{
		// bidirectional attribute: register indexing actions for other side
		getEntityType().getMappedByAttributes().forEach(mappedByAttr ->
		{
			EntityType refEntity = mappedByAttr.getRefEntity();
			indexActionRegisterService.register(refEntity, null);
		});

		getEntityType().getInversedByAttributes().forEach(inversedByAttr ->
		{
			EntityType refEntity = inversedByAttr.getRefEntity();
			indexActionRegisterService.register(refEntity, null);
		});
	}

	/**
	 * Register index actions for the given entity for entity types with bidirectional attribute values.
	 *
	 * @param entity entity to add or delete
	 */
	private void registerRefEntityIndexActions(Entity entity)
	{
		// bidirectional attribute: register indexing actions for other side
		getEntityType().getMappedByAttributes().forEach(mappedByAttr ->
		{
			EntityType mappedByAttrRefEntity = mappedByAttr.getRefEntity();
			entity.getEntities(mappedByAttr.getName())
				  .forEach(refEntity -> indexActionRegisterService.register(mappedByAttrRefEntity,
						  refEntity.getIdValue()));
		});

		getEntityType().getInversedByAttributes().forEach(inversedByAttr ->
		{
			Entity refEntity = entity.getEntity(inversedByAttr.getName());
			if (refEntity != null)
			{
				EntityType inversedByAttrRefEntity = inversedByAttr.getRefEntity();
				indexActionRegisterService.register(inversedByAttrRefEntity, refEntity.getIdValue());
			}
		});
	}
}

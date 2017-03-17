package org.molgenis.data;

import com.google.common.collect.Iterators;
import org.molgenis.data.meta.model.Attribute;

import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.support.EntityTypeUtils.isSingleReferenceType;
import static org.molgenis.util.EntityUtils.asStream;

public class CascadeDeleteRepositoryDecorator extends AbstractRepositoryDecorator<Entity>
{
	private static final int BATCH_SIZE = 1000;

	private final Repository<Entity> decoratedRepository;
	private final DataService dataService;

	public CascadeDeleteRepositoryDecorator(Repository<Entity> decoratedRepository, DataService dataService)
	{
		this.decoratedRepository = requireNonNull(decoratedRepository);
		this.dataService = requireNonNull(dataService);
	}

	@Override
	protected Repository<Entity> delegate()
	{
		return decoratedRepository;
	}

	@Override
	public void delete(Entity entity)
	{
		prepareCascadeDeletes(entity);
		super.delete(entity);
		handleCascadeDeletes(entity);
	}

	@Override
	public void deleteById(Object id)
	{
		Entity entity = findOneById(id);
		super.deleteById(id);
		handleCascadeDeletes(entity);
	}

	@Override
	public void deleteAll()
	{
		Iterators.partition(query().findAll().iterator(), BATCH_SIZE).forEachRemaining(entitiesBatch ->
		{
			super.delete(entitiesBatch.stream());
			entitiesBatch.forEach(this::handleCascadeDeletes);
		});
	}

	@Override
	public void delete(Stream<Entity> entities)
	{
		Iterators.partition(entities.iterator(), BATCH_SIZE).forEachRemaining(entitiesBatch ->
		{
			entitiesBatch.forEach(this::prepareCascadeDeletes);
			super.delete(entitiesBatch.stream());
			entitiesBatch.forEach(this::handleCascadeDeletes);
		});
	}

	@Override
	public void deleteAll(Stream<Object> ids)
	{
		Iterators.partition(ids.iterator(), BATCH_SIZE).forEachRemaining(idsBatch ->
		{
			Stream<Entity> entities = findAll(idsBatch.stream());
			super.deleteAll(idsBatch.stream());
			entities.forEach(this::handleCascadeDeletes);
		});
	}

	/**
	 * Guarantee that referenced entities for attributes with cascade delete are loaded,
	 * e.g. in case entity is a lazy or partial entity.
	 */
	private void prepareCascadeDeletes(Entity entity)
	{
		getCascadeDeleteAttributes().forEach(attr -> entity.get(attr.getName()));
	}

	private void handleCascadeDeletes(Entity entity)
	{
		getCascadeDeleteAttributes().forEach(attr -> handleCascadeDeletes(entity, attr));
	}

	private void handleCascadeDeletes(Entity entity, Attribute attribute)
	{
		Stream<Entity> refEntityStream;
		if (isSingleReferenceType(attribute))
		{
			Entity refEntity = entity.getEntity(attribute.getName());
			refEntityStream = refEntity != null ? Stream.of(refEntity) : Stream.empty();
		}
		else
		{
			Iterable<Entity> entities = entity.getEntities(attribute.getName());
			refEntityStream = asStream(entities);
		}

		// delete one-by-one and first check if exists because entities might not exist due to earlier deletes
		String refEntityTypeName = attribute.getRefEntity().getFullyQualifiedName();
		refEntityStream.forEach(refEntity ->
		{
			if (dataService.findOneById(refEntityTypeName, refEntity.getIdValue()) != null)
			{
				dataService.delete(refEntityTypeName, refEntity);
			}
		});
	}

	private Stream<Attribute> getCascadeDeleteAttributes()
	{
		return asStream(getEntityType().getAtomicAttributes())
				.filter(attribute -> attribute.getCascadeDelete() != null && attribute.getCascadeDelete());
	}
}

package org.molgenis.data.meta;

import com.google.common.collect.TreeTraverser;
import org.molgenis.data.*;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;

import javax.annotation.Nonnull;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Spliterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static com.google.common.collect.Lists.reverse;
import static com.google.common.collect.Sets.difference;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.Spliterators.spliteratorUnknownSize;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.StreamSupport.stream;
import static org.molgenis.data.meta.model.AttributeMetadata.ATTRIBUTE_META_DATA;

/**
 * Decorator for the entity meta data repository:
 * - filters requested entities based on the permissions of the current user.
 * - applies updates to the repository collection for entity meta data adds/deletes
 * - adds and removes attribute columns to the repository collection for entity meta data updates
 * <p>
 * TODO replace permission based entity filtering with generic row-level security once available
 */
public class EntityTypeRepositoryDecorator extends AbstractRepositoryDecorator<EntityType>
{
	private final Repository<EntityType> decoratedRepo;
	private final DataService dataService;
	private final EntityTypeDependencyResolver entityTypeDependencyResolver;

	public EntityTypeRepositoryDecorator(Repository<EntityType> decoratedRepo, DataService dataService,
			EntityTypeDependencyResolver entityTypeDependencyResolver)
	{
		this.decoratedRepo = requireNonNull(decoratedRepo);
		this.dataService = requireNonNull(dataService);
		this.entityTypeDependencyResolver = entityTypeDependencyResolver;
	}

	@Override
	protected Repository<EntityType> delegate()
	{
		return decoratedRepo;
	}

	@Override
	public void update(EntityType entity)
	{
		updateEntity(entity);
	}

	@Override
	public void update(Stream<EntityType> entities)
	{
		entities.forEach(this::updateEntity);
	}

	@Override
	public void delete(EntityType entity)
	{
		deleteEntityType(entity);
	}

	@Override
	public void delete(Stream<EntityType> entities)
	{
		Map<String, EntityType> resolvedEntityTypes = resolveDependencies(entities.collect(toList()));
		removeMappedByAttributes(resolvedEntityTypes);
		resolvedEntityTypes.values().forEach(this::deleteEntityType);
	}

	@Override
	public void deleteById(Object id)
	{
		EntityType entityType = findOneById(id);
		if (entityType == null)
		{
			throw new UnknownEntityException(
					format("Unknown entity meta data [%s] with id [%s]", getName(), id.toString()));
		}
		deleteEntityType(entityType);
	}

	@Override
	public void deleteAll(Stream<Object> ids)
	{
		delete(findAll(ids));
	}

	@Override
	public void deleteAll()
	{
		delete(stream(spliteratorUnknownSize(iterator(), Spliterator.ORDERED), false));
	}

	@Override
	public void add(EntityType entity)
	{
		addEntityType(entity);
	}

	@Override
	public Integer add(Stream<EntityType> entities)
	{
		AtomicInteger count = new AtomicInteger();
		entities.filter(entity ->
		{
			count.incrementAndGet();
			return true;
		}).forEach(this::addEntityType);
		return count.get();
	}

	private void addEntityType(EntityType entityType)
	{
		// add row to entities table
		decoratedRepo.add(entityType);
		if (!entityType.isAbstract() && !MetaDataService.isMetaEntityType(entityType))
		{
			RepositoryCollection repoCollection = dataService.getMeta().getBackend(entityType);
			if (repoCollection == null)
			{
				throw new MolgenisDataException(format("Unknown backend [%s]", entityType.getBackend()));
			}
			repoCollection.createRepository(entityType);
		}
	}

	private void updateEntity(EntityType newEntityType)
	{
		addAndRemoveAttributesInBackend(newEntityType);
		updateEntityTypeInBackend(newEntityType);
		// update entity
		decoratedRepo.update(newEntityType);
	}

	private void updateEntityTypeInBackend(EntityType updatedEntityType)
	{
		EntityType existingEntityType = decoratedRepo.findOneById(updatedEntityType.getId());
		if (!existingEntityType.isAbstract())
		{
			RepositoryCollection backend = dataService.getMeta().getBackend(existingEntityType);
			backend.updateRepository(existingEntityType, updatedEntityType);
		}
	}

	/**
	 * Add and remove entity attributes in the backend for an {@link EntityType}.
	 * If the {@link EntityType} is abstract, will update all concrete extending {@link EntityType}s.
	 * Attribute updates are handled by the {@link AttributeRepositoryDecorator}.
	 *
	 * @param entityType {@link EntityType} containing the desired situation.
	 */
	private void addAndRemoveAttributesInBackend(EntityType entityType)
	{
		EntityType existingEntityType = decoratedRepo.findOneById(entityType.getId());
		Map<String, Attribute> attrsMap = toAttributesMap(entityType);
		Map<String, Attribute> existingAttrsMap = toAttributesMap(existingEntityType);

		dataService.getMeta().getConcreteChildren(entityType).forEach(concreteEntityType ->
		{
			RepositoryCollection backend = dataService.getMeta().getBackend(concreteEntityType);
			EntityType concreteExistingEntityType = decoratedRepo.findOneById(concreteEntityType.getId());

			addNewAttributesInBackend(attrsMap, existingAttrsMap, backend, concreteExistingEntityType);
			deleteRemovedAttributesInBackend(attrsMap, existingAttrsMap, backend, concreteExistingEntityType);
		});
	}

	private Map<String, Attribute> toAttributesMap(EntityType entityType)
	{
		return stream(entityType.getOwnAllAttributes().spliterator(), false).collect(
				toMap(Attribute::getName, identity()));
	}

	private void deleteRemovedAttributesInBackend(Map<String, Attribute> attrsMap,
			Map<String, Attribute> existingAttrsMap, RepositoryCollection backend,
			EntityType concreteExistingEntityType)
	{
		difference(existingAttrsMap.keySet(), attrsMap.keySet()).stream()
																.map(existingAttrsMap::get)
																.forEach(removedAttribute -> backend.deleteAttribute(
																		concreteExistingEntityType, removedAttribute));
	}

	private void addNewAttributesInBackend(Map<String, Attribute> attrsMap, Map<String, Attribute> existingAttrsMap,
			RepositoryCollection backend, EntityType concreteExistingEntityType)
	{
		difference(attrsMap.keySet(), existingAttrsMap.keySet()).stream()
																.map(attrsMap::get)
																.forEach(addedAttribute -> backend.addAttribute(
																		concreteExistingEntityType, addedAttribute));
	}

	private LinkedHashMap<String, EntityType> resolveDependencies(List<EntityType> entityTypes)
	{
		List<EntityType> resolvedEntityTypes = reverse(entityTypeDependencyResolver.resolve(entityTypes));
		return resolvedEntityTypes.stream()
								  .collect(toMap(EntityType::getId, identity(), (u, v) -> u, LinkedHashMap::new));
	}

	/**
	 * Removes the mappedBy attributes of each OneToMany pair in the supplied map of EntityTypes
	 */
	private void removeMappedByAttributes(Map<String, EntityType> resolvedEntityTypes)
	{
		resolvedEntityTypes.values()
						   .stream()
						   .flatMap(EntityType::getMappedByAttributes)
						   .filter(attribute -> resolvedEntityTypes.containsKey(attribute.getEntity().getId()))
						   .forEach(attribute -> dataService.delete(ATTRIBUTE_META_DATA, attribute));
	}

	private void deleteEntityType(EntityType entityType)
	{
		// delete EntityType table
		if (!entityType.isAbstract())
		{
			deleteEntityRepository(entityType);
		}

		// delete rows from attributes table
		deleteEntityAttributes(entityType);

		// delete row from entities table
		decoratedRepo.delete(entityType);
	}

	private void deleteEntityAttributes(EntityType entityType)
	{
		Iterable<Attribute> rootAttrs = entityType.getOwnAttributes();
		Stream<Attribute> allAttrs = stream(rootAttrs.spliterator(), false).flatMap(
				attrEntity -> stream(new AttributeTreeTraverser().preOrderTraversal(attrEntity).spliterator(), false));
		dataService.delete(ATTRIBUTE_META_DATA, allAttrs);
	}

	private void deleteEntityRepository(EntityType entityType)
	{
		String backend = entityType.getBackend();
		dataService.getMeta().getBackend(backend).deleteRepository(entityType);
	}

	private static class AttributeTreeTraverser extends TreeTraverser<Attribute>
	{
		@Override
		public Iterable<Attribute> children(@Nonnull Attribute attr)
		{
			return attr.getChildren();
		}
	}
}
package org.molgenis.data.index;

import com.google.common.collect.ImmutableSet;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.util.GenericDependencyResolver;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import static com.google.common.collect.Maps.uniqueIndex;
import static java.util.Collections.emptySet;
import static java.util.stream.StreamSupport.stream;

/**
 * Models the dependencies between Entities.
 */
public class DependencyModel
{
	private final Map<String, EntityType> entityTypes;
	private final GenericDependencyResolver genericDependencyResolver = new GenericDependencyResolver();

	/**
	 * Creates an IndexDependencyModel for a list of EntityTypes.
	 *
	 * @param entityTypes the EntityTypes for which the IndexDependencyModel is created
	 */
	public DependencyModel(List<EntityType> entityTypes)
	{
		this.entityTypes = uniqueIndex(entityTypes, EntityType::getId);
	}

	private Set<String> getReferencingEntities(String entityTypeId)
	{
		ImmutableSet.Builder<String> result = ImmutableSet.builder();
		EntityType entityType = entityTypes.get(entityTypeId);
		if (entityType == null)
		{
			return emptySet();
		}
		for (Map.Entry<String, EntityType> candidate : entityTypes.entrySet())
		{
			EntityType candidateEntityType = candidate.getValue();
			if (hasAttributeThatReferences(candidateEntityType, entityTypeId))
			{
				if (candidateEntityType.isAbstract())
				{
					result.addAll(getDescendants(candidate.getKey()));
				}
				else
				{
					result.add(candidate.getKey());
				}
			}
		}
		return result.build();
	}

	private Set<String> getDescendants(String entityTypeId)
	{
		ImmutableSet.Builder<String> result = ImmutableSet.builder();
		for (Map.Entry<String, EntityType> candidate : entityTypes.entrySet())
		{
			EntityType candidateEntityType = candidate.getValue();
			if (extendsFrom(candidateEntityType, entityTypeId))
			{
				if (candidateEntityType.isAbstract())
				{
					result.addAll(getDescendants(candidate.getKey()));
				}
				else
				{
					result.add(candidate.getKey());
				}
			}
		}
		return result.build();
	}

	private boolean extendsFrom(EntityType candidateEntityType, String entityTypeId)
	{
		return candidateEntityType.getExtends() != null && entityTypeId
				.equals(candidateEntityType.getExtends().getId());
	}

	/**
	 * Determines if an entityType has an attribute that references another entity
	 *
	 * @param candidate
	 * @param entityTypeId
	 * @return
	 */
	private boolean hasAttributeThatReferences(EntityType candidate, String entityTypeId)
	{
		Iterable<Attribute> attributes = candidate.getOwnAtomicAttributes();
		return stream(attributes.spliterator(), false).map(Attribute::getRefEntity).filter(Objects::nonNull).
				map(EntityType::getId).anyMatch(entityTypeId::equals);
	}

	public Stream<String> getEntityTypesDependentOn(String entityTypeId)
	{
		return genericDependencyResolver.getAllDependants(entityTypeId, id -> entityTypes.get(id).getIndexingDepth(),
				this::getReferencingEntities).stream();
	}
}

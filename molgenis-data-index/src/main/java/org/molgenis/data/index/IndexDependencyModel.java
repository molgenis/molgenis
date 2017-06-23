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
import static java.util.stream.StreamSupport.stream;

/**
 * Models the dependencies between Entities.
 */
public class IndexDependencyModel
{
	private final Map<String, EntityType> entityTypes;
	private final GenericDependencyResolver genericDependencyResolver = new GenericDependencyResolver();

	/**
	 * Creates an IndexDependencyModel for a list of EntityTypes.
	 *
	 * @param entityTypes the EntityTypes for which the IndexDependencyModel is created
	 */
	public IndexDependencyModel(List<EntityType> entityTypes)
	{
		this.entityTypes = uniqueIndex(entityTypes, EntityType::getId);
	}

	private Set<String> getDirectDependencies(String entityTypeId)
	{
		ImmutableSet.Builder<String> result = ImmutableSet.builder();
		EntityType entityType = entityTypes.get(entityTypeId);
		Iterable<Attribute> attributes = entityType.getOwnAtomicAttributes();
		stream(attributes.spliterator(), false).map(Attribute::getRefEntity).filter(Objects::nonNull)
				.map(EntityType::getId).forEach(result::add);

		EntityType abstractParent = entityType.getExtends();
		if (abstractParent != null)
		{
			result.addAll(getDirectDependencies(abstractParent.getId()));
		}
		return result.build();
	}

	public Stream<String> getEntityTypesDependentOn(String entityTypeId)
	{
		return genericDependencyResolver.getAllDependants(entityTypeId, id -> entityTypes.get(id).getIndexingDepth(),
				this::getDirectDependencies).stream();
	}
}

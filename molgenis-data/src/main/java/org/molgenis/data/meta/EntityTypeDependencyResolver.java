package org.molgenis.data.meta;

import org.molgenis.data.meta.model.EntityType;
import org.molgenis.util.GenericDependencyResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.*;
import static java.util.stream.StreamSupport.stream;

/**
 * Sort {@link EntityType} collection based on their dependencies.
 */
@Component
public class EntityTypeDependencyResolver
{
	private final GenericDependencyResolver genericDependencyResolver;

	@Autowired
	public EntityTypeDependencyResolver(GenericDependencyResolver genericDependencyResolver)
	{
		this.genericDependencyResolver = requireNonNull(genericDependencyResolver);
	}

	/**
	 * Sort {@link EntityType} collection based on their dependencies.
	 *
	 * @param entityTypes entity meta data collection
	 * @return sorted entity meta data collection based on dependencies
	 */
	public List<EntityType> resolve(Collection<EntityType> entityTypes)
	{
		if (entityTypes.isEmpty())
		{
			return emptyList();
		}
		if (entityTypes.size() == 1)
		{
			return singletonList(entityTypes.iterator().next());
		}

		// EntityType doesn't have equals/hashcode methods, map to nodes first
		// ensure that nodes exist for all dependencies
		Set<EntityTypeNode> entityTypeNodes = entityTypes.stream().map(EntityTypeNode::new)
				.flatMap(node -> Stream.concat(Stream.of(node), EntityTypeNode.getDependencies(node).stream()))
				.collect(toSet());

		// Sort nodes based on dependencies
		List<EntityTypeNode> resolvedEntityMetaNodes = genericDependencyResolver
				.resolve(entityTypeNodes, EntityTypeNode::getDependencies);

		// Map nodes back to EntityType
		List<EntityType> resolvedEntityMetas = resolvedEntityMetaNodes.stream().map(EntityTypeNode::getEntityType)
				.collect(toList());

		// getDependencies might have included items that are not in the input list, remove additional items
		if (resolvedEntityMetas.size() == entityTypes.size())
		{
			return resolvedEntityMetas;
		}
		else
		{
			Map<String, EntityType> entityTypeMap = entityTypes.stream()
					.collect(toMap(EntityType::getName, Function.identity()));
			return resolvedEntityMetas.stream()
					.filter(resolvedEntityMeta -> entityTypeMap.containsKey(resolvedEntityMeta.getName()))
					.collect(toList());
		}
	}

	/**
	 * EntityType wrapper with equals/hashcode
	 */
	private static class EntityTypeNode
	{
		private final EntityType entityType;

		EntityTypeNode(EntityType entityType)
		{
			this.entityType = requireNonNull(entityType);
		}

		public EntityType getEntityType()
		{
			return entityType;
		}

		/**
		 * Returns dependencies of the given entity meta data.
		 *
		 * @param entityTypeNode entity meta data node
		 * @return dependencies of the entity meta data node
		 */
		public static Set<EntityTypeNode> getDependencies(EntityTypeNode entityTypeNode)
		{
			// get referenced entities excluding entities of mappedBy attributes
			EntityType entityType = entityTypeNode.entityType;
			Set<EntityTypeNode> refEntityMetaSet = stream(entityType.getOwnAllAttributes().spliterator(), false)
					.flatMap(attr ->
					{
						EntityType refEntity = attr.getRefEntity();
						if (refEntity != null && !attr.isMappedBy() && !refEntity.getName()
								.equals(entityType.getName()))
						{
							return Stream.of(new EntityTypeNode(refEntity));
						}
						else
						{
							return Stream.empty();
						}
					}).collect(toCollection(HashSet::new));

			EntityType extendsEntityMeta = entityType.getExtends();
			if (extendsEntityMeta != null)
			{
				refEntityMetaSet.add(new EntityTypeNode(extendsEntityMeta));
			}
			return refEntityMetaSet;
		}

		@Override
		public boolean equals(Object o)
		{
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			EntityTypeNode that = (EntityTypeNode) o;
			return entityType.getName().equals(that.entityType.getName());
		}

		@Override
		public int hashCode()
		{
			return entityType.getName().hashCode();
		}

		@Override
		public String toString()
		{
			return entityType.getName();
		}
	}
}

package org.molgenis.data.meta;

import com.google.common.collect.Sets;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.util.GenericDependencyResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
	private static final Logger LOG = LoggerFactory.getLogger(EntityTypeDependencyResolver.class);

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
		Set<EntityTypeNode> entityTypeNodes = entityTypes.stream()
														 .map(EntityTypeNode::new)
														 .flatMap(node -> Stream.concat(Stream.of(node),
																 expandEntityTypeDependencies(node).stream()))
														 .collect(toCollection(HashSet::new));

		// Sort nodes based on dependencies
		List<EntityTypeNode> resolvedEntityMetaNodes = genericDependencyResolver.resolve(entityTypeNodes,
				getDependencies());
		// Map nodes back to EntityType
		List<EntityType> resolvedEntityMetas = resolvedEntityMetaNodes.stream()
																	  .map(EntityTypeNode::getEntityType)
																	  .collect(toList());

		// getDependencies might have included items that are not in the input list, remove additional items
		if (resolvedEntityMetas.size() == entityTypes.size())
		{
			return resolvedEntityMetas;
		}
		else
		{
			Map<String, EntityType> entityTypeMap = entityTypes.stream()
															   .collect(toMap(EntityType::getId, Function.identity()));
			return resolvedEntityMetas.stream()
									  .filter(resolvedEntityMeta -> entityTypeMap.containsKey(
											  resolvedEntityMeta.getId()))
									  .collect(toList());
		}
	}

	/**
	 * Returns dependencies of the given entity meta data.
	 *
	 * @return dependencies of the entity meta data node
	 */
	private static Function<EntityTypeNode, Set<EntityTypeNode>> getDependencies()
	{
		return entityTypeNode ->
		{
			// get referenced entities excluding entities of mappedBy attributes
			EntityType entityType = entityTypeNode.getEntityType();
			Set<EntityTypeNode> refEntityMetaSet = stream(entityType.getOwnAllAttributes().spliterator(),
					false).flatMap(attr ->
			{
				EntityType refEntity = attr.getRefEntity();
				if (refEntity != null && !attr.isMappedBy() && !refEntity.getId().equals(entityType.getId()))
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
		};
	}

	/**
	 * Returns whole tree dependencies of the given entity meta data.
	 *
	 * @param entityTypeNode entity meta data node
	 * @return dependencies of the entity meta data node
	 */
	private static Set<EntityTypeNode> expandEntityTypeDependencies(EntityTypeNode entityTypeNode)
	{
		if (LOG.isTraceEnabled())
		{
			LOG.trace("expandEntityTypeDependencies(EntityTypeNode entityTypeNode) --- entity: [{}], skip: [{}]",
					entityTypeNode.getEntityType().getId(), entityTypeNode.isSkip());
		}

		if (!entityTypeNode.isSkip())
		{
			// get referenced entities excluding entities of mappedBy attributes
			EntityType entityType = entityTypeNode.getEntityType();
			Set<EntityTypeNode> refEntityMetaSet = stream(entityType.getOwnAllAttributes().spliterator(),
					false).flatMap(attr ->
			{
				EntityType refEntity = attr.getRefEntity();
				if (refEntity != null && !attr.isMappedBy() && !refEntity.getId().equals(entityType.getId()))
				{
					EntityTypeNode nodeRef = new EntityTypeNode(refEntity, entityTypeNode.getStack());
					Set<EntityTypeNode> dependenciesRef = expandEntityTypeDependencies(nodeRef);
					dependenciesRef.add(nodeRef);
					return dependenciesRef.stream();
				}
				else
				{
					return Stream.empty();
				}
			}).collect(toCollection(HashSet::new));

			EntityType extendsEntityMeta = entityType.getExtends();
			if (extendsEntityMeta != null)
			{
				EntityTypeNode nodeRef = new EntityTypeNode(extendsEntityMeta, entityTypeNode.getStack());

				// Add extended entity to set
				refEntityMetaSet.add(nodeRef);

				// Add dependencies of extended entity to set
				Set<EntityTypeNode> dependenciesRef = expandEntityTypeDependencies(nodeRef);
				refEntityMetaSet.addAll(dependenciesRef);
			}
			return refEntityMetaSet;
		}
		else
		{
			return Sets.newHashSet();
		}
	}

	/**
	 * EntityType wrapper with equals/hashcode
	 */
	private static class EntityTypeNode
	{
		private final EntityType entityType;
		private final Set<EntityTypeNode> stack;
		private boolean skip = false;

		private EntityTypeNode(EntityType entityType)
		{
			this(entityType, Sets.newHashSet());
		}

		private EntityTypeNode(EntityType entityType, Set<EntityTypeNode> stack)
		{
			this.entityType = requireNonNull(entityType);
			this.stack = requireNonNull(stack);

			// Check if EntityTypeNod is already used
			if (stack.contains(this))
			{
				skip = true;
			}
			this.stack.add(this);
		}

		private EntityType getEntityType()
		{
			return entityType;
		}

		@Override
		public boolean equals(Object o)
		{
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			EntityTypeNode that = (EntityTypeNode) o;
			return entityType.getId().equals(that.entityType.getId());
		}

		@Override
		public int hashCode()
		{
			return entityType.getId().hashCode();
		}

		@Override
		public String toString()
		{
			return entityType.getId();
		}

		private Set<EntityTypeNode> getStack()
		{
			return stack;
		}

		private boolean isSkip()
		{
			return skip;
		}
	}
}

package org.molgenis.data.meta;

import org.molgenis.data.meta.model.EntityMetaData;
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
import static org.molgenis.data.meta.model.AttributeMetaData.ATTRIBUTE_META_DATA;
import static org.molgenis.data.meta.model.EntityMetaDataMetaData.ENTITY_META_DATA;

/**
 * Sort {@link EntityMetaData} collection based on their dependencies.
 */
@Component
public class EntityMetaDataDependencyResolver
{
	private final GenericDependencyResolver genericDependencyResolver;

	@Autowired
	public EntityMetaDataDependencyResolver(GenericDependencyResolver genericDependencyResolver)
	{
		this.genericDependencyResolver = requireNonNull(genericDependencyResolver);
	}

	/**
	 * Sort {@link EntityMetaData} collection based on their dependencies.
	 *
	 * @param entityMetas entity meta data collection
	 * @return sorted entity meta data collection based on dependencies
	 */
	public List<EntityMetaData> resolve(Collection<EntityMetaData> entityMetas)
	{
		if (entityMetas.isEmpty())
		{
			return emptyList();
		}
		if (entityMetas.size() == 1)
		{
			return singletonList(entityMetas.iterator().next());
		}

		// EntityMetaData doesn't have equals/hashcode methods, map to nodes first
		// ensure that nodes exist for all dependencies
		Set<EntityMetaDataNode> entityMetaNodes = entityMetas.stream().map(EntityMetaDataNode::new)
				.flatMap(node -> Stream.concat(Stream.of(node), EntityMetaDataNode.getDependencies(node).stream()))
				.collect(toSet());

		// Sort nodes based on dependencies
		List<EntityMetaDataNode> resolvedEntityMetaNodes = genericDependencyResolver
				.resolve(entityMetaNodes, EntityMetaDataNode::getDependencies);

		// Map nodes back to EntityMetaData
		List<EntityMetaData> resolvedEntityMetas = resolvedEntityMetaNodes.stream()
				.map(EntityMetaDataNode::getEntityMetaData).collect(toList());

		// getDependencies might have included items that are not in the input list, remove additional items
		if (resolvedEntityMetas.size() == entityMetas.size())
		{
			return resolvedEntityMetas;
		}
		else
		{
			Map<String, EntityMetaData> entityMetaMap = entityMetas.stream()
					.collect(toMap(EntityMetaData::getName, Function.identity()));
			return resolvedEntityMetas.stream()
					.filter(resolvedEntityMeta -> entityMetaMap.containsKey(resolvedEntityMeta.getName()))
					.collect(toList());
		}
	}

	/**
	 * EntityMetaData wrapper with equals/hashcode
	 */
	private static class EntityMetaDataNode
	{
		private final EntityMetaData entityMetaData;

		EntityMetaDataNode(EntityMetaData entityMetaData)
		{
			this.entityMetaData = requireNonNull(entityMetaData);
		}

		public EntityMetaData getEntityMetaData()
		{
			return entityMetaData;
		}

		/**
		 * Returns dependencies of the given entity meta data.
		 *
		 * @param entityMetaDataNode entity meta data node
		 * @return dependencies of the entity meta data node
		 */
		public static Set<EntityMetaDataNode> getDependencies(EntityMetaDataNode entityMetaDataNode)
		{
			// get referenced entities excluding entities of mappedBy attributes
			EntityMetaData entityMeta = entityMetaDataNode.entityMetaData;
			Set<EntityMetaDataNode> refEntityMetaSet = stream(entityMeta.getOwnAllAttributes().spliterator(), false)
					.flatMap(attr ->
					{
						EntityMetaData refEntity = attr.getRefEntity();
						if (refEntity != null && !attr.isMappedBy() && !refEntity.getName()
								.equals(entityMeta.getName()))
						{
							// TODO remove workaround after http://www.molgenis.org/ticket/4479 is finished
							// workaround for the cyclic dependency between entity meta <--> attribute meta
							if (entityMeta.getName().equals(ATTRIBUTE_META_DATA) && refEntity.getName()
									.equals(ENTITY_META_DATA))
							{
								return Stream.empty();
							}
							return Stream.of(new EntityMetaDataNode(refEntity));
						}
						else
						{
							return Stream.empty();
						}
					}).collect(toCollection(HashSet::new));

			EntityMetaData extendsEntityMeta = entityMeta.getExtends();
			if (extendsEntityMeta != null)
			{
				refEntityMetaSet.add(new EntityMetaDataNode(extendsEntityMeta));
			}
			return refEntityMetaSet;
		}

		@Override
		public boolean equals(Object o)
		{
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			EntityMetaDataNode that = (EntityMetaDataNode) o;
			return entityMetaData.getName().equals(that.entityMetaData.getName());
		}

		@Override
		public int hashCode()
		{
			return entityMetaData.getName().hashCode();
		}

		@Override
		public String toString()
		{
			return entityMetaData.getName();
		}
	}
}

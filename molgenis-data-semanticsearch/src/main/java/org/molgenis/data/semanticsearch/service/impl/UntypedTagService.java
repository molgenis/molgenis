package org.molgenis.data.semanticsearch.service.impl;

import static java.util.stream.StreamSupport.stream;
import static org.molgenis.data.meta.EntityMetaDataMetaData.ATTRIBUTES;
import static org.molgenis.data.meta.EntityMetaDataMetaData.ENTITY_NAME;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Package;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.meta.AttributeMetaDataMetaData;
import org.molgenis.data.meta.EntityMetaDataMetaData;
import org.molgenis.data.meta.PackageMetaData;
import org.molgenis.data.semantic.LabeledResource;
import org.molgenis.data.semantic.Relation;
import org.molgenis.data.semantic.Tag;
import org.molgenis.data.semantic.TagImpl;
import org.molgenis.data.semanticsearch.repository.TagRepository;
import org.molgenis.data.semanticsearch.service.TagService;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.core.runas.RunAsSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

/**
 * Service to tag metadata with simple String terms.
 */
public class UntypedTagService implements TagService<LabeledResource, LabeledResource>
{
	private static final Logger LOG = LoggerFactory.getLogger(UntypedTagService.class);

	private final DataService dataService;
	private final TagRepository tagRepository;

	public UntypedTagService(DataService dataService, TagRepository tagRepository)
	{
		this.dataService = dataService;
		this.tagRepository = tagRepository;
	}

	private Entity findAttributeEntity(EntityMetaData entityMetaData, String attributeName)
	{
		Entity entityMetaDataEntity = dataService.findOne(ENTITY_NAME, entityMetaData.getName());
		Optional<Entity> result = stream(entityMetaDataEntity.getEntities(ATTRIBUTES).spliterator(), false).filter(
				att -> attributeName.equals(att.getString(AttributeMetaDataMetaData.NAME))).findFirst();
		return result.isPresent() ? result.get() : null;
	}

	private Entity findEntity(EntityMetaData emd)
	{
		return dataService.findOne(EntityMetaDataMetaData.ENTITY_NAME, emd.getName());
	}

	@Override
	public void removeAttributeTag(EntityMetaData entityMetaData,
			Tag<AttributeMetaData, LabeledResource, LabeledResource> removeTag)
	{
		AttributeMetaData attributeMetaData = removeTag.getSubject();
		Entity attributeEntity = findAttributeEntity(entityMetaData, attributeMetaData.getName());
		List<Entity> tags = new ArrayList<Entity>();
		for (Entity tagEntity : attributeEntity.getEntities(AttributeMetaDataMetaData.TAGS))
		{
			Tag<AttributeMetaData, LabeledResource, LabeledResource> tag = TagImpl.asTag(attributeMetaData, tagEntity);
			if (!removeTag.equals(tag))
			{
				tags.add(tagEntity);
			}
		}
		attributeEntity.set(AttributeMetaDataMetaData.TAGS, tags);
		dataService.update(AttributeMetaDataMetaData.ENTITY_NAME, attributeEntity);
	}

	@Override
	@RunAsSystem
	public Multimap<Relation, LabeledResource> getTagsForAttribute(EntityMetaData entityMetaData,
			AttributeMetaData attributeMetaData)
	{
		Entity entity = findAttributeEntity(entityMetaData, attributeMetaData.getName());
		if (entity == null) return ArrayListMultimap.<Relation, LabeledResource> create();

		Multimap<Relation, LabeledResource> tags = ArrayListMultimap.<Relation, LabeledResource> create();
		for (Entity tagEntity : entity.getEntities(AttributeMetaDataMetaData.TAGS))
		{
			TagImpl<AttributeMetaData, LabeledResource, LabeledResource> tag = TagImpl.asTag(attributeMetaData,
					tagEntity);
			tags.put(tag.getRelation(), tag.getObject());
		}
		return tags;
	}

	@Override
	@RunAsSystem
	public Iterable<Tag<EntityMetaData, LabeledResource, LabeledResource>> getTagsForEntity(
			EntityMetaData entityMetaData)
	{
		List<Tag<EntityMetaData, LabeledResource, LabeledResource>> result = new ArrayList<Tag<EntityMetaData, LabeledResource, LabeledResource>>();
		Entity entity = findEntity(entityMetaData);
		if (entity == null)
		{
			LOG.warn("No known entity with name " + entityMetaData.getName() + ".");
		}
		else
		{
			for (Entity tagEntity : entity.getEntities(EntityMetaDataMetaData.TAGS))
			{
				result.add(TagImpl.asTag(entityMetaData, tagEntity));
			}
		}
		return result;
	}

	@Override
	public void addAttributeTag(EntityMetaData entityMetaData,
			Tag<AttributeMetaData, LabeledResource, LabeledResource> tag)
	{
		Entity entity = findAttributeEntity(entityMetaData, tag.getSubject().getName());
		List<Entity> tags = new ArrayList<Entity>();
		for (Entity tagEntity : entity.getEntities(AttributeMetaDataMetaData.TAGS))
		{
			tags.add(tagEntity);
		}
		tags.add(getTagEntity(tag));
		entity.set(AttributeMetaDataMetaData.TAGS, tags);
		dataService.update(AttributeMetaDataMetaData.ENTITY_NAME, entity);
	}

	@Override
	public void addEntityTag(Tag<EntityMetaData, LabeledResource, LabeledResource> tag)
	{
		Entity entity = findEntity(tag.getSubject());
		if (entity == null)
		{
			throw new UnknownEntityException("Unknown entity [" + tag.getSubject().getName() + "]");
		}
		ImmutableList<Tag<EntityMetaData, LabeledResource, LabeledResource>> existingTags = ImmutableList
				.<Tag<EntityMetaData, LabeledResource, LabeledResource>> copyOf(getTagsForEntity(tag.getSubject()));
		if (existingTags.contains(tag))
		{
			LOG.debug("Tag already present");
			return;
		}

		ImmutableList.Builder<Entity> builder = ImmutableList.<Entity> builder();
		builder.addAll(entity.getEntities(EntityMetaDataMetaData.TAGS));
		builder.add(getTagEntity(tag));
		entity.set(EntityMetaDataMetaData.TAGS, builder.build());
		dataService.update(EntityMetaDataMetaData.ENTITY_NAME, entity);
	}

	public Entity getTagEntity(Tag<?, LabeledResource, LabeledResource> tag)
	{
		return tagRepository.getTagEntity(tag.getObject().getIri(), tag.getObject().getLabel(), tag.getRelation(), tag
				.getCodeSystem().getIri());
	}

	@Override
	@RunAsSystem
	public Iterable<Tag<Package, LabeledResource, LabeledResource>> getTagsForPackage(Package p)
	{
		Entity packageEntity = dataService.findOne(PackageMetaData.ENTITY_NAME,
				new QueryImpl().eq(PackageMetaData.FULL_NAME, p.getName()));

		if (packageEntity == null)
		{
			throw new UnknownEntityException("Unknown package [" + p.getName() + "]");
		}

		List<Tag<Package, LabeledResource, LabeledResource>> tags = Lists.newArrayList();
		for (Entity tagEntity : packageEntity.getEntities(PackageMetaData.TAGS))
		{
			tags.add(TagImpl.asTag(p, tagEntity));
		}

		return tags;
	}

	@Override
	public void removeEntityTag(Tag<EntityMetaData, LabeledResource, LabeledResource> tag)
	{
		throw new UnsupportedOperationException("not yet implemented");
	}

	@Override
	public void removeAllTagsFromEntity(String entityName)
	{
		// TODO Auto-generated method stub

	}
}

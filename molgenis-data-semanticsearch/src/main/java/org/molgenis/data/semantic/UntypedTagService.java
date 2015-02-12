package org.molgenis.data.semantic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Package;
import org.molgenis.data.Query;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.meta.AttributeMetaDataMetaData;
import org.molgenis.data.meta.EntityMetaDataMetaData;
import org.molgenis.data.meta.PackageMetaData;
import org.molgenis.data.support.QueryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

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
		Query q = new QueryImpl().eq(AttributeMetaDataMetaData.ENTITY, entityMetaData.getName()).and()
				.eq(AttributeMetaDataMetaData.NAME, attributeName);
		Entity entity = dataService.findOne(AttributeMetaDataMetaData.ENTITY_NAME, q);
		return entity;
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
	public Iterable<Tag<AttributeMetaData, LabeledResource, LabeledResource>> getTagsForAttribute(
			EntityMetaData entityMetaData, AttributeMetaData attributeMetaData)
	{
		Entity entity = findAttributeEntity(entityMetaData, attributeMetaData.getName());
		if (entity == null) return Collections.<Tag<AttributeMetaData, LabeledResource, LabeledResource>> emptyList();

		List<Tag<AttributeMetaData, LabeledResource, LabeledResource>> tags = new ArrayList<Tag<AttributeMetaData, LabeledResource, LabeledResource>>();
		for (Entity tagEntity : entity.getEntities(AttributeMetaDataMetaData.TAGS))
		{
			tags.add(TagImpl.asTag(attributeMetaData, tagEntity));
		}
		return tags;
	}

	@Override
	public Iterable<Tag<EntityMetaData, LabeledResource, LabeledResource>> getTagsForEntity(
			EntityMetaData entityMetaData)
	{
		Entity entity = findEntity(entityMetaData);
		if (entity == null)
		{
			throw new UnknownEntityException("No known entity with name " + entityMetaData.getName() + ".");
		}
		List<Tag<EntityMetaData, LabeledResource, LabeledResource>> tags = new ArrayList<Tag<EntityMetaData, LabeledResource, LabeledResource>>();
		for (Entity tagEntity : entity.getEntities(EntityMetaDataMetaData.TAGS))
		{
			tags.add(TagImpl.asTag(entityMetaData, tagEntity));
		}
		return tags;
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
}

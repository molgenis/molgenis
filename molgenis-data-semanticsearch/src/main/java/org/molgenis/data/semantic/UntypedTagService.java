package org.molgenis.data.semantic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.CrudRepository;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Query;
import org.molgenis.data.meta.AttributeMetaDataMetaData;
import org.molgenis.data.meta.TagMetaData;
import org.molgenis.data.support.QueryImpl;

/**
 * Service to tag metadata with simple String terms.
 */
public class UntypedTagService implements TagService<LabeledResource, LabeledResource>
{
	private CrudRepository repository;
	private TagRepository tagRepository;

	public UntypedTagService(DataService dataService, TagRepository tagRepository)
	{
		this.repository = dataService.getCrudRepository(AttributeMetaDataMetaData.ENTITY_NAME);
		this.tagRepository = tagRepository;
	}

	private TagImpl<AttributeMetaData, LabeledResource, LabeledResource> asTag(AttributeMetaData attributeMetaData,
			Entity tagEntity)
	{
		Relation relation = Relation.forIRI(tagEntity.getString(TagMetaData.RELATION_IRI));
		LabeledResource codeSystem = new LabeledResource(tagEntity.getString(TagMetaData.CODE_SYSTEM));
		LabeledResource objectResource = new LabeledResource(tagEntity.getString(TagMetaData.OBJECT_IRI),
				tagEntity.getString(TagMetaData.LABEL));

		TagImpl<AttributeMetaData, LabeledResource, LabeledResource> tag = new TagImpl<AttributeMetaData, LabeledResource, LabeledResource>(
				attributeMetaData, relation, objectResource, codeSystem);
		return tag;
	}

	private Entity findAttributeEntity(String entityName, String attributeName)
	{
		Query q = new QueryImpl().eq(AttributeMetaDataMetaData.ENTITY, entityName).and()
				.eq(AttributeMetaDataMetaData.NAME, attributeName);
		Entity entity = repository.findOne(q);
		return entity;
	}

	@Override
	public void removeAttributeTag(EntityMetaData entityMetaData,
			Tag<AttributeMetaData, LabeledResource, LabeledResource> removeTag)
	{
		AttributeMetaData attributeMetaData = removeTag.getSubject();
		Entity attributeEntity = findAttributeEntity(entityMetaData.getName(), attributeMetaData.getName());
		List<Entity> tags = new ArrayList<Entity>();
		for (Entity tagEntity : attributeEntity.getEntities(AttributeMetaDataMetaData.TAGS))
		{
			Tag<AttributeMetaData, LabeledResource, LabeledResource> tag = asTag(attributeMetaData, tagEntity);
			if (!removeTag.equals(tag))
			{
				tags.add(tagEntity);
			}
		}
		attributeEntity.set(AttributeMetaDataMetaData.TAGS, tags);
		repository.update(attributeEntity);
	}

	@Override
	public Iterable<Tag<AttributeMetaData, LabeledResource, LabeledResource>> getTagsForAttribute(
			EntityMetaData entityMetaData, AttributeMetaData attributeMetaData)
	{
		Entity entity = findAttributeEntity(entityMetaData.getName(), attributeMetaData.getName());
		if (entity == null) return Collections.<Tag<AttributeMetaData, LabeledResource, LabeledResource>> emptyList();

		List<Tag<AttributeMetaData, LabeledResource, LabeledResource>> tags = new ArrayList<Tag<AttributeMetaData, LabeledResource, LabeledResource>>();
		for (Entity tagEntity : entity.getEntities(AttributeMetaDataMetaData.TAGS))
		{
			tags.add(asTag(attributeMetaData, tagEntity));
		}
		return tags;
	}

	@Override
	public void addAttributeTag(EntityMetaData entityMetaData,
			Tag<AttributeMetaData, LabeledResource, LabeledResource> tag)
	{
		Entity entity = findAttributeEntity(entityMetaData.getName(), tag.getSubject().getName());
		List<Entity> tags = new ArrayList<Entity>();
		for (Entity tagEntity : entity.getEntities(AttributeMetaDataMetaData.TAGS))
		{
			tags.add(tagEntity);
		}
		tags.add(getTagEntity(tag));
		entity.set(AttributeMetaDataMetaData.TAGS, tags);
		repository.update(entity);
	}

	public Entity getTagEntity(Tag<?, LabeledResource, LabeledResource> tag)
	{
		return tagRepository.getTagEntity(tag.getObject().getIri(), tag.getObject().getLabel(), tag.getRelation(), tag
				.getCodeSystem().getIri());
	}
}

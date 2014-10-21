package org.molgenis.data.semantic;

import java.util.ArrayList;
import java.util.List;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.CrudRepository;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Query;
import org.molgenis.data.meta.AttributeMetaDataMetaData;
import org.molgenis.data.meta.TagMetaData;
import org.molgenis.data.support.QueryImpl;

public class OntologyTagService implements TagService<OntologyTerm, Ontology>
{
	private CrudRepository repository;
	private TagRepository tagRepository;
	private OntologyService ontologyService;

	public OntologyTagService(CrudRepository repository, TagRepository tagRepository, OntologyService ontologyService)
	{
		super();
		this.repository = repository;
		this.tagRepository = tagRepository;
		this.ontologyService = ontologyService;
	}

	public Iterable<Tag<AttributeMetaData, OntologyTerm, Ontology>> getOntologyTagsForAttribute(
			EntityMetaData entityMetaData, AttributeMetaData attributeMetaData)
	{
		Entity entity = findAttributeEntity(entityMetaData.getName(), attributeMetaData.getName());
		List<Tag<AttributeMetaData, OntologyTerm, Ontology>> tags = new ArrayList<Tag<AttributeMetaData, OntologyTerm, Ontology>>();
		for (Entity tagEntity : entity.getEntities(AttributeMetaDataMetaData.TAGS))
		{
			tags.add(asTag(attributeMetaData, tagEntity));
		}
		return tags;
	}

	private TagImpl<AttributeMetaData, OntologyTerm, Ontology> asTag(AttributeMetaData attributeMetaData,
			Entity tagEntity)
	{
		Relation relation = Relation.forIRI(tagEntity.getString(TagMetaData.RELATION_IRI));
		Ontology ontology = ontologyService.getOntology(tagEntity.getString(TagMetaData.CODE_SYSTEM));
		OntologyTerm ontologyTerm = ontologyService.getOntologyTerm(tagEntity.getString(TagMetaData.OBJECT_IRI),
				ontology.getIri());
		TagImpl<AttributeMetaData, OntologyTerm, Ontology> tag = new TagImpl<AttributeMetaData, OntologyTerm, Ontology>(
				attributeMetaData, relation, ontologyTerm, ontology);
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
			Tag<AttributeMetaData, OntologyTerm, Ontology> removeTag)
	{
		AttributeMetaData attributeMetaData = removeTag.getSubject();
		Entity attributeEntity = findAttributeEntity(entityMetaData.getName(), attributeMetaData.getName());
		List<Entity> tags = new ArrayList<Entity>();
		for (Entity tagEntity : attributeEntity.getEntities(AttributeMetaDataMetaData.TAGS))
		{
			Tag<AttributeMetaData, OntologyTerm, Ontology> tag = asTag(attributeMetaData, tagEntity);
			if (!removeTag.equals(tag))
			{
				tags.add(tagEntity);
			}
		}
		attributeEntity.set(AttributeMetaDataMetaData.TAGS, tags);
		repository.update(attributeEntity);
	}

	public void addAttributeTag(EntityMetaData entityMetaData, AttributeMetaData subject, Entity attributeTagEntity)
	{
		Entity entity = findAttributeEntity(entityMetaData.getName(), subject.getName());
		List<Entity> tags = new ArrayList<Entity>();
		for (Entity tagEntity : entity.getEntities(AttributeMetaDataMetaData.TAGS))
		{
			tags.add(tagEntity);
		}
		tags.add(attributeTagEntity);
		entity.set(AttributeMetaDataMetaData.TAGS, tags);
		repository.update(entity);
	}

	@Override
	public Iterable<Tag<AttributeMetaData, OntologyTerm, Ontology>> getTagsForAttribute(EntityMetaData entityMetaData,
			AttributeMetaData attributeMetaData)
	{
		return getOntologyTagsForAttribute(entityMetaData, attributeMetaData);
	}

	@Override
	public void addAttributeTag(EntityMetaData entityMetaData, Tag<AttributeMetaData, OntologyTerm, Ontology> tag)
	{
		addAttributeTag(entityMetaData, tag.getSubject(), tagRepository.getTagEntity(tag));
	}
}

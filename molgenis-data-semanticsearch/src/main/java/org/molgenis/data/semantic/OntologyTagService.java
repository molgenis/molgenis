package org.molgenis.data.semantic;

import static java.util.stream.StreamSupport.stream;
import static org.molgenis.data.meta.EntityMetaDataMetaData.ATTRIBUTES;
import static org.molgenis.data.meta.EntityMetaDataMetaData.ENTITY_NAME;

import java.util.ArrayList;
import java.util.List;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Package;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.meta.AttributeMetaDataMetaData;
import org.molgenis.data.meta.PackageMetaData;
import org.molgenis.data.meta.TagMetaData;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.ontology.Ontology;
import org.molgenis.ontology.OntologyService;
import org.molgenis.ontology.OntologyTerm;

import com.google.common.collect.Lists;

/**
 * Service to tag metadata with ontology terms.
 */
public class OntologyTagService implements TagService<OntologyTerm, Ontology>
{
	private final DataService dataService;
	private final TagRepository tagRepository;
	private final OntologyService ontologyService;

	public OntologyTagService(DataService dataService, OntologyService ontologyService, TagRepository tagRepository)
	{
		this.dataService = dataService;
		this.tagRepository = tagRepository;
		this.ontologyService = ontologyService;
	}

	private Entity findAttributeEntity(String entityName, String attributeName)
	{
		Entity entityMetaDataEntity = dataService.findOne(ENTITY_NAME, entityName);
		return stream(entityMetaDataEntity.getEntities(ATTRIBUTES).spliterator(), false)
				.filter(att -> attributeName.equals(att.getString(AttributeMetaDataMetaData.NAME))).findFirst().get();
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
		dataService.update(AttributeMetaDataMetaData.ENTITY_NAME, attributeEntity);
	}

	@Override
	public Iterable<Tag<AttributeMetaData, OntologyTerm, Ontology>> getTagsForAttribute(EntityMetaData entityMetaData,
			AttributeMetaData attributeMetaData)
	{
		Entity entity = findAttributeEntity(entityMetaData.getName(), attributeMetaData.getName());
		List<Tag<AttributeMetaData, OntologyTerm, Ontology>> tags = new ArrayList<Tag<AttributeMetaData, OntologyTerm, Ontology>>();
		for (Entity tagEntity : entity.getEntities(AttributeMetaDataMetaData.TAGS))
		{
			tags.add(asTag(attributeMetaData, tagEntity));
		}
		return tags;
	}

	@Override
	public Iterable<Tag<Package, OntologyTerm, Ontology>> getTagsForPackage(Package p)
	{
		Entity packageEntity = dataService.findOne(PackageMetaData.ENTITY_NAME,
				new QueryImpl().eq(PackageMetaData.FULL_NAME, p.getName()));

		if (packageEntity == null)
		{
			throw new UnknownEntityException("Unknown package [" + p.getName() + "]");
		}

		List<Tag<Package, OntologyTerm, Ontology>> tags = Lists.newArrayList();
		for (Entity tagEntity : packageEntity.getEntities(PackageMetaData.TAGS))
		{
			tags.add(asTag(p, tagEntity));
		}

		return tags;
	}

	private <SubjectType> TagImpl<SubjectType, OntologyTerm, Ontology> asTag(SubjectType subjectType, Entity tagEntity)
	{
		String identifier = tagEntity.getString(TagMetaData.IDENTIFIER);
		Relation relation = Relation.forIRI(tagEntity.getString(TagMetaData.RELATION_IRI));
		Ontology ontology = ontologyService.getOntology(tagEntity.getString(TagMetaData.CODE_SYSTEM));
		OntologyTerm ontologyTerm = ontologyService.getOntologyTerm(tagEntity.getString(TagMetaData.OBJECT_IRI),
				ontology.getIri());

		return new TagImpl<SubjectType, OntologyTerm, Ontology>(identifier, subjectType, relation, ontologyTerm,
				ontology);
	}

	@Override
	public void addAttributeTag(EntityMetaData entityMetaData, Tag<AttributeMetaData, OntologyTerm, Ontology> tag)
	{
		Entity entity = findAttributeEntity(entityMetaData.getName(), tag.getSubject().getName());
		List<Entity> tags = new ArrayList<Entity>();
		for (Entity tagEntity : entity.getEntities(AttributeMetaDataMetaData.TAGS))
		{
			tags.add(tagEntity);
		}
		tags.add(getTagEntity(tag));
		entity.set(AttributeMetaDataMetaData.TAGS, tags);
		dataService.update(AttributeMetaDataMetaData.ENTITY_NAME, entity);
	}

	public Entity getTagEntity(Tag<?, OntologyTerm, Ontology> tag)
	{
		return tagRepository.getTagEntity(tag.getObject().getIRI(), tag.getObject().getLabel(), tag.getRelation(), tag
				.getCodeSystem().getIri());
	}

	@Override
	public void addEntityTag(Tag<EntityMetaData, OntologyTerm, Ontology> tag)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void removeEntityTag(Tag<EntityMetaData, OntologyTerm, Ontology> tag)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public Iterable<Tag<EntityMetaData, LabeledResource, LabeledResource>> getTagsForEntity(
			EntityMetaData entityMetaData)
	{
		// TODO Auto-generated method stub
		return null;
	}

}

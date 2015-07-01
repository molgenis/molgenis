package org.molgenis.data.semanticsearch.service.impl;

import static java.util.Collections.emptyList;
import static java.util.stream.StreamSupport.stream;
import static org.molgenis.data.meta.EntityMetaDataMetaData.ATTRIBUTES;
import static org.molgenis.data.meta.EntityMetaDataMetaData.ENTITY_NAME;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Stream;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.IdGenerator;
import org.molgenis.data.Package;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.meta.AttributeMetaDataMetaData;
import org.molgenis.data.meta.EntityMetaDataMetaData;
import org.molgenis.data.meta.PackageMetaData;
import org.molgenis.data.meta.TagMetaData;
import org.molgenis.data.semantic.LabeledResource;
import org.molgenis.data.semantic.Relation;
import org.molgenis.data.semantic.Tag;
import org.molgenis.data.semantic.TagImpl;
import org.molgenis.data.semanticsearch.repository.TagRepository;
import org.molgenis.data.semanticsearch.semantic.OntologyTag;
import org.molgenis.data.semanticsearch.service.OntologyTagService;
import org.molgenis.data.support.DefaultEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.ontology.core.model.Ontology;
import org.molgenis.ontology.core.model.OntologyTerm;
import org.molgenis.ontology.core.service.OntologyService;
import org.molgenis.security.core.runas.RunAsSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Maps;

/**
 * Service to tag metadata with ontology terms.
 */
public class OntologyTagServiceImpl implements OntologyTagService
{
	private final DataService dataService;
	private final TagRepository tagRepository;
	private final OntologyService ontologyService;
	private final IdGenerator idGenerator;

	private static final Logger LOG = LoggerFactory.getLogger(OntologyTagServiceImpl.class);

	public OntologyTagServiceImpl(DataService dataService, OntologyService ontologyService,
			TagRepository tagRepository, IdGenerator idGenerator)
	{
		this.dataService = dataService;
		this.tagRepository = tagRepository;
		this.ontologyService = ontologyService;
		this.idGenerator = idGenerator;
	}

	@Override
	public void removeAttributeTag(String entity, String attribute, String relationIRI, String ontologyTermIRI)
	{
		Entity attributeEntity = findAttributeEntity(entity, attribute);
		Iterable<Entity> tags = attributeEntity.getEntities(AttributeMetaDataMetaData.TAGS);
		Iterable<Entity> newTags = Iterables.filter(tags, e -> !isSameTag(relationIRI, ontologyTermIRI, e));
		attributeEntity.set(AttributeMetaDataMetaData.TAGS, newTags);
		dataService.update(AttributeMetaDataMetaData.ENTITY_NAME, attributeEntity);
		updateEntityMetaDataEntityWithNewAttributeEntity(entity, attribute, attributeEntity);
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
	@RunAsSystem
	public Multimap<Relation, OntologyTerm> getTagsForAttribute(EntityMetaData entityMetaData,
			AttributeMetaData attributeMetaData)
	{
		Multimap<Relation, OntologyTerm> tags = LinkedHashMultimap.<Relation, OntologyTerm> create();
		Entity entity = findAttributeEntity(entityMetaData.getName(), attributeMetaData.getName());
		if (entity == null)
		{
			LOG.warn("Cannot find attribute {}.{}", entityMetaData.getName(), attributeMetaData.getName());
			return tags;
		}
		for (Entity tagEntity : entity.getEntities(AttributeMetaDataMetaData.TAGS))
		{
			Tag<AttributeMetaData, OntologyTerm, Ontology> tag = asTag(attributeMetaData, tagEntity);
			tags.put(tag.getRelation(), tag.getObject());
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

	@Override
	public OntologyTag addAttributeTag(String entity, String attribute, String relationIRI,
			List<String> ontologyTermIRIs)
	{
		boolean added = false;
		Entity attributeEntity = findAttributeEntity(entity, attribute);
		Entity tagEntity = new DefaultEntity(TagRepository.META_DATA, dataService);
		Stream<OntologyTerm> terms = ontologyTermIRIs.stream().map(ontologyService::getOntologyTerm);
		OntologyTerm combinedOntologyTerm = OntologyTerm.and(terms.toArray(OntologyTerm[]::new));
		Relation relation = Relation.forIRI(relationIRI);
		tagEntity.set(TagMetaData.IDENTIFIER, idGenerator.generateId());
		tagEntity.set(TagMetaData.CODE_SYSTEM, null);
		tagEntity.set(TagMetaData.RELATION_IRI, relation.getIRI());
		tagEntity.set(TagMetaData.RELATION_LABEL, relation.getLabel());
		tagEntity.set(TagMetaData.LABEL, combinedOntologyTerm.getLabel());
		tagEntity.set(TagMetaData.OBJECT_IRI, combinedOntologyTerm.getIRI());
		dataService.add(TagMetaData.ENTITY_NAME, tagEntity);

		Map<String, Entity> tags = Maps.<String, Entity> newHashMap();
		for (Entity tag : attributeEntity.getEntities(AttributeMetaDataMetaData.TAGS))
		{
			tags.put(tag.get(TagMetaData.OBJECT_IRI).toString(), tag);
		}
		if (!tags.containsKey(tagEntity.get(TagMetaData.OBJECT_IRI).toString()))
		{
			tags.put(tagEntity.get(TagMetaData.OBJECT_IRI).toString(), tagEntity);
			added = true;
		}
		attributeEntity.set(AttributeMetaDataMetaData.TAGS, tags.values());
		dataService.update(AttributeMetaDataMetaData.ENTITY_NAME, attributeEntity);
		updateEntityMetaDataEntityWithNewAttributeEntity(entity, attribute, attributeEntity);
		return added ? OntologyTag.create(combinedOntologyTerm, relation) : null;
	}

	public Entity getTagEntity(Tag<?, OntologyTerm, Ontology> tag)
	{
		return tagRepository.getTagEntity(tag.getObject().getIRI(), tag.getObject().getLabel(), tag.getRelation(), tag
				.getCodeSystem().getIRI());
	}

	@Override
	public void removeAllTagsFromEntity(String entityName)
	{
		EntityMetaData entityMetadata = dataService.getEntityMetaData(entityName);
		Iterable<AttributeMetaData> attributeMetaDatas = entityMetadata.getAtomicAttributes();

		for (AttributeMetaData attributeMetaData : attributeMetaDatas)
		{
			Entity attributeEntity = findAttributeEntity(entityName, attributeMetaData.getName());
			attributeEntity.set(AttributeMetaDataMetaData.TAGS, emptyList());
			dataService.update(AttributeMetaDataMetaData.ENTITY_NAME, attributeEntity);
			updateEntityMetaDataEntityWithNewAttributeEntity(entityName, attributeMetaData.getName(), attributeEntity);
		}
	}

	public Map<String, OntologyTag> tagAttributesInEntity(String entity, Map<AttributeMetaData, OntologyTerm> tags)
	{
		Map<String, OntologyTag> result = new LinkedHashMap<>();
		for (Entry<AttributeMetaData, OntologyTerm> tag : tags.entrySet())
		{

			OntologyTerm ontologyTerm = tag.getValue();
			OntologyTag ontologyTag = addAttributeTag(entity, tag.getKey().getName(),
					Relation.isAssociatedWith.getIRI(), Collections.singletonList(ontologyTerm.getIRI()));
			result.put(tag.getKey().getName(), ontologyTag);
		}
		return result;
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

	/**
	 * The attribute just got updated, but the entity does not know this yet. To reindex this document in elasticsearch,
	 * update it.
	 * 
	 * @param entity
	 *            name of the entity
	 * @param attribute
	 *            the name of the attribute that got changed
	 * @param attributeEntity
	 *            the entity of the attribute that got changed
	 */
	private void updateEntityMetaDataEntityWithNewAttributeEntity(String entity, String attribute,
			Entity attributeEntity)
	{
		Entity entityEntity = dataService.findOne(EntityMetaDataMetaData.ENTITY_NAME, entity);
		Iterable<Entity> attributes = entityEntity.getEntities(ATTRIBUTES);
		entityEntity.set(ATTRIBUTES, Iterables.transform(attributes,
				att -> att.getString(AttributeMetaDataMetaData.NAME).equals(attribute) ? attributeEntity : att));
		dataService.update(EntityMetaDataMetaData.ENTITY_NAME, entityEntity);
	}

	private boolean isSameTag(String relationIRI, String ontologyTermIRI, Entity e)
	{
		return ontologyTermIRI.equals(e.getString(TagMetaData.OBJECT_IRI))
				&& relationIRI.equals(e.getString(TagMetaData.RELATION_IRI));
	}

	@RunAsSystem
	private Entity findAttributeEntity(String entityName, String attributeName)
	{
		Entity entityMetaDataEntity = dataService.findOne(ENTITY_NAME, entityName);
		Optional<Entity> result = stream(entityMetaDataEntity.getEntities(ATTRIBUTES).spliterator(), false).filter(
				att -> attributeName.equals(att.getString(AttributeMetaDataMetaData.NAME))).findFirst();
		return result.isPresent() ? result.get() : null;
	}

	private <SubjectType> TagImpl<SubjectType, OntologyTerm, Ontology> asTag(SubjectType subjectType, Entity tagEntity)
	{
		String identifier = tagEntity.getString(TagMetaData.IDENTIFIER);
		Relation relation = asRelation(tagEntity);
		Ontology ontology = asOntology(tagEntity);
		OntologyTerm ontologyTerm = asOntologyTerm(tagEntity);
		if (relation == null || ontologyTerm == null)
		{
			return null;
		}
		return new TagImpl<SubjectType, OntologyTerm, Ontology>(identifier, subjectType, relation, ontologyTerm,
				ontology);
	}

	private static Relation asRelation(Entity tagEntity)
	{
		String relationIRI = tagEntity.getString(TagMetaData.RELATION_IRI);
		if (relationIRI == null)
		{
			return null;
		}
		return Relation.forIRI(relationIRI);
	}

	private OntologyTerm asOntologyTerm(Entity tagEntity)
	{
		String objectIRI = tagEntity.getString(TagMetaData.OBJECT_IRI);
		if (objectIRI == null)
		{
			return null;
		}
		return ontologyService.getOntologyTerm(objectIRI);
	}

	private Ontology asOntology(Entity tagEntity)
	{
		String codeSystemIRI = tagEntity.getString(TagMetaData.CODE_SYSTEM);
		if (codeSystemIRI == null)
		{
			return null;
		}
		return ontologyService.getOntology(codeSystemIRI);
	}
}

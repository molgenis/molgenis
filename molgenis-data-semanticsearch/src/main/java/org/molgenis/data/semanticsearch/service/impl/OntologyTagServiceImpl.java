package org.molgenis.data.semanticsearch.service.impl;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.meta.model.*;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.populate.IdGenerator;
import org.molgenis.data.semantic.LabeledResource;
import org.molgenis.data.semantic.Relation;
import org.molgenis.data.semantic.SemanticTag;
import org.molgenis.data.semanticsearch.repository.TagRepository;
import org.molgenis.data.semanticsearch.semantic.OntologyTag;
import org.molgenis.data.semanticsearch.service.OntologyTagService;
import org.molgenis.ontology.core.model.Ontology;
import org.molgenis.ontology.core.model.OntologyTerm;
import org.molgenis.ontology.core.service.OntologyService;
import org.molgenis.security.core.runas.RunAsSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Stream;

import static com.google.common.collect.LinkedHashMultimap.create;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.StreamSupport.stream;
import static org.molgenis.data.meta.model.AttributeMetadata.ATTRIBUTE_META_DATA;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ATTRIBUTES;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ENTITY_TYPE_META_DATA;
import static org.molgenis.data.meta.model.PackageMetadata.PACKAGE;
import static org.molgenis.data.meta.model.TagMetadata.TAG;

/**
 * Service to tag metadata with ontology terms.
 */
public class OntologyTagServiceImpl implements OntologyTagService
{
	private final DataService dataService;
	private final TagRepository tagRepository;
	private final OntologyService ontologyService;
	private final IdGenerator idGenerator;
	private final TagMetadata tagMetadata;

	private static final Logger LOG = LoggerFactory.getLogger(OntologyTagServiceImpl.class);

	public OntologyTagServiceImpl(DataService dataService, OntologyService ontologyService, TagRepository tagRepository,
			IdGenerator idGenerator, TagMetadata tagMetadata)
	{
		this.dataService = requireNonNull(dataService);
		this.tagRepository = requireNonNull(tagRepository);
		this.ontologyService = requireNonNull(ontologyService);
		this.idGenerator = requireNonNull(idGenerator);
		this.tagMetadata = requireNonNull(tagMetadata);
	}

	@Override
	public void removeAttributeTag(String entity, String attribute, String relationIRI, String ontologyTermIRI)
	{
		Entity attributeEntity = findAttributeEntity(entity, attribute);
		Iterable<Entity> tags = attributeEntity.getEntities(AttributeMetadata.TAGS);
		Iterable<Entity> newTags = Iterables.filter(tags, e -> !isSameTag(relationIRI, ontologyTermIRI, e));
		attributeEntity.set(AttributeMetadata.TAGS, newTags);
		dataService.update(ATTRIBUTE_META_DATA, attributeEntity);
		updateEntityTypeEntityWithNewAttributeEntity(entity, attribute, attributeEntity);
	}

	@Override
	public void removeAttributeTag(EntityType entityType, SemanticTag<Attribute, OntologyTerm, Ontology> removeTag)
	{
		Attribute attribute = removeTag.getSubject();
		Entity attributeEntity = findAttributeEntity(entityType.getName(), attribute.getName());
		List<Entity> tags = new ArrayList<Entity>();
		for (Entity tagEntity : attributeEntity.getEntities(AttributeMetadata.TAGS))
		{
			SemanticTag<Attribute, OntologyTerm, Ontology> tag = asTag(attribute, tagEntity);
			if (!removeTag.equals(tag))
			{
				tags.add(tagEntity);
			}
		}
		attributeEntity.set(AttributeMetadata.TAGS, tags);
		dataService.update(ATTRIBUTE_META_DATA, attributeEntity);
	}

	@Override
	@RunAsSystem
	public Multimap<Relation, OntologyTerm> getTagsForAttribute(EntityType entityType, Attribute attribute)
	{
		Multimap<Relation, OntologyTerm> tags = create();
		Entity entity = findAttributeEntity(entityType.getName(), attribute.getName());
		if (entity == null)
		{
			LOG.warn("Cannot find attribute {}.{}", entityType.getName(), attribute.getName());
			return tags;
		}
		for (Entity tagEntity : entity.getEntities(AttributeMetadata.TAGS))
		{
			SemanticTag<Attribute, OntologyTerm, Ontology> tag = asTag(attribute, tagEntity);
			tags.put(tag.getRelation(), tag.getObject());
		}
		return tags;
	}

	@Override
	public Iterable<SemanticTag<Package, OntologyTerm, Ontology>> getTagsForPackage(Package package_)
	{
		Entity packageEntity = dataService.findOneById(PACKAGE, package_.getIdValue());

		if (packageEntity == null)
		{
			throw new UnknownEntityException("Unknown package [" + package_.getName() + "]");
		}

		List<SemanticTag<Package, OntologyTerm, Ontology>> tags = Lists.newArrayList();
		for (Entity tagEntity : packageEntity.getEntities(PackageMetadata.TAGS))
		{
			tags.add(asTag(package_, tagEntity));
		}

		return tags;
	}

	@Override
	public void addAttributeTag(EntityType entityType, SemanticTag<Attribute, OntologyTerm, Ontology> tag)
	{
		Entity entity = findAttributeEntity(entityType.getName(), tag.getSubject().getName());
		List<Entity> tags = new ArrayList<Entity>();
		for (Entity tagEntity : entity.getEntities(AttributeMetadata.TAGS))
		{
			tags.add(tagEntity);
		}
		tags.add(getTagEntity(tag));
		entity.set(AttributeMetadata.TAGS, tags);
		dataService.update(ATTRIBUTE_META_DATA, entity);
	}

	@Override
	public OntologyTag addAttributeTag(String entity, String attribute, String relationIRI,
			List<String> ontologyTermIRIs)
	{
		boolean added = false;
		Entity attributeEntity = findAttributeEntity(entity, attribute);
		Tag tag = new Tag(tagMetadata);
		Stream<OntologyTerm> terms = ontologyTermIRIs.stream().map(ontologyService::getOntologyTerm);
		OntologyTerm combinedOntologyTerm = OntologyTerm.and(terms.toArray(OntologyTerm[]::new));
		Relation relation = Relation.forIRI(relationIRI);
		tag.setId(idGenerator.generateId());
		tag.setCodeSystem(null);
		tag.setRelationIri(relation.getIRI());
		tag.setRelationLabel(relation.getLabel());
		tag.setLabel(combinedOntologyTerm.getLabel());
		tag.setObjectIri(combinedOntologyTerm.getIRI());
		dataService.add(TAG, tag);

		Map<String, Entity> tags = Maps.newHashMap();
		for (Entity attrTag : attributeEntity.getEntities(AttributeMetadata.TAGS))
		{
			tags.put(attrTag.get(TagMetadata.OBJECT_IRI).toString(), attrTag);
		}
		if (!tags.containsKey(tag.get(TagMetadata.OBJECT_IRI).toString()))
		{
			tags.put(tag.get(TagMetadata.OBJECT_IRI).toString(), tag);
			added = true;
		}
		attributeEntity.set(AttributeMetadata.TAGS, tags.values());
		dataService.update(ATTRIBUTE_META_DATA, attributeEntity);
		updateEntityTypeEntityWithNewAttributeEntity(entity, attribute, attributeEntity);
		return added ? OntologyTag.create(combinedOntologyTerm, relation) : null;
	}

	public Entity getTagEntity(SemanticTag<?, OntologyTerm, Ontology> tag)
	{
		return tagRepository.getTagEntity(tag.getObject().getIRI(), tag.getObject().getLabel(), tag.getRelation(),
				tag.getCodeSystem().getIRI());
	}

	@Override
	public void removeAllTagsFromEntity(String entityName)
	{
		EntityType entityTypedata = dataService.getEntityType(entityName);
		Iterable<Attribute> attributes = entityTypedata.getAtomicAttributes();

		for (Attribute attribute : attributes)
		{
			Entity attributeEntity = findAttributeEntity(entityName, attribute.getName());
			attributeEntity.set(AttributeMetadata.TAGS, emptyList());
			dataService.update(ATTRIBUTE_META_DATA, attributeEntity);
			updateEntityTypeEntityWithNewAttributeEntity(entityName, attribute.getName(), attributeEntity);
		}
	}

	@Override
	public Map<String, OntologyTag> tagAttributesInEntity(String entity, Map<Attribute, OntologyTerm> tags)
	{
		Map<String, OntologyTag> result = new LinkedHashMap<>();
		for (Entry<Attribute, OntologyTerm> tag : tags.entrySet())
		{

			OntologyTerm ontologyTerm = tag.getValue();
			OntologyTag ontologyTag = addAttributeTag(entity, tag.getKey().getName(),
					Relation.isAssociatedWith.getIRI(), Collections.singletonList(ontologyTerm.getIRI()));
			result.put(tag.getKey().getName(), ontologyTag);
		}
		return result;
	}

	@Override
	public void addEntityTag(SemanticTag<EntityType, OntologyTerm, Ontology> tag)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeEntityTag(SemanticTag<EntityType, OntologyTerm, Ontology> tag)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Iterable<SemanticTag<EntityType, LabeledResource, LabeledResource>> getTagsForEntity(EntityType entityType)
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * The attribute just got updated, but the entity does not know this yet. To reindex this document in elasticsearch,
	 * update it.
	 *
	 * @param entity          name of the entity
	 * @param attribute       the name of the attribute that got changed
	 * @param attributeEntity the entity of the attribute that got changed
	 */
	private void updateEntityTypeEntityWithNewAttributeEntity(String entity, String attribute, Entity attributeEntity)
	{
		Entity entityEntity = dataService.findOneById(ENTITY_TYPE_META_DATA, entity);
		Iterable<Entity> attributes = entityEntity.getEntities(ATTRIBUTES);
		entityEntity.set(ATTRIBUTES, Iterables.transform(attributes,
				att -> att.getString(AttributeMetadata.NAME).equals(attribute) ? attributeEntity : att));
		dataService.update(ENTITY_TYPE_META_DATA, entityEntity);
	}

	private boolean isSameTag(String relationIRI, String ontologyTermIRI, Entity e)
	{
		return ontologyTermIRI.equals(e.getString(TagMetadata.OBJECT_IRI)) && relationIRI
				.equals(e.getString(TagMetadata.RELATION_IRI));
	}

	@RunAsSystem
	private Entity findAttributeEntity(String entityName, String attributeName)
	{
		Entity entityTypeEntity = dataService.findOneById(ENTITY_TYPE_META_DATA, entityName);
		Optional<Entity> result = stream(entityTypeEntity.getEntities(ATTRIBUTES).spliterator(), false)
				.filter(att -> attributeName.equals(att.getString(AttributeMetadata.NAME))).findFirst();

		if (!result.isPresent() && entityTypeEntity.get(EntityTypeMetadata.EXTENDS) != null)
		{
			return findAttributeEntity(
					entityTypeEntity.getEntity(EntityTypeMetadata.EXTENDS).getString(EntityTypeMetadata.FULL_NAME),
					attributeName);
		}

		return result.isPresent() ? result.get() : null;
	}

	private <SubjectType> SemanticTag<SubjectType, OntologyTerm, Ontology> asTag(SubjectType subjectType,
			Entity tagEntity)
	{
		String identifier = tagEntity.getString(TagMetadata.ID);
		Relation relation = asRelation(tagEntity);
		Ontology ontology = asOntology(tagEntity);
		OntologyTerm ontologyTerm = asOntologyTerm(tagEntity);
		if (relation == null || ontologyTerm == null)
		{
			return null;
		}
		return new SemanticTag<SubjectType, OntologyTerm, Ontology>(identifier, subjectType, relation, ontologyTerm,
				ontology);
	}

	private static Relation asRelation(Entity tagEntity)
	{
		String relationIRI = tagEntity.getString(TagMetadata.RELATION_IRI);
		if (relationIRI == null)
		{
			return null;
		}
		return Relation.forIRI(relationIRI);
	}

	private OntologyTerm asOntologyTerm(Entity tagEntity)
	{
		String objectIRI = tagEntity.getString(TagMetadata.OBJECT_IRI);
		if (objectIRI == null)
		{
			return null;
		}
		return ontologyService.getOntologyTerm(objectIRI);
	}

	private Ontology asOntology(Entity tagEntity)
	{
		String codeSystemIRI = tagEntity.getString(TagMetadata.CODE_SYSTEM);
		if (codeSystemIRI == null)
		{
			return null;
		}
		return ontologyService.getOntology(codeSystemIRI);
	}
}

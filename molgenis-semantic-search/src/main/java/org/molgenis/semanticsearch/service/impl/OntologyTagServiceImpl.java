package org.molgenis.semanticsearch.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.UnknownAttributeException;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.meta.model.*;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.populate.IdGenerator;
import org.molgenis.data.semantic.LabeledResource;
import org.molgenis.data.semantic.Relation;
import org.molgenis.data.semantic.SemanticTag;
import org.molgenis.ontology.core.model.Ontology;
import org.molgenis.ontology.core.model.OntologyTerm;
import org.molgenis.ontology.core.service.OntologyService;
import org.molgenis.security.core.runas.RunAsSystem;
import org.molgenis.semanticsearch.repository.TagRepository;
import org.molgenis.semanticsearch.semantic.OntologyTag;
import org.molgenis.semanticsearch.service.OntologyTagService;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.google.common.collect.LinkedHashMultimap.create;
import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
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
		Iterable<Entity> newTags = StreamSupport.stream(tags.spliterator(), false)
												.filter(e -> !isSameTag(relationIRI, ontologyTermIRI, e))
												.collect(Collectors.toList());
		attributeEntity.set(AttributeMetadata.TAGS, newTags);
		dataService.update(ATTRIBUTE_META_DATA, attributeEntity);
		updateEntityTypeEntityWithNewAttributeEntity(entity, attribute, attributeEntity);
	}

	@Override
	public void removeAttributeTag(EntityType entityType, SemanticTag<Attribute, OntologyTerm, Ontology> removeTag)
	{
		Attribute attribute = removeTag.getSubject();
		Entity attributeEntity = findAttributeEntity(entityType.getId(), attribute.getName());
		List<Entity> tags = new ArrayList<>();
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

		for (Tag tagEntity : attribute.getTags())
		{
			SemanticTag<Attribute, OntologyTerm, Ontology> tag = asTag(attribute, tagEntity);
			if (tag != null)
			{
				tags.put(tag.getRelation(), tag.getObject());
			}
		}
		return tags;
	}

	@Override
	public Iterable<SemanticTag<Package, OntologyTerm, Ontology>> getTagsForPackage(Package package_)
	{
		Entity packageEntity = dataService.findOneById(PACKAGE, package_.getId());

		if (packageEntity == null)
		{
			throw new UnknownEntityException("Unknown package [" + package_.getId() + "]");
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
		Entity entity = findAttributeEntity(entityType.getId(), tag.getSubject().getName());
		List<Entity> tags = new ArrayList<>();
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
	@Transactional
	public void removeAllTagsFromEntity(String entityTypeId)
	{
		EntityType entityTypedata = dataService.getEntityType(entityTypeId);
		Iterable<Attribute> attributes = entityTypedata.getAtomicAttributes();

		for (Attribute attribute : attributes)
		{
			Entity attributeEntity = findAttributeEntity(entityTypeId, attribute.getName());
			attributeEntity.set(AttributeMetadata.TAGS, emptyList());
			dataService.update(ATTRIBUTE_META_DATA, attributeEntity);
			updateEntityTypeEntityWithNewAttributeEntity(entityTypeId, attribute.getName(), attributeEntity);
		}
	}

	@Override
	@Transactional
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
		EntityType entityEntity = dataService.getEntityType(entity);
		Iterable<Attribute> attributes = entityEntity.getOwnAllAttributes();
		entityEntity.set(ATTRIBUTES, StreamSupport.stream(attributes.spliterator(), false)
												  .map(att -> att.getName().equals(attribute) ? attributeEntity : att)
												  .collect(Collectors.toList()));
		dataService.update(ENTITY_TYPE_META_DATA, entityEntity);
	}

	private boolean isSameTag(String relationIRI, String ontologyTermIRI, Entity e)
	{
		return ontologyTermIRI.equals(e.getString(TagMetadata.OBJECT_IRI)) && relationIRI.equals(
				e.getString(TagMetadata.RELATION_IRI));
	}

	@RunAsSystem
	private @NotNull
	Entity findAttributeEntity(String entityTypeId, String attributeName)
	{
		EntityType entityTypeEntity = dataService.getEntityType(entityTypeId);
		Attribute attributeEntity = entityTypeEntity.getAttribute(attributeName);

		if (attributeEntity == null)
		{
			throw new UnknownAttributeException(format("Unknown attribute [%s]", attributeName));
		}

		return attributeEntity;
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
		return new SemanticTag<>(identifier, subjectType, relation, ontologyTerm, ontology);
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

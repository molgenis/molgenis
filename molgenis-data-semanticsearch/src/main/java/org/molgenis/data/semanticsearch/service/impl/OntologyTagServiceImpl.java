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
import org.molgenis.ontology.core.model.CombinedOntologyTermImpl;
import org.molgenis.ontology.core.model.Ontology;
import org.molgenis.ontology.core.model.OntologyTerm;
import org.molgenis.ontology.core.model.OntologyTermImpl;
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
import static org.molgenis.data.meta.model.AttributeMetaDataMetaData.ATTRIBUTE_META_DATA;
import static org.molgenis.data.meta.model.EntityMetaDataMetaData.ATTRIBUTES;
import static org.molgenis.data.meta.model.EntityMetaDataMetaData.ENTITY_META_DATA;
import static org.molgenis.data.meta.model.PackageMetaData.PACKAGE;
import static org.molgenis.data.meta.model.TagMetaData.TAG;

/**
 * Service to tag metadata with ontology terms.
 */
public class OntologyTagServiceImpl implements OntologyTagService
{
	private final DataService dataService;
	private final TagRepository tagRepository;
	private final OntologyService ontologyService;
	private final IdGenerator idGenerator;
	private final TagMetaData tagMetaData;

	private static final Logger LOG = LoggerFactory.getLogger(OntologyTagServiceImpl.class);

	public OntologyTagServiceImpl(DataService dataService, OntologyService ontologyService, TagRepository tagRepository,
			IdGenerator idGenerator, TagMetaData tagMetaData)
	{
		this.dataService = requireNonNull(dataService);
		this.tagRepository = requireNonNull(tagRepository);
		this.ontologyService = requireNonNull(ontologyService);
		this.idGenerator = requireNonNull(idGenerator);
		this.tagMetaData = requireNonNull(tagMetaData);
	}

	@Override
	public void removeAttributeTag(String entity, String attribute, String relationIRI, String ontologyTermIRI)
	{
		Entity attributeEntity = findAttributeEntity(entity, attribute);
		Iterable<Entity> tags = attributeEntity.getEntities(AttributeMetaDataMetaData.TAGS);
		Iterable<Entity> newTags = Iterables.filter(tags, e -> !isSameTag(relationIRI, ontologyTermIRI, e));
		attributeEntity.set(AttributeMetaDataMetaData.TAGS, newTags);
		dataService.update(ATTRIBUTE_META_DATA, attributeEntity);
		updateEntityMetaDataEntityWithNewAttributeEntity(entity, attribute, attributeEntity);
	}

	@Override
	public void removeAttributeTag(EntityMetaData entityMetaData,
			SemanticTag<AttributeMetaData, OntologyTerm, Ontology> removeTag)
	{
		AttributeMetaData attributeMetaData = removeTag.getSubject();
		Entity attributeEntity = findAttributeEntity(entityMetaData.getName(), attributeMetaData.getName());
		List<Entity> tags = new ArrayList<Entity>();
		for (Entity tagEntity : attributeEntity.getEntities(AttributeMetaDataMetaData.TAGS))
		{
			SemanticTag<AttributeMetaData, OntologyTerm, Ontology> tag = asTag(attributeMetaData, tagEntity);
			if (!removeTag.equals(tag))
			{
				tags.add(tagEntity);
			}
		}
		attributeEntity.set(AttributeMetaDataMetaData.TAGS, tags);
		dataService.update(ATTRIBUTE_META_DATA, attributeEntity);
	}

	@Override
	@RunAsSystem
	public Multimap<Relation, OntologyTerm> getTagsForAttribute(EntityMetaData entityMetaData,
			AttributeMetaData attributeMetaData)
	{
		Multimap<Relation, OntologyTerm> tags = create();
		Entity entity = findAttributeEntity(entityMetaData.getName(), attributeMetaData.getName());
		if (entity == null)
		{
			LOG.warn("Cannot find attribute {}.{}", entityMetaData.getName(), attributeMetaData.getName());
			return tags;
		}
		for (Entity tagEntity : entity.getEntities(AttributeMetaDataMetaData.TAGS))
		{
			SemanticTag<AttributeMetaData, OntologyTerm, Ontology> tag = asTag(attributeMetaData, tagEntity);
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
		for (Entity tagEntity : packageEntity.getEntities(PackageMetaData.TAGS))
		{
			tags.add(asTag(package_, tagEntity));
		}

		return tags;
	}

	@Override
	public void addAttributeTag(EntityMetaData entityMetaData,
			SemanticTag<AttributeMetaData, OntologyTerm, Ontology> tag)
	{
		Entity entity = findAttributeEntity(entityMetaData.getName(), tag.getSubject().getName());
		List<Entity> tags = new ArrayList<Entity>();
		for (Entity tagEntity : entity.getEntities(AttributeMetaDataMetaData.TAGS))
		{
			tags.add(tagEntity);
		}
		tags.add(getTagEntity(tag));
		entity.set(AttributeMetaDataMetaData.TAGS, tags);
		dataService.update(ATTRIBUTE_META_DATA, entity);
	}

	@Override
	public OntologyTag addAttributeTag(String entity, String attribute, String relationIRI,
			List<String> ontologyTermIRIs)
	{
		boolean added = false;
		Entity attributeEntity = findAttributeEntity(entity, attribute);
		Tag tag = new Tag(tagMetaData);
		Stream<OntologyTermImpl> terms = ontologyTermIRIs.stream().map(ontologyService::getOntologyTerm);
		OntologyTerm combinedOntologyTerm = CombinedOntologyTermImpl.and(terms.toArray(OntologyTermImpl[]::new));
		Relation relation = Relation.forIRI(relationIRI);
		tag.setIdentifier(idGenerator.generateId());
		tag.setCodeSystem(null);
		tag.setRelationIri(relation.getIRI());
		tag.setRelationLabel(relation.getLabel());
		tag.setLabel(combinedOntologyTerm.getLabel());
		tag.setObjectIri(combinedOntologyTerm.getIRI());
		dataService.add(TAG, tag);

		Map<String, Entity> tags = Maps.newHashMap();
		for (Entity attrTag : attributeEntity.getEntities(AttributeMetaDataMetaData.TAGS))
		{
			tags.put(attrTag.get(TagMetaData.OBJECT_IRI).toString(), attrTag);
		}
		if (!tags.containsKey(tag.get(TagMetaData.OBJECT_IRI).toString()))
		{
			tags.put(tag.get(TagMetaData.OBJECT_IRI).toString(), tag);
			added = true;
		}
		attributeEntity.set(AttributeMetaDataMetaData.TAGS, tags.values());
		dataService.update(ATTRIBUTE_META_DATA, attributeEntity);
		updateEntityMetaDataEntityWithNewAttributeEntity(entity, attribute, attributeEntity);
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
		EntityMetaData entityMetadata = dataService.getEntityMetaData(entityName);
		Iterable<AttributeMetaData> attributeMetaDatas = entityMetadata.getAtomicAttributes();

		for (AttributeMetaData attributeMetaData : attributeMetaDatas)
		{
			Entity attributeEntity = findAttributeEntity(entityName, attributeMetaData.getName());
			attributeEntity.set(AttributeMetaDataMetaData.TAGS, emptyList());
			dataService.update(ATTRIBUTE_META_DATA, attributeEntity);
			updateEntityMetaDataEntityWithNewAttributeEntity(entityName, attributeMetaData.getName(), attributeEntity);
		}
	}

	@Override
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
	public void addEntityTag(SemanticTag<EntityMetaData, OntologyTerm, Ontology> tag)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void removeEntityTag(SemanticTag<EntityMetaData, OntologyTerm, Ontology> tag)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public Iterable<SemanticTag<EntityMetaData, LabeledResource, LabeledResource>> getTagsForEntity(
			EntityMetaData entityMetaData)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * The attribute just got updated, but the entity does not know this yet. To reindex this document in elasticsearch,
	 * update it.
	 *
	 * @param entity          name of the entity
	 * @param attribute       the name of the attribute that got changed
	 * @param attributeEntity the entity of the attribute that got changed
	 */
	private void updateEntityMetaDataEntityWithNewAttributeEntity(String entity, String attribute,
			Entity attributeEntity)
	{
		Entity entityEntity = dataService.findOneById(ENTITY_META_DATA, entity);
		Iterable<Entity> attributes = entityEntity.getEntities(ATTRIBUTES);
		entityEntity.set(ATTRIBUTES, Iterables.transform(attributes,
				att -> att.getString(AttributeMetaDataMetaData.NAME).equals(attribute) ? attributeEntity : att));
		dataService.update(ENTITY_META_DATA, entityEntity);
	}

	private boolean isSameTag(String relationIRI, String ontologyTermIRI, Entity e)
	{
		return ontologyTermIRI.equals(e.getString(TagMetaData.OBJECT_IRI)) && relationIRI
				.equals(e.getString(TagMetaData.RELATION_IRI));
	}

	@RunAsSystem
	private Entity findAttributeEntity(String entityName, String attributeName)
	{
		Entity entityMetaDataEntity = dataService.findOneById(ENTITY_META_DATA, entityName);
		Optional<Entity> result = stream(entityMetaDataEntity.getEntities(ATTRIBUTES).spliterator(), false)
				.filter(att -> attributeName.equals(att.getString(AttributeMetaDataMetaData.NAME))).findFirst();

		if (!result.isPresent() && entityMetaDataEntity.get(EntityMetaDataMetaData.EXTENDS) != null)
		{
			return findAttributeEntity(entityMetaDataEntity.getEntity(EntityMetaDataMetaData.EXTENDS)
					.getString(EntityMetaDataMetaData.FULL_NAME), attributeName);
		}

		return result.isPresent() ? result.get() : null;
	}

	private <SubjectType> SemanticTag<SubjectType, OntologyTerm, Ontology> asTag(SubjectType subjectType,
			Entity tagEntity)
	{
		String identifier = tagEntity.getString(TagMetaData.IDENTIFIER);
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
		String objectLabel = tagEntity.getString(TagMetaData.LABEL);

		if (objectIRI == null)
		{
			return null;
		}

		return CombinedOntologyTermImpl.create(objectIRI, objectLabel);
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

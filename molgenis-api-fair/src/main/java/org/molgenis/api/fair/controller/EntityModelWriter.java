package org.molgenis.api.fair.controller;

import static com.google.common.collect.Iterables.contains;
import static java.util.Arrays.stream;
import static java.util.Objects.requireNonNull;
import static org.molgenis.api.fair.controller.FairController.BASE_URI;

import com.google.common.collect.Streams;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.IllegalAttributeTypeException;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.semantic.LabeledResource;
import org.molgenis.data.semantic.Relation;
import org.molgenis.data.semantic.SemanticTag;
import org.molgenis.semanticsearch.service.TagService;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class EntityModelWriter {
  private static final String KEYWORD = "http://www.w3.org/ns/dcat#keyword";
  static final String DCAT_RESOURCE = "http://www.w3.org/ns/dcat#Resource";
  private static final String FDP_METADATA_IDENTIFIER =
      "http://rdf.biosemantics.org/ontologies/fdp-o#metadataIdentifier";
  private static final String DATACITE_IDENTIFIER = "http://purl.org/spar/datacite/Identifier";
  private static final String DCT_IDENTIFIER = "http://purl.org/dc/terms/identifier";
  private final IRI rdfTypePredicate;

  private final SimpleValueFactory valueFactory;
  private final TagService<LabeledResource, LabeledResource> tagService;
  private static final DatatypeFactory DATATYPE_FACTORY;

  static {
    try {
      DATATYPE_FACTORY = DatatypeFactory.newInstance();
    } catch (DatatypeConfigurationException e) {
      throw new Error("Could not instantiate javax.xml.datatype.DatatypeFactory", e);
    }
  }

  public EntityModelWriter(
      TagService<LabeledResource, LabeledResource> tagService, SimpleValueFactory valueFactory) {
    this.valueFactory = requireNonNull(valueFactory);
    this.tagService = requireNonNull(tagService);
    this.rdfTypePredicate =
        valueFactory.createIRI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
  }

  private void setNamespacePrefixes(Model model) {
    model.setNamespace("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
    model.setNamespace("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
    model.setNamespace("dcat", "http://www.w3.org/ns/dcat#");
    model.setNamespace("xsd", "http://www.w3.org/2001/XMLSchema#");
    model.setNamespace("owl", "http://www.w3.org/2002/07/owl#");
    model.setNamespace("dct", "http://purl.org/dc/terms/");
    model.setNamespace("lang", "http://id.loc.gov/vocabulary/iso639-1/");
    model.setNamespace("fdpo", "http://rdf.biosemantics.org/ontologies/fdp-o#");
    model.setNamespace("ldp", "http://www.w3.org/ns/ldp#");
    model.setNamespace("foaf", "http://xmlns.com/foaf/0.1/");
    model.setNamespace("orcid", "http://orcid.org/");
    model.setNamespace("r3d", "http://www.re3data.org/schema/3-0#");
    model.setNamespace("sio", "http://semanticscience.org/resource/");
    model.setNamespace("datacite", "http://purl.org/spar/datacite/");
  }

  public Model createRdfModel(Entity objectEntity) {
    Model model = createEmptyModel();
    Resource subject = createResource(objectEntity);
    addEntityToModel(subject, objectEntity, model);
    return model;
  }

  public Model createEmptyModel() {
    Model model = new LinkedHashModel();
    setNamespacePrefixes(model);
    return model;
  }

  public void addEntityToModel(Resource subject, Entity objectEntity, Model model) {
    EntityType entityType = objectEntity.getEntityType();
    addStatementsForAttributeTags(objectEntity, model, subject, entityType);
    addStatementsForEntity(model, subject, objectEntity);
  }

  private void addStatementsForAttributeTags(
      Entity objectEntity, Model model, Resource subject, EntityType entityType) {
    for (Attribute objectAttribute : entityType.getAtomicAttributes()) {
      Object value = objectEntity.get(objectAttribute.getName());
      if (value == null) {
        continue;
      }
      for (LabeledResource tag :
          tagService
              .getTagsForAttribute(entityType, objectAttribute)
              .get(Relation.isAssociatedWith)) {
        IRI predicate = valueFactory.createIRI(tag.getIri());
        addRelationForAttribute(model, subject, predicate, objectEntity, objectAttribute);
      }
    }
  }

  void addStatementsForEntity(Model model, Resource subject, Entity entity) {
    for (SemanticTag<EntityType, LabeledResource, LabeledResource> tag :
        tagService.getTagsForEntity(entity.getEntityType())) {
      if (tag.getRelation() == Relation.isAssociatedWith) {
        LabeledResource object = tag.getObject();
        model.add(subject, rdfTypePredicate, valueFactory.createIRI(object.getIri()));
        if (DCAT_RESOURCE.equals(object.getIri())) {
          addFdpMetadataIdentifier(model, subject);
        }
      }
    }
  }

  private void addFdpMetadataIdentifier(Model model, Resource subject) {
    BNode resourceIdentifier = valueFactory.createBNode();
    model.add(subject, valueFactory.createIRI(FDP_METADATA_IDENTIFIER), resourceIdentifier);
    model.add(resourceIdentifier, rdfTypePredicate, valueFactory.createIRI(DATACITE_IDENTIFIER));
    model.add(resourceIdentifier, valueFactory.createIRI(DCT_IDENTIFIER), subject);
  }

  private void addRelationForAttribute(
      Model model,
      Resource subject,
      IRI predicate,
      Entity objectEntity,
      Attribute objectAttribute) {
    String name = objectAttribute.getName();

    AttributeType attributeType = objectAttribute.getDataType();
    switch (attributeType) {
      case MREF:
      case CATEGORICAL_MREF:
        addRelationForMrefTypeAttribute(model, subject, predicate, objectEntity.getEntities(name));
        break;
      case BOOL:
        model.add(subject, predicate, valueFactory.createLiteral(objectEntity.getBoolean(name)));
        break;
      case DATE:
        XMLGregorianCalendar calendar =
            DATATYPE_FACTORY.newXMLGregorianCalendar(objectEntity.getLocalDate(name).toString());
        model.add(subject, predicate, valueFactory.createLiteral(calendar));
        break;
      case DATE_TIME:
        calendar =
            DATATYPE_FACTORY.newXMLGregorianCalendar(objectEntity.getInstant(name).toString());
        model.add(subject, predicate, valueFactory.createLiteral(calendar));
        break;
      case DECIMAL:
        model.add(subject, predicate, valueFactory.createLiteral(objectEntity.getDouble(name)));
        break;
      case LONG:
        model.add(subject, predicate, valueFactory.createLiteral(objectEntity.getLong(name)));
        break;
      case INT:
        model.add(subject, predicate, valueFactory.createLiteral(objectEntity.getInt(name)));
        break;
      case ENUM:
      case EMAIL:
      case HTML:
      case TEXT:
      case SCRIPT:
      case STRING:
        addRelationForStringTypeAttribute(model, subject, predicate, objectEntity.getString(name));
        break;
      case HYPERLINK:
        model.add(subject, predicate, valueFactory.createIRI(objectEntity.getString(name)));
        break;
      case XREF:
      case CATEGORICAL:
      case FILE:
        addRelationForXrefTypeAttribute(model, subject, predicate, objectEntity.getEntity(name));
        break;
      default:
        throw new IllegalAttributeTypeException(attributeType);
    }
  }

  private void addRelationForStringTypeAttribute(
      Model model, Resource subject, IRI predicate, String value) {
    if (predicate.stringValue().equals(KEYWORD)) {
      stream(value.split(","))
          .map(String::trim)
          .forEach(keyword -> model.add(subject, predicate, valueFactory.createLiteral(keyword)));
    } else {
      model.add(subject, predicate, valueFactory.createLiteral(value));
    }
  }

  private void addRelationForMrefTypeAttribute(
      Model model, Resource subject, IRI predicate, Iterable<Entity> objectEntities) {
    for (Entity objectEntity : objectEntities) {
      addRelationForXrefTypeAttribute(model, subject, predicate, objectEntity);
    }
  }

  private void addRelationForXrefTypeAttribute(
      Model model, Resource subject, IRI predicate, Entity objectEntity) {
    var objectIRI = createResource(objectEntity);
    model.add(subject, predicate, objectIRI);
    if (objectIRI instanceof BNode) {
      addEntityToModel(objectIRI, objectEntity, model);
    }
  }

  public boolean isADcatResource(EntityType entityType) {
    var tagsForEntity = tagService.getTagsForEntity(entityType);
    return Streams.stream(tagsForEntity)
        .filter(tag -> tag.getRelation() == Relation.isAssociatedWith)
        .map(SemanticTag::getObject)
        .map(LabeledResource::getIri)
        .anyMatch(DCAT_RESOURCE::equals);
  }

  /**
   * Create a resource. For the resources served by the Controller, give the controller URI as IRI.
   * For entities with an IRI attribute, give the value of that attribute. Otherwise, create a blank
   * node.
   *
   * @param entity the entity to create a resource for
   * @return Resource
   */
  private Resource createResource(Entity entity) {
    var entityType = entity.getEntityType();
    if ("fdp_Metadata".equals(entityType.getId())) {
      return valueFactory.createIRI(getServletUriComponentsBuilder().build().toUriString());
    }
    if (isADcatResource(entityType)) {
      var iri =
          getServletUriComponentsBuilder()
              .pathSegment(entityType.getId(), entity.getIdValue().toString())
              .build()
              .toUriString();
      return valueFactory.createIRI(iri);
    }
    if (contains(entity.getEntityType().getAttributeNames(), "IRI")) {
      return valueFactory.createIRI(entity.getString("IRI"));
    }
    return valueFactory.createBNode();
  }

  UriComponentsBuilder getServletUriComponentsBuilder() {
    return ServletUriComponentsBuilder.fromCurrentContextPath().path(BASE_URI);
  }
}

package org.molgenis.api.fair.controller;

import static com.google.common.collect.Iterables.contains;
import static java.util.Arrays.stream;
import static java.util.Objects.requireNonNull;
import static org.molgenis.api.fair.controller.FairControllerV2.BASE_URI;

import com.google.common.collect.Streams;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
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
public class EntityModelWriterV2 {

  public static final String NS_DCAT = "http://www.w3.org/ns/dcat#";
  public static final String NS_FDP = "https://w3id.org/fdp/fdp-o#";
  public static final String NS_DATACITE = "http://purl.org/spar/datacite/";
  public static final String NS_DCT = "http://purl.org/dc/terms/";
  public static final String NS_RDF = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
  public static final String NS_LDP = "http://www.w3.org/ns/ldp#";
  static final String DCAT_RESOURCE = NS_DCAT + "Resource";
  static final String LDP_DIRECT_CONTAINER = NS_LDP + "DirectContainer";
  private static final String KEYWORD = NS_DCAT + "keyword";
  private static final DatatypeFactory DATATYPE_FACTORY;

  static {
    try {
      DATATYPE_FACTORY = DatatypeFactory.newInstance();
    } catch (DatatypeConfigurationException e) {
      throw new Error("Could not instantiate javax.xml.datatype.DatatypeFactory", e);
    }
  }

  private final IRI rdfTypePredicate;
  private final IRI dctIdentifier;
  private final IRI dataciteIdentifier;
  private final IRI fdpMetadataIdentifier;
  private final IRI ldpDirectContainer;

  private final SimpleValueFactory valueFactory;
  private final TagService<LabeledResource, LabeledResource> tagService;

  public EntityModelWriterV2(
      TagService<LabeledResource, LabeledResource> tagService, SimpleValueFactory valueFactory) {
    this.valueFactory = requireNonNull(valueFactory);
    this.tagService = requireNonNull(tagService);
    rdfTypePredicate = valueFactory.createIRI(NS_RDF, "type");
    dctIdentifier = valueFactory.createIRI(NS_DCT, "identifier");
    dataciteIdentifier = valueFactory.createIRI(NS_DATACITE, "Identifier");
    fdpMetadataIdentifier = valueFactory.createIRI(NS_FDP, "metadataIdentifier");
    ldpDirectContainer = valueFactory.createIRI(NS_LDP, "DirectContainer");
  }

  private void setNamespacePrefixes(Model model) {
    model.setNamespace("rdf", NS_RDF);
    model.setNamespace("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
    model.setNamespace("dcat", NS_DCAT);
    model.setNamespace("xsd", "http://www.w3.org/2001/XMLSchema#");
    model.setNamespace("owl", "http://www.w3.org/2002/07/owl#");
    model.setNamespace("dct", NS_DCT);
    model.setNamespace("lang", "http://id.loc.gov/vocabulary/iso639-1/");
    model.setNamespace("fdp-o", NS_FDP);
    model.setNamespace("foaf", "http://xmlns.com/foaf/0.1/");
    model.setNamespace("orcid", "http://orcid.org/");
    model.setNamespace("sio", "http://semanticscience.org/resource/");
    model.setNamespace("datacite", NS_DATACITE);
    model.setNamespace("mlga", "http://molgenis.org/audit/");
    model.setNamespace("ldp", NS_LDP);
  }

  public Model createRdfModel(Entity objectEntity) {
    Model model = createEmptyModel();
    Resource subject = createResource(objectEntity);
    addEntityToModel(subject, objectEntity, model);
    // If it contains children, the ldp navigation information are included
    if (objectEntity.getString("hasMemberRelation") != null) {
      createNavigationResource(objectEntity, subject, model);
    }
    return model;
  }

  /** Creates navigation information using LDP ontology */
  private void createNavigationResource(Entity objectEntity, Resource subject, Model model) {
    IRI hasMemberRelation = valueFactory.createIRI(NS_LDP + "hasMemberRelation");
    IRI membershipResource = valueFactory.createIRI(NS_LDP + "membershipResource");
    IRI ldpContains = valueFactory.createIRI(NS_LDP + "contains");
    IRI dctermsTitle = valueFactory.createIRI(NS_DCT + "title");
    IRI memberRelation = valueFactory.createIRI(objectEntity.getString("hasMemberRelation"));

    Resource directContainer = valueFactory.createIRI(subject + "/dc");
    model.add(
        directContainer,
        dctermsTitle,
        valueFactory.createLiteral(objectEntity.getString("directContainerTitle")));
    model.add(directContainer, rdfTypePredicate, ldpDirectContainer);
    model.add(directContainer, membershipResource, subject);
    model.add(directContainer, hasMemberRelation, memberRelation);
    for (Statement child : model.filter(subject, memberRelation, null)) {
      model.add(directContainer, ldpContains, child.getObject());
    }
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
          model.add(subject, fdpMetadataIdentifier, createDataciteIdentifierNode(model, subject));
        }
      }
    }
  }

  private BNode createDataciteIdentifierNode(Model model, Resource identifier) {
    BNode result = valueFactory.createBNode();
    model.add(result, rdfTypePredicate, dataciteIdentifier);
    model.add(result, dctIdentifier, identifier);
    return result;
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
      case ONE_TO_MANY:
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
    if (contains(entity.getEntityType().getAttributeNames(), "IRI")) {
      return valueFactory.createIRI(entity.getString("IRI"));
    }

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
    return valueFactory.createBNode(
        String.format("%s_%s", entity.getEntityType().getId(), entity.getIdValue().toString()));
  }

  UriComponentsBuilder getServletUriComponentsBuilder() {
    return ServletUriComponentsBuilder.fromCurrentContextPath().path(BASE_URI);
  }
}

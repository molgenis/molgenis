package org.molgenis.api.fair.controller;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.rdf4j.model.vocabulary.RDF.TYPE;
import static org.eclipse.rdf4j.rio.RDFFormat.TURTLE;
import static org.eclipse.rdf4j.rio.helpers.BasicWriterSettings.INLINE_BLANK_NODES;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.api.fair.controller.EntityModelWriter.DCAT_RESOURCE;
import static org.molgenis.api.fair.controller.EntityModelWriter.R3D_REPOSITORY;
import static org.molgenis.data.meta.AttributeType.BOOL;
import static org.molgenis.data.meta.AttributeType.DATE;
import static org.molgenis.data.meta.AttributeType.DATE_TIME;
import static org.molgenis.data.meta.AttributeType.DECIMAL;
import static org.molgenis.data.meta.AttributeType.HYPERLINK;
import static org.molgenis.data.meta.AttributeType.INT;
import static org.molgenis.data.meta.AttributeType.LONG;
import static org.molgenis.data.meta.AttributeType.MREF;
import static org.molgenis.data.meta.AttributeType.STRING;
import static org.molgenis.data.meta.AttributeType.XREF;
import static org.molgenis.data.semantic.Relation.isAssociatedWith;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import java.io.StringWriter;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.WriterConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.semantic.LabeledResource;
import org.molgenis.data.semantic.Relation;
import org.molgenis.data.semantic.SemanticTag;
import org.molgenis.semanticsearch.service.TagService;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.web.util.UriComponentsBuilder;

class EntityModelWriterTest extends AbstractMockitoTest {
  @Mock private TagService<LabeledResource, LabeledResource> tagService;
  @Mock private Entity objectEntity;
  @Mock private Entity refEntity;
  @Mock private EntityType entityType;
  @Mock private EntityType refEntityType;
  @Mock private Attribute attribute;
  @Mock private SemanticTag<EntityType, LabeledResource, LabeledResource> entityTag;
  @Mock private SemanticTag<EntityType, LabeledResource, LabeledResource> entityTag2;
  @Mock private LabeledResource labeledResource;
  @Mock private LabeledResource labeledResource2;
  private EntityModelWriter writer;
  private SimpleValueFactory valueFactory = SimpleValueFactory.getInstance();

  @SuppressWarnings("unchecked")
  @BeforeEach
  void beforeMethod() {
    writer =
        new EntityModelWriter(tagService, valueFactory) {
          @Override
          UriComponentsBuilder getServletUriComponentsBuilder() {
            return UriComponentsBuilder.fromUriString("http://example.org/api/fdp");
          }
        };
  }

  @Test
  void testCreateRfdModelForEntity() {
    List<Attribute> attributeList = singletonList(attribute);

    when(objectEntity.getEntityType()).thenReturn(entityType);
    when(entityType.getId()).thenReturn("fdp_Catalog");
    when(objectEntity.getIdValue()).thenReturn("catalogId");
    when(objectEntity.get("attributeName1")).thenReturn("value1");
    when(objectEntity.getString("attributeName1")).thenReturn("value1");

    when(entityType.getAtomicAttributes()).thenReturn(attributeList);

    when(attribute.getName()).thenReturn("attributeName1");
    when(attribute.getDataType()).thenReturn(STRING);

    when(tagService.getTagsForEntity(entityType)).thenReturn(List.of(entityTag, entityTag2));
    when(entityTag.getRelation()).thenReturn(isAssociatedWith);
    when(entityTag.getObject()).thenReturn(labeledResource);
    when(labeledResource.getIri()).thenReturn(DCAT_RESOURCE);

    when(entityTag2.getRelation()).thenReturn(isAssociatedWith);
    when(entityTag2.getObject()).thenReturn(labeledResource2);
    when(labeledResource2.getIri()).thenReturn(R3D_REPOSITORY);

    LabeledResource tag1 = new LabeledResource("http://IRI1.nl", "tag1Label");
    Multimap<Relation, LabeledResource> tags = ImmutableMultimap.of(isAssociatedWith, tag1);
    when(tagService.getTagsForAttribute(entityType, attribute)).thenReturn(tags);

    Model result = writer.createRdfModel(objectEntity);

    assertEquals(9, result.size());
    StringWriter writer = new StringWriter();
    Rio.write(result, writer, TURTLE, new WriterConfig().set(INLINE_BLANK_NODES, true));
    assertThat(writer.toString())
        .contains("<http://IRI1.nl> \"value1\";")
        .contains(
            "fdp:metadataIdentifier [ a datacite:Identifier;\n"
                + "      dct:identifier <http://example.org/api/fdp/fdp_Catalog/catalogId>\n"
                + "    ]")
        .contains(
            "r3d:repositoryIdentifier [ a datacite:Identifier;\n"
                + "      dct:identifier <http://example.org/api/fdp/fdp_Catalog/catalogId>\n"
                + "    ]");
  }

  @Test
  void testCreateRfdModelWithCustomIRI() {

    List<Attribute> attributeList = singletonList(attribute);

    when(objectEntity.getEntityType()).thenReturn(entityType);
    when(entityType.getId()).thenReturn("fdp_Catalog");
    when(objectEntity.getString("IRI")).thenReturn("http://purl.org/example/catalog_id");
    when(entityType.getAttributeNames()).thenReturn(singletonList("IRI"));
    when(entityType.getAtomicAttributes()).thenReturn(attributeList);

    when(attribute.getName()).thenReturn("IRI");

    when(tagService.getTagsForEntity(entityType)).thenReturn(List.of(entityTag, entityTag2));
    when(entityTag.getRelation()).thenReturn(isAssociatedWith);
    when(entityTag.getObject()).thenReturn(labeledResource);
    when(labeledResource.getIri()).thenReturn(DCAT_RESOURCE);

    Model result = writer.createRdfModel(objectEntity);

    assertEquals(4, result.size());
    StringWriter writer = new StringWriter();
    Rio.write(result, writer, TURTLE, new WriterConfig().set(INLINE_BLANK_NODES, true));
    assertThat(writer.toString())
        .contains("<http://purl.org/example/catalog_id> a dcat:Resource")
        .contains(
            "fdp:metadataIdentifier [ a datacite:Identifier;\n"
                + "      dct:identifier <http://purl.org/example/catalog_id>\n"
                + "    ]");
  }

  static Object[][] createStatementForAttributeProvider() {
    return new Object[][] {
      new Object[] {
        STRING,
        "value",
        (Consumer<Entity>)
            objectEntity -> when(objectEntity.getString("attributeName")).thenReturn("value"),
        "<http://example.org/iri> \"value\""
      },
      new Object[] {
        INT,
        2,
        (Consumer<Entity>) objectEntity -> when(objectEntity.getInt("attributeName")).thenReturn(2),
        "<http://example.org/iri> \"2\"^^xsd:int"
      },
      new Object[] {
        BOOL,
        false,
        (Consumer<Entity>)
            objectEntity -> when(objectEntity.getBoolean("attributeName")).thenReturn(false),
        "<http://example.org/iri> false"
      },
      new Object[] {
        DATE,
        LocalDate.parse("2020-05-09"),
        (Consumer<Entity>)
            objectEntity ->
                when(objectEntity.getLocalDate("attributeName"))
                    .thenReturn(LocalDate.parse("2020-05-09")),
        "<http://example.org/iri> \"2020-05-09\"^^xsd:date"
      },
      new Object[] {
        DATE_TIME,
        Instant.parse("2011-12-03T10:15:30Z"),
        (Consumer<Entity>)
            objectEntity ->
                when(objectEntity.getInstant("attributeName"))
                    .thenReturn(Instant.parse("2011-12-03T10:15:30Z")),
        "<http://example.org/iri> \"2011-12-03T10:15:30Z\"^^xsd:dateTime"
      },
      new Object[] {
        DECIMAL,
        2.18,
        (Consumer<Entity>)
            objectEntity -> when(objectEntity.getDouble("attributeName")).thenReturn(2.18),
        "<http://example.org/iri> 2.18E0"
      },
      new Object[] {
        LONG,
        987654321L,
        (Consumer<Entity>)
            objectEntity -> when(objectEntity.getLong("attributeName")).thenReturn(987654321L),
        "<http://example.org/iri> \"987654321\"^^xsd:long"
      },
      new Object[] {
        HYPERLINK,
        "http://example.org/",
        (Consumer<Entity>)
            objectEntity ->
                when(objectEntity.getString("attributeName")).thenReturn("http://example.org"),
        "<http://example.org/iri> <http://example.org>"
      }
    };
  }

  @ParameterizedTest
  @MethodSource("createStatementForAttributeProvider")
  void testCreateStatementForAttribute(
      AttributeType attributeType, Object value, Consumer<Entity> consumer, String fragment) {
    when(objectEntity.getEntityType()).thenReturn(entityType);
    when(entityType.getId()).thenReturn("fdp_Catalog");
    when(objectEntity.getIdValue()).thenReturn("attributeName");
    when(objectEntity.get("attributeName")).thenReturn(value);
    consumer.accept(objectEntity);

    when(entityType.getAtomicAttributes()).thenReturn(List.of(attribute));

    when(attribute.getName()).thenReturn("attributeName");
    when(attribute.getDataType()).thenReturn(attributeType);

    Multimap<Relation, LabeledResource> tags =
        ImmutableMultimap.of(isAssociatedWith, labeledResource);
    when(labeledResource.getIri()).thenReturn("http://example.org/iri");
    when(tagService.getTagsForAttribute(entityType, attribute)).thenReturn(tags);

    Model result = writer.createRdfModel(objectEntity);

    StringWriter writer = new StringWriter();
    Rio.write(result, writer, TURTLE, new WriterConfig().set(INLINE_BLANK_NODES, true));
    assertThat(writer.toString()).contains(fragment);
  }

  @Test
  void testCreateRfdModelXREF() {
    when(objectEntity.getEntityType()).thenReturn(entityType);
    when(entityType.getId()).thenReturn("fdp_Catalog");
    when(objectEntity.getIdValue()).thenReturn("attributeName");
    when(objectEntity.get("attributeName")).thenReturn(refEntity);
    when(objectEntity.getEntity("attributeName")).thenReturn(refEntity);

    when(refEntity.getEntityType()).thenReturn(refEntityType);
    when(refEntityType.getAttributeNames()).thenReturn(List.of("IRI"));
    when(refEntity.getString("IRI")).thenReturn("http://example.org/refEntity");

    when(entityType.getAtomicAttributes()).thenReturn(List.of(attribute));
    when(attribute.getName()).thenReturn("attributeName");

    when(attribute.getDataType()).thenReturn(XREF);

    when(tagService.getTagsForAttribute(entityType, attribute))
        .thenReturn(ImmutableMultimap.of(isAssociatedWith, labeledResource));
    when(labeledResource.getIri()).thenReturn("http://example.org/relation");

    Model result = writer.createRdfModel(objectEntity);

    assertEquals(1, result.size());
    StringWriter writer = new StringWriter();
    Rio.write(result, writer, TURTLE, new WriterConfig().set(INLINE_BLANK_NODES, true));
    assertThat(writer.toString())
        .contains("<http://example.org/relation> <http://example.org/refEntity>");
  }

  @Test
  void testCreateRfdModelXREFMultipleTags() {
    when(objectEntity.getEntityType()).thenReturn(entityType);
    when(entityType.getId()).thenReturn("fdp_Catalog");
    when(objectEntity.getIdValue()).thenReturn("attributeName");
    when(objectEntity.get("attributeName")).thenReturn(refEntity);
    when(objectEntity.getEntity("attributeName")).thenReturn(refEntity);

    when(refEntity.getEntityType()).thenReturn(refEntityType);
    when(refEntity.getIdValue()).thenReturn("refEntityId");
    when(refEntityType.getId()).thenReturn("refEntityType");
    when(refEntityType.getAttributeNames()).thenReturn(new ArrayList<>());

    when(entityType.getAtomicAttributes()).thenReturn(List.of(attribute));
    when(attribute.getName()).thenReturn("attributeName");

    when(attribute.getDataType()).thenReturn(XREF);

    when(tagService.getTagsForAttribute(entityType, attribute))
        .thenReturn(
            ImmutableMultimap.of(
                isAssociatedWith, labeledResource, isAssociatedWith, labeledResource2));
    when(labeledResource.getIri()).thenReturn("http://example.org/relation");
    when(labeledResource2.getIri()).thenReturn("http://example.org/relation2");

    Model result = writer.createRdfModel(objectEntity);

    assertEquals(2, result.size());
    StringWriter writer = new StringWriter();
    Rio.write(result, writer, TURTLE, new WriterConfig().set(INLINE_BLANK_NODES, true));
    assertThat(writer.toString())
        .contains("<http://example.org/relation> _:refEntityType_refEntityId")
        .contains("<http://example.org/relation2> _:refEntityType_refEntityId");
  }

  @Test
  void testCreateRfdModelMREF() {
    when(objectEntity.getEntityType()).thenReturn(entityType);
    when(entityType.getId()).thenReturn("fdp_Catalog");
    when(objectEntity.getIdValue()).thenReturn("attributeName");
    when(objectEntity.get("attributeName")).thenReturn(refEntity);
    when(objectEntity.getEntities("attributeName")).thenReturn(List.of(refEntity));

    when(refEntity.getEntityType()).thenReturn(refEntityType);
    when(refEntityType.getAttributeNames()).thenReturn(List.of("IRI"));
    when(refEntity.getString("IRI")).thenReturn("http://example.org/refEntity");

    when(entityType.getAtomicAttributes()).thenReturn(List.of(attribute));
    when(attribute.getName()).thenReturn("attributeName");

    when(attribute.getDataType()).thenReturn(MREF);

    when(tagService.getTagsForAttribute(entityType, attribute))
        .thenReturn(ImmutableMultimap.of(isAssociatedWith, labeledResource));
    when(labeledResource.getIri()).thenReturn("http://example.org/relation");

    Model result = writer.createRdfModel(objectEntity);

    assertEquals(1, result.size());
    StringWriter writer = new StringWriter();
    Rio.write(result, writer, TURTLE, new WriterConfig().set(INLINE_BLANK_NODES, true));
    assertThat(writer.toString())
        .contains("<http://example.org/relation> <http://example.org/refEntity>");
  }

  @Test
  void testCreateRfdModelSTRINGKeywords() {
    Entity objectEntity = mock(Entity.class);
    EntityType entityType = mock(EntityType.class);

    Attribute attribute = mock(Attribute.class);
    List<Attribute> attributeList = singletonList(attribute);

    when(objectEntity.getEntityType()).thenReturn(entityType);
    String value = "molgenis,genetics,fair";
    when(objectEntity.getIdValue()).thenReturn("attributeName");
    when(objectEntity.get("attributeName")).thenReturn(value);
    when(objectEntity.getString("attributeName")).thenReturn(value);

    when(entityType.getAtomicAttributes()).thenReturn(attributeList);
    when(attribute.getName()).thenReturn("attributeName");

    when(attribute.getDataType()).thenReturn(STRING);

    LabeledResource tag = new LabeledResource("http://www.w3.org/ns/dcat#keyword", "keywords");
    Multimap<Relation, LabeledResource> tags = ImmutableMultimap.of(isAssociatedWith, tag);
    when(tagService.getTagsForAttribute(entityType, attribute)).thenReturn(tags);

    Model result = writer.createRdfModel(objectEntity);

    assertEquals(3, result.size());
    StringWriter writer = new StringWriter();
    Rio.write(result, writer, TURTLE, new WriterConfig().set(INLINE_BLANK_NODES, true));

    assertThat(writer.toString()).contains("dcat:keyword \"molgenis\", \"genetics\", \"fair\"");
  }

  @Test
  void testCreateRfdModelNullValue() {
    when(objectEntity.getEntityType()).thenReturn(entityType);
    when(entityType.getAtomicAttributes()).thenReturn(List.of(attribute));
    when(objectEntity.getIdValue()).thenReturn("attributeName1");
    when(objectEntity.get("attributeName1")).thenReturn(null);
    when(attribute.getName()).thenReturn("attributeName1");

    Model result = writer.createRdfModel(objectEntity);

    assertTrue(result.isEmpty());
  }

  @Test
  void testAddStatementsForEntityType() {
    Model model = new LinkedHashModel();
    Resource subject = valueFactory.createIRI("http://example.org/subject");
    LabeledResource object = new LabeledResource("http://example.org/object", "object");
    LabeledResource codeSystem = new LabeledResource("ex:object");

    SemanticTag<EntityType, LabeledResource, LabeledResource> tag =
        new SemanticTag<>("tagId", entityType, isAssociatedWith, object, codeSystem);

    when(objectEntity.getEntityType()).thenReturn(entityType);
    when(tagService.getTagsForEntity(entityType)).thenReturn(List.of(tag));

    writer.addStatementsForEntity(model, subject, objectEntity);

    Statement statement =
        valueFactory.createStatement(
            subject, TYPE, valueFactory.createIRI("http://example.org/object"));
    assertEquals(singletonList(statement), newArrayList(model));
  }
}

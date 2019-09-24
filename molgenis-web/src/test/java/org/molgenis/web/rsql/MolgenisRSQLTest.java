package org.molgenis.web.rsql;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.AttributeType.INT;
import static org.molgenis.data.meta.AttributeType.STRING;
import static org.molgenis.data.meta.AttributeType.XREF;

import cz.jirutka.rsql.parser.RSQLParser;
import cz.jirutka.rsql.parser.RSQLParserException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.molgenis.data.UnknownAttributeException;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.test.AbstractMockitoTest;

class MolgenisRSQLTest extends AbstractMockitoTest {
  @Mock private Repository<Entity> repository;
  @Mock private EntityType entityType;
  @Mock private EntityType genderEntityType;
  private MolgenisRSQL molgenisRSQL;

  @BeforeEach
  void beforeMethod() {
    molgenisRSQL = new MolgenisRSQL(new RSQLParser());
    when(repository.getEntityType()).thenReturn(entityType);
  }

  @Test
  void testEquals() throws RSQLParserException {
    Attribute nameAttr = when(mock(Attribute.class).getDataType()).thenReturn(STRING).getMock();
    Attribute ageAttr = when(mock(Attribute.class).getDataType()).thenReturn(INT).getMock();
    doReturn(nameAttr).when(entityType).getAttribute("name");
    doReturn(ageAttr).when(entityType).getAttribute("age");

    Query<Entity> q = molgenisRSQL.createQuery("name==piet", repository);
    assertEquals(new QueryImpl<>().eq("name", "piet"), q);

    q = molgenisRSQL.createQuery("name=='piet paulusma'", repository);
    assertEquals(new QueryImpl<>().eq("name", "piet paulusma"), q);

    q = molgenisRSQL.createQuery("age==87", repository);
    assertEquals(new QueryImpl<>().eq("age", 87), q);
  }

  @Test
  void testUnknowAttribute() throws RSQLParserException {
    assertThrows(
        UnknownAttributeException.class,
        () -> molgenisRSQL.createQuery("nonexistingattribute==piet", repository));
  }

  @Test
  void testGreaterThanOrEqual() throws RSQLParserException {
    Attribute ageAttr = when(mock(Attribute.class).getDataType()).thenReturn(INT).getMock();
    doReturn(ageAttr).when(entityType).getAttribute("age");

    Query<Entity> q = molgenisRSQL.createQuery("age>=87", repository);
    assertEquals(new QueryImpl<>().ge("age", 87), q);
  }

  @Test
  void testGreaterThan() throws RSQLParserException {
    Attribute ageAttr = when(mock(Attribute.class).getDataType()).thenReturn(INT).getMock();
    doReturn(ageAttr).when(entityType).getAttribute("age");

    Query<Entity> q = molgenisRSQL.createQuery("age>87", repository);
    assertEquals(new QueryImpl<>().gt("age", 87), q);
  }

  @Test
  void testLessThanOrEqual() throws RSQLParserException {
    Attribute ageAttr = when(mock(Attribute.class).getDataType()).thenReturn(INT).getMock();
    doReturn(ageAttr).when(entityType).getAttribute("age");

    Query<Entity> q = molgenisRSQL.createQuery("age<=87", repository);
    assertEquals(new QueryImpl<>().le("age", 87), q);
  }

  @Test
  void testLessThan() throws RSQLParserException {
    Attribute ageAttr = when(mock(Attribute.class).getDataType()).thenReturn(INT).getMock();
    doReturn(ageAttr).when(entityType).getAttribute("age");

    Query<Entity> q = molgenisRSQL.createQuery("age<87", repository);
    assertEquals(new QueryImpl<>().lt("age", 87), q);
  }

  @Test
  void testAnd() throws RSQLParserException {
    Attribute nameAttr = when(mock(Attribute.class).getDataType()).thenReturn(STRING).getMock();
    Attribute ageAttr = when(mock(Attribute.class).getDataType()).thenReturn(INT).getMock();
    doReturn(nameAttr).when(entityType).getAttribute("name");
    doReturn(ageAttr).when(entityType).getAttribute("age");

    // ';' and 'and' or synonyms

    Query<Entity> q = molgenisRSQL.createQuery("name==piet and age==87", repository);
    assertEquals(new QueryImpl<>().nest().eq("name", "piet").and().eq("age", 87).unnest(), q);

    q = molgenisRSQL.createQuery("name==piet;age==87", repository);
    assertEquals(new QueryImpl<>().nest().eq("name", "piet").and().eq("age", 87).unnest(), q);
  }

  @Test
  void testOr() throws RSQLParserException {
    Attribute nameAttr = when(mock(Attribute.class).getDataType()).thenReturn(STRING).getMock();
    Attribute ageAttr = when(mock(Attribute.class).getDataType()).thenReturn(INT).getMock();
    doReturn(nameAttr).when(entityType).getAttribute("name");
    doReturn(ageAttr).when(entityType).getAttribute("age");

    // ',' and 'or' or synonyms

    Query<Entity> q = molgenisRSQL.createQuery("name==piet or age==87", repository);
    assertEquals(new QueryImpl<>().nest().eq("name", "piet").or().eq("age", 87).unnest(), q);

    q = molgenisRSQL.createQuery("name==piet,age==87", repository);
    assertEquals(new QueryImpl<>().nest().eq("name", "piet").or().eq("age", 87).unnest(), q);
  }

  @Test
  void testGreaterThanOnNonNumericalAttribute() throws RSQLParserException {
    Attribute nameAttr = when(mock(Attribute.class).getDataType()).thenReturn(STRING).getMock();
    doReturn(nameAttr).when(entityType).getAttribute("name");

    assertThrows(
        IllegalArgumentException.class, () -> molgenisRSQL.createQuery("name>87", repository));
  }

  @Test
  void testGreaterThanWithNonNumericalArg() throws RSQLParserException {
    Attribute ageAttr = when(mock(Attribute.class).getDataType()).thenReturn(INT).getMock();
    doReturn(ageAttr).when(entityType).getAttribute("age");

    assertThrows(
        NumberFormatException.class, () -> molgenisRSQL.createQuery("age>bogus", repository));
  }

  @Test
  void testComplexQuery() throws RSQLParserException {
    Attribute nameAttr = when(mock(Attribute.class).getDataType()).thenReturn(STRING).getMock();
    Attribute ageAttr = when(mock(Attribute.class).getDataType()).thenReturn(INT).getMock();
    doReturn(nameAttr).when(entityType).getAttribute("name");
    doReturn(ageAttr).when(entityType).getAttribute("age");

    Query<Entity> q =
        molgenisRSQL.createQuery("((name==piet;age==87),(name==klaas;age>100))", repository);
    assertEquals(
        new QueryImpl<>()
            .nest()
            .nest()
            .eq("name", "piet")
            .and()
            .eq("age", 87)
            .unnest()
            .or()
            .nest()
            .eq("name", "klaas")
            .and()
            .gt("age", 100)
            .unnest()
            .unnest(),
        q);
  }

  @Test
  void testXrefIntegerIdValue() {
    Attribute genderIdAttribute = mock(Attribute.class);
    when(genderIdAttribute.getDataType()).thenReturn(INT);
    when(genderEntityType.getIdAttribute()).thenReturn(genderIdAttribute);
    Attribute genderAttr = mock(Attribute.class);
    when(genderAttr.getDataType()).thenReturn(XREF);
    when(genderAttr.getRefEntity()).thenReturn(genderEntityType);
    doReturn(genderAttr).when(entityType).getAttribute("gender");

    Query<Entity> q = molgenisRSQL.createQuery("gender==2", repository);
    assertEquals(new QueryImpl<>().eq("gender", 2), q);
  }
}

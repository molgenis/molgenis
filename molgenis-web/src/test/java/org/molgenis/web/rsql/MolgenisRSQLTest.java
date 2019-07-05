package org.molgenis.web.rsql;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.AttributeType.INT;
import static org.molgenis.data.meta.AttributeType.STRING;
import static org.molgenis.data.meta.AttributeType.XREF;
import static org.testng.Assert.assertEquals;

import cz.jirutka.rsql.parser.RSQLParser;
import cz.jirutka.rsql.parser.RSQLParserException;
import org.mockito.Mock;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.molgenis.data.UnknownAttributeException;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class MolgenisRSQLTest extends AbstractMockitoTest {
  @Mock private Repository<Entity> repository;
  @Mock private EntityType entityType;
  @Mock private EntityType genderEntityType;
  private MolgenisRSQL molgenisRSQL;

  @BeforeMethod
  public void beforeMethod() {
    molgenisRSQL = new MolgenisRSQL(new RSQLParser());
    when(repository.getEntityType()).thenReturn(entityType);
  }

  @Test
  public void testEquals() throws RSQLParserException {
    Attribute nameAttr = when(mock(Attribute.class).getDataType()).thenReturn(STRING).getMock();
    Attribute ageAttr = when(mock(Attribute.class).getDataType()).thenReturn(INT).getMock();
    doReturn(nameAttr).when(entityType).getAttribute("name");
    doReturn(ageAttr).when(entityType).getAttribute("age");

    Query<Entity> q = molgenisRSQL.createQuery("name==piet", repository);
    assertEquals(q, new QueryImpl<>().eq("name", "piet"));

    q = molgenisRSQL.createQuery("name=='piet paulusma'", repository);
    assertEquals(q, new QueryImpl<>().eq("name", "piet paulusma"));

    q = molgenisRSQL.createQuery("age==87", repository);
    assertEquals(q, new QueryImpl<>().eq("age", 87));
  }

  @Test(expectedExceptions = UnknownAttributeException.class)
  public void testUnknowAttribute() throws RSQLParserException {
    molgenisRSQL.createQuery("nonexistingattribute==piet", repository);
  }

  @Test
  public void testGreaterThanOrEqual() throws RSQLParserException {
    Attribute ageAttr = when(mock(Attribute.class).getDataType()).thenReturn(INT).getMock();
    doReturn(ageAttr).when(entityType).getAttribute("age");

    Query<Entity> q = molgenisRSQL.createQuery("age>=87", repository);
    assertEquals(q, new QueryImpl<>().ge("age", 87));
  }

  @Test
  public void testGreaterThan() throws RSQLParserException {
    Attribute ageAttr = when(mock(Attribute.class).getDataType()).thenReturn(INT).getMock();
    doReturn(ageAttr).when(entityType).getAttribute("age");

    Query<Entity> q = molgenisRSQL.createQuery("age>87", repository);
    assertEquals(q, new QueryImpl<>().gt("age", 87));
  }

  @Test
  public void testLessThanOrEqual() throws RSQLParserException {
    Attribute ageAttr = when(mock(Attribute.class).getDataType()).thenReturn(INT).getMock();
    doReturn(ageAttr).when(entityType).getAttribute("age");

    Query<Entity> q = molgenisRSQL.createQuery("age<=87", repository);
    assertEquals(q, new QueryImpl<>().le("age", 87));
  }

  @Test
  public void testLessThan() throws RSQLParserException {
    Attribute ageAttr = when(mock(Attribute.class).getDataType()).thenReturn(INT).getMock();
    doReturn(ageAttr).when(entityType).getAttribute("age");

    Query<Entity> q = molgenisRSQL.createQuery("age<87", repository);
    assertEquals(q, new QueryImpl<>().lt("age", 87));
  }

  @Test
  public void testAnd() throws RSQLParserException {
    Attribute nameAttr = when(mock(Attribute.class).getDataType()).thenReturn(STRING).getMock();
    Attribute ageAttr = when(mock(Attribute.class).getDataType()).thenReturn(INT).getMock();
    doReturn(nameAttr).when(entityType).getAttribute("name");
    doReturn(ageAttr).when(entityType).getAttribute("age");

    // ';' and 'and' or synonyms

    Query<Entity> q = molgenisRSQL.createQuery("name==piet and age==87", repository);
    assertEquals(q, new QueryImpl<>().nest().eq("name", "piet").and().eq("age", 87).unnest());

    q = molgenisRSQL.createQuery("name==piet;age==87", repository);
    assertEquals(q, new QueryImpl<>().nest().eq("name", "piet").and().eq("age", 87).unnest());
  }

  @Test
  public void testOr() throws RSQLParserException {
    Attribute nameAttr = when(mock(Attribute.class).getDataType()).thenReturn(STRING).getMock();
    Attribute ageAttr = when(mock(Attribute.class).getDataType()).thenReturn(INT).getMock();
    doReturn(nameAttr).when(entityType).getAttribute("name");
    doReturn(ageAttr).when(entityType).getAttribute("age");

    // ',' and 'or' or synonyms

    Query<Entity> q = molgenisRSQL.createQuery("name==piet or age==87", repository);
    assertEquals(q, new QueryImpl<>().nest().eq("name", "piet").or().eq("age", 87).unnest());

    q = molgenisRSQL.createQuery("name==piet,age==87", repository);
    assertEquals(q, new QueryImpl<>().nest().eq("name", "piet").or().eq("age", 87).unnest());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGreaterThanOnNonNumericalAttribute() throws RSQLParserException {
    Attribute nameAttr = when(mock(Attribute.class).getDataType()).thenReturn(STRING).getMock();
    doReturn(nameAttr).when(entityType).getAttribute("name");

    molgenisRSQL.createQuery("name>87", repository);
  }

  @Test(expectedExceptions = NumberFormatException.class)
  public void testGreaterThanWithNonNumericalArg() throws RSQLParserException {
    Attribute ageAttr = when(mock(Attribute.class).getDataType()).thenReturn(INT).getMock();
    doReturn(ageAttr).when(entityType).getAttribute("age");

    molgenisRSQL.createQuery("age>bogus", repository);
  }

  @Test
  public void testComplexQuery() throws RSQLParserException {
    Attribute nameAttr = when(mock(Attribute.class).getDataType()).thenReturn(STRING).getMock();
    Attribute ageAttr = when(mock(Attribute.class).getDataType()).thenReturn(INT).getMock();
    doReturn(nameAttr).when(entityType).getAttribute("name");
    doReturn(ageAttr).when(entityType).getAttribute("age");

    Query<Entity> q =
        molgenisRSQL.createQuery("((name==piet;age==87),(name==klaas;age>100))", repository);
    assertEquals(
        q,
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
            .unnest());
  }

  @Test
  public void testXrefIntegerIdValue() {
    Attribute genderIdAttribute = mock(Attribute.class);
    when(genderIdAttribute.getDataType()).thenReturn(INT);
    when(genderEntityType.getIdAttribute()).thenReturn(genderIdAttribute);
    Attribute genderAttr = mock(Attribute.class);
    when(genderAttr.getDataType()).thenReturn(XREF);
    when(genderAttr.getRefEntity()).thenReturn(genderEntityType);
    doReturn(genderAttr).when(entityType).getAttribute("gender");

    Query<Entity> q = molgenisRSQL.createQuery("gender==2", repository);
    assertEquals(q, new QueryImpl<>().eq("gender", 2));
  }
}

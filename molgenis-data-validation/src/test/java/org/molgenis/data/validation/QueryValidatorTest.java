package org.molgenis.data.validation;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.QueryRule.Operator.EQUALS;
import static org.molgenis.data.QueryRule.Operator.FUZZY_MATCH;
import static org.molgenis.data.QueryRule.Operator.FUZZY_MATCH_NGRAM;
import static org.molgenis.data.QueryRule.Operator.GREATER;
import static org.molgenis.data.QueryRule.Operator.GREATER_EQUAL;
import static org.molgenis.data.QueryRule.Operator.IN;
import static org.molgenis.data.QueryRule.Operator.LESS;
import static org.molgenis.data.QueryRule.Operator.LESS_EQUAL;
import static org.molgenis.data.QueryRule.Operator.LIKE;
import static org.molgenis.data.QueryRule.Operator.RANGE;
import static org.molgenis.data.meta.AttributeType.BOOL;
import static org.molgenis.data.meta.AttributeType.CATEGORICAL;
import static org.molgenis.data.meta.AttributeType.CATEGORICAL_MREF;
import static org.molgenis.data.meta.AttributeType.COMPOUND;
import static org.molgenis.data.meta.AttributeType.DATE;
import static org.molgenis.data.meta.AttributeType.DATE_TIME;
import static org.molgenis.data.meta.AttributeType.DECIMAL;
import static org.molgenis.data.meta.AttributeType.EMAIL;
import static org.molgenis.data.meta.AttributeType.ENUM;
import static org.molgenis.data.meta.AttributeType.FILE;
import static org.molgenis.data.meta.AttributeType.HTML;
import static org.molgenis.data.meta.AttributeType.HYPERLINK;
import static org.molgenis.data.meta.AttributeType.INT;
import static org.molgenis.data.meta.AttributeType.LONG;
import static org.molgenis.data.meta.AttributeType.MREF;
import static org.molgenis.data.meta.AttributeType.ONE_TO_MANY;
import static org.molgenis.data.meta.AttributeType.SCRIPT;
import static org.molgenis.data.meta.AttributeType.STRING;
import static org.molgenis.data.meta.AttributeType.TEXT;
import static org.molgenis.data.meta.AttributeType.XREF;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityManager;
import org.molgenis.data.Query;
import org.molgenis.data.QueryRule;
import org.molgenis.data.file.model.FileMeta;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.QueryImpl;

class QueryValidatorTest {
  private QueryValidator queryValidator;

  @BeforeEach
  void setUpBeforeMethod() {
    EntityManager entityManager = mock(EntityManager.class);
    this.queryValidator = new QueryValidator(entityManager);
  }

  @Test
  void testQueryValidator() {
    assertThrows(NullPointerException.class, () -> new QueryValidator(null));
  }

  static Iterator<Object[]> validateValidProvider() {
    List<Object[]> queries = new ArrayList<>(256);

    EnumSet.of(EQUALS)
        .forEach(
            operator -> {
              // BOOL
              Entity boolEntityType = createEntityType(BOOL);
              asList(Boolean.TRUE, Boolean.FALSE, null, "true", "false", "True", "False")
                  .forEach(
                      value ->
                          queries.add(
                              new Object[] {boolEntityType, new QueryImpl<>().eq("attr", value)}));

              // CATEGORICAL, XREF, CATEGORICAL_MREF, MREF, ONE_TO_MANY
              EnumSet.of(STRING, INT, LONG, EMAIL, HYPERLINK)
                  .forEach(
                      refIdAttrType ->
                          EnumSet.of(CATEGORICAL, XREF, CATEGORICAL_MREF, MREF, ONE_TO_MANY)
                              .forEach(
                                  refAttrType -> {
                                    Entity refEntityType =
                                        createEntityType(refAttrType, refIdAttrType);
                                    asList("1", 1, 1L, null)
                                        .forEach(
                                            idValue ->
                                                queries.add(
                                                    new Object[] {
                                                      refEntityType,
                                                      new QueryImpl<>().eq("attr", idValue)
                                                    }));

                                    Entity refEntity =
                                        when(mock(Entity.class).getIdValue())
                                            .thenReturn("1")
                                            .getMock();
                                    queries.add(
                                        new Object[] {
                                          refEntityType, new QueryImpl<>().eq("attr", refEntity)
                                        });
                                  }));

              // DATE
              Entity dateEntityType = createEntityType(DATE);
              asList(LocalDate.now(), "2016-11-25", null)
                  .forEach(
                      value ->
                          queries.add(
                              new Object[] {dateEntityType, new QueryImpl<>().eq("attr", value)}));

              // DATE_TIME
              Entity dateTimeEntityType = createEntityType(DATE_TIME);
              asList(Instant.now(), "1985-08-12T11:12:13+0500", null)
                  .forEach(
                      value ->
                          queries.add(
                              new Object[] {
                                dateTimeEntityType, new QueryImpl<>().eq("attr", value)
                              }));

              // DECIMAL
              Entity decimalEntityType = createEntityType(DECIMAL);
              asList(1.23, "1.23", 1, 1L, null)
                  .forEach(
                      value ->
                          queries.add(
                              new Object[] {
                                decimalEntityType, new QueryImpl<>().eq("attr", value)
                              }));

              // EMAIL, HTML, HYPERLINK, SCRIPT, STRING, TEXT
              EnumSet.of(EMAIL, HTML, HYPERLINK, SCRIPT, STRING, TEXT)
                  .forEach(
                      attrType -> {
                        Entity entityType = createEntityType(attrType);
                        asList("abc", 1, 1L, 1.23, null)
                            .forEach(
                                value ->
                                    queries.add(
                                        new Object[] {
                                          entityType, new QueryImpl<>().eq("attr", value)
                                        }));
                      });

              // INT, LONG
              EnumSet.of(INT, LONG)
                  .forEach(
                      attrType -> {
                        Entity entityType = createEntityType(attrType);
                        asList(1, 1L, "1", null)
                            .forEach(
                                value ->
                                    queries.add(
                                        new Object[] {
                                          entityType, new QueryImpl<>().eq("attr", value)
                                        }));
                      });

              // FILE
              Entity fileEntityType = createEntityType(FILE, STRING);
              asList("file0", mock(FileMeta.class), null)
                  .forEach(
                      idValue ->
                          queries.add(
                              new Object[] {
                                fileEntityType, new QueryImpl<>().eq("attr", idValue)
                              }));

              // ENUM
              Entity enumEntityType = createEntityType(ENUM);
              asList(TestEnum.ENUM0, TestEnum.ENUM1, "ENUM0", "ENUM1", null)
                  .forEach(
                      value ->
                          queries.add(
                              new Object[] {enumEntityType, new QueryImpl<>().eq("attr", value)}));
            });

    EnumSet.of(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)
        .forEach(
            operator -> {
              Entity entityType = createEntityType(INT);
              QueryImpl<Entity> query = new QueryImpl<>();
              query.addRule(new QueryRule("attr", operator, 1));
              queries.add(new Object[] {entityType, query});
            });

    EnumSet.of(FUZZY_MATCH, FUZZY_MATCH_NGRAM, LIKE)
        .forEach(
            operator -> {
              Entity entityType = createEntityType(STRING);
              QueryImpl<Entity> query = new QueryImpl<>();
              query.addRule(new QueryRule("attr", operator, "abc"));
              queries.add(new Object[] {entityType, query});
            });

    EnumSet.of(IN, RANGE)
        .forEach(
            operator -> {
              Entity entityType = createEntityType(INT);
              QueryImpl<Entity> query = new QueryImpl<>();
              query.addRule(new QueryRule("attr", operator, asList(1, 2)));
              queries.add(new Object[] {entityType, query});
            });

    return queries.iterator();
  }

  private enum TestEnum {
    ENUM0,
    ENUM1
  }

  private static Entity createEntityType(AttributeType attrType) {
    return createEntityType(attrType, null);
  }

  private static Entity createEntityType(AttributeType attrType, AttributeType refAttrType) {
    String attrName = "attr";
    Attribute attr = when(mock(Attribute.class).getDataType()).thenReturn(attrType).getMock();
    when(attr.getName()).thenReturn(attrName);
    if (attrType == ENUM) {
      when(attr.getEnumOptions()).thenReturn(asList("ENUM0", "ENUM1"));
    }
    EntityType entityType =
        when(mock(EntityType.class).getAttribute(attrName)).thenReturn(attr).getMock();
    when(entityType.toString()).thenReturn(attrType.toString());

    if (refAttrType == null
        && EnumSet.of(CATEGORICAL, CATEGORICAL_MREF, XREF, MREF).contains(attrType)) {
      refAttrType = INT;
    }
    if (refAttrType != null) {
      Attribute refIdAttr =
          when(mock(Attribute.class).getDataType()).thenReturn(refAttrType).getMock();
      EntityType refEntityType =
          when(mock(EntityType.class).getIdAttribute()).thenReturn(refIdAttr).getMock();
      when(attr.getRefEntity()).thenReturn(refEntityType);
    }
    return entityType;
  }

  @ParameterizedTest
  @MethodSource("validateValidProvider")
  void testValidateValid(EntityType entityType, Query<Entity> query) {
    queryValidator.validate(query, entityType);
    // test passes if not exception occurred
  }

  static Iterator<Object[]> validateInvalidProvider() {
    List<Object[]> queries = new ArrayList<>(6);
    EnumSet.of(BOOL, DECIMAL, INT, LONG, DATE, DATE_TIME, ENUM)
        .forEach(
            attrType ->
                queries.add(
                    new Object[] {
                      new QueryImpl().eq("attr", "invalid"), createEntityType(attrType)
                    }));
    EnumSet.of(
            BOOL,
            DECIMAL,
            INT,
            LONG,
            DATE,
            DATE_TIME,
            ENUM,
            XREF,
            MREF,
            CATEGORICAL,
            CATEGORICAL_MREF)
        .forEach(
            attrType ->
                queries.add(
                    new Object[] {
                      new QueryImpl().eq("attr", new Object()), createEntityType(attrType)
                    }));
    queries.add(new Object[] {new QueryImpl().eq("unknownAttr", "str"), createEntityType(STRING)});
    queries.add(new Object[] {new QueryImpl().eq("attr", "str"), createEntityType(COMPOUND)});
    return queries.iterator();
  }

  @SuppressWarnings("deprecation")
  @ParameterizedTest
  @MethodSource("validateInvalidProvider")
  void testValidateInvalid(Query<Entity> query, EntityType entityType) {
    assertThrows(
        MolgenisValidationException.class, () -> queryValidator.validate(query, entityType));
  }
}

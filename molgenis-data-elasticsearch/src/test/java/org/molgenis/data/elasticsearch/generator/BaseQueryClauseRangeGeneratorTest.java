package org.molgenis.data.elasticsearch.generator;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.data.MolgenisQueryException;
import org.molgenis.data.QueryRule;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.test.AbstractMockitoTest;

class BaseQueryClauseRangeGeneratorTest extends AbstractMockitoTest {
  @Mock DocumentIdGenerator documentIdGenerator;
  private BaseQueryClauseRangeGenerator baseQueryClauseRangeGenerator;

  @BeforeEach
  void setUpBeforeEach() {
    baseQueryClauseRangeGenerator =
        new BaseQueryClauseRangeGenerator(documentIdGenerator, Operator.GREATER) {};
  }

  @SuppressWarnings("deprecation")
  @Test
  void mapQueryRuleNullValue() {
    QueryRule queryRule = mock(QueryRule.class);
    when(queryRule.getField()).thenReturn("attr");

    Attribute attribute = mock(Attribute.class);
    when(attribute.getDataType()).thenReturn(AttributeType.INT);
    when(documentIdGenerator.generateId(attribute)).thenReturn("attr");

    EntityType entityType = mock(EntityType.class);
    when(entityType.getAttributeByName("attr")).thenReturn(attribute);

    assertThrows(
        MolgenisQueryException.class,
        () -> baseQueryClauseRangeGenerator.mapQueryRule(queryRule, entityType));
  }
}

package org.molgenis.data.elasticsearch.generator;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.elasticsearch.generator.model.SortDirection.ASC;
import static org.molgenis.data.elasticsearch.generator.model.SortDirection.DESC;
import static org.molgenis.data.meta.AttributeType.INT;
import static org.molgenis.data.meta.AttributeType.STRING;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.molgenis.data.elasticsearch.generator.model.Sort;
import org.molgenis.data.elasticsearch.generator.model.SortOrder;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;

class SortGeneratorTest {
  private SortGenerator sortGenerator;
  private EntityType entityType;

  @BeforeEach
  void beforeMethod() {
    DocumentIdGenerator documentIdGenerator = mock(DocumentIdGenerator.class);
    when(documentIdGenerator.generateId(any(Attribute.class)))
        .thenAnswer(invocation -> ((Attribute) invocation.getArguments()[0]).getName());

    sortGenerator = new SortGenerator(documentIdGenerator);
    EntityType entityType = when(mock(EntityType.class).getId()).thenReturn("entity").getMock();
    Attribute intAttr = when(mock(Attribute.class).getName()).thenReturn("int").getMock();
    when(intAttr.getDataType()).thenReturn(INT);
    when(entityType.getAttribute("int")).thenReturn(intAttr);
    Attribute stringAttr = when(mock(Attribute.class).getName()).thenReturn("string").getMock();
    when(stringAttr.getDataType()).thenReturn(STRING);
    when(entityType.getAttribute("string")).thenReturn(stringAttr);
    this.entityType = entityType;
  }

  @Test
  void testGenerateAsc() {
    org.molgenis.data.Sort sort =
        new org.molgenis.data.Sort("int", org.molgenis.data.Sort.Direction.ASC);
    Sort elasticSort = sortGenerator.generateSort(sort, entityType);
    Sort expectedElasticSort = Sort.create(singletonList(SortOrder.create("int", ASC)));
    assertEquals(expectedElasticSort, elasticSort);
  }

  @Test
  void testGenerateAscRaw() {
    org.molgenis.data.Sort sort =
        new org.molgenis.data.Sort("string", org.molgenis.data.Sort.Direction.ASC);
    Sort elasticSort = sortGenerator.generateSort(sort, entityType);
    Sort expectedElasticSort = Sort.create(singletonList(SortOrder.create("string.raw", ASC)));
    assertEquals(expectedElasticSort, elasticSort);
  }

  @Test
  void testGenerateDesc() {
    org.molgenis.data.Sort sort =
        new org.molgenis.data.Sort("int", org.molgenis.data.Sort.Direction.DESC);
    Sort elasticSort = sortGenerator.generateSort(sort, entityType);
    Sort expectedElasticSort = Sort.create(singletonList(SortOrder.create("int", DESC)));
    assertEquals(expectedElasticSort, elasticSort);
  }

  @Test
  void testGenerateDescRaw() {
    org.molgenis.data.Sort sort =
        new org.molgenis.data.Sort("string", org.molgenis.data.Sort.Direction.ASC);
    Sort elasticSort = sortGenerator.generateSort(sort, entityType);
    Sort expectedElasticSort = Sort.create(singletonList(SortOrder.create("string.raw", ASC)));
    assertEquals(expectedElasticSort, elasticSort);
  }

  @Test
  void testGenerateDescAscRaw() {
    org.molgenis.data.Sort sort =
        new org.molgenis.data.Sort("int", org.molgenis.data.Sort.Direction.DESC)
            .on("string", org.molgenis.data.Sort.Direction.ASC);
    Sort elasticSort = sortGenerator.generateSort(sort, entityType);
    Sort expectedElasticSort =
        Sort.create(asList(SortOrder.create("int", DESC), SortOrder.create("string.raw", ASC)));
    assertEquals(expectedElasticSort, elasticSort);
  }
}

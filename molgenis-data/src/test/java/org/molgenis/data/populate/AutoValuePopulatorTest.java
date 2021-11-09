package org.molgenis.data.populate;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.quality.Strictness.LENIENT;
import static org.molgenis.data.meta.AttributeType.DATE;
import static org.molgenis.data.meta.AttributeType.DATE_TIME;
import static org.molgenis.data.meta.AttributeType.STRING;
import static org.molgenis.data.semantic.Relation.hasIDDigitCount;
import static org.molgenis.data.semantic.Relation.hasIDPrefix;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.Tag;
import org.molgenis.data.support.DynamicEntity;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = LENIENT)
class AutoValuePopulatorTest {
  private static final String ATTR_ID = "id";
  private static final String ATTR_DATE_AUTO_DEFAULT = "date_auto-default";
  private static final String ATTR_DATE_AUTO_FALSE = "date_auto-false";
  private static final String ATTR_DATE_AUTO_TRUE = "date_auto-true";
  private static final String ATTR_DATETIME_AUTO_DEFAULT = "datetime_auto-default";
  private static final String ATTR_DATETIME_AUTO_FALSE = "datetime_auto-false";
  private static final String ATTR_DATETIME_AUTO_TRUE = "datetime_auto-true";

  private EntityType entityType;
  private Attribute attrId;
  private AutoValuePopulator autoValuePopulator;
  @Mock private Sequences sequences;
  @Mock private IdGenerator idGenerator;
  @Mock private Tag idDigitCountTag;
  @Mock private Tag idPrefixTag;

  @BeforeEach
  void setUpBeforeMethod() {
    entityType = when(mock(EntityType.class).getId()).thenReturn("entity").getMock();
    attrId = when(mock(Attribute.class).getName()).thenReturn(ATTR_ID).getMock();
    when(attrId.getDataType()).thenReturn(STRING);
    when(attrId.isAuto()).thenReturn(true);
    Attribute attrDateAutoDefault =
        when(mock(Attribute.class).getName()).thenReturn(ATTR_DATE_AUTO_DEFAULT).getMock();
    when(attrDateAutoDefault.getDataType()).thenReturn(DATE);
    Attribute attrDateAutoFalse =
        when(mock(Attribute.class).getName()).thenReturn(ATTR_DATE_AUTO_FALSE).getMock();
    when(attrDateAutoFalse.getDataType()).thenReturn(DATE);
    when(attrDateAutoFalse.isAuto()).thenReturn(false);
    Attribute attrDateAutoTrue =
        when(mock(Attribute.class).getName()).thenReturn(ATTR_DATE_AUTO_TRUE).getMock();
    when(attrDateAutoTrue.getDataType()).thenReturn(DATE);
    when(attrDateAutoTrue.isAuto()).thenReturn(true);
    Attribute attrDateTimeAutoDefault =
        when(mock(Attribute.class).getName()).thenReturn(ATTR_DATETIME_AUTO_DEFAULT).getMock();
    when(attrDateTimeAutoDefault.getDataType()).thenReturn(DATE_TIME);
    Attribute attrDateTimeAutoFalse =
        when(mock(Attribute.class).getName()).thenReturn(ATTR_DATETIME_AUTO_FALSE).getMock();
    when(attrDateTimeAutoFalse.getDataType()).thenReturn(DATE_TIME);
    when(attrDateTimeAutoFalse.isAuto()).thenReturn(false);
    Attribute attrDateTimeAutoTrue =
        when(mock(Attribute.class).getName()).thenReturn(ATTR_DATETIME_AUTO_TRUE).getMock();
    when(attrDateTimeAutoTrue.getDataType()).thenReturn(DATE_TIME);
    when(attrDateTimeAutoTrue.isAuto()).thenReturn(true);
    when(entityType.getIdAttribute()).thenReturn(attrId);
    when(entityType.getAtomicAttributes())
        .thenReturn(
            asList(
                attrId,
                attrDateAutoDefault,
                attrDateAutoFalse,
                attrDateAutoTrue,
                attrDateTimeAutoDefault,
                attrDateTimeAutoFalse,
                attrDateTimeAutoTrue));
    when(entityType.getAtomicAttributes())
        .thenReturn(
            asList(
                attrId,
                attrDateAutoDefault,
                attrDateAutoFalse,
                attrDateAutoTrue,
                attrDateTimeAutoDefault,
                attrDateTimeAutoFalse,
                attrDateTimeAutoTrue));
    when(entityType.getAttribute(ATTR_ID)).thenReturn(attrId);
    when(entityType.getAttribute(ATTR_DATE_AUTO_DEFAULT)).thenReturn(attrDateAutoDefault);
    when(entityType.getAttribute(ATTR_DATE_AUTO_FALSE)).thenReturn(attrDateAutoFalse);
    when(entityType.getAttribute(ATTR_DATE_AUTO_TRUE)).thenReturn(attrDateAutoTrue);
    when(entityType.getAttribute(ATTR_DATETIME_AUTO_DEFAULT)).thenReturn(attrDateTimeAutoDefault);
    when(entityType.getAttribute(ATTR_DATETIME_AUTO_FALSE)).thenReturn(attrDateTimeAutoFalse);
    when(entityType.getAttribute(ATTR_DATETIME_AUTO_TRUE)).thenReturn(attrDateTimeAutoTrue);
    when(idGenerator.generateId()).thenReturn("ID1").thenReturn("ID2");
    autoValuePopulator = new AutoValuePopulator(idGenerator, sequences);
  }

  @Test
  void populateAutoValues() {
    Entity entity = new DynamicEntity(entityType);
    autoValuePopulator.populate(entity);

    assertNotNull(entity.getIdValue());
    assertNull(entity.getLocalDate(ATTR_DATE_AUTO_DEFAULT));
    assertNull(entity.getLocalDate(ATTR_DATE_AUTO_FALSE));
    assertNotNull(entity.getLocalDate(ATTR_DATE_AUTO_TRUE));
    assertNull(entity.getInstant(ATTR_DATETIME_AUTO_DEFAULT));
    assertNull(entity.getInstant(ATTR_DATETIME_AUTO_FALSE));
    assertNotNull(entity.getInstant(ATTR_DATETIME_AUTO_TRUE));
  }

  @Test
  void populateStringFromSequence() {
    when(attrId.getTags()).thenReturn(List.of(idDigitCountTag, idPrefixTag));
    when(idDigitCountTag.getRelationIri()).thenReturn(hasIDDigitCount.getIRI());
    when(idDigitCountTag.getLabel()).thenReturn("7");
    when(idPrefixTag.getRelationIri()).thenReturn(hasIDPrefix.getIRI());
    when(idPrefixTag.getLabel()).thenReturn("GEN-");
    when(sequences.generateId(attrId)).thenReturn(123L);

    Entity entity = new DynamicEntity(entityType);
    autoValuePopulator.populate(entity);

    assertEquals("GEN-0000123", entity.getString(attrId));
  }
}

package org.molgenis.api.metadata.v3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.test.AbstractMockitoTest;

class MetadataUtilsTest extends AbstractMockitoTest {

  @Test
  void getI18n() {
    EntityType entityType = mock(EntityType.class);
    when(entityType.getString("labelEn")).thenReturn("English");
    when(entityType.getString("labelNl")).thenReturn("Dutch");
    assertEquals(
        ImmutableMap.of("en", "English", "nl", "Dutch"),
        MetadataUtils.getI18n(entityType, "label"));
  }
}

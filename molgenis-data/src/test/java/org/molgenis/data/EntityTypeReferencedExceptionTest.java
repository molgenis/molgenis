package org.molgenis.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.util.exception.ExceptionMessageTest;

class EntityTypeReferencedExceptionTest extends ExceptionMessageTest {
  @BeforeEach
  void setUp() {
    messageSource.addMolgenisNamespaces("data");
  }

  @ParameterizedTest
  @MethodSource("languageMessageProvider")
  @Override
  protected void testGetLocalizedMessage(String lang, String message) {
    Map<String, Set<String>> entityTypeDependencyMap =
        ImmutableMap.of(
            "MyEntityType0",
            ImmutableSet.of("MyRefEntityType0", "MyRefEntityType1"),
            "MyEntityType1",
            ImmutableSet.of("MyRefEntityType2"));
    ExceptionMessageTest.assertExceptionMessageEquals(
        new EntityTypeReferencedException(entityTypeDependencyMap, mock(Throwable.class)),
        lang,
        message);
  }

  @Test
  void testGetMessage() {
    Map<String, Set<String>> entityTypeDependencyMap =
        ImmutableMap.of(
            "MyEntityType0",
            ImmutableSet.of("MyRefEntityType0", "MyRefEntityType1"),
            "MyEntityType1",
            ImmutableSet.of("MyRefEntityType2"));
    EntityTypeReferencedException ex =
        new EntityTypeReferencedException(entityTypeDependencyMap, mock(Throwable.class));
    assertEquals(
        ex.getMessage(),
        "dependencies:MyEntityType0=MyRefEntityType0,MyRefEntityType1;MyEntityType1=MyRefEntityType2");
  }

  static Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {
        "en",
        "Data cannot be deleted due to dependencies: MyEntityType0 -> [MyRefEntityType0, MyRefEntityType1], MyEntityType1 -> [MyRefEntityType2]."
      },
      new Object[] {
        "nl",
        "Data kan niet verwijdered worden vanwege afhankelijkheden: MyEntityType0 -> [MyRefEntityType0, MyRefEntityType1], MyEntityType1 -> [MyRefEntityType2]."
      }
    };
  }
}

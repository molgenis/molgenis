package org.molgenis.data;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Map;
import java.util.Set;
import org.molgenis.util.exception.ExceptionMessageTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class EntityTypeReferencedExceptionTest extends ExceptionMessageTest {
  @BeforeMethod
  public void setUp() {
    messageSource.addMolgenisNamespaces("data");
  }

  @Test(dataProvider = "languageMessageProvider")
  @Override
  public void testGetLocalizedMessage(String lang, String message) {
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
  public void testGetMessage() {
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

  @DataProvider(name = "languageMessageProvider")
  @Override
  public Object[][] languageMessageProvider() {
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

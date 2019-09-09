package org.molgenis.core.ui.style;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Repository;
import org.molgenis.settings.AppSettings;

class StyleSheetRepositoryDecoratorTest {
  private StyleSheetRepositoryDecorator decorator;
  private AppSettings settings;
  private Repository<StyleSheet> delegate;

  @SuppressWarnings("unchecked")
  @BeforeEach
  void setUp() {

    delegate = mock(Repository.class);
    settings = mock(AppSettings.class);
    when(settings.getBootstrapTheme()).thenReturn("2");

    decorator = new StyleSheetRepositoryDecorator(delegate, settings);
  }

  @Test
  void testDeleteById() {
    decorator.deleteById("1");
    verify(delegate).deleteById("1");
  }

  @SuppressWarnings("deprecation")
  @Test
  void testDeleteByIdCurrent() {
    assertThrows(MolgenisDataException.class, () -> decorator.deleteById("2"));
  }

  @Test
  void testDelete() {
    StyleSheet sheet = mock(StyleSheet.class);
    when(sheet.getId()).thenReturn("1");
    decorator.delete(sheet);
    verify(delegate).delete(sheet);
  }

  @SuppressWarnings("deprecation")
  @Test
  void testDeleteCurrent() {
    StyleSheet sheet = mock(StyleSheet.class);
    when(sheet.getId()).thenReturn("2");
    assertThrows(MolgenisDataException.class, () -> decorator.delete(sheet));
    verifyZeroInteractions(delegate);
  }

  @SuppressWarnings("deprecation")
  @Test
  void testDeleteAll() {
    assertThrows(MolgenisDataException.class, () -> decorator.deleteAll());
  }

  @SuppressWarnings("deprecation")
  @Test
  void testDeleteAllStream() {
    assertThrows(
        MolgenisDataException.class, () -> decorator.deleteAll(Stream.of("1", "2", "3", "4")));
  }
}

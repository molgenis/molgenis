package org.molgenis.core.ui.style;

import static org.mockito.Mockito.*;

import java.util.stream.Stream;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Repository;
import org.molgenis.settings.AppSettings;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class StyleSheetRepositoryDecoratorTest {
  private StyleSheetRepositoryDecorator decorator;
  private AppSettings settings;
  private Repository<StyleSheet> delegate;

  @SuppressWarnings("unchecked")
  @BeforeClass
  public void setUp() {

    delegate = mock(Repository.class);
    settings = mock(AppSettings.class);
    when(settings.getBootstrapTheme()).thenReturn("2");

    decorator = new StyleSheetRepositoryDecorator(delegate, settings);
  }

  @Test
  public void testDeleteById() {
    decorator.deleteById("1");
    verify(delegate).deleteById("1");
  }

  @SuppressWarnings("deprecation")
  @Test(expectedExceptions = MolgenisDataException.class)
  public void testDeleteByIdCurrent() {
    decorator.deleteById("2");
    verifyZeroInteractions(delegate);
  }

  @Test
  public void testDelete() {
    StyleSheet sheet = mock(StyleSheet.class);
    when(sheet.getId()).thenReturn("1");
    decorator.delete(sheet);
    verify(delegate).delete(sheet);
  }

  @SuppressWarnings("deprecation")
  @Test(expectedExceptions = MolgenisDataException.class)
  public void testDeleteCurrent() {
    StyleSheet sheet = mock(StyleSheet.class);
    when(sheet.getId()).thenReturn("2");
    decorator.delete(sheet);
    verifyZeroInteractions(delegate);
  }

  @SuppressWarnings("deprecation")
  @Test(expectedExceptions = MolgenisDataException.class)
  public void testDeleteAll() {
    decorator.deleteAll();
    verifyZeroInteractions(delegate);
  }

  @SuppressWarnings("deprecation")
  @Test(expectedExceptions = MolgenisDataException.class)
  public void testDeleteAllStream() {
    decorator.deleteAll(Stream.of("1", "2", "3", "4"));
  }
}

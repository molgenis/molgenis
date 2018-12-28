package org.molgenis.core.ui.style;

import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.core.ui.style.StyleServiceImpl.BOOTSTRAP_FALL_BACK_THEME;
import static org.molgenis.core.ui.style.StyleSheetMetadata.STYLE_SHEET;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.apache.commons.io.IOUtils;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.molgenis.data.file.FileStore;
import org.molgenis.data.file.model.FileMeta;
import org.molgenis.data.file.model.FileMetaFactory;
import org.molgenis.data.file.model.FileMetaMetaData;
import org.molgenis.data.populate.IdGenerator;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.settings.AppSettings;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.core.io.FileSystemResource;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class StyleServiceTest extends AbstractMockitoTest {
  private static final String THEME_MOLGENIS_NAME = "molgenis";
  private static final String THEME_MOLGENIS = "bootstrap-" + THEME_MOLGENIS_NAME + ".min.css";

  @Mock private AppSettings appSettings;
  @Mock private IdGenerator idGenerator;
  @Mock private FileStore fileStore;
  @Mock private FileMetaFactory fileMetaFactory;
  @Mock private StyleSheetFactory styleSheetFactory;
  @Mock private DataService dataService;
  private StyleServiceImpl styleServiceImpl;

  @BeforeMethod
  public void setUpBeforeMethod() {
    styleServiceImpl =
        new StyleServiceImpl(
            appSettings, idGenerator, fileStore, fileMetaFactory, styleSheetFactory, dataService);
  }

  @Test
  public void testGetAvailableStyles() {
    StyleSheet styleSheet = mock(StyleSheet.class);
    List<StyleSheet> styleSheets = Collections.singletonList(styleSheet);
    when(styleSheet.getName()).thenReturn(THEME_MOLGENIS);
    when(dataService.findAll(StyleSheetMetadata.STYLE_SHEET, StyleSheet.class))
        .thenReturn(styleSheets.stream());

    Set<Style> availableStyles = styleServiceImpl.getAvailableStyles();
    assertTrue(
        availableStyles.stream().anyMatch(style -> style.getName().equals(THEME_MOLGENIS_NAME)));
  }

  @Test
  public void testSetSelectedStyleUndefined() {
    styleServiceImpl.setSelectedStyle("undefined");
    verify(appSettings, never()).setBootstrapTheme(anyString());
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void testSetSelectedStyleUnknownStyle() {
    styleServiceImpl.setSelectedStyle("unknown");
  }

  @Test
  public void testSetSelectedStyle() {
    String newTheme = "yeti";
    StyleSheet yetiSheet = mock(StyleSheet.class);
    List<StyleSheet> styleSheets = Collections.singletonList(yetiSheet);
    when(yetiSheet.getName()).thenReturn("bootstrap-" + newTheme + ".min.css");
    when(dataService.findAll(StyleSheetMetadata.STYLE_SHEET, StyleSheet.class))
        .thenReturn(styleSheets.stream());

    styleServiceImpl.setSelectedStyle(newTheme);

    ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);
    verify(appSettings).setBootstrapTheme(argument.capture());
    assertEquals("bootstrap-" + newTheme + ".min.css", argument.getValue());
  }

  @Test
  public void testGetSelectedStyle() {
    when(appSettings.getBootstrapTheme()).thenReturn(THEME_MOLGENIS);

    StyleSheet styleSheet = mock(StyleSheet.class);
    List<StyleSheet> styleSheets = Collections.singletonList(styleSheet);
    when(styleSheet.getName()).thenReturn(THEME_MOLGENIS);
    when(dataService.findAll(StyleSheetMetadata.STYLE_SHEET, StyleSheet.class))
        .thenReturn(styleSheets.stream());

    assertEquals(styleServiceImpl.getSelectedStyle().getName(), THEME_MOLGENIS_NAME);
  }

  @SuppressWarnings("deprecation")
  @Test(expectedExceptions = MolgenisStyleException.class)
  public void addStylesWithExistingId() throws IOException, MolgenisStyleException {
    String styleId = "style";
    String bs3FileName = "any";
    String bs4FileName = "any";
    InputStream bs3Data = IOUtils.toInputStream("any", "UTF-8");
    InputStream bs4Data = IOUtils.toInputStream("any", "UTF-8");

    StyleSheet styleSheet = mock(StyleSheet.class);
    @SuppressWarnings("unchecked")
    Repository<Entity> styleSheetRepository = mock(Repository.class);
    when(dataService.getRepository(STYLE_SHEET)).thenReturn(styleSheetRepository);
    when(styleSheetRepository.findOneById(styleId)).thenReturn(styleSheet);
    styleServiceImpl.addStyle(styleId, bs3FileName, bs3Data, bs4FileName, bs4Data);
  }

  @SuppressWarnings("deprecation")
  @Test
  public void addBootstrap3And4Styles() throws IOException, MolgenisStyleException {
    // setup
    String styleId = "my-style.min.css";
    String bs3FileName = "bs3FileName";
    String bs4FileName = "bs4FileName";
    InputStream bs3Data = IOUtils.toInputStream("bs 3 data", "UTF-8");
    InputStream bs4Data = IOUtils.toInputStream("bs 4 data", "UTF-8");

    @SuppressWarnings("unchecked")
    Repository<Entity> styleSheetRepository = mock(Repository.class);
    when(dataService.getRepository(STYLE_SHEET)).thenReturn(styleSheetRepository);
    when(styleSheetRepository.findOneById(styleId)).thenReturn(null);

    String generatedId = "my-id";
    when(idGenerator.generateId()).thenReturn(generatedId);

    StyleSheet styleSheet = mock(StyleSheet.class);
    when(styleSheet.getName()).thenReturn(styleId);
    when(styleSheetFactory.create(styleId)).thenReturn(styleSheet);

    FileMeta fileMeta = mock(FileMeta.class);
    when(fileMetaFactory.create(generatedId)).thenReturn(fileMeta);

    File storedFile = mock(File.class);
    when(storedFile.length()).thenReturn(123L);
    when(fileStore.getFileUnchecked(generatedId)).thenReturn(storedFile);

    // execute
    styleServiceImpl.addStyle(styleId, bs3FileName, bs3Data, bs4FileName, bs4Data);

    // verify
    verify(styleSheet).setName(styleId);
    verify(dataService, times(1)).add(STYLE_SHEET, styleSheet);
    verify(fileStore, times(1)).store(bs3Data, generatedId);
    verify(fileStore, times(1)).store(bs4Data, generatedId);
    // two times, once for each style file
    verify(dataService, times(2)).add(FileMetaMetaData.FILE_META, fileMeta);
  }

  @SuppressWarnings("deprecation")
  @Test
  public void addBootstrap3StyleOnly() throws IOException, MolgenisStyleException {
    // setup
    String styleId = "my-style.min.css";
    String bs3FileName = "bs3FileName";
    InputStream bs3Data = IOUtils.toInputStream("bs 3 data", "UTF-8");

    @SuppressWarnings("unchecked")
    Repository<Entity> styleSheetRepository = mock(Repository.class);
    when(dataService.getRepository(STYLE_SHEET)).thenReturn(styleSheetRepository);
    when(styleSheetRepository.findOneById(styleId)).thenReturn(null);

    String generatedId = "my-id";
    when(idGenerator.generateId()).thenReturn(generatedId);

    StyleSheet styleSheet = mock(StyleSheet.class);
    when(styleSheet.getName()).thenReturn(styleId);
    when(styleSheetFactory.create(styleId)).thenReturn(styleSheet);

    FileMeta fileMeta = mock(FileMeta.class);
    when(fileMetaFactory.create(generatedId)).thenReturn(fileMeta);

    File storedFile = mock(File.class);
    when(storedFile.length()).thenReturn(123L);
    when(fileStore.getFileUnchecked(generatedId)).thenReturn(storedFile);

    // execute
    styleServiceImpl.addStyle(styleId, bs3FileName, bs3Data, null, null);

    // verify
    verify(styleSheet).setName(styleId);
    verify(dataService, times(1)).add(STYLE_SHEET, styleSheet);
    verify(fileStore, times(1)).store(bs3Data, generatedId);
    verify(dataService, times(1)).add(FileMetaMetaData.FILE_META, fileMeta);
  }

  @SuppressWarnings("deprecation")
  @Test(expectedExceptions = MolgenisStyleException.class)
  public void getUnknownThemeData() throws MolgenisStyleException {
    String styleName = "no-body";
    Query<StyleSheet> expectedQuery =
        new QueryImpl<StyleSheet>().eq(StyleSheetMetadata.NAME, styleName);
    when(dataService.findOne(STYLE_SHEET, expectedQuery, StyleSheet.class)).thenReturn(null);

    BootstrapVersion version = BootstrapVersion.BOOTSTRAP_VERSION_3;
    styleServiceImpl.getThemeData(styleName, version);
  }

  @SuppressWarnings("deprecation")
  @Test
  public void getBootstrap3ThemeData() throws MolgenisStyleException, IOException {
    String styleName = "my-style";
    StyleSheet styleSheet = mock(StyleSheet.class);
    FileMeta fileMeta = mock(FileMeta.class);
    String fileId = "fileId";
    when(fileMeta.getId()).thenReturn(fileId);
    when(styleSheet.getBootstrap3Theme()).thenReturn(fileMeta);
    Query<StyleSheet> expectedQuery =
        new QueryImpl<StyleSheet>().eq(StyleSheetMetadata.NAME, styleName);
    when(dataService.findOne(STYLE_SHEET, expectedQuery, StyleSheet.class)).thenReturn(styleSheet);
    String mockFilePath = "mock/file/path";
    BootstrapVersion version = BootstrapVersion.BOOTSTRAP_VERSION_3;

    File file = mock(File.class);
    when(file.getPath()).thenReturn(mockFilePath);
    when(fileStore.getFile(fileId)).thenReturn(file);
    FileSystemResource themeData = styleServiceImpl.getThemeData(styleName, version);

    assertEquals(themeData.getPath(), mockFilePath);
  }

  @SuppressWarnings("deprecation")
  @Test
  public void getBootstrap4ThemeData() throws MolgenisStyleException, IOException {
    String styleName = "my-style";
    StyleSheet styleSheet = mock(StyleSheet.class);
    FileMeta fileMeta = mock(FileMeta.class);
    String fileId = "fileId";
    when(fileMeta.getId()).thenReturn(fileId);
    when(styleSheet.getBootstrap4Theme()).thenReturn(fileMeta);
    Query<StyleSheet> expectedQuery =
        new QueryImpl<StyleSheet>().eq(StyleSheetMetadata.NAME, styleName);
    when(dataService.findOne(STYLE_SHEET, expectedQuery, StyleSheet.class)).thenReturn(styleSheet);
    File styleFile = mock(File.class);
    String mockFilePath = "mock/file/path";
    when(styleFile.getPath()).thenReturn(mockFilePath);
    when(fileStore.getFile(fileId)).thenReturn(styleFile);
    BootstrapVersion version = BootstrapVersion.BOOTSTRAP_VERSION_4;

    FileSystemResource themeData = styleServiceImpl.getThemeData(styleName, version);

    assertEquals(themeData.getPath(), mockFilePath);
  }

  @SuppressWarnings("deprecation")
  @Test
  public void getBootstrap4FallBackThemeData() throws MolgenisStyleException, IOException {
    String styleName = "my-style";
    StyleSheet styleSheet = mock(StyleSheet.class);
    StyleSheet fallBackTheme = mock(StyleSheet.class);
    FileMeta fileMeta = mock(FileMeta.class);
    String fileId = "fileId";
    when(fileMeta.getId()).thenReturn(fileId);
    when(styleSheet.getBootstrap4Theme()).thenReturn(null);
    when(fallBackTheme.getBootstrap4Theme()).thenReturn(fileMeta);
    Query<StyleSheet> expectedQuery =
        new QueryImpl<StyleSheet>().eq(StyleSheetMetadata.NAME, styleName);
    Query<StyleSheet> fallBackQuery =
        new QueryImpl<StyleSheet>().eq(StyleSheetMetadata.NAME, BOOTSTRAP_FALL_BACK_THEME);
    doReturn(styleSheet).when(dataService).findOne(STYLE_SHEET, expectedQuery, StyleSheet.class);
    doReturn(fallBackTheme).when(dataService).findOne(STYLE_SHEET, fallBackQuery, StyleSheet.class);
    File styleFile = mock(File.class);
    String mockFilePath = "mock/file/path";
    when(styleFile.getPath()).thenReturn(mockFilePath);
    when(fileStore.getFile(fileId)).thenReturn(styleFile);
    BootstrapVersion version = BootstrapVersion.BOOTSTRAP_VERSION_4;

    FileSystemResource themeData = styleServiceImpl.getThemeData(styleName, version);

    assertEquals(themeData.getPath(), mockFilePath);
  }
}

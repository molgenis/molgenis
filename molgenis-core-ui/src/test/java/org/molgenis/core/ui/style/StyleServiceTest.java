package org.molgenis.core.ui.style;

import com.google.common.collect.Iterables;
import org.apache.commons.io.IOUtils;
import org.mockito.ArgumentCaptor;
import org.molgenis.data.DataService;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.molgenis.data.file.FileStore;
import org.molgenis.data.file.model.FileMeta;
import org.molgenis.data.file.model.FileMetaFactory;
import org.molgenis.data.file.model.FileMetaMetaData;
import org.molgenis.data.populate.IdGenerator;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.settings.AppSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.*;
import static org.molgenis.core.ui.style.StyleServiceImpl.BOOTSTRAP_FALL_BACK_THEME;
import static org.molgenis.core.ui.style.StyleSheetMetadata.STYLE_SHEET;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@ContextConfiguration(classes = StyleServiceTest.Config.class)
public class StyleServiceTest extends AbstractTestNGSpringContextTests
{
	private static final String THEME_MOLGENIS_NAME = "molgenis";
	private static final String THEME_MOLGENIS = "bootstrap-" + THEME_MOLGENIS_NAME + ".min.css";

	@Autowired
	private StyleService styleService;

	@Autowired
	private StyleSheetFactory styleSheetFactory;

	@Autowired
	private IdGenerator idGenerator;

	@Autowired
	private FileMetaFactory fileMetaFactory;

	@Autowired
	private FileStore fileStore;

	@Autowired
	private AppSettings appSettings;

	@Autowired
	private DataService dataService;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		reset(appSettings, dataService, styleSheetFactory, idGenerator, fileMetaFactory, fileStore);
		when(appSettings.getBootstrapTheme()).thenReturn(THEME_MOLGENIS);
	}

	@Test
	public void testGetAvailableStyles()
	{
		StyleSheet styleSheet = mock(StyleSheet.class);
		List<StyleSheet> styleSheets = Collections.singletonList(styleSheet);
		when(styleSheet.getName()).thenReturn(THEME_MOLGENIS);
		when(dataService.findAll(StyleSheetMetadata.STYLE_SHEET, StyleSheet.class)).thenReturn(styleSheets.stream());

		Set<Style> availableStyles = styleService.getAvailableStyles();
		assertTrue(Iterables.any(availableStyles, style -> style.getName().equals(THEME_MOLGENIS_NAME)));
	}

	@Test
	public void testSetSelectedStyleUndefined()
	{
		styleService.setSelectedStyle("undefined");
		verify(appSettings, never()).setBootstrapTheme(anyString());
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void testSetSelectedStyleUnknownStyle()
	{
		styleService.setSelectedStyle("unknown");
	}

	@Test
	public void testSetSelectedStyle()
	{
		String newTheme = "yeti";
		StyleSheet yetiSheet = mock(StyleSheet.class);
		List<StyleSheet> styleSheets = Collections.singletonList(yetiSheet);
		when(yetiSheet.getName()).thenReturn("bootstrap-" + newTheme + ".min.css");
		when(dataService.findAll(StyleSheetMetadata.STYLE_SHEET, StyleSheet.class)).thenReturn(styleSheets.stream());

		styleService.setSelectedStyle(newTheme);

		ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);
		verify(appSettings).setBootstrapTheme(argument.capture());
		assertEquals("bootstrap-" + newTheme + ".min.css", argument.getValue());
	}

	@Test
	public void testGetSelectedStyle()
	{
		StyleSheet styleSheet = mock(StyleSheet.class);
		List<StyleSheet> styleSheets = Collections.singletonList(styleSheet);
		when(styleSheet.getName()).thenReturn(THEME_MOLGENIS);
		when(dataService.findAll(StyleSheetMetadata.STYLE_SHEET, StyleSheet.class)).thenReturn(styleSheets.stream());

		assertEquals(styleService.getSelectedStyle().getName(), THEME_MOLGENIS_NAME);
	}

	@Test(expectedExceptions = MolgenisStyleException.class)
	public void addStylesWithExistingId() throws IOException, MolgenisStyleException
	{
		String styleId = "style";
		String bs3FileName = "any";
		String bs4FileName = "any";
		InputStream bs3Data = IOUtils.toInputStream("any", "UTF-8");
		InputStream bs4Data = IOUtils.toInputStream("any", "UTF-8");

		StyleSheet styleSheet = mock(StyleSheet.class);
		Repository styleSheetRepository = mock(Repository.class);
		when(dataService.getRepository(STYLE_SHEET)).thenReturn(styleSheetRepository);
		when(styleSheetRepository.findOneById(styleId)).thenReturn(styleSheet);
		styleService.addStyle(styleId, bs3FileName, bs3Data, bs4FileName, bs4Data);
	}

	@Test
	public void addBootstrap3And4Styles() throws IOException, MolgenisStyleException
	{
		// setup
		String styleId = "my-style.min.css";
		String bs3FileName = "bs3FileName";
		String bs4FileName = "bs4FileName";
		InputStream bs3Data = IOUtils.toInputStream("bs 3 data", "UTF-8");
		InputStream bs4Data = IOUtils.toInputStream("bs 4 data", "UTF-8");

		Repository styleSheetRepository = mock(Repository.class);
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
		when(fileStore.getFile(generatedId)).thenReturn(storedFile);

		// execute
		styleService.addStyle(styleId, bs3FileName, bs3Data, bs4FileName, bs4Data);

		// verify
		verify(styleSheet).setName(styleId);
		verify(dataService, times(1)).add(STYLE_SHEET, styleSheet);
		verify(fileStore, times(1)).store(bs3Data, generatedId);
		verify(fileStore, times(1)).store(bs4Data, generatedId);
		// two times, once for each style file
		verify(dataService, times(2)).add(FileMetaMetaData.FILE_META, fileMeta);
	}

	@Test
	public void addBootstrap3StyleOnly() throws IOException, MolgenisStyleException
	{
		// setup
		String styleId = "my-style.min.css";
		String bs3FileName = "bs3FileName";
		InputStream bs3Data = IOUtils.toInputStream("bs 3 data", "UTF-8");

		Repository styleSheetRepository = mock(Repository.class);
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
		when(fileStore.getFile(generatedId)).thenReturn(storedFile);

		// execute
		styleService.addStyle(styleId, bs3FileName, bs3Data, null, null);

		// verify
		verify(styleSheet).setName(styleId);
		verify(dataService, times(1)).add(STYLE_SHEET, styleSheet);
		verify(fileStore, times(1)).store(bs3Data, generatedId);
		verify(dataService, times(1)).add(FileMetaMetaData.FILE_META, fileMeta);
	}

	@Test(expectedExceptions = MolgenisStyleException.class)
	public void getUnknownThemeData() throws MolgenisStyleException
	{
		String styleName = "no-body";
		Query<StyleSheet> expectedQuery = new QueryImpl<StyleSheet>().eq(StyleSheetMetadata.NAME, styleName);
		when(dataService.findOne(STYLE_SHEET, expectedQuery, StyleSheet.class)).thenReturn(null);

		BootstrapVersion version = BootstrapVersion.BOOTSTRAP_VERSION_3;
		styleService.getThemeData(styleName, version);
	}

	@Test
	public void getBootstrap3ThemeData() throws MolgenisStyleException
	{
		String styleName = "my-style";
		StyleSheet styleSheet = mock(StyleSheet.class);
		when(styleSheet.getId()).thenReturn(styleName);
		FileMeta fileMeta = mock(FileMeta.class);
		String fileId = "fileId";
		when(fileMeta.getId()).thenReturn(fileId);
		when(styleSheet.getBootstrap3Theme()).thenReturn(fileMeta);
		Query<StyleSheet> expectedQuery = new QueryImpl<StyleSheet>().eq(StyleSheetMetadata.NAME, styleName);
		when(dataService.findOne(STYLE_SHEET, expectedQuery, StyleSheet.class)).thenReturn(styleSheet);
		File styleFile = mock(File.class);
		String mockFilePath = "mock/file/path";
		when(styleFile.getPath()).thenReturn(mockFilePath);
		when(fileStore.getFile(fileId)).thenReturn(styleFile);
		BootstrapVersion version = BootstrapVersion.BOOTSTRAP_VERSION_3;

		FileSystemResource themeData = styleService.getThemeData(styleName, version);

		assertEquals(themeData.getPath(), mockFilePath);
	}

	@Test
	public void getBootstrap4ThemeData() throws MolgenisStyleException
	{
		String styleName = "my-style";
		StyleSheet styleSheet = mock(StyleSheet.class);
		when(styleSheet.getId()).thenReturn(styleName);
		FileMeta fileMeta = mock(FileMeta.class);
		String fileId = "fileId";
		when(fileMeta.getId()).thenReturn(fileId);
		when(styleSheet.getBootstrap4Theme()).thenReturn(fileMeta);
		Query<StyleSheet> expectedQuery = new QueryImpl<StyleSheet>().eq(StyleSheetMetadata.NAME, styleName);
		when(dataService.findOne(STYLE_SHEET, expectedQuery, StyleSheet.class)).thenReturn(styleSheet);
		File styleFile = mock(File.class);
		String mockFilePath = "mock/file/path";
		when(styleFile.getPath()).thenReturn(mockFilePath);
		when(fileStore.getFile(fileId)).thenReturn(styleFile);
		BootstrapVersion version = BootstrapVersion.BOOTSTRAP_VERSION_4;

		FileSystemResource themeData = styleService.getThemeData(styleName, version);

		assertEquals(themeData.getPath(), mockFilePath);
	}

	@Test
	public void getBootstrap4FallBackThemeData() throws MolgenisStyleException
	{
		String styleName = "my-style";
		StyleSheet styleSheet = mock(StyleSheet.class);
		StyleSheet fallBackTheme = mock(StyleSheet.class);
		FileMeta fileMeta = mock(FileMeta.class);
		String fileId = "fileId";
		when(fileMeta.getId()).thenReturn(fileId);
		when(styleSheet.getBootstrap4Theme()).thenReturn(null);
		when(fallBackTheme.getBootstrap4Theme()).thenReturn(fileMeta);
		Query<StyleSheet> expectedQuery = new QueryImpl<StyleSheet>().eq(StyleSheetMetadata.NAME, styleName);
		Query<StyleSheet> fallBackQuery = new QueryImpl<StyleSheet>().eq(StyleSheetMetadata.NAME,
				BOOTSTRAP_FALL_BACK_THEME);
		when(dataService.findOne(STYLE_SHEET, expectedQuery, StyleSheet.class)).thenReturn(styleSheet);
		when(dataService.findOne(STYLE_SHEET, fallBackQuery, StyleSheet.class)).thenReturn(fallBackTheme);
		File styleFile = mock(File.class);
		String mockFilePath = "mock/file/path";
		when(styleFile.getPath()).thenReturn(mockFilePath);
		when(fileStore.getFile(fileId)).thenReturn(styleFile);
		BootstrapVersion version = BootstrapVersion.BOOTSTRAP_VERSION_4;

		FileSystemResource themeData = styleService.getThemeData(styleName, version);

		assertEquals(themeData.getPath(), mockFilePath);
	}

	@Configuration
	static class Config
	{
		@Bean
		StyleService styleService()
		{
			return new StyleServiceImpl(appSettings(), idGenerator(), fileStore(), fileMetaFactory(),
					styleSheetFactory(), dataService());
		}

		@Bean
		AppSettings appSettings()
		{
			return mock(AppSettings.class);
		}

		@Bean
		StyleSheetFactory styleSheetFactory()
		{
			return mock(StyleSheetFactory.class);
		}

		@Bean
		IdGenerator idGenerator()
		{
			return mock(IdGenerator.class);
		}

		@Bean
		FileStore fileStore()
		{
			return mock(FileStore.class);
		}

		@Bean
		FileMetaFactory fileMetaFactory()
		{
			return mock(FileMetaFactory.class);
		}

		@Bean
		DataService dataService()
		{
			return mock(DataService.class);
		}
	}
}

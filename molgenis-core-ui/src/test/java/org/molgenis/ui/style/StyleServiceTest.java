package org.molgenis.ui.style;

import com.google.common.collect.Iterables;
import org.mockito.ArgumentCaptor;
import org.molgenis.data.DataService;
import org.molgenis.data.populate.IdGenerator;
import org.molgenis.data.settings.AppSettings;
import org.molgenis.file.FileStore;
import org.molgenis.file.model.FileMetaFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.*;
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
	private AppSettings appSettings;

	@Autowired
	private DataService dataService;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		reset(appSettings, dataService);
		when(appSettings.getBootstrapTheme()).thenReturn(THEME_MOLGENIS);
	}

	@Test
	public void testGetAvailableStyles()
	{
		StyleSheet styleSheet = mock(StyleSheet.class);
		List<StyleSheet> styleSheets = Collections.singletonList(styleSheet);
		when(styleSheet.getName()).thenReturn(THEME_MOLGENIS);
		when(dataService.findAll(StyleMetadata.STYLE_SHEET, StyleSheet.class)).thenReturn(styleSheets.stream());

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
		when(dataService.findAll(StyleMetadata.STYLE_SHEET, StyleSheet.class)).thenReturn(styleSheets.stream());

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
		when(dataService.findAll(StyleMetadata.STYLE_SHEET, StyleSheet.class)).thenReturn(styleSheets.stream());

		assertEquals(styleService.getSelectedStyle().getName(), THEME_MOLGENIS_NAME);
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

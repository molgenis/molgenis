package org.molgenis.ui.style;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.Set;

import org.mockito.ArgumentCaptor;
import org.molgenis.data.settings.AppSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

@ContextConfiguration(classes = StyleServiceTest.Config.class)
public class StyleServiceTest extends AbstractTestNGSpringContextTests
{
	private static final String THEME_MOLGENIS_NAME = "molgenis";
	private static final String THEME_MOLGENIS = "bootstrap-" + THEME_MOLGENIS_NAME + ".min.css";

	@Autowired
	private StyleService styleService;

	@Autowired
	private AppSettings appSettings;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		reset(appSettings);
		when(appSettings.getBootstrapTheme()).thenReturn(THEME_MOLGENIS);
	}

	@Test
	public void testGetAvailableStyles()
	{
		Set<Style> availableStyles = styleService.getAvailableStyles();
		assertTrue(Iterables.any(availableStyles, new Predicate<Style>()
		{
			@Override
			public boolean apply(Style style)
			{
				return style.getName().equals(THEME_MOLGENIS_NAME);
			}
		}));
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
		styleService.setSelectedStyle(newTheme);

		ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);
		verify(appSettings).setBootstrapTheme(argument.capture());
		assertEquals("bootstrap-" + newTheme + ".min.css", argument.getValue());
	}

	@Test
	public void testGetSelectedStyle()
	{
		assertEquals(styleService.getSelectedStyle().getName(), THEME_MOLGENIS_NAME);
	}

	@Configuration
	static class Config
	{
		@Bean
		StyleService styleService()
		{
			return new StyleServiceImpl(appSettings());
		}

		@Bean
		AppSettings appSettings()
		{
			return mock(AppSettings.class);
		}
	}
}

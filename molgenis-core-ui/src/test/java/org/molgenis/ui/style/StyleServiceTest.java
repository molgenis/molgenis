package org.molgenis.ui.style;

import static org.mockito.Mockito.mock;

import org.molgenis.framework.server.MolgenisSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

@ContextConfiguration(classes = StyleServiceTest.Config.class)
public class StyleServiceTest extends AbstractTestNGSpringContextTests
{
	@Autowired
	private MolgenisSettings molgenisSettings;
	
	@Autowired
	private StyleService styleService;

	@Test
	public void testGetAvailableStyles()
	{
		styleService.getAvailableStyles();
	}

	@Test
	public void testSetSelectedStyle()
	{
	}

	@Test
	public void testGetSelectedStyle()
	{

	}

	@Configuration
	static class Config
	{
		@Bean
		MolgenisSettings molgenisSettings()
		{
			return mock(MolgenisSettings.class);
		}

		@Bean
		StyleService styleService()
		{
			return new StyleServiceImpl();
		}
	}
}

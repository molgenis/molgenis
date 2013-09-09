package org.molgenis.ui;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.io.IOException;

import org.molgenis.framework.server.MolgenisPermissionService;
import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.ui.XmlMolgenisUiTest.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@ContextConfiguration(classes = Config.class, loader = AnnotationConfigContextLoader.class)
public class XmlMolgenisUiTest extends AbstractTestNGSpringContextTests
{
	@Configuration
	static class Config
	{
		@Bean
		public XmlMolgenisUi xmlMolgenisUi() throws IOException
		{
			return new XmlMolgenisUi(xmlMolgenisUiLoader(), molgenisSettings(), molgenisPermissionService());
		}

		@Bean
		public XmlMolgenisUiLoader xmlMolgenisUiLoader() throws IOException
		{
			return when(mock(XmlMolgenisUiLoader.class).load()).thenReturn(molgenis()).getMock();
		}

		@Bean
		public Molgenis molgenis()
		{
			return mock(Molgenis.class);
		}

		@Bean
		public MolgenisSettings molgenisSettings()
		{
			return mock(MolgenisSettings.class);
		}

		@Bean
		public MolgenisPermissionService molgenisPermissionService()
		{
			return mock(MolgenisPermissionService.class);
		}
	}

	@Autowired
	private XmlMolgenisUi xmlMolgenisUi;

	@Autowired
	private MolgenisSettings molgenisSettings;

	@Autowired
	private MolgenisPermissionService molgenisPermissionService;

	@Autowired
	private Molgenis molgenis;

	@BeforeMethod
	public void setUp() throws IOException
	{
		reset(molgenisSettings);
		reset(molgenisPermissionService);
	}

	@Test
	public void getHrefCss()
	{
		String cssName = "default.css";
		when(molgenisSettings.getProperty(XmlMolgenisUi.KEY_APP_HREF_CSS)).thenReturn(cssName);
		assertEquals(xmlMolgenisUi.getHrefCss(), cssName);
	}

	@Test
	public void getHrefLogo()
	{
		String logoName = "logo.png";
		when(molgenisSettings.getProperty(XmlMolgenisUi.KEY_APP_HREF_LOGO, XmlMolgenisUi.DEFAULT_APP_HREF_LOGO))
				.thenReturn(logoName);
		assertEquals(xmlMolgenisUi.getHrefLogo(), logoName);
	}

	@Test
	public void getMenu()
	{
		String menuId = "menu1";
		MenuType menuType = new MenuType();
		menuType.setName(menuId);
		when(molgenis.getMenu()).thenReturn(menuType);
		MolgenisUiMenu menu = xmlMolgenisUi.getMenu();
		assertEquals(menu.getId(), menuId);
	}

	@Test
	public void getTitle() throws IOException
	{
		String title = "title";
		when(molgenis.getLabel()).thenReturn(title);
		when(molgenisSettings.getProperty(eq(XmlMolgenisUi.KEY_APP_NAME), anyString())).thenReturn(title);
		assertEquals(xmlMolgenisUi.getTitle(), title);
	}

	@Test
	public void getTitleFromXml() throws IOException
	{
		String xmlTitle = "xmlTitle";
		when(molgenis.getLabel()).thenReturn(xmlTitle);
		when(molgenis.getName()).thenReturn("name");
		when(molgenisSettings.getProperty(eq(XmlMolgenisUi.KEY_APP_NAME), anyString())).thenReturn(xmlTitle);
		assertEquals(xmlMolgenisUi.getTitle(), xmlTitle);
	}

	@Test
	public void getTitleFromXmlByName() throws IOException
	{
		String xmlName = "xmlTitle";
		when(molgenis.getName()).thenReturn(xmlName);
		when(molgenisSettings.getProperty(eq(XmlMolgenisUi.KEY_APP_NAME), anyString())).thenReturn(xmlName);
		assertEquals(xmlMolgenisUi.getTitle(), xmlName);
	}
}

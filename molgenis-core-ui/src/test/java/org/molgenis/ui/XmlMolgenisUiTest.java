package org.molgenis.ui;

import org.molgenis.security.core.MolgenisPermissionService;
import org.molgenis.ui.XmlMolgenisUiTest.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

@SuppressWarnings("deprecation")
@ContextConfiguration(classes = Config.class, loader = AnnotationConfigContextLoader.class)
public class XmlMolgenisUiTest extends AbstractTestNGSpringContextTests
{
	@Autowired
	private XmlMolgenisUi xmlMolgenisUi;

	@Autowired
	private Molgenis molgenis;

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

	@Configuration
	static class Config
	{
		@Bean
		public XmlMolgenisUi xmlMolgenisUi() throws IOException
		{
			return new XmlMolgenisUi(xmlMolgenisUiLoader(), molgenisPermissionService());
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
		public MolgenisPermissionService molgenisPermissionService()
		{
			return mock(MolgenisPermissionService.class);
		}
	}
}

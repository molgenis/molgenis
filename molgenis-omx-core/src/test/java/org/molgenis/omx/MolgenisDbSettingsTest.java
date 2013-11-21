package org.molgenis.omx;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import org.molgenis.data.DataService;
import org.molgenis.data.Query;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.omx.core.RuntimeProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

@ContextConfiguration
public class MolgenisDbSettingsTest extends AbstractTestNGSpringContextTests
{
	@Configuration
	static class Config
	{
		@Bean
		public MolgenisDbSettings molgenisDbSettings()
		{

			return new MolgenisDbSettings(dataService());
		}

		@Bean
		public DataService dataService()
		{

			DataService dataservice = mock(DataService.class);
			Query q;

			q = new QueryImpl();
			q.eq(RuntimeProperty.IDENTIFIER, RuntimeProperty.class.getSimpleName() + "_property0");
			RuntimeProperty property0 = new RuntimeProperty();
			property0.setValue("value0");
			when(dataservice.findOne(RuntimeProperty.ENTITY_NAME, q)).thenReturn(property0);

			q.eq(RuntimeProperty.IDENTIFIER, RuntimeProperty.class.getSimpleName() + "_property1");
			RuntimeProperty property1 = new RuntimeProperty();
			property0.setValue("true");
			when(dataservice.findOne(RuntimeProperty.ENTITY_NAME, q)).thenReturn(property1);

			q.eq(RuntimeProperty.IDENTIFIER, RuntimeProperty.class.getSimpleName() + "_property0");
			RuntimeProperty property2 = new RuntimeProperty();
			property0.setValue("false");
			when(dataservice.findOne(RuntimeProperty.ENTITY_NAME, q)).thenReturn(property2);

			return dataservice;
		}
	}

	@Autowired
	private DataService dataService;

	@Autowired
	private MolgenisDbSettings molgenisDbSettings;

	@Test
	public void getPropertyString()
	{
		assertEquals(molgenisDbSettings.getProperty("property0"), "value0");
	}

	@Test
	public void getPropertyString_unknownProperty()
	{
		assertNull(molgenisDbSettings.getProperty("unknown-property"));
	}

	@Test
	public void getPropertyStringString()
	{
		assertEquals(molgenisDbSettings.getProperty("property0", "default-value"), "value0");
	}

	@Test
	public void getPropertyStringString_unknownProperty()
	{
		assertEquals(molgenisDbSettings.getProperty("unknown-property", "default-value"), "default-value");
	}

	@Test
	public void getPropertyBooleanTrue()
	{
		assertTrue(molgenisDbSettings.getBooleanProperty("property1"));
	}

	@Test
	public void getPropertyBooleanFalse()
	{
		assertFalse(molgenisDbSettings.getBooleanProperty("property2"));
	}

	@Test
	public void getBooleanProperty_unknownProperty()
	{
		assertNull(molgenisDbSettings.getBooleanProperty("unknown-property"));
	}

	@Test
	public void getBooleanProperty_unknownProperty_with_default()
	{
		assertTrue(molgenisDbSettings.getBooleanProperty("unknown-property", true));
	}

	@Test
	public void setProperty()
	{
		molgenisDbSettings.setProperty("property0", "value0");

		RuntimeProperty property0 = new RuntimeProperty();
		property0.setIdentifier(RuntimeProperty.class.getSimpleName() + "_property0");
		property0.setName("property0");
		property0.setValue("value0-updated");
		verify(dataService).add(RuntimeProperty.ENTITY_NAME, property0);
	}
}

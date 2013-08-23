package org.molgenis.omx;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;
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
			return new MolgenisDbSettings();
		}

		@Bean
		public Database unauthorizedDatabase() throws DatabaseException
		{
			Database database = mock(Database.class);

			RuntimeProperty property0 = new RuntimeProperty();
			property0.setValue("value0");
			when(
					database.find(RuntimeProperty.class, new QueryRule(RuntimeProperty.IDENTIFIER, Operator.EQUALS,
							RuntimeProperty.class.getSimpleName() + "_property0")))
					.thenReturn(Arrays.asList(property0));

			RuntimeProperty property1 = new RuntimeProperty();
			property1.setValue("true");
			when(
					database.find(RuntimeProperty.class, new QueryRule(RuntimeProperty.IDENTIFIER, Operator.EQUALS,
							RuntimeProperty.class.getSimpleName() + "_property1")))
					.thenReturn(Arrays.asList(property1));

			RuntimeProperty property2 = new RuntimeProperty();
			property2.setValue("false");
			when(
					database.find(RuntimeProperty.class, new QueryRule(RuntimeProperty.IDENTIFIER, Operator.EQUALS,
							RuntimeProperty.class.getSimpleName() + "_property2")))
					.thenReturn(Arrays.asList(property2));

			return database;
		}
	}

	@Autowired
	private Database database;

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
	public void setProperty() throws DatabaseException
	{
		molgenisDbSettings.setProperty("property0", "value0");

		RuntimeProperty property0 = new RuntimeProperty();
		property0.setIdentifier(RuntimeProperty.class.getSimpleName() + "_property0");
		property0.setName("property0");
		property0.setValue("value0-updated");
		verify(database).add(property0);
	}
}

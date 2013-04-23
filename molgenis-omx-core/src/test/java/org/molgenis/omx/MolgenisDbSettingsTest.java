package org.molgenis.omx;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

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

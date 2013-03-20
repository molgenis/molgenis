package org.molgenis.omx;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import java.util.Arrays;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.DatabaseFactory;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;
import org.molgenis.omx.core.RuntimeProperty;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class MolgenisDbSettingsTest
{
	private Database db;
	private MolgenisDbSettings molgenisDbSettings;

	@BeforeMethod
	public void setUp() throws Exception
	{
		db = mock(Database.class);
		RuntimeProperty property0 = new RuntimeProperty();
		property0.setValue("value0");
		when(db.find(RuntimeProperty.class, new QueryRule(RuntimeProperty.IDENTIFIER, Operator.EQUALS, "property0")))
				.thenReturn(Arrays.asList(property0));
		DatabaseFactory.create(db);

		molgenisDbSettings = new MolgenisDbSettings();
	}

	@AfterMethod
	public void tearDown()
	{
		DatabaseFactory.destroy();
	}

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
		verify(db).add(property0);
	}
}

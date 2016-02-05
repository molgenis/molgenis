package org.molgenis.data.i18n;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Stream;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.support.MapEntity;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class MolgenisResourceBundleControlTest
{
	private DataService dataServiceMock;
	private Query queryMock;
	private MolgenisResourceBundleControl molgenisResourceBundleControl;

	@BeforeMethod
	public void setUp()
	{
		dataServiceMock = mock(DataService.class);
		queryMock = mock(Query.class);
		when(queryMock.eq(any(), any())).thenReturn(queryMock);
		when(dataServiceMock.query(LanguageMetaData.ENTITY_NAME)).thenReturn(queryMock);
		molgenisResourceBundleControl = new MolgenisResourceBundleControl(dataServiceMock);
	}

	@Test
	public void newBundleWithUnknownBundleName() throws IllegalAccessException, InstantiationException, IOException
	{
		assertNull(molgenisResourceBundleControl.newBundle("bogus", new Locale("en"), "java.class",
				getClass().getClassLoader(), true));
	}

	@Test
	public void newBundleWithUnknownLanguage() throws IllegalAccessException, InstantiationException, IOException
	{
		when(queryMock.count()).thenReturn(0L);
		assertNull(molgenisResourceBundleControl.newBundle(I18nStringMetaData.ENTITY_NAME, new Locale("nl"),
				"java.class", getClass().getClassLoader(), true));
	}

	@Test
	public void newBundle() throws IllegalAccessException, InstantiationException, IOException
	{
		Entity entity = new MapEntity();
		entity.set(I18nStringMetaData.MSGID, "test");
		entity.set("en", "testen");
		entity.set("nl", "testnl");

		when(queryMock.count()).thenReturn(1L);
		when(dataServiceMock.findAll(I18nStringMetaData.ENTITY_NAME)).thenReturn(Stream.of(entity));

		ResourceBundle bundle = molgenisResourceBundleControl.newBundle(I18nStringMetaData.ENTITY_NAME,
				new Locale("nl"), "java.class", getClass().getClassLoader(), true);
		assertNotNull(bundle);

		Set<String> keys = bundle.keySet();
		assertEquals(keys.size(), 1);
		assertEquals(keys.iterator().next(), "test");
		assertEquals(bundle.getString("test"), "testnl");
	}
}

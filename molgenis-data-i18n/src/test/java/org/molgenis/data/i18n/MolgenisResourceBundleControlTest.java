package org.molgenis.data.i18n;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.i18n.model.I18nStringMetaData;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.settings.AppSettings;
import org.molgenis.data.support.DynamicEntity;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Stream;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.i18n.model.I18nStringMetaData.I18N_STRING;
import static org.molgenis.data.i18n.model.LanguageMetadata.LANGUAGE;
import static org.molgenis.data.meta.AttributeType.STRING;
import static org.testng.Assert.*;

public class MolgenisResourceBundleControlTest
{
	private DataService dataServiceMock;
	private Query<Entity> queryMock;
	private MolgenisResourceBundleControl molgenisResourceBundleControl;

	@BeforeMethod
	public void setUp()
	{
		dataServiceMock = mock(DataService.class);
		queryMock = mock(Query.class);
		when(queryMock.eq(any(), any())).thenReturn(queryMock);
		when(dataServiceMock.query(LANGUAGE)).thenReturn(queryMock);

		AppSettings settings = mock(AppSettings.class);
		when(settings.getLanguageCode()).thenReturn(null);

		molgenisResourceBundleControl = new MolgenisResourceBundleControl(dataServiceMock, settings);
	}

	@Test
	public void newBundleWithUnknownBundleName() throws IllegalAccessException, InstantiationException, IOException
	{
		assertNull(molgenisResourceBundleControl
				.newBundle("bogus", new Locale("en"), "java.class", getClass().getClassLoader(), true));
	}

	@Test
	public void newBundleWithUnknownLanguage() throws IllegalAccessException, InstantiationException, IOException
	{
		when(queryMock.count()).thenReturn(0L);
		assertNull(molgenisResourceBundleControl
				.newBundle(I18N_STRING, new Locale("nl"), "java.class", getClass().getClassLoader(), true));
	}

	@Test
	public void newBundle() throws IllegalAccessException, InstantiationException, IOException
	{
		EntityType entityType = mock(EntityType.class);
		Attribute msgAttr = when(mock(Attribute.class).getDataType()).thenReturn(STRING).getMock();
		Attribute nlAttr = when(mock(Attribute.class).getDataType()).thenReturn(STRING).getMock();
		Attribute enAttr = when(mock(Attribute.class).getDataType()).thenReturn(STRING).getMock();
		when(entityType.getAttribute(I18nStringMetaData.MSGID)).thenReturn(msgAttr);
		when(entityType.getAttribute("nl")).thenReturn(nlAttr);
		when(entityType.getAttribute("en")).thenReturn(enAttr);

		Entity entity = new DynamicEntity(entityType);
		entity.set(I18nStringMetaData.MSGID, "test");
		entity.set("en", "testen");
		entity.set("nl", "testnl");

		Entity entity1 = new DynamicEntity(entityType);
		entity1.set(I18nStringMetaData.MSGID, "testmissingnl");
		entity1.set("en", "testen");

		when(queryMock.count()).thenReturn(1L);
		when(dataServiceMock.findAll(I18N_STRING)).thenReturn(Stream.of(entity, entity1));

		ResourceBundle bundle = molgenisResourceBundleControl
				.newBundle(I18N_STRING, new Locale("nl"), "java.class", getClass().getClassLoader(), true);
		assertNotNull(bundle);

		Set<String> keys = bundle.keySet();
		assertEquals(keys.size(), 2);
		assertTrue(keys.contains("test"));
		assertTrue(keys.contains("testmissingnl"));
		assertEquals(bundle.getString("test"), "testnl");
		assertEquals(bundle.getString("testmissingnl"), "testen");// Missing nl -> return en
	}
}

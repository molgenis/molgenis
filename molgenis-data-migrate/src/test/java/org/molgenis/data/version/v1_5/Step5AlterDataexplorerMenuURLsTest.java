package org.molgenis.data.version.v1_5;

import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.testng.Assert.assertEquals;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.migrate.version.v1_5.Step5AlterDataexplorerMenuURLs;
import org.molgenis.system.core.RuntimeProperty;
import org.molgenis.system.core.RuntimePropertyRepository;
import org.molgenis.ui.menu.Menu;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Step5AlterDataexplorerMenuURLsTest
{
	Menu menu;
	Menu menuTransformed;
	@Mock
	RuntimePropertyRepository rtpRepo;
	Gson gson = new GsonBuilder().create();
	@Captor
	ArgumentCaptor<RuntimeProperty> captor;

	@BeforeMethod
	public void beforeMethod() throws IOException
	{
		initMocks(this);
		menu = readMenu("menu.json");
		menuTransformed = readMenu("menu-transformed.json");
		RuntimeProperty rtp = new RuntimeProperty();
		rtp.setName("molgenis.menu");
		rtp.setValue(gson.toJson(menu));
		when(rtpRepo.findOne(QueryImpl.EQ("Name", "molgenis.menu"))).thenReturn(rtp);
	}

	private Menu readMenu(String name) throws IOException
	{
		return gson.fromJson(IOUtils.toString(getClass().getResourceAsStream(name), "UTF-8"), Menu.class);
	}

	@Test
	public void testUpgrade()
	{
		Step5AlterDataexplorerMenuURLs step5 = new Step5AlterDataexplorerMenuURLs(rtpRepo, gson);
		step5.upgrade();
		Mockito.verify(rtpRepo).update(captor.capture());
		assertEquals(gson.fromJson(captor.getValue().getValue(), Menu.class), menuTransformed);
	}
}

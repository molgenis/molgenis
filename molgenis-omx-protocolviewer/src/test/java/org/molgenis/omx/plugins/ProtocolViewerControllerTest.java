package org.molgenis.omx.plugins;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.protocolviewer.ProtocolViewerController;
import org.molgenis.util.ShoppingCart;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.testng.annotations.Test;

public class ProtocolViewerControllerTest
{
	@Test
	public void handleRequest_download_xls() throws Exception
	{
		// mock db
		DataService dataService = mock(DataService.class);
		MolgenisSettings settings = mock(MolgenisSettings.class);
		ShoppingCart shoppingCart = mock(ShoppingCart.class);

		ObservableFeature feature1 = new ObservableFeature();
		feature1.setId(1);
		feature1.setName("feature1");
		feature1.setIdentifier("featureid1");
		feature1.setDescription("this is feature1");

		ObservableFeature feature2 = new ObservableFeature();
		feature2.setId(2);
		feature2.setName("feature2");
		feature2.setIdentifier("featureid2");
		feature2.setDescription("this is feature2");

		ObservableFeature feature3 = new ObservableFeature();
		feature3.setId(3);
		feature3.setName("feature3");
		feature3.setIdentifier("featureid3");
		feature3.setDescription("this is feature3");

		Query q = mock(Query.class);
		when(q.in(ObservableFeature.ID, Arrays.asList(1, 2, 3))).thenReturn(q);
		Iterable<Entity> entities = Arrays.<Entity> asList(feature1, feature2, feature3);
		when(dataService.findAll(ObservableFeature.ENTITY_NAME, q)).thenReturn(entities);
		ProtocolViewerController controller = new ProtocolViewerController(dataService, settings, shoppingCart);

		// mock request
		MockHttpServletRequest httpRequest = new MockHttpServletRequest();
		httpRequest.setMethod("GET");

		when(shoppingCart.getCart()).thenReturn(Arrays.asList(1, 2, 3));
		MockHttpServletResponse response = new MockHttpServletResponse();

		controller.download(response);
	}

	@Test
	public void handleRequest_download_xls_noFeatures() throws Exception
	{
		// mock db
		DataService dataService = mock(DataService.class);
		MolgenisSettings settings = mock(MolgenisSettings.class);
		ShoppingCart shoppingCart = mock(ShoppingCart.class);

		Query q = mock(Query.class);
		when(q.in(ObservableFeature.ID, Arrays.asList(1, 2, 3))).thenReturn(q);
		Iterable<Entity> it = Collections.emptyList();
		when(dataService.findAll(ObservableFeature.ENTITY_NAME, q)).thenReturn(it);
		ProtocolViewerController controller = new ProtocolViewerController(dataService, settings, shoppingCart);

		// mock request
		MockHttpServletRequest httpRequest = new MockHttpServletRequest();
		httpRequest.setMethod("GET");

		when(shoppingCart.getCart()).thenReturn(Arrays.asList(1, 2, 3));
		MockHttpServletResponse response = new MockHttpServletResponse();

		controller.download(response);
	}
}

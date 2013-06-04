package org.molgenis.omx.plugins;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;
import org.molgenis.framework.server.MolgenisRequest;
import org.molgenis.framework.ui.ScreenController;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.omx.observ.ObservableFeature;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.testng.annotations.Test;

public class ProtocolViewerControllerTest
{
	@Test
	public void handleRequest_download_xls_protocol() throws Exception
	{
		// mock db
		Database db = mock(Database.class);

		DataSet dataSet = new DataSet();
		dataSet.setId(1);
		dataSet.setName("dataset");
		dataSet.setProtocolUsed_Identifier("protocol1");

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

		when(db.find(DataSet.class, new QueryRule(DataSet.ID, Operator.EQUALS, 1))).thenReturn(
				Collections.singletonList(dataSet));
		when(db.find(ObservableFeature.class, new QueryRule(ObservableFeature.ID, Operator.IN, Arrays.asList(1, 2, 3))))
				.thenReturn(Arrays.asList(feature1, feature2, feature3));

		ProtocolViewerController controller = new ProtocolViewerController("test", mock(ScreenController.class));

		// mock request
		MockHttpServletRequest httpRequest = new MockHttpServletRequest();
		httpRequest.setMethod("GET");
		MolgenisRequest request = mock(MolgenisRequest.class);
		when(request.getRequest()).thenReturn(httpRequest);
		when(request.getInt("datasetid")).thenReturn(1);
		when(request.getString("features")).thenReturn("1,2,3");
		when(request.getAction()).thenReturn("download_xls");
		MockHttpServletResponse httpResponse = new MockHttpServletResponse();
		when(request.getResponse()).thenReturn(httpResponse);

		controller.handleRequest(db, request);
		// assertEquals(DigestUtils.md5Hex(httpResponse.getContentAsByteArray()),
		// "c9dea8a729c83d5428137bddc77f5540");
	}

	@Test
	public void handleRequest_download_xls_noProtocol() throws Exception
	{
		// mock db
		Database db = mock(Database.class);

		DataSet dataSet = new DataSet();
		dataSet.setId(1);
		dataSet.setName("dataset");

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

		when(db.find(DataSet.class, new QueryRule(DataSet.ID, Operator.EQUALS, 1))).thenReturn(
				Collections.singletonList(dataSet));
		when(db.find(ObservableFeature.class, new QueryRule(ObservableFeature.ID, Operator.IN, Arrays.asList(1, 2, 3))))
				.thenReturn(Arrays.asList(feature1, feature2, feature3));

		ProtocolViewerController controller = new ProtocolViewerController("test", mock(ScreenController.class));

		// mock request
		MockHttpServletRequest httpRequest = new MockHttpServletRequest();
		httpRequest.setMethod("GET");
		MolgenisRequest request = mock(MolgenisRequest.class);
		when(request.getRequest()).thenReturn(httpRequest);
		when(request.getInt("datasetid")).thenReturn(1);
		when(request.getString("features")).thenReturn("1,2,3");
		when(request.getAction()).thenReturn("download_xls");
		MockHttpServletResponse httpResponse = new MockHttpServletResponse();
		when(request.getResponse()).thenReturn(httpResponse);

		controller.handleRequest(db, request);
		// assertEquals(DigestUtils.md5Hex(httpResponse.getContentAsByteArray()),
		// "ed7513d8dc5ef44e36c51b6595f44dbf");
	}

	@Test
	public void handleRequest_download_xls_noFeatures() throws Exception
	{
		// mock db
		Database db = mock(Database.class);

		DataSet dataSet = new DataSet();
		dataSet.setId(1);
		dataSet.setName("dataset");

		when(db.find(DataSet.class, new QueryRule(DataSet.ID, Operator.EQUALS, 1))).thenReturn(
				Collections.singletonList(dataSet));

		ProtocolViewerController controller = new ProtocolViewerController("test", mock(ScreenController.class));

		// mock request
		MockHttpServletRequest httpRequest = new MockHttpServletRequest();
		httpRequest.setMethod("GET");
		MolgenisRequest request = mock(MolgenisRequest.class);
		when(request.getRequest()).thenReturn(httpRequest);
		when(request.getInt("datasetid")).thenReturn(1);
		when(request.getString("features")).thenReturn("1,2,3");
		when(request.getAction()).thenReturn("download_xls");
		MockHttpServletResponse httpResponse = new MockHttpServletResponse();
		when(request.getResponse()).thenReturn(httpResponse);

		controller.handleRequest(db, request);
		// assertEquals(DigestUtils.md5Hex(httpResponse.getContentAsByteArray()),
		// "b5eeb35c257e5dced33bb2367c8924bd");
	}
}

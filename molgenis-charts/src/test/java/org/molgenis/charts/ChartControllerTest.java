package org.molgenis.charts;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.mockito.Matchers;
import org.molgenis.charts.charttypes.HeatMapChart;
import org.molgenis.charts.data.DataMatrix;
import org.molgenis.charts.data.Target;
import org.molgenis.charts.r.RChartService;
import org.molgenis.charts.requests.HeatMapRequest;
import org.molgenis.util.FileStore;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import freemarker.template.TemplateException;

public class ChartControllerTest
{
	private ChartDataService chartDataServiceMock;
	private RChartService rchartServiceMock;
	private FileStore fileStoreMock;
	private ChartController chartController;

	@BeforeMethod
	public void beforeMethod()
	{
		chartDataServiceMock = mock(ChartDataService.class);
		rchartServiceMock = mock(RChartService.class);
		fileStoreMock = mock(FileStore.class);
		chartController = new ChartController(chartDataServiceMock, rchartServiceMock, fileStoreMock);
	}

	@Test
	public void renderHeatMap() throws IOException, TemplateException
	{
		@SuppressWarnings("unchecked")
		DataMatrix matrix = new DataMatrix(Arrays.asList(new Target("col")), Arrays.asList(new Target("row")),
				Arrays.asList(Arrays.<Number> asList(1.2)));
		when(chartDataServiceMock.getDataMatrix("entity", Arrays.asList("col"), "row", null)).thenReturn(matrix);
		when(rchartServiceMock.renderHeatMap(Matchers.any(HeatMapChart.class))).thenReturn("fileName");

		Model model = new ExtendedModelMap();
		HeatMapRequest request = new HeatMapRequest();
		request.setEntity("entity");
		request.setHeight(100);
		request.setTitle("title");
		request.setWidth(200);
		request.setX(Arrays.asList("col"));
		request.setY("row");

		List<String> fileNames = new ArrayList<String>();

		String view = chartController.renderHeatMap(request, fileNames, model);
		assertNotNull(view);
		assertEquals(fileNames, Arrays.asList("fileName"));
		assertEquals("fileName", model.asMap().get("fileName"));
		assertEquals(1, model.asMap().get("nRow"));
		assertEquals(1, model.asMap().get("nCol"));
	}
}

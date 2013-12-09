package org.molgenis.charts;

import static org.molgenis.charts.ChartController.URI;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.log4j.Logger;
import org.molgenis.charts.AbstractChart.AbstractChartType;
import org.molgenis.charts.charttypes.HeatMapChart;
import org.molgenis.charts.charttypes.LineChart;
import org.molgenis.charts.data.DataMatrix;
import org.molgenis.charts.data.XYDataSerie;
import org.molgenis.charts.highcharts.HighchartService;
import org.molgenis.charts.highcharts.Options;
import org.molgenis.charts.highcharts.dataexplorer.requestpayload.LineChartRequestPayLoad;
import org.molgenis.charts.requests.HeatMapRequest;
import org.molgenis.data.QueryRule;
import org.molgenis.util.FileStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import freemarker.template.TemplateException;

@Controller
@RequestMapping(URI)
public class ChartController
{
	public static final String URI = "/charts";
	private static final Logger logger = Logger.getLogger(ChartController.class);

	private final ChartDataService chartDataService;
	private final ChartVisualizationServiceFactory chartVisualizationServiceFactory;
	private final FileStore fileStore;

	@Autowired
	public ChartController(ChartDataService chartDataService,
			ChartVisualizationServiceFactory chartVisualizationServiceFactory, FileStore fileStore)
	{
		if (chartDataService == null) throw new IllegalArgumentException("chartDataService is null");
		if (chartVisualizationServiceFactory == null) throw new IllegalArgumentException(
				"chartVisualizationServiceFactory is null");
		if (fileStore == null) throw new IllegalArgumentException("fileStore is null");

		this.chartDataService = chartDataService;
		this.chartVisualizationServiceFactory = chartVisualizationServiceFactory;
		this.fileStore = fileStore;
	}

	@RequestMapping("/test")
	public String test(HttpServletRequest request, Model model)
	{
		model.addAttribute("queryString", request.getQueryString());
		return "test";
	}

	@RequestMapping(value = "/line", method = RequestMethod.POST, produces = "application/json")
	@ResponseBody
	public Options renderLineChart(@Valid LineChartRequestPayLoad request, Model model)
	{
		List<QueryRule> queryRules = null; // TODO JJ
		
		List<XYDataSerie> series = new ArrayList<XYDataSerie>();

		XYDataSerie xYDataSerie = chartDataService.getXYDataSerie("heatmap", "probe4",
				"probe2", queryRules);
		series.add(xYDataSerie);

		LineChart lineChart = new LineChart(series);
		
		HighchartService highchartService = new HighchartService();

		return highchartService.createLine(request, lineChart, model);
	}

	/**
	 * Gets a file from the filestore.
	 * 
	 * User can only view his own files he created with the charts module
	 * 
	 * 
	 * @param out
	 * @param name
	 * @param extension
	 * @param fileNames
	 * @param response
	 * @throws IOException
	 */
	@RequestMapping("/get/{name}.{extension}")
	public void getFile(OutputStream out, @PathVariable("name")
	String name, @PathVariable("extension")
	String extension, HttpServletResponse response) throws IOException
	{
		File f = fileStore.getFile(name + "." + extension);
		if (!f.exists())
		{
			logger.warn("Chart file not found [" + name + "]");
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		response.setContentType(MimeTypes.getContentType(extension));

		FileCopyUtils.copy(new FileInputStream(f), out);
	}

	/**
	 * Renders a heatmap with r
	 * 
	 * Returns a piece of javascript that can be retrieved by an html page with a ajax request.
	 * 
	 * The page must have an element with id named 'container'. The svg image will be added to this container element.
	 * 
	 * @param request
	 * @param model
	 * @return
	 * @throws IOException
	 * @throws TemplateException
	 */
	@RequestMapping("/heatmap")
	public String renderHeatMap(@Valid
	HeatMapRequest request, Model model) throws IOException, TemplateException
	{
		DataMatrix matrix = chartDataService.getDataMatrix(request.getEntity(), request.getX(), request.getY(),
				request.getQueryRules());

		HeatMapChart chart = new HeatMapChart(matrix);
		chart.setTitle(request.getTitle());
		chart.setWidth(request.getWidth());
		chart.setHeight(request.getHeight());

		ChartVisualizationService service = chartVisualizationServiceFactory
				.getVisualizationService(AbstractChartType.HEAT_MAP);

		return (String) service.renderChart(chart, model);
	}
}

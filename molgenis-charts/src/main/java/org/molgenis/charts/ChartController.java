package org.molgenis.charts;

import static org.molgenis.charts.ChartController.URI;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import org.apache.log4j.Logger;
import org.molgenis.charts.AbstractChart.MolgenisChartType;
import org.molgenis.charts.charttypes.HeatMapChart;
import org.molgenis.charts.data.DataMatrix;
import org.molgenis.charts.data.XYDataSerie;
import org.molgenis.charts.highcharts.Options;
import org.molgenis.charts.requests.BoxPlotChartRequest;
import org.molgenis.charts.requests.HeatMapRequest;
import org.molgenis.charts.requests.XYDataChartRequest;
import org.molgenis.util.FileStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
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

	@RequestMapping(value = "/xydatachart", method = RequestMethod.POST, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public Options renderXYDataChart(@Valid @RequestBody XYDataChartRequest request, Model model)
	{		
		XYDataChart xYDataChart = chartDataService.getXYDataChart(
				request.getEntity(),
				request.getX(),
				request.getY(),
				request.getSplit(),
				request.getQuery().getRules());
		
		xYDataChart.setTitle(request.getTitle());
		xYDataChart.setHeight(request.getHeight());
		xYDataChart.setWidth(request.getWidth());
		xYDataChart.setType(MolgenisChartType.valueOf(request.getType()));
		xYDataChart.setxAxisLabel(request.getxAxisLabel());
		xYDataChart.setyAxisLabel(request.getyAxisLabel());
		
		ChartVisualizationService service = chartVisualizationServiceFactory.getVisualizationService(MolgenisChartType.valueOf(request.getType()));
		
		return (Options) service.renderChart(xYDataChart, model);
	}
	
	@RequestMapping(value = "/boxplot", method = RequestMethod.POST, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public Options renderPlotBoxChart(@Valid @RequestBody BoxPlotChartRequest request, Model model)
	{
		BoxPlotChart chart = chartDataService.getBoxPlotChart(
				request.getEntity(), 
				request.getObservableFeature(), 
				request.getQuery().getRules(), 
				request.getSplit(),
				request.getMultiplyIQR());
		
		chart.setHeight(request.getHeight());
		chart.setWidth(request.getWidth());
		chart.setTitle(request.getTitle());
		
		ChartVisualizationService service = chartVisualizationServiceFactory.getVisualizationService(MolgenisChartType.BOXPLOT_CHART);
		return (Options) service.renderChart(chart, model);
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
	String extension,  HttpServletResponse response) throws IOException
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
	 * Returns a piece of javascript that can be retrieved by an html page with an ajax request.
	 * 
	 * The page must have an element with id named 'container'. The svg image will be added to this container element.
	 * 
	 * @param request
	 * @param model
	 * @return
	 * @throws IOException
	 * @throws TemplateException
	 * @throws FactoryConfigurationError 
	 * @throws XMLStreamException 
	 */
	@RequestMapping(value = "/heatmap", method = RequestMethod.POST, consumes = APPLICATION_JSON_VALUE)
	@ResponseBody
	public String renderHeatMap(@Valid @RequestBody HeatMapRequest request, Model model) throws IOException, TemplateException, XMLStreamException, FactoryConfigurationError
	{
//		DataMatrix matrix = chartDataService.getDataMatrix(request.getEntity(), request.getX(), request.getY(),
//				request.getQueryRules());
//
//		HeatMapChart chart = new HeatMapChart(matrix);
//		chart.setTitle(request.getTitle());
//		chart.setWidth(request.getWidth());
//		chart.setHeight(request.getHeight());
//		chart.setxLabel(request.getxLabel());
//		chart.setyLabel(request.getyLabel());
//		chart.setScale(request.getScale());
//		
//		ChartVisualizationService service = chartVisualizationServiceFactory.getVisualizationService(MolgenisChartType.HEAT_MAP);
//
//		return (String) service.renderChart(chart, model);
		return "{test: 'test'}";
	}
}

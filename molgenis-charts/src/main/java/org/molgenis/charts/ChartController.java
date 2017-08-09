package org.molgenis.charts;

import freemarker.template.TemplateException;
import org.molgenis.charts.AbstractChart.MolgenisChartType;
import org.molgenis.charts.charttypes.HeatMapChart;
import org.molgenis.charts.data.DataMatrix;
import org.molgenis.charts.highcharts.basic.Options;
import org.molgenis.charts.requests.BoxPlotChartRequest;
import org.molgenis.charts.requests.HeatMapRequest;
import org.molgenis.charts.requests.XYDataChartRequest;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.file.FileStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Map;

import static org.molgenis.charts.ChartController.URI;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Controller
@RequestMapping(URI)
public class ChartController
{
	private static final Logger LOG = LoggerFactory.getLogger(ChartController.class);

	public static final String URI = "/charts";

	private final ChartDataService chartDataService;
	private final ChartVisualizationServiceFactory chartVisualizationServiceFactory;
	private final FileStore fileStore;

	@Autowired
	public ChartController(ChartDataService chartDataService,
			ChartVisualizationServiceFactory chartVisualizationServiceFactory, FileStore fileStore)
	{
		if (chartDataService == null) throw new IllegalArgumentException("chartDataService is null");
		if (chartVisualizationServiceFactory == null)
			throw new IllegalArgumentException("chartVisualizationServiceFactory is null");
		if (fileStore == null) throw new IllegalArgumentException("fileStore is null");

		this.chartDataService = chartDataService;
		this.chartVisualizationServiceFactory = chartVisualizationServiceFactory;
		this.fileStore = fileStore;
	}

	@RequestMapping(value = "/xydatachart", method = RequestMethod.POST, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public Options renderXYDataChart(@Valid @RequestBody XYDataChartRequest request, Model model)
	{
		Query<Entity> query = request.getQuery();
		XYDataChart xYDataChart = chartDataService.getXYDataChart(request.getEntity(), request.getX(), request.getY(),
				request.getSplit(), query != null ? query.getRules() : Collections.emptyList());

		xYDataChart.setTitle(request.getTitle());
		xYDataChart.setHeight(request.getHeight());
		xYDataChart.setWidth(request.getWidth());
		xYDataChart.setType(MolgenisChartType.valueOf(request.getType()));
		xYDataChart.setxAxisLabel(request.getxAxisLabel());
		xYDataChart.setyAxisLabel(request.getyAxisLabel());

		ChartVisualizationService service = chartVisualizationServiceFactory.getVisualizationService(
				MolgenisChartType.valueOf(request.getType()));

		return (Options) service.renderChart(xYDataChart, model);
	}

	@RequestMapping(value = "/boxplot", method = RequestMethod.POST, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public Options renderPlotBoxChart(@Valid @RequestBody BoxPlotChartRequest request, Model model)
	{
		Query<Entity> query = request.getQuery();
		BoxPlotChart chart = chartDataService.getBoxPlotChart(request.getEntity(), request.getObservableFeature(),
				query != null ? query.getRules() : Collections.emptyList(), request.getSplit(),
				request.getScale());

		chart.setHeight(request.getHeight());
		chart.setWidth(request.getWidth());
		chart.setTitle(request.getTitle());

		ChartVisualizationService service = chartVisualizationServiceFactory.getVisualizationService(
				MolgenisChartType.BOXPLOT_CHART);
		return (Options) service.renderChart(chart, model);
	}

	/**
	 * Gets a file from the filestore.
	 * <p>
	 * User can only view his own files he created with the charts module
	 *
	 * @param out
	 * @param name
	 * @param extension
	 * @param response
	 * @throws IOException
	 */
	@RequestMapping("/get/{name}.{extension}")
	public void getFile(OutputStream out, @PathVariable("name") String name,
			@PathVariable("extension") String extension, HttpServletResponse response) throws IOException
	{
		File f = fileStore.getFile(name + "." + extension);
		if (!f.exists())
		{
			LOG.warn("Chart file not found [" + name + "]");
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		response.setContentType(MimeTypes.getContentType(extension));

		FileCopyUtils.copy(new FileInputStream(f), out);
	}

	/**
	 * Renders a heatmap with r
	 * <p>
	 * Returns a piece of javascript that can be retrieved by an html page with an ajax request.
	 * <p>
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
	public String renderHeatMap(@Valid @RequestBody HeatMapRequest request, Model model)
			throws IOException, TemplateException, XMLStreamException, FactoryConfigurationError
	{
		DataMatrix matrix = chartDataService.getDataMatrix(request.getEntity(), request.getX(), request.getY(),
				request.getQueryRules());

		HeatMapChart chart = new HeatMapChart(matrix);
		chart.setTitle(request.getTitle());
		chart.setWidth(request.getWidth());
		chart.setHeight(request.getHeight());
		chart.setxLabel(request.getxLabel());
		chart.setyLabel(request.getyLabel());
		chart.setScale(request.getScale());

		ChartVisualizationService service = chartVisualizationServiceFactory.getVisualizationService(
				MolgenisChartType.HEAT_MAP);

		return (String) service.renderChart(chart, model);
	}

	@ExceptionHandler(RuntimeException.class)
	@ResponseBody
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public Map<String, String> handleRuntimeException(RuntimeException e)
	{
		LOG.error(null, e);
		return Collections.singletonMap("errorMessage",
				"An error occurred. Please contact the administrator.<br />Message:" + e.getMessage());
	}
}

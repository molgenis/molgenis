package org.molgenis.charts.r;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import org.apache.log4j.Logger;
import org.molgenis.charts.AbstractChart;
import org.molgenis.charts.AbstractChart.MolgenisChartType;
import org.molgenis.charts.AbstractChartVisualizationService;
import org.molgenis.charts.MolgenisChartException;
import org.molgenis.charts.charttypes.HeatMapChart;
import org.molgenis.charts.svg.SVGEditor;
import org.molgenis.r.RScriptRunner;
import org.molgenis.r.StringROutputHandler;
import org.molgenis.util.FileStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

import freemarker.template.TemplateException;

@Component
public class RChartService extends AbstractChartVisualizationService
{
	private static final Logger logger = Logger.getLogger(RChartService.class);
	private final FileStore fileStore;
	private RScriptRunner rScriptRunner;

	@Autowired
	public RChartService(FileStore fileStore, RScriptRunner rScriptRunner)
	{
		super(Arrays.asList(MolgenisChartType.HEAT_MAP));

		if (fileStore == null) throw new IllegalArgumentException("fileStore is null");
		if (rScriptRunner == null) throw new IllegalArgumentException("rScriptRunner is null");

		this.fileStore = fileStore;
	}

	@Override
	protected Object renderChartInternal(AbstractChart chart, Model model)
	{
		// For now r is only used for HeatMaps
		HeatMapChart heatMapChart = (HeatMapChart) chart;

		String chartFileName;
		try
		{
			chartFileName = renderHeatMap(heatMapChart);
		}
		catch (Exception e)
		{
			throw new MolgenisChartException(e);
		}

		model.addAttribute("fileName", chartFileName);
		model.addAttribute("nRow", heatMapChart.getData().getRowTargets().size());
		model.addAttribute("nCol", heatMapChart.getData().getColumnTargets().size());

		return "heatmap";
	}

	private String renderHeatMap(HeatMapChart chart) throws IOException, TemplateException, XMLStreamException,
			FactoryConfigurationError
	{
		String fileName = UUID.randomUUID().toString();

		// Build the data-model
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("chart", chart);
		data.put("nRow", chart.getData().getRowTargets().size());
		data.put("nCol", chart.getData().getColumnTargets().size());
		data.put("wd", fileStore.getStorageDir());
		data.put("fileName", fileName);

		StringROutputHandler outputHandler = new StringROutputHandler();
		rScriptRunner.runRScript(fileName + ".r", "R_heatmap.ftl", data, outputHandler);

		logger.info("R output:" + outputHandler.toString());

		// annotate the SVG here
		File in = fileStore.getFile(fileName + ".svg");
		File out = new File(fileStore.getStorageDir() + "/" + fileName + "_annotated.svg");

		SVGEditor svge = new SVGEditor(in, out);
		svge.annotateHeatMap(chart);

		return fileName;
	}
}

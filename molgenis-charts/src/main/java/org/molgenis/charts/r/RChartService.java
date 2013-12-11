package org.molgenis.charts.r;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.molgenis.charts.AbstractChart;
import org.molgenis.charts.AbstractChart.ChartType;
import org.molgenis.charts.AbstractChartVisualizationService;
import org.molgenis.charts.MolgenisChartException;
import org.molgenis.charts.charttypes.HeatMapChart;
import org.molgenis.r.ROutputHandler;
import org.molgenis.r.RScriptExecutor;
import org.molgenis.util.FileStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import freemarker.template.Template;
import freemarker.template.TemplateException;

@Component
public class RChartService extends AbstractChartVisualizationService
{
	private static final Logger logger = Logger.getLogger(RChartService.class);
	private final FileStore fileStore;
	private final FreeMarkerConfigurer freeMarkerConfig;
	private final RScriptExecutor rScriptExecutor;

	@Autowired
	public RChartService(FileStore fileStore, FreeMarkerConfigurer freeMarkerConfig, RScriptExecutor rScriptExecutor)
	{
		super(Arrays.asList(ChartType.HEAT_MAP));

		if (fileStore == null) throw new IllegalArgumentException("fileStore is null");
		if (freeMarkerConfig == null) throw new IllegalArgumentException("FreeMarkerConfig is null");
		if (rScriptExecutor == null) throw new IllegalArgumentException("rScriptExecutor is null");

		this.fileStore = fileStore;
		this.freeMarkerConfig = freeMarkerConfig;
		this.rScriptExecutor = rScriptExecutor;
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
		catch (IOException e)
		{
			throw new MolgenisChartException(e);
		}
		catch (TemplateException e)
		{
			throw new MolgenisChartException(e);
		}

		model.addAttribute("fileName", chartFileName);
		model.addAttribute("nRow", heatMapChart.getData().getRowTargets().size());
		model.addAttribute("nCol", heatMapChart.getData().getColumnTargets().size());

		return "heatmap";
	}

	private String renderHeatMap(HeatMapChart chart) throws IOException, TemplateException
	{
		String fileName = UUID.randomUUID().toString();

		// Build the data-model
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("chart", chart);
		data.put("nRow", chart.getData().getRowTargets().size());
		data.put("nCol", chart.getData().getColumnTargets().size());
		data.put("wd", fileStore.getStorageDir());
		data.put("fileName", fileName);

		File script = generateScript("R_heatmap.ftl", data, fileName + ".r");
		runScript(script);

		return fileName;
	}

	private void runScript(File script)
	{
		final StringBuilder sb = new StringBuilder();

		rScriptExecutor.executeScript(script, new ROutputHandler()
		{
			@Override
			public void outputReceived(String output)
			{
				sb.append(output);
			}
		});

		logger.info("R output:" + sb.toString());
	}

	private File generateScript(String templateName, Map<String, Object> parameters, String scriptName)
			throws IOException, TemplateException
	{
		File rScriptFile = fileStore.getFile(scriptName);

		Template template = freeMarkerConfig.getConfiguration().getTemplate(templateName);
		Writer w = new FileWriter(rScriptFile);
		try
		{
			template.process(parameters, w);
		}
		finally
		{
			IOUtils.closeQuietly(w);
		}

		return rScriptFile;
	}

}

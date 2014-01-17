package org.molgenis.charts;

import java.util.ArrayList;
import java.util.List;

import org.molgenis.charts.AbstractChart.MolgenisChartType;
import org.molgenis.charts.highcharts.basic.HighchartService;
import org.molgenis.charts.r.RChartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Here all the visualizationservices are registered.
 * 
 * You can ask a visualization service for a particular charttype
 */
@Component
public class ChartVisualizationServiceFactory
{
	private final List<ChartVisualizationService> chartVisualiationServices;

	@Autowired
	public ChartVisualizationServiceFactory(RChartService rChartService, HighchartService highchartsService)
	{
		chartVisualiationServices = new ArrayList<ChartVisualizationService>();
		chartVisualiationServices.add(rChartService);
		chartVisualiationServices.add(highchartsService);
	}

	/**
	 * Gets a visualizationservice for a charttype
	 * 
	 * Throws MolgenisChartException if no service can be found for this charttype
	 * 
	 * @param chartType
	 * @return
	 */
	public ChartVisualizationService getVisualizationService(MolgenisChartType chartType)
	{
		for (ChartVisualizationService service : chartVisualiationServices)
		{
			if (service.getCapabilities().contains(chartType))
			{
				return service;
			}
		}

		throw new MolgenisChartException("No service found for charttype [" + chartType + "]");
	}
}

package org.molgenis.compute.ui.analysis.event;

public interface AnalysisHandlerRegistratorService
{
	void registerAnalysisHandlers();

	void registerAnalysisHandler(String entityName, String attributeName);
}

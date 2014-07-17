package org.molgenis.reportbuilder.controller;

import static org.molgenis.reportbuilder.controller.ReportBuilderController.URI;

import java.util.Collections;
import java.util.Map;

import org.apache.log4j.Logger;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Controller for the reportbuilder
 * 
 * @author mdehaan
 * 
 */
@Controller
@RequestMapping(URI)
public class ReportBuilderController extends MolgenisPluginController
{

	private static final Logger logger = Logger.getLogger(ReportBuilderController.class);

	public static final String ID = "reportbuilder";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;

	private final ReportBuilderController reportBuilderController;

	@Autowired
	public ReportBuilderController(ReportBuilderController reportBuilderController)
	{
		super(URI);
		if (reportBuilderController == null) throw new IllegalArgumentException("reportBuilderController is null");
		this.reportBuilderController = reportBuilderController;
	}
	
	private void generateFreemarkerContent(Class EntityClass, String EntityId, Map<?, ?> ParameterMap){
		// TODO This guy makes HTML to insert into a freemarker template based on an entity
	}
	
	@ExceptionHandler(RuntimeException.class)
	@ResponseBody
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public Map<String, String> handleRuntimeException(RuntimeException e)
	{
		logger.error(null, e);
		return Collections.singletonMap("errorMessage",
				"An error occured. Please contact the administrator.<br />Message:" + e.getMessage());
	}
}

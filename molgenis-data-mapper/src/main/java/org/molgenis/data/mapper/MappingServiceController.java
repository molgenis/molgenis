package org.molgenis.data.mapper;

import static org.molgenis.data.mapper.MappingServiceController.URI;

import org.molgenis.framework.ui.MolgenisPluginController;
import org.molgenis.util.ErrorMessageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
@RequestMapping(URI)
public class MappingServiceController extends MolgenisPluginController
{
	private static final Logger LOG = LoggerFactory.getLogger(MappingServiceController.class);
	
	public static final String ID = "mappingservice";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;
	private static final String VIEW_NAME = "view-mapper";
	
	@Autowired
	public MappingServiceController(){
		super(URI);
	}
	
	@RequestMapping
	public String init(Model model){
		return VIEW_NAME;
	}
	
	@ExceptionHandler(RuntimeException.class)
	@ResponseBody
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ErrorMessageResponse handleRuntimeException(RuntimeException e)
	{
		LOG.error(e.getMessage(), e);
		return new ErrorMessageResponse(new ErrorMessageResponse.ErrorMessage(
				"An error occurred. Please contact the administrator.<br />Message:" + e.getMessage()));
	}
}

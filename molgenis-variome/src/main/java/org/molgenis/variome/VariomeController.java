/**
 * 
 */
package org.molgenis.variome;

import static org.molgenis.variome.VariomeController.URI;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import javax.servlet.http.Part;

import org.apache.log4j.Logger;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.molgenis.util.FileUploadUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Controller wrapper for the VariomeService.
 * 
 * @author Mark-de-Haan
 * 
 */
@Controller
@RequestMapping(URI)
public class VariomeController extends MolgenisPluginController{
	
	private static final Logger logger = Logger.getLogger(VariomeController.class);

	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + "variome";

	private final VariomeService pluginVariomeService;
	
	@Autowired
	public VariomeController(VariomeService pluginVariomeService)
	{
		super(URI);
		
		if (pluginVariomeService == null) throw new IllegalArgumentException(
				"PluginVariomeService is null");
		this.pluginVariomeService = pluginVariomeService;
	}
	
	@RequestMapping(method = RequestMethod.GET)
	public String init(Model model){
		return "view-variome";
	}
	
	@RequestMapping(value = "/upload-vcf", method = RequestMethod.POST)
	public String handleVcfInput(@RequestParam("file") Part part, Model model) throws IOException {
 
		if (!part.equals(null) && part.getSize() > 5000000){ // 5mb limit
			throw new RuntimeException("File too large");
		}
 
		File file = FileUploadUtils.saveToTempFolder(part);
		if(file == null){
			new ObjectError("variome", "No file selected");
		}else{ 
			pluginVariomeService.vcfFile(file, model);
		}
 
		return "view-variome";
	}
	
	@RequestMapping(value = "/upload-pasted-vcf", method = RequestMethod.POST)
	public String handleManualInput() {
		
		return "view-variome";
	}
	
	@RequestMapping(value = "/upload-zip", method = RequestMethod.POST)
	public String handleZipInput() {
		
		return "view-variome";
	}
	
	@RequestMapping(value = "/upload-pasted-gene-list", method = RequestMethod.POST)
	public String handlePastedGeneList() {
		
		return "view-variome";
	}
	
	@RequestMapping(value = "/execute-variant-app", method = RequestMethod.POST)
	public String filterMyVariants() {
		
		return "view-result-page";
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

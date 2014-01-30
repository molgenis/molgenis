/**
 * 
 */
package org.molgenis.variome;

import static org.molgenis.variome.VariomeController.URI;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Part;

import org.apache.log4j.Logger;
import org.molgenis.data.DataService;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryAnnotator;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.molgenis.toolAnnotators.CaddAnnotator;
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
import org.molgenis.toolAnnotators.*;

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
	public List<File> listOfFiles = new ArrayList<File>();
	
	@Autowired
	DataService dataService;
	
	@Autowired
	RepositoryAnnotator caddAnnotator;
	
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
	
	@RequestMapping(value = "/upload-vcf", headers = "content-type=multipart/*",  method = RequestMethod.POST)
	public String handleVcfInput(@RequestParam("vcf-file-input-field") Part part, Model model) throws IOException {
 
		if (!part.equals(null) && part.getSize() > 5000000){ // 5mb limit
			throw new RuntimeException("File too large");
		}
 
		File file = FileUploadUtils.saveToTempFolder(part);
		listOfFiles.add(file);
		
		// CURRENT FUNCTION: CALLING DATASET ANNOTATER TO TEST
		// TODO: CHANGE THIS INTO A FULLY FUNCTIONAL SINGLE FILE UPLOAD CONTROLLER
		
		if(file == null){
			new ObjectError("variome", "No file selected");
		}else{ 
			pluginVariomeService.dataSetAnnotater();
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
	public String handleGeneInput() {
		
		return "view-variome";
	}
	
	@RequestMapping(value = "/upload-region-filter", method = RequestMethod.POST)
	public String handleRegionInput() {
		
		return "view-variome";
	}
	
	@RequestMapping(value = "/upload-bed", method = RequestMethod.POST)
	public String handleBedInput() {
		
		return "view-variome";
	}
	
	@RequestMapping(value = "/upload-phenotype-filter", method = RequestMethod.POST)
	public String handlePhenotypeInput() {
		
		return "view-variome";
	}
	
	@RequestMapping(value = "/execute-variant-app", method = RequestMethod.POST)
	public String filterMyVariants() {
		//TODO: Collect all selected databases / uploaded vcf files
		
		//TODO: Make sub selections based on region and phenotype selections
		
		//TODO: Make it into OMX format, load it into database
		
		//TODO: Check which tools are selected, run annotators based on selection via scheduler
		// Each tool will add to the feature and value lists 
		
		caddAnnotator.annotate(dataService.getRepositoryByEntityName("CAR_Batch123"));
		
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

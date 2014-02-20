package org.molgenis.variome;

import static org.molgenis.variome.VariomeController.URI;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.Part;

import org.apache.log4j.Logger;
import org.molgenis.data.DataService;
import org.molgenis.data.RepositoryAnnotator;
import org.molgenis.data.omx.annotation.OmxDataSetAnnotator;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.omx.search.DataSetsIndexer;
import org.molgenis.search.SearchService;
import org.molgenis.util.FileStore;
import org.molgenis.util.FileUploadUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
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

import com.google.common.collect.Lists;

/**
 * Controller wrapper for the VariomeService.
 * 
 * @author mdehaan
 * 
 */
@Controller
@RequestMapping(URI)
public class VariomeController extends MolgenisPluginController
{

	private static final Logger logger = Logger.getLogger(VariomeController.class);

	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + "variome";

	private final VariomeService pluginVariomeService;

	@Autowired
	DataService dataService;

	@Autowired
	FileStore fileStore;

	@Autowired
	SearchService searchService;

	@Autowired
	DataSetsIndexer indexer;

	@Resource(name = "ebiService")
	RepositoryAnnotator ebiServiceAnnotator;

	@Resource(name = "caddService")
	RepositoryAnnotator caddServiceAnnotator;

	@Resource(name = "dbnsfpVariantService")
	RepositoryAnnotator dbnsfpVariantServiceAnnotator;

	@Resource(name = "dbnsfpGeneService")
	RepositoryAnnotator dbnsfpGeneServiceAnnotator;

	@Resource(name = "omimHpoService")
	RepositoryAnnotator omimHpoAnnotator;

	@Autowired
	public VariomeController(VariomeService pluginVariomeService)
	{
		super(URI);

		if (pluginVariomeService == null) throw new IllegalArgumentException("PluginVariomeService is null");
		this.pluginVariomeService = pluginVariomeService;
	}

	@RequestMapping(method = RequestMethod.GET)
	public String init(String selectedDataSetIdentifier, Model model)
	{

		List<DataSet> dataSets = Lists.newArrayList(dataService.findAll(DataSet.ENTITY_NAME,
				new QueryImpl().sort(new Sort(Direction.DESC, DataSet.STARTTIME)), DataSet.class));

		model.addAttribute("dataSets", dataSets);

		if (dataSets != null && !dataSets.isEmpty())
		{
			// determine selected data set and add to model
			DataSet selectedDataSet = null;
			if (selectedDataSetIdentifier != null)
			{
				for (DataSet dataSet : dataSets)
				{
					if (dataSet.getIdentifier().equals(selectedDataSetIdentifier))
					{
						selectedDataSet = dataSet;
						break;
					}
				}

				if (selectedDataSet == null) throw new IllegalArgumentException(selectedDataSetIdentifier
						+ " is not a valid data set identifier");
			}
			else
			{
				// select first data set by default
				selectedDataSet = dataSets.iterator().next();
			}
			model.addAttribute("selectedDataSet", selectedDataSet);
		}
		
		return "view-variome";
	}

	@RequestMapping(value = "/upload-file", headers = "content-type=multipart/*", method = RequestMethod.POST)
	public String handleVcfInput(@RequestParam("file-input-field")
	Part part, Model model) throws IOException
	{

		if (!part.equals(null) && part.getSize() > 5000000)
		{ // 5mb limit
			throw new RuntimeException("File too large");
		}
		else if (part.equals(null))
		{
			throw new RuntimeException("No file submitted");
		}

		String file = "cartagenia-export-file";
		fileStore.store(part.getInputStream(), file);

		pluginVariomeService.tsvToOmxRepository(file, model);

		return "view-variome";
	}

	@RequestMapping(value = "/upload-pasted-vcf", method = RequestMethod.POST)
	public String handleManualInput()
	{

		return "view-variome";
	}

	@RequestMapping(value = "/upload-zip", method = RequestMethod.POST)
	public String handleZipInput()
	{

		return "view-variome";
	}

	@RequestMapping(value = "/upload-pasted-gene-list", method = RequestMethod.POST)
	public String handleGeneInput()
	{

		return "view-variome";
	}

	@RequestMapping(value = "/upload-region-filter", method = RequestMethod.POST)
	public String handleRegionInput()
	{

		return "view-variome";
	}

	@RequestMapping(value = "/upload-bed", method = RequestMethod.POST)
	public String handleBedInput()
	{

		return "view-variome";
	}

	@RequestMapping(value = "/upload-phenotype-filter", method = RequestMethod.POST)
	public String handlePhenotypeInput()
	{

		return "view-variome";
	}

	@RequestMapping(value = "/execute-variant-app", method = RequestMethod.POST)
	public String filterMyVariants()
	{

		OmxDataSetAnnotator omxDataSetAnnotator = new OmxDataSetAnnotator(dataService, searchService, indexer);

		// omxDataSetAnnotator.annotate(ebiServiceAnnotator, dataService.getRepositoryByEntityName("uniprotTest"),
		// true);
		// omxDataSetAnnotator.annotate(caddServiceAnnotator, dataService.getRepositoryByEntityName("variantSet"),
		// false);
		omxDataSetAnnotator.annotate(dbnsfpVariantServiceAnnotator,
				dataService.getRepositoryByEntityName("variantSet"), true);
		omxDataSetAnnotator.annotate(dbnsfpGeneServiceAnnotator, dataService.getRepositoryByEntityName("variantSet"),
				true);
		// omxDataSetAnnotator.annotate(omimHpoAnnotator, dataService.getRepositoryByEntityName("5gpm set"), true);

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

package org.molgenis.omx.datasetdeleter;

import static org.molgenis.omx.datasetdeleter.DataSetDeleterController.URI;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.molgenis.omx.observ.DataSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(URI)
public class DataSetDeleterController extends MolgenisPluginController
{
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + "datasetdeleter";

	private static final List<String> RUNTIME_PROPERTIES = Arrays.asList("app.href.logo", "app.href.css");

	private final DataSetDeleterService dataSetDeleterService;
	private final MolgenisSettings molgenisSettings;

	@Autowired
	public DataSetDeleterController(DataSetDeleterService dataSetDeleterService, MolgenisSettings molgenisSettings)
	{
		super(URI);
		if (dataSetDeleterService == null) throw new IllegalArgumentException("Data set deleter service is null");
		if (molgenisSettings == null) throw new IllegalArgumentException("Molgenis settings is null");
		this.dataSetDeleterService = dataSetDeleterService;
		this.molgenisSettings = molgenisSettings;
	}

	@RequestMapping(method = RequestMethod.GET)
	public String init(Model model) throws Exception
	{
		for (String property : RUNTIME_PROPERTIES)
		{
			String value = molgenisSettings.getProperty(property);
			if (StringUtils.isNotBlank(value)) model.addAttribute(property.replaceAll("\\.", "_"), value);
		}
		return "view-datasetdeleter";
	}

	@RequestMapping(value = "/delete", method = RequestMethod.POST)
	@ResponseBody
	public String delete(@RequestParam("dataset") String dataSetIdentifier,
			@RequestParam(value = "deletemetadata", required = false) Boolean deleteMetaData) throws DatabaseException
	{
		boolean doDeleteMetaData = deleteMetaData != null ? deleteMetaData.booleanValue() : false;
		DataSet dataSet = dataSetDeleterService.delete(dataSetIdentifier, doDeleteMetaData);
		return dataSet.getName();
	}
}

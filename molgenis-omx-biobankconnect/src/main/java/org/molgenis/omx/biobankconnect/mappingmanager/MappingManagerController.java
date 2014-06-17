package org.molgenis.omx.biobankconnect.mappingmanager;

import static org.molgenis.omx.biobankconnect.mappingmanager.MappingManagerController.URI;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.molgenis.data.DataService;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.molgenis.omx.biobankconnect.ontologymatcher.OntologyMatcher;
import org.molgenis.omx.biobankconnect.wizard.CurrentUserStatus;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.security.core.utils.SecurityUtils;
import org.molgenis.security.user.UserAccountService;
import org.molgenis.util.FileStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping(URI)
public class MappingManagerController extends MolgenisPluginController
{

	public static final String ID = "mappingmanager";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;

	private static final String PROTOCOL_IDENTIFIER = "store_mapping";
	private final OntologyMatcher ontologyMatcher;
	private final DataService dataService;
	private final UserAccountService userAccountService;

	@Autowired
	private CurrentUserStatus currentUserStatus;

	@Autowired
	private FileStore fileStore;

	@Autowired
	public MappingManagerController(OntologyMatcher ontologyMatcher, UserAccountService userAccountService,
			DataService dataService)
	{
		super(URI);
		if (ontologyMatcher == null) throw new IllegalArgumentException("OntologyMatcher is null");
		if (dataService == null) throw new IllegalArgumentException("DataService is null");
		if (userAccountService == null) throw new IllegalArgumentException("userAccountService is null");
		this.userAccountService = userAccountService;
		this.ontologyMatcher = ontologyMatcher;
		this.dataService = dataService;
	}

	@RequestMapping(method = RequestMethod.GET)
	public String init(@RequestParam(value = "selectedDataSet", required = false)
	String selectedDataSetId, HttpServletRequest request, Model model)
	{
		List<DataSet> dataSets = new ArrayList<DataSet>();
		Iterable<DataSet> allDataSets = dataService.findAll(DataSet.ENTITY_NAME, new QueryImpl(), DataSet.class);
		for (DataSet dataSet : allDataSets)
		{
			if (!dataSet.getProtocolUsed().getIdentifier().equals(PROTOCOL_IDENTIFIER)) dataSets.add(dataSet);
		}
		model.addAttribute("dataSets", dataSets);
		model.addAttribute("userName", SecurityUtils.getCurrentUsername());
		if (selectedDataSetId != null) model.addAttribute("selectedDataSet", selectedDataSetId);
		model.addAttribute("isRunning", ontologyMatcher.isRunning());
		currentUserStatus.setUserLoggedIn(userAccountService.getCurrentUser().getUsername(),
				request.getRequestedSessionId());

		return "MappingManagerPlugin";
	}
}
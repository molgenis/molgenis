package org.molgenis.omx.importer;

import static org.molgenis.omx.importer.ImportWizardController.URI;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import javax.servlet.http.HttpServletRequest;

import org.molgenis.framework.db.Database;
import org.molgenis.ui.MolgenisPluginController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttributes;

@Controller
@RequestMapping(URI)
@SessionAttributes(
{ "wizard" })
public class ImportWizardController extends MolgenisPluginController
{
	public static final String URI = "/plugin/importwizard";
	private static final String VIEW_NAME = "view-importwizard";
	private final Database database;

	@Autowired
	public ImportWizardController(Database database)
	{
		super(URI);
		if (database == null) throw new IllegalArgumentException("Database is null");
		this.database = database;
	}

	@RequestMapping
	public String init(Model model)
	{
		model.addAttribute("wizard", new ImportWizard());
		return VIEW_NAME;
	}

	@RequestMapping(value = "/next", method = POST)
	public String next(@ModelAttribute("wizard")
	ImportWizard importWizard, HttpServletRequest request) throws Exception
	{

		importWizard.setErrorMessage(null);
		importWizard.setValidationMessage(null);
		importWizard.setSuccessMessage(null);

		importWizard.getCurrentPage().handleRequest(database, request);

		if (importWizard.getErrorMessage() == null)
		{
			importWizard.next();
		}

		return VIEW_NAME;
	}

	@RequestMapping("/previous")
	public String next(@ModelAttribute("wizard")
	ImportWizard importWizard)
	{
		importWizard.setErrorMessage(null);
		importWizard.setValidationMessage(null);
		importWizard.setSuccessMessage(null);

		importWizard.previous();

		return VIEW_NAME;
	}

}

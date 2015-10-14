package org.molgenis.gaf;

import static org.molgenis.gaf.GafListImporterController.URI;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;

import org.molgenis.ui.MolgenisPluginController;
import org.molgenis.util.ErrorMessageResponse;
import org.molgenis.util.ErrorMessageResponse.ErrorMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping(URI)
@Scope("request")
public class GafListImporterController extends MolgenisPluginController
{
	private static final Logger LOG = LoggerFactory.getLogger(GafListImporterController.class);

	public static final String ID = "gaflistimporter";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;
	public static final String KEY_GAF_LIST_PROTOCOL_NAME = "gafList.protocol.name";
	public static final String KEY_GAF_LIST_DATASET_IDENTIFIER = "gafList.dataset.identifier";
	private final GafListFileImporterService gafListFileImporterService;

	@Autowired
	private GafListValidationReport report;

	@Autowired
	public GafListImporterController(GafListFileImporterService gafListFileImporter)
	{
		super(URI);
		if (gafListFileImporter == null) throw new IllegalArgumentException("gafListFileImporter is null");
		this.gafListFileImporterService = gafListFileImporter;
	}

	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAnyRole('ROLE_SU')")
	public String init(Model model) throws Exception
	{
		model.addAttribute("action", "/validate");
		model.addAttribute("enctype", "multipart/form-data");
		return "view-gaflistimporter";
	}

	@RequestMapping(method = RequestMethod.GET, value = "/validate")
	@PreAuthorize("hasAnyRole('ROLE_SU')")
	public String validateGAFList(Model model) throws Exception
	{
		return init(model);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/import")
	@PreAuthorize("hasAnyRole('ROLE_SU')")
	public String importGAFList(Model model) throws Exception
	{
		return init(model);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/validate")
	@PreAuthorize("hasAnyRole('ROLE_SU')")
	public String validateGAFList(HttpServletRequest request, @RequestParam("csvFile") MultipartFile csvFile,
			Model model) throws IOException, MessagingException, Exception
	{
		boolean submitState = false;
		String action = "/validate";
		String enctype = "multipart/form-data";

		final List<String> messages = new ArrayList<String>();
		if (!csvFile.isEmpty())
		{
			try
			{
				this.gafListFileImporterService.validateGAFList(report, csvFile);
				model.addAttribute("hasValidationError", (report.hasGlobalErrors() || report.hasRunIdsErrors()));
				model.addAttribute("validationReport", report.toStringHtml());

				if (!report.getValidRunIds().isEmpty())
				{
					submitState = true;
					action = "/import";
					enctype = "application/x-www-form-urlencoded";
					model.addAttribute("fileName", report.getTempFileOriginalName());
					messages.add("Run id's: <b>" + report.getValidRunIds() + "</b> can be imported");
				}
				else
				{
					messages.add("Validation for all runs failed");
				}

				if (report.hasGlobalErrors())
				{
					messages.addAll(report.getValidationGlobalErrorMessages());
				}

				if (report.hasRunIdsErrors())
				{
					messages.add("Run id's: <b>" + report.getInvalidRunIds() + "</b> cannot be imported");
				}
			}
			catch (Exception e)
			{
				String errorMessage = "Failed to validate this file";
				messages.add(errorMessage);
				LOG.error(errorMessage, e);
			}
		}
		else
		{
			String errorMessage = "The file you try to upload is empty! Filename: " + csvFile.getOriginalFilename();
			messages.add(errorMessage);
			LOG.error(errorMessage);
		}
		model.addAttribute("action", action);
		model.addAttribute("enctype", enctype);
		model.addAttribute("submit_state", submitState);
		model.addAttribute("messages", messages);
		return "view-gaflistimporter";
	}

	@RequestMapping(method = RequestMethod.POST, value = "/import")
	@PreAuthorize("hasAnyRole('ROLE_SU')")
	public String importGAFList(HttpServletRequest request, Model model) throws Exception
	{
		final List<String> messages = new ArrayList<String>();
		try
		{
			this.gafListFileImporterService.importGAFList(report, KEY_GAF_LIST_PROTOCOL_NAME);

			if (!report.getValidRunIds().isEmpty())
			{
				messages.add("Successfully imported GAF list named: <b><a href=" + "/menu/main/dataexplorer?entity="
						+ report.getDataSetIdentifier() + ">" + report.getDataSetName() + "</a></b>");

				messages.add("Imported run id's: <b>" + report.getValidRunIds() + "</b>");
			}

			if (report.hasRunIdsErrors())
			{
				messages.add("Not imported run id's: <b>" + report.getInvalidRunIds() + "</b>");
			}
		}
		catch (Exception e)
		{
			String errorMessage = "Failed to import this file";
			messages.add(errorMessage);
			LOG.error(errorMessage, e);
		}
		finally
		{
			model.addAttribute("messages", messages);
			model.addAttribute("submit_state", false);
			model.addAttribute("action", "/validate");
			model.addAttribute("enctype", "multipart/form-data");
			report.cleanUp();
		}

		return "view-gaflistimporter";
	}

	@ExceptionHandler(value = Throwable.class)
	@ResponseBody
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ErrorMessageResponse handleThrowable(Throwable t)
	{
		LOG.error("", t);
		return new ErrorMessageResponse(new ErrorMessage(t.getMessage()));
	}
}

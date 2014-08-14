package org.molgenis.gaf;

import static org.molgenis.gaf.GafListImporterController.URI;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.mail.MessagingException;

import org.apache.log4j.Logger;
import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.molgenis.omx.converters.ValueConverterException;
import org.molgenis.util.ErrorMessageResponse;
import org.molgenis.util.ErrorMessageResponse.ErrorMessage;
import org.springframework.beans.factory.annotation.Autowired;
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

import com.google.gdata.util.ServiceException;

@Controller
@RequestMapping(URI)
public class GafListImporterController extends MolgenisPluginController
{
	private static final Logger logger = Logger.getLogger(GafListImporterController.class);

	public static final String ID = "gaflistimporter";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;
	public static final String KEY_GAF_LIST_PROTOCOL_NAME = "gafList.protocol.name";
	public static final String KEY_GAF_LIST_DATASET_IDENTIFIER = "gafList.dataset.identifier";
	private final GafListFileImporterService gafListFileImporterService;

	@Autowired
	public GafListImporterController(GafListFileImporterService gafListFileImporter, MolgenisSettings molgenisSettings)
	{
		super(URI);
		if (gafListFileImporter == null) throw new IllegalArgumentException("gafListFileImporter is null");
		this.gafListFileImporterService = gafListFileImporter;
	}

	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAnyRole('ROLE_SU')")
	public String init()
	{
		return "view-gaflistimporter";
	}

	@RequestMapping(method = RequestMethod.POST)
	@PreAuthorize("hasAnyRole('ROLE_SU')")
	public String importGafListFromFile(@RequestParam("csvFile") MultipartFile csvFile,
			@RequestParam("separator") Character separator, Model model) throws IOException, ServiceException,
			ValueConverterException, MessagingException
	{
		final List<String> messages = new ArrayList<String>();
		if (!csvFile.isEmpty())
		{
			try
			{
				GafListValidationReport gafListValidationReport = this.gafListFileImporterService.importGafList(
						csvFile, separator, KEY_GAF_LIST_PROTOCOL_NAME);

				model.addAttribute("hasValidationError", gafListValidationReport.hasErrors());
				model.addAttribute("validationReport", gafListValidationReport.toStringHtml());

				if (!gafListValidationReport.getValidRunIds().isEmpty())
				{
					messages.add("Successfully imported GAF list named: <b>" + gafListValidationReport.getDataSetName()
							+ "</b>");

					messages.add("Imported run id's: <b>" + gafListValidationReport.getValidRunIds() + "</b>");
				}
				else
				{
					messages.add("This file is not imported because the validation for all runs failed");
				}

				if (gafListValidationReport.hasErrors())
				{
					messages.add("Not imported run id's: <b>" + gafListValidationReport.getInvalidRunIds() + "</b>");
				}
			}
			catch (Exception e)
			{
				String errorMessage = "Failed to import data into database.";
				messages.add(errorMessage);
				logger.error(errorMessage, e);
			}
		}
		else
		{
			String errorMessage = "The file you try to upload is empty! Filename: " + csvFile.getOriginalFilename();
			messages.add(errorMessage);
			logger.error(errorMessage);
		}
		model.addAttribute("messages", messages);
		return "view-gaflistimporter";
	}

	@ExceptionHandler(value = Throwable.class)
	@ResponseBody
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ErrorMessageResponse handleThrowable(Throwable t)
	{
		logger.error("", t);
		return new ErrorMessageResponse(new ErrorMessage(t.getMessage()));
	}
}

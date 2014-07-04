package org.molgenis.gaf;

import static org.molgenis.gaf.GafListImporterController.URI;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.mail.MessagingException;

import org.apache.log4j.Logger;
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
	private final GafListFileImporterService gafListFileImporterService;

	@Autowired
	public GafListImporterController(GafListFileImporterService gafListFileImporter)
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
			this.gafListFileImporterService.createCsvRepository(csvFile, separator);
			this.gafListFileImporterService.createValidationReport();
			model.addAttribute("hasValidationError", this.gafListFileImporterService.hasValidationError());
			model.addAttribute("validationReport", this.gafListFileImporterService.getValidationReportHtml());

			try
			{
				String nameGafList = this.gafListFileImporterService.importValidatedGafList();
				messages.add("Successfully imported! the new list name is: <input type=\"text\" value=\"" + nameGafList + "\">");
				messages.add("The imported runs are: " + this.gafListFileImporterService.getReport().getValidRunIds());

				if (this.gafListFileImporterService.hasValidationError())
				{
					messages.add("Not imported runs id's: "
							+ this.gafListFileImporterService.getReport().getInvalidRunIds());
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
			String errorMessage = "The file you try to upload is empty! Filename: " + csvFile;
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

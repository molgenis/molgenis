package org.molgenis.gaf;

import static org.molgenis.gaf.GafListImporterController.URI;

import java.io.IOException;

import javax.mail.MessagingException;

import org.apache.log4j.Logger;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.molgenis.omx.converters.ValueConverterException;
import org.molgenis.util.ErrorMessageResponse;
import org.molgenis.util.ErrorMessageResponse.ErrorMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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

	private final GafListImporterService gafListImporterService;
	private final GafListFileImporterService gafListFileImporterService;

	@Autowired
	public GafListImporterController(GafListImporterService gafListImporterService,
			GafListFileImporterService gafListFileImporter)
	{
		super(URI);
		if (gafListImporterService == null) throw new IllegalArgumentException("gafListImporterService is null");
		this.gafListImporterService = gafListImporterService;

		if (gafListFileImporter == null) throw new IllegalArgumentException("gafListFileImporter is null");
		this.gafListFileImporterService = gafListFileImporter;
	}

	@RequestMapping(method = RequestMethod.GET)
	public String init()
	{
		return "view-gaflistimporter";
	}

	@RequestMapping(value = "/import", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	public void importGafList() throws IOException, ServiceException, ValueConverterException, MessagingException
	{
		gafListImporterService.importGafListAsSuperuser();
	}

	@RequestMapping(value = "/import-file", method = RequestMethod.POST)
	public String importGafListFromFile(@RequestParam("csvFile") MultipartFile csvFile,
			@RequestParam("separator") Character separator, Model model)
	{
		if (!csvFile.isEmpty())
		{
			gafListFileImporterService.createCsvRepo(csvFile, separator);
			gafListFileImporterService.createValidationReport();
			model.addAttribute("hasValidationError", gafListFileImporterService.hasValidationError());
			model.addAttribute("validationReport", gafListFileImporterService.getValidationReportHtml());

			if (!gafListFileImporterService.hasValidationError())
			{
				try
				{
					gafListFileImporterService.importValidatedGafList();
					model.addAttribute("importMessage", "Successfully imported into database.");
				}
				catch (Exception e)
				{
					logger.error(e);
					model.addAttribute("importMessage", "Failed to import data into database.");
				}
			}
		}
		else
		{
			logger.error("The file you try to upload is empty! Filename: " + csvFile);
		}
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

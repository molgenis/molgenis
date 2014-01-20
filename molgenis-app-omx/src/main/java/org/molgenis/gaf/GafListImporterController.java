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
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.google.gdata.util.ServiceException;

@Controller
@RequestMapping(URI)
public class GafListImporterController extends MolgenisPluginController
{
	private static final Logger logger = Logger.getLogger(GafListImporterController.class);

	public static final String ID = "gaflistimporter";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;

	private final GafListImporterService gafListImporterService;

	@Autowired
	public GafListImporterController(GafListImporterService gafListImporterService)
	{
		super(URI);
		if (gafListImporterService == null) throw new IllegalArgumentException("gafListImporterService is null");
		this.gafListImporterService = gafListImporterService;
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

	@ExceptionHandler(value = Throwable.class)
	@ResponseBody
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ErrorMessageResponse handleThrowable(Throwable t)
	{
		logger.error("", t);
		return new ErrorMessageResponse(new ErrorMessage(t.getMessage()));
	}
}

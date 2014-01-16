package org.molgenis.gaf;

import static org.molgenis.gaf.GafListImporterController.URI;

import java.io.IOException;

import javax.mail.MessagingException;

import org.molgenis.framework.ui.MolgenisPluginController;
import org.molgenis.omx.converters.ValueConverterException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.google.gdata.util.ServiceException;

@Controller
@RequestMapping(URI)
public class GafListImporterController extends MolgenisPluginController
{
	public static final String ID = "gaflist";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;

	private final GafListImporterService gafListImporterService;

	@Autowired
	public GafListImporterController(GafListImporterService gafListImporterService)
	{
		super(URI);
		if (gafListImporterService == null) throw new IllegalArgumentException("gafListImporterService is null");
		this.gafListImporterService = gafListImporterService;
	}

	@RequestMapping("/import")
	@ResponseStatus(HttpStatus.OK)
	public void importGafList() throws IOException, ServiceException, ValueConverterException, MessagingException
	{
		gafListImporterService.importGafListAsSuperuser();
	}
}

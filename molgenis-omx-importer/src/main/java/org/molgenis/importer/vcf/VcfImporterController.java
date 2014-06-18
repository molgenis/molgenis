package org.molgenis.importer.vcf;

import static org.molgenis.importer.vcf.VcfImporterController.URI;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import org.apache.log4j.Logger;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping(URI)
public class VcfImporterController extends MolgenisPluginController
{
	private static final Logger logger = Logger.getLogger(VcfImporterController.class);

	static final String ID = "vcfimporter";
	static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;

	private final VcfImporterService vcfImporterService;

	@Autowired
	public VcfImporterController(VcfImporterService vcfImporterService)
	{
		super(URI);
		if (vcfImporterService == null) throw new IllegalArgumentException("vcfImporterService is null");
		this.vcfImporterService = vcfImporterService;
	}

	@RequestMapping(method = GET)
	public String init()
	{
		return "view-vcfimporter";
	}

	@RequestMapping(value = "/import", method = POST)
	@ResponseStatus(HttpStatus.OK)
	public void importVcf(@RequestParam("file") MultipartFile multipartFile) throws IOException
	{
		File tempFile = File.createTempFile("vcf" + System.currentTimeMillis(), null);
		try
		{
			multipartFile.transferTo(tempFile);
			vcfImporterService.importVcf(tempFile, multipartFile.getName());
		}
		finally
		{
			boolean deleteOk = tempFile.delete();
			if (!deleteOk)
			{
				logger.error("failed to delete " + tempFile.getName());
				tempFile.deleteOnExit();
			}
		}
	}

	@ExceptionHandler(IOException.class)
	@ResponseBody
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public Map<String, String> handleIOException(IOException e)
	{
		logger.error(null, e);
		return Collections.singletonMap("errorMessage",
				"An error occurred. Please contact the administrator.<br />Message:" + e.getMessage());
	}

	@ExceptionHandler(RuntimeException.class)
	@ResponseBody
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public Map<String, String> handleRuntimeException(RuntimeException e)
	{
		logger.error(null, e);
		return Collections.singletonMap("errorMessage",
				"An error occurred. Please contact the administrator.<br />Message:" + e.getMessage());
	}
}

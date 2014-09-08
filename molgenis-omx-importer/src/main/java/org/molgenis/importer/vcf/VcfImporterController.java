package org.molgenis.importer.vcf;

import static org.molgenis.importer.vcf.VcfImporterController.URI;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.molgenis.util.ErrorMessageResponse;
import org.molgenis.util.ErrorMessageResponse.ErrorMessage;
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
	public void importVcf(@RequestParam("name") String entityName, @RequestParam("file") MultipartFile multipartFile)
			throws IOException
	{
		String name = multipartFile.getOriginalFilename().toLowerCase();
		String suffix;
		if (name.endsWith(".vcf")) suffix = ".vcf";
		else if (name.endsWith(".vcf.gz")) suffix = ".vcf.gz";
		else throw new MolgenisDataException("Unsupported file type " + multipartFile.getOriginalFilename());

		String tmpPath = System.getProperty("java.io.tmpdir");
		if (!tmpPath.endsWith(File.separator)) tmpPath += File.separator;
		tmpPath += UUID.randomUUID().toString();
		File tmpDir = new File(tmpPath);
		boolean mkdirOk = tmpDir.mkdir();
		if (!mkdirOk) throw new IOException("Could not create " + tmpPath);

		try
		{
			File tempFile = new File(tmpPath + File.separator + entityName + suffix);
			try
			{
				// use copyInputStreamToFile because multipartFile.transferTo(tempFile) results in a tempFile of 0 bytes
				InputStream inputStream = multipartFile.getInputStream();
				try
				{
					FileUtils.copyInputStreamToFile(inputStream, tempFile);
				}
				finally
				{
					inputStream.close();
				}
				vcfImporterService.importVcf(tempFile);
			}
			finally
			{
				boolean deleteOk = tempFile.delete();
				if (!deleteOk)
				{
					logger.error("Failed to delete " + tempFile.getName());
					tempFile.deleteOnExit();
				}
			}
		}
		finally
		{
			boolean deleteOk = tmpDir.delete();
			if (!deleteOk)
			{
				logger.error("Failed to delete " + tmpDir.getName());
				tmpDir.deleteOnExit();
			}
		}
	}

	@ExceptionHandler(MolgenisDataException.class)
	@ResponseBody
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ErrorMessageResponse handleMolgenisDataException(MolgenisDataException e)
	{
		logger.debug(null, e);
		return new ErrorMessageResponse(new ErrorMessage(e.getMessage()));
	}

	@ExceptionHandler(IOException.class)
	@ResponseBody
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ErrorMessageResponse handleIOException(IOException e)
	{
		logger.error(null, e);
		return new ErrorMessageResponse(new ErrorMessage(
				"An error occurred. Please contact the administrator.<br />Message:" + e.getMessage()));
	}

	@ExceptionHandler(RuntimeException.class)
	@ResponseBody
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ErrorMessageResponse handleRuntimeException(RuntimeException e)
	{
		logger.error(null, e);
		return new ErrorMessageResponse(new ErrorMessage(
				"An error occurred. Please contact the administrator.<br />Message:" + e.getMessage()));
	}
}

package org.molgenis.gavin.controller;

import org.molgenis.data.DataService;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.file.FileStore;
import org.molgenis.gavin.job.GavinJob;
import org.molgenis.gavin.job.GavinJobExecution;
import org.molgenis.gavin.job.GavinJobFactory;
import org.molgenis.security.user.UserAccountService;
import org.molgenis.ui.MolgenisPluginController;
import org.molgenis.util.ErrorMessageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.ExecutorService;

import static java.io.File.separator;
import static java.lang.String.format;
import static org.molgenis.gavin.controller.GavinController.URI;
import static org.molgenis.gavin.job.GavinJobExecutionMetaData.GAVIN_JOB_EXECUTION;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Controller
@RequestMapping(URI)
public class GavinController extends MolgenisPluginController
{
	private static final Logger LOG = LoggerFactory.getLogger(GavinController.class);

	public static final String ID = "gavin";
	static final String URI = PLUGIN_URI_PREFIX + ID;

	public GavinController()
	{
		super(URI);
	}

	@Autowired
	private DataService dataService;

	@Autowired
	private ExecutorService executorService;

	@Autowired
	private GavinJobFactory gavinJobFactory;

	@Autowired
	private FileStore fileStore;

	@Autowired
	private UserAccountService userAccountService;

	/**
	 * Shows the gavin page.
	 *
	 * @return the view name
	 */
	@RequestMapping(method = RequestMethod.GET)
	public String init(Model model)
	{
		List<String> annotatorsWithMissingResources = gavinJobFactory.getAnnotatorsWithMissingResources();
		if (!annotatorsWithMissingResources.isEmpty())
		{
			model.addAttribute("annotatorsWithMissingResources", annotatorsWithMissingResources);
		}
		return "view-gavin";
	}

	@RequestMapping(value = "/annotate-file", method = POST)
	@ResponseBody
	public String annotateFile(HttpServletRequest request, @RequestParam(value = "file") MultipartFile inputFile)
			throws IOException, URISyntaxException
	{
		GavinJobExecution gavinJobExecution = new GavinJobExecution(dataService);
		gavinJobExecution.setUser(userAccountService.getCurrentUser());
		GavinJob gavinJob = gavinJobFactory.createJob(gavinJobExecution);

		String gavinJobIdentifier = gavinJobExecution.getIdentifier();
		String fileName = "gavin/" + gavinJobIdentifier + "/input.vcf";

		File file = fileStore.getFile(fileName);
		file.getParentFile().mkdirs();
		inputFile.transferTo(file);

		executorService.submit(gavinJob);

		return "/api/v2/GavinJobExecution/" + gavinJobIdentifier;
	}

	@RequestMapping(value = "/result/{jobIdentifier}", method = GET, produces = APPLICATION_OCTET_STREAM_VALUE)
	@ResponseBody
	public FileSystemResource result(HttpServletResponse response, @PathVariable(value = "jobIdentifier") String jobIdentifier)
			throws IOException
	{
		File file = fileStore.getFile("gavin" + separator + jobIdentifier + separator + "gavin-result.vcf");
		if (!file.exists())
		{
			throw new MolgenisDataException("Sorry, " + file.getName() + " does not exist for job " + jobIdentifier);
		}
		response.setHeader("Content-Disposition", String.format("inline; filename=\"" + file.getName() + "\""));
		return new FileSystemResource(file);
	}

	@ExceptionHandler(RuntimeException.class)
	@ResponseBody
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ErrorMessageResponse handleRuntimeException(RuntimeException e)
	{
		LOG.warn(e.getMessage(), e);
		return new ErrorMessageResponse(new ErrorMessageResponse.ErrorMessage(e.getMessage()));
	}
}

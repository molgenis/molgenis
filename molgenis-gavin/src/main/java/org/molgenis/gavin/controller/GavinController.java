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
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;

import static java.io.File.separator;
import static java.text.MessageFormat.format;
import static org.apache.commons.io.FileUtils.deleteDirectory;
import static org.molgenis.gavin.controller.GavinController.URI;
import static org.molgenis.gavin.job.GavinJobExecutionMetaData.GAVIN_JOB_EXECUTION;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Controller
@RequestMapping(URI)
@EnableScheduling
public class GavinController extends MolgenisPluginController
{
	private static final Logger LOG = LoggerFactory.getLogger(GavinController.class);

	public static final String GAVIN_APP = "gavin-app";
	public static final String URI = PLUGIN_URI_PREFIX + GAVIN_APP;

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

	/**
	 * Starts a job to annotate a VCF file
	 *
	 * @param inputFile  the input file, should be VCF
	 * @param entityName the name of the file, the download will be
	 * @return the URL where the job progress can be monitored
	 * @throws IOException
	 */
	@RequestMapping(value = "/annotate-file", method = POST)
	@ResponseBody
	public String annotateFile(@RequestParam(value = "file") MultipartFile inputFile, @RequestParam String entityName)
			throws IOException
	{
		GavinJobExecution gavinJobExecution = new GavinJobExecution(dataService);
		gavinJobExecution.setFilename(entityName + "-gavin.vcf");
		gavinJobExecution.setUser(userAccountService.getCurrentUser());
		GavinJob gavinJob = gavinJobFactory.createJob(gavinJobExecution);

		String gavinJobIdentifier = gavinJobExecution.getIdentifier();
		String fileName = format("{0}{1}{2}{3}input.vcf", GAVIN_APP, separator, gavinJobIdentifier, separator);

		File file = fileStore.getFile(fileName);
		file.getParentFile().mkdirs();
		inputFile.transferTo(file);

		executorService.submit(gavinJob);

		return "/api/v2/GavinJobExecution/" + gavinJobIdentifier;
	}

	/**
	 * Downloads the result of a gavin annotation job.
	 *
	 * @param response      {@link HttpServletResponse} to write the Content-Disposition header to with the filename
	 * @param jobIdentifier GAVIN_APP of the annotation job
	 * @return {@link FileSystemResource} with the annotated file
	 */
	@RequestMapping(value = "/result/{jobIdentifier}", method = GET, produces = APPLICATION_OCTET_STREAM_VALUE)
	@ResponseBody
	public FileSystemResource result(HttpServletResponse response,
			@PathVariable(value = "jobIdentifier") String jobIdentifier)
	{
		GavinJobExecution jobExecution = dataService
				.findOne(GAVIN_JOB_EXECUTION, jobIdentifier, GavinJobExecution.class);
		File file = fileStore.getFile(GAVIN_APP + separator + jobIdentifier + separator + "gavin-result.vcf");
		if (!file.exists())
		{
			LOG.warn(format("File {0} not found for job {1}", file.getName(), jobIdentifier));
			throw new MolgenisDataException("No output file found for this job");
		}
		response.setHeader("Content-Disposition", format("inline; filename=\"{0}\"", jobExecution.getFilename()));
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

package org.molgenis.gavin.controller;

import org.molgenis.data.DataService;
import org.molgenis.file.FileStore;
import org.molgenis.gavin.job.GavinJob;
import org.molgenis.gavin.job.GavinJobExecution;
import org.molgenis.gavin.job.GavinJobFactory;
import org.molgenis.gavin.job.meta.GavinJobExecutionFactory;
import org.molgenis.security.user.UserAccountService;
import org.molgenis.ui.MolgenisPluginController;
import org.molgenis.util.ErrorMessageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;

import static java.io.File.separator;
import static java.text.MessageFormat.format;
import static java.util.Objects.requireNonNull;
import static org.molgenis.gavin.controller.GavinController.URI;
import static org.molgenis.gavin.job.meta.GavinJobExecutionMetaData.GAVIN_JOB_EXECUTION;
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

	private DataService dataService;
	private ExecutorService executorService;
	private GavinJobFactory gavinJobFactory;
	private GavinJobExecutionFactory gavinJobExecutionFactory;
	private FileStore fileStore;
	private UserAccountService userAccountService;

	@Autowired
	public GavinController(DataService dataService, ExecutorService executorService, GavinJobFactory gavinJobFactory,
			GavinJobExecutionFactory gavinJobExecutionFactory, FileStore fileStore,
			UserAccountService userAccountService)
	{
		super(URI);
		this.dataService = requireNonNull(dataService);
		this.executorService = requireNonNull(executorService);
		this.gavinJobFactory = requireNonNull(gavinJobFactory);
		this.gavinJobExecutionFactory = requireNonNull(gavinJobExecutionFactory);
		this.fileStore = requireNonNull(fileStore);
		this.userAccountService = requireNonNull(userAccountService);
	}

	/**
	 * Shows the gavin page.
	 * This page shows the configuration wheels if the annotation resources are not yet properly configured.
	 * If the annotation resources are fine, it shows the upload control.
	 *
	 * @return the view name
	 */
	@SuppressWarnings({ "SameReturnValue", "UnusedReturnValue" })
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
	 * @param inputFile  the uploaded input file
	 * @param entityName the name of the file
	 * @return the ID of the created {@link GavinJobExecution}
	 * @throws IOException if interaction with the file store fails
	 */
	@RequestMapping(value = "/annotate-file", method = POST)
	@ResponseBody
	public String annotateFile(@RequestParam(value = "file") MultipartFile inputFile, @RequestParam String entityName)
			throws IOException
	{
		final GavinJobExecution gavinJobExecution = gavinJobExecutionFactory.create();
		gavinJobExecution.setFilename(entityName + "-gavin.vcf");
		gavinJobExecution.setUser(userAccountService.getCurrentUser().getUsername());
		final GavinJob gavinJob = gavinJobFactory.createJob(gavinJobExecution);

		final String gavinJobIdentifier = gavinJobExecution.getIdentifier();
		fileStore.createDirectory(GAVIN_APP);
		final String jobDir = format("{0}{1}{2}", GAVIN_APP, separator, gavinJobIdentifier);
		fileStore.createDirectory(jobDir);
		final String fileName = format("{0}{1}input.vcf", jobDir, separator);
		fileStore.writeToFile(inputFile.getInputStream(), fileName);

		executorService.submit(gavinJob);

		return "/api/v2/" + gavinJobExecution.getEntityType().getName() + "/" + gavinJobIdentifier;
	}

	/**
	 * Shows result page for a job. The job may still be running.
	 *
	 * @param jobIdentifier identifier of the annotation job
	 * @return {@link FileSystemResource} with the annotated file
	 */
	@RequestMapping(value = "/job/{jobIdentifier}", method = GET)
	public String job(@PathVariable(value = "jobIdentifier") String jobIdentifier, Model model)
	{
		model.addAttribute("jobExecution",
				dataService.findOneById(GAVIN_JOB_EXECUTION, jobIdentifier, GavinJobExecution.class));
		return "view-gavin-result";
	}

	/**
	 * Downloads the result of a gavin annotation job.
	 *
	 * @param response      {@link HttpServletResponse} to write the Content-Disposition header to with the filename
	 * @param jobIdentifier GAVIN_APP of the annotation job
	 * @return {@link FileSystemResource} with the annotated file
	 */
	@RequestMapping(value = "/download/{jobIdentifier}", method = GET, produces = APPLICATION_OCTET_STREAM_VALUE)
	@ResponseBody
	public FileSystemResource download(HttpServletResponse response,
			@PathVariable(value = "jobIdentifier") String jobIdentifier) throws FileNotFoundException
	{
		GavinJobExecution jobExecution = dataService
				.findOneById(GAVIN_JOB_EXECUTION, jobIdentifier, GavinJobExecution.class);
		File file = fileStore.getFile(GAVIN_APP + separator + jobIdentifier + separator + "gavin-result.vcf");
		if (!file.exists())
		{
			LOG.warn("No result file found for job {}", jobIdentifier);
			throw new FileNotFoundException("No result file found for this job. Results are removed every night.");
		}
		response.setHeader("Content-Disposition", format("inline; filename=\"{0}\"", jobExecution.getFilename()));
		return new FileSystemResource(file);
	}

	/**
	 * Downloads the result of a gavin annotation job.
	 *
	 * @param response      {@link HttpServletResponse} to write the Content-Disposition header to with the filename
	 * @param jobIdentifier GAVIN_APP of the annotation job
	 * @return {@link FileSystemResource} with the annotated file
	 */
	@RequestMapping(value = "/error/{jobIdentifier}", method = GET)
	@ResponseBody
	public Resource downloadErrorReport(HttpServletResponse response,
			@PathVariable(value = "jobIdentifier") String jobIdentifier) throws FileNotFoundException
	{
		GavinJobExecution jobExecution = dataService
				.findOneById(GAVIN_JOB_EXECUTION, jobIdentifier, GavinJobExecution.class);
		response.setHeader("Content-Disposition",
				format("inline; filename=\"{0}-error.txt\"", jobExecution.getFilename()));
		final File file = fileStore.getFile(GAVIN_APP + separator + jobIdentifier + separator + "error.txt");
		if (!file.exists())
		{
			LOG.warn("No error file found for job {}", jobIdentifier);
			throw new FileNotFoundException("No error report found for this job. Results are removed every night.");
		}
		return new FileSystemResource(file);
	}

	@ExceptionHandler(value = FileNotFoundException.class)
	public void handleFileNotFound(FileNotFoundException ex, HttpServletResponse res) throws IOException
	{
		res.sendError(404, ex.getMessage());
	}

	@ExceptionHandler(RuntimeException.class)
	@ResponseBody
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ErrorMessageResponse handleRuntimeException(RuntimeException e)
	{
		LOG.warn(e.getMessage(), e);
		return new ErrorMessageResponse(new ErrorMessageResponse.ErrorMessage(e.getMessage()));
	}

	/**
	 * Removes the working directory from the file store.
	 */
	@Scheduled(cron = "0 0 0 * * *")
	public void cleanUp()
	{
		LOG.info("Clean up working directory in the file store...");
		try
		{
			fileStore.deleteDirectory(GAVIN_APP);
			LOG.info("Done.");
		}
		catch (IOException e)
		{
			LOG.error("Failed to clean up working directory", e);
		}
	}
}

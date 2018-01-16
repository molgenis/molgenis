package org.molgenis.gavin.controller;

import org.molgenis.core.ui.controller.AbstractStaticContentController;
import org.molgenis.core.ui.menu.MenuReaderService;
import org.molgenis.data.file.FileStore;
import org.molgenis.data.populate.IdGenerator;
import org.molgenis.gavin.job.GavinJob;
import org.molgenis.gavin.job.GavinJobExecution;
import org.molgenis.gavin.job.GavinJobFactory;
import org.molgenis.gavin.job.JobNotFoundException;
import org.molgenis.gavin.job.meta.GavinJobExecutionFactory;
import org.molgenis.security.user.UserAccountService;
import org.molgenis.web.ErrorMessageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;

import static java.io.File.separator;
import static java.text.MessageFormat.format;
import static java.time.ZonedDateTime.now;
import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.molgenis.data.populate.IdGenerator.Strategy.SECURE_RANDOM;
import static org.molgenis.gavin.controller.GavinController.URI;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;

@Controller
@RequestMapping(URI)
@EnableScheduling
public class GavinController extends AbstractStaticContentController
{
	private static final Logger LOG = LoggerFactory.getLogger(GavinController.class);

	public static final String GAVIN_APP = "gavin-app";
	static final String URI = PLUGIN_URI_PREFIX + GAVIN_APP;
	public static final String TSV_GZ = "tsv.gz";
	public static final String TSV = "tsv";
	public static final String GZ = "gz";

	private final ExecutorService executorService;
	private final GavinJobFactory gavinJobFactory;
	private final GavinJobExecutionFactory gavinJobExecutionFactory;
	private final FileStore fileStore;
	private final UserAccountService userAccountService;
	private final IdGenerator idGenerator;
	private final MenuReaderService menuReaderService;

	public GavinController(@Qualifier("gavinExecutors") ExecutorService executorService,
			GavinJobFactory gavinJobFactory, GavinJobExecutionFactory gavinJobExecutionFactory, FileStore fileStore,
			UserAccountService userAccountService, MenuReaderService menuReaderService, IdGenerator idGenerator)
	{
		super(GAVIN_APP, URI);
		this.executorService = requireNonNull(executorService);
		this.gavinJobFactory = requireNonNull(gavinJobFactory);
		this.gavinJobExecutionFactory = requireNonNull(gavinJobExecutionFactory);
		this.fileStore = requireNonNull(fileStore);
		this.userAccountService = requireNonNull(userAccountService);
		this.menuReaderService = requireNonNull(menuReaderService);
		this.idGenerator = requireNonNull(idGenerator);
	}

	/**
	 * Shows the gavin page.
	 * This page shows the configuration wheels if the annotation resources are not yet properly configured.
	 * If the annotation resources are fine, it shows the upload control.
	 *
	 * @return the view name
	 */
	@GetMapping
	public String init(Model model)
	{
		super.init(model);
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
	 * @param inputFile    the uploaded input file
	 * @param entityTypeId the name of the file
	 * @return the ID of the created {@link GavinJobExecution}
	 * @throws IOException if interaction with the file store fails
	 */
	@PostMapping("/annotate-file")
	public ResponseEntity<String> annotateFile(@RequestParam(value = "file") MultipartFile inputFile,
			@RequestParam String entityTypeId) throws IOException
	{
		String extension = TSV;
		if (inputFile.getOriginalFilename().endsWith(GZ))
		{
			extension = TSV_GZ;
		}

		final GavinJobExecution gavinJobExecution = gavinJobExecutionFactory.create(
				idGenerator.generateId(SECURE_RANDOM));
		gavinJobExecution.setFilename(entityTypeId);
		gavinJobExecution.setUser(userAccountService.getCurrentUser().getUsername());
		gavinJobExecution.setInputFileExtension(extension);
		final GavinJob gavinJob = gavinJobFactory.createJob(gavinJobExecution);

		final String gavinJobIdentifier = gavinJobExecution.getIdentifier();
		fileStore.createDirectory(GAVIN_APP);
		final String jobDir = format("{0}{1}{2}", GAVIN_APP, separator, gavinJobIdentifier);
		fileStore.createDirectory(jobDir);

		final String fileName = format("{0}{1}input.{2}", jobDir, separator, extension);
		fileStore.writeToFile(inputFile.getInputStream(), fileName);

		executorService.submit(gavinJob);

		String location = "/plugin/gavin-app/job/" + gavinJobIdentifier;
		return ResponseEntity.created(java.net.URI.create(location)).body(location);
	}

	/**
	 * Retrieves {@link GavinJobExecution} job.
	 * May be called by anyone who has the identifier.
	 *
	 * @param jobIdentifier identifier of the annotation job
	 * @return GavinJobExecution, or null if no GavinJobExecution exists with this ID.
	 */
	@GetMapping(value = "/job/{jobIdentifier}", produces = APPLICATION_JSON_VALUE)
	public @ResponseBody
	GavinJobExecution getGavinJobExecution(@PathVariable(value = "jobIdentifier") String jobIdentifier)
			throws JobNotFoundException
	{
		return gavinJobFactory.findGavinJobExecution(jobIdentifier);
	}

	/**
	 * Shows result page for a job. The job may still be running.
	 *
	 * @param jobIdentifier identifier of the annotation job
	 * @return {@link FileSystemResource} with the annotated file
	 */
	@GetMapping("/result/{jobIdentifier}")
	public String result(@PathVariable(value = "jobIdentifier") String jobIdentifier, Model model,
			HttpServletRequest request) throws JobNotFoundException
	{
		model.addAttribute("jobExecution", gavinJobFactory.findGavinJobExecution(jobIdentifier));
		model.addAttribute("downloadFileExists", getDownloadFileForJob(jobIdentifier).exists());
		model.addAttribute("errorFileExists", getErrorFileForJob(jobIdentifier).exists());
		model.addAttribute("pageUrl", getPageUrl(jobIdentifier, request));
		return "view-gavin-result";
	}

	private String getPageUrl(String jobIdentifier, HttpServletRequest request)
	{
		String host;
		if (StringUtils.isEmpty(request.getHeader("X-Forwarded-Host")))
		{
			host = request.getScheme() + "://" + request.getServerName() + ":" + request.getLocalPort();
		}
		else
		{
			host = request.getScheme() + "://" + request.getHeader("X-Forwarded-Host");
		}
		return format("{0}{1}/result/{2}", host, menuReaderService.getMenu().findMenuItemPath(GAVIN_APP),
				jobIdentifier);
	}

	/**
	 * Downloads the result of a gavin annotation job.
	 *
	 * @param response      {@link HttpServletResponse} to write the Content-Disposition header to with the filename
	 * @param jobIdentifier GAVIN_APP of the annotation job
	 * @return {@link FileSystemResource} with the annotated file
	 */
	@GetMapping(value = "/download/{jobIdentifier}", produces = APPLICATION_OCTET_STREAM_VALUE)
	@ResponseBody
	public FileSystemResource download(HttpServletResponse response,
			@PathVariable(value = "jobIdentifier") String jobIdentifier)
			throws FileNotFoundException, JobNotFoundException
	{
		GavinJobExecution jobExecution = gavinJobFactory.findGavinJobExecution(jobIdentifier);
		File file = getDownloadFileForJob(jobIdentifier);
		if (!file.exists())
		{
			LOG.warn("No result file found for job {}", jobIdentifier);
			throw new FileNotFoundException("No result file found for this job. Results are removed every night.");
		}
		response.setHeader("Content-Disposition",
				format("inline; filename=\"{0}-gavin.vcf\"", jobExecution.getFilename()));
		return new FileSystemResource(file);
	}

	private File getDownloadFileForJob(String jobIdentifier)
	{
		return fileStore.getFile(GAVIN_APP + separator + jobIdentifier + separator + "gavin-result.vcf");
	}

	private File getErrorFileForJob(String jobIdentifier)
	{
		return fileStore.getFile(GAVIN_APP + separator + jobIdentifier + separator + "error.txt");
	}

	/**
	 * Downloads the error report of a gavin annotation job.
	 *
	 * @param response      {@link HttpServletResponse} to write the Content-Disposition header to with the filename
	 * @param jobIdentifier GAVIN_APP of the annotation job
	 * @return {@link FileSystemResource} with the annotated file
	 */
	@GetMapping(value = "/error/{jobIdentifier}", produces = APPLICATION_OCTET_STREAM_VALUE)
	@ResponseBody
	public Resource downloadErrorReport(HttpServletResponse response,
			@PathVariable(value = "jobIdentifier") String jobIdentifier)
			throws FileNotFoundException, JobNotFoundException
	{
		GavinJobExecution jobExecution = gavinJobFactory.findGavinJobExecution(jobIdentifier);
		response.setHeader("Content-Disposition",
				format("inline; filename=\"{0}-error.txt\"", jobExecution.getFilename()));
		final File file = getErrorFileForJob(jobIdentifier);
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

	@ExceptionHandler(value = JobNotFoundException.class)
	public void handleJobNotFound(JobNotFoundException ex, HttpServletResponse res) throws IOException
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
	 * Removes old files in the gavin working directory from the file store.
	 */
	@Scheduled(cron = "0 0 * * * *")
	public void cleanUp()
	{
		LOG.debug("Clean up old jobs in the file store...");
		try
		{
			final File[] oldFiles = fileStore.getFile(GAVIN_APP)
											 .listFiles(file -> file.isDirectory()
													 && MILLISECONDS.toSeconds(file.lastModified()) < now().minusHours(
													 24).toEpochSecond());
			if (oldFiles != null)
			{
				for (File file : oldFiles)
				{
					LOG.info("Deleting job directory {}", file.getName());
					fileStore.deleteDirectory(GAVIN_APP + separator + file.getName());
				}
			}
			LOG.debug("Done.");
		}
		catch (IOException e)
		{
			LOG.error("Failed to clean up working directory", e);
		}
	}
}

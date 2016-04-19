package org.molgenis.gavin.controller;

import static java.io.File.separator;
import static java.net.URLConnection.guessContentTypeFromName;
import static org.molgenis.gavin.controller.GavinController.URI;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutorService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.molgenis.data.DataService;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.file.FileStore;
import org.molgenis.gavin.job.GavinJob;
import org.molgenis.gavin.job.GavinJobExecution;
import org.molgenis.gavin.job.GavinJobFactory;
import org.molgenis.security.user.UserAccountService;
import org.molgenis.util.ErrorMessageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping(URI)
public class GavinController
{
	private static final Logger LOG = LoggerFactory.getLogger(GavinController.class);

	public static final String URI = "/gavin";

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

		// TODO Do this with mkDirs()
		fileStore.createDirectory("gavin");
		fileStore.createDirectory("gavin/" + gavinJobIdentifier);
		fileStore.store(inputFile.getInputStream(), fileName);

		executorService.submit(gavinJob);

		return "api/v2/GavinJobExecution/" + gavinJobIdentifier;
	}

	@RequestMapping(value = "/annotate-text-input", method = POST)
	@ResponseBody
	public String annotateTextInput(HttpServletRequest request, @RequestParam(value = "text") String text)
			throws IOException
	{
		GavinJobExecution gavinJobExecution = new GavinJobExecution(dataService);
		String fileName = "gavin/" + gavinJobExecution.getIdentifier() + "/input.vcf";

		File inputFile = writeInputTextToFile(text, fileName);
		fileStore.store(new FileInputStream(inputFile), fileName);

		GavinJob gavinJob = gavinJobFactory.createJob(gavinJobExecution);
		executorService.submit(gavinJob);

		return "api/v2/GavinJobExecution/" + gavinJobExecution.getIdentifier();
	}

	private File writeInputTextToFile(String text, String fileName) throws IOException
	{
		File inputFile = new File(fileName);
		BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(inputFile));
		bufferedWriter.write(text);
		bufferedWriter.close();
		return inputFile;
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

	@ExceptionHandler(IllegalArgumentException.class)
	@ResponseBody
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ErrorMessageResponse handleRuntimeException(RuntimeException e)
	{
		LOG.warn(e.getMessage());
		return new ErrorMessageResponse(new ErrorMessageResponse.ErrorMessage(e.getMessage()));
	}
}

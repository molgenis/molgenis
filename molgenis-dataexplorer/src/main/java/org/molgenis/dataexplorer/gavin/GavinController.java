package org.molgenis.dataexplorer.gavin;

import static org.molgenis.dataexplorer.controller.AnnotatorController.URI;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutorService;

import javax.servlet.http.HttpServletRequest;

import org.molgenis.data.DataService;
import org.molgenis.file.FileStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping(URI)
public class GavinController
{
	public static final String ID = "gavin";

	@Autowired
	DataService dataService;

	@Autowired
	ExecutorService executorService;

	@Autowired
	GavinJobFactory gavinJobFactory;

	@Autowired
	FileStore fileStore;

	@RequestMapping(value = "/file-annotator", method = RequestMethod.POST)
	@ResponseBody
	public String fileAnnotator(HttpServletRequest request, @RequestParam(value = "file") MultipartFile inputFile)
			throws IOException, URISyntaxException
	{
		GavinJobExecution gavinJobExecution = new GavinJobExecution(dataService);
		String fileName = "gavin/" + gavinJobExecution.getIdentifier() + "/input.vcf";
		fileStore.store(inputFile.getInputStream(), fileName);

		GavinJob gavinJob = gavinJobFactory.createJob(gavinJobExecution);
		executorService.submit(gavinJob);

		return "api/v2/JobExecution/" + gavinJobExecution.getIdentifier();
	}

	@RequestMapping(value = "/result/{jobIdentifier}")
	public File result()
	{
		// TODO use the jobIdentifier to get the gavin/{jobIdentifier}/gavin-result.vcf and return it
		return new File("test");
	}
}

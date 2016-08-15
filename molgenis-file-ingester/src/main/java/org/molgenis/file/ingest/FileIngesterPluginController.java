package org.molgenis.file.ingest;

import org.molgenis.ui.MolgenisPluginController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Controller
@RequestMapping(FileIngesterPluginController.URI)
public class FileIngesterPluginController extends MolgenisPluginController
{
	public static final String ID = "fileingest";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;
	private final FileIngesterJobScheduler scheduler;

	@Autowired
	public FileIngesterPluginController(FileIngesterJobScheduler scheduler)
	{
		super(URI);
		this.scheduler = scheduler;
	}

	@RequestMapping(method = GET)
	public String init()
	{
		return "view-file-ingest";
	}

	@RequestMapping(value = "/run/{fileIngestId}", method = POST)
	@ResponseStatus(NO_CONTENT)
	public void runNow(@PathVariable("fileIngestId") String fileIngestId)
	{
		scheduler.runNow(fileIngestId);
	}
}

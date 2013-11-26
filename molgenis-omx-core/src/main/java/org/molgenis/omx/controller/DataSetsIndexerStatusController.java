package org.molgenis.omx.controller;

import static org.molgenis.omx.controller.DataSetsIndexerStatusController.URI;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import org.molgenis.omx.search.DataSetsIndexer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Controller class for the data explorer.
 * 
 * @author Jonathan
 * 
 */
@Controller
@RequestMapping(URI)
public class DataSetsIndexerStatusController
{
	public static final String URI = "/dataindexerstatus";

	public DataSetsIndexerStatusController(){}

	@Autowired
	private DataSetsIndexer dataSetsIndexer;
	
	@RequestMapping(method = RequestMethod.GET, produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public DataSetIndexStatusResponse statusMessage() {
		if (dataSetsIndexer.isIndexingRunning())
		{
			return new DataSetIndexStatusResponse(true, "warning", "Indexer is running <i class=\"icon-refresh\"></i> functionality is not fully active!");
		} 
		else
		{
			return new DataSetIndexStatusResponse(false, "success", "Indexing is finished <i class=\"icon-ok\"></i>");
		}
	}
	
	static class DataSetIndexStatusResponse
	{
		private final boolean isRunning;
		private final String message;
		private final String type;

		public DataSetIndexStatusResponse(boolean isRunning, String type, String message)
		{
			this.isRunning = isRunning;
			this.type = type;
			this.message = message;
		}

		public boolean isRunning()
		{
			return isRunning;
		}

		public String getMessage()
		{
			return message;
		}
		
		public String getType()
		{
			return type;
		}
	}
}
package org.molgenis.data.elasticsearch.admin;

import org.molgenis.ui.MolgenisPluginController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import static org.molgenis.data.elasticsearch.admin.ElasticsearchIndexManagerController.URI;

/**
 * Index manager plugin
 */
@Controller
@RequestMapping(URI)
public class ElasticsearchIndexManagerController extends MolgenisPluginController
{
	public static final String ID = "indexmanager";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;

	@Autowired
	private ElasticsearchIndexManagerService elasticsearchIndexManagerService;

	public ElasticsearchIndexManagerController()
	{
		super(URI);
	}

	@RequestMapping(method = RequestMethod.GET)
	public String init(Model model)
	{
		model.addAttribute("entities", elasticsearchIndexManagerService.getIndexedEntities());
		return "view-indexmanager";
	}

	@RequestMapping(value = "/reindex", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.OK)
	public void reindexType(@Valid @ModelAttribute ReindexRequest reindexRequest)
	{
		elasticsearchIndexManagerService.rebuildIndex(reindexRequest.getType());
	}

	private static class ReindexRequest
	{
		@NotNull
		private String type;

		public String getType()
		{
			return type;
		}

		@SuppressWarnings("unused")
		public void setType(String type)
		{
			this.type = type;
		}
	}
}

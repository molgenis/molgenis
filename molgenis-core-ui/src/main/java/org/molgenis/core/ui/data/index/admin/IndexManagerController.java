package org.molgenis.core.ui.data.index.admin;

import org.molgenis.web.PluginController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import static org.molgenis.core.ui.data.index.admin.IndexManagerController.URI;

/**
 * Index manager plugin
 */
@Controller
@RequestMapping(URI)
public class IndexManagerController extends PluginController
{
	public static final String ID = "indexmanager";
	public static final String URI = PluginController.PLUGIN_URI_PREFIX + ID;

	@Autowired
	private IndexManagerService indexManagerService;

	public IndexManagerController()
	{
		super(URI);
	}

	@GetMapping
	public String init(Model model)
	{
		model.addAttribute("entities", indexManagerService.getIndexedEntities());
		return "view-indexmanager";
	}

	@PostMapping("/reindex")
	@ResponseStatus(value = HttpStatus.OK)
	public void reindexType(@Valid @ModelAttribute ReindexRequest reindexRequest)
	{
		indexManagerService.rebuildIndex(reindexRequest.getType());
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

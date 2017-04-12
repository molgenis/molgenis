package org.molgenis.dataexplorer.freemarker;

import freemarker.core.Environment;
import freemarker.template.*;
import org.molgenis.data.DataConverter;
import org.molgenis.data.DataService;
import org.molgenis.dataexplorer.controller.DataExplorerController;
import org.molgenis.framework.ui.MolgenisPlugin;
import org.molgenis.framework.ui.MolgenisPluginRegistry;

import java.io.IOException;
import java.io.Writer;
import java.net.URLEncoder;
import java.util.Map;

/**
 * Creates a link to the dataexplorer for a dataset
 * <p>
 * usage: <@dataExplorerLink dataset='celiacsprue' class='btn'>Explore data</@dataExplorerLink>
 * <p>
 * created html: <a href='/menu/main/dataexplorer' class='btn'>Explore data</a>
 */
public class DataExplorerHyperlinkDirective implements TemplateDirectiveModel
{
	private final MolgenisPluginRegistry molgenisPluginRegistry;
	private final DataService dataService;

	public DataExplorerHyperlinkDirective(MolgenisPluginRegistry molgenisPluginRegistry, DataService dataService)
	{
		this.molgenisPluginRegistry = molgenisPluginRegistry;
		this.dataService = dataService;
	}

	@Override
	public void execute(Environment env, @SuppressWarnings("rawtypes") Map params, TemplateModel[] loopVars,
			TemplateDirectiveBody body) throws TemplateException, IOException
	{
		if (!params.containsKey("entityTypeId")) throw new TemplateModelException("Missing 'entityTypeId' parameter");
		String dataset = DataConverter.toString(params.get("entityTypeId"));
		MolgenisPlugin dataexplorer = molgenisPluginRegistry.getPlugin(DataExplorerController.ID);
		Writer w = env.getOut();

		if (dataService.hasRepository(dataset) && (dataexplorer != null))
		{
			String cssClass = DataConverter.toString(params.get("class"));
			String dataexplorerPageUri = String
					.format("%s?entity=%s", dataexplorer.getFullUri(), URLEncoder.encode(dataset, "UTF-8"));

			w.write("<a href='");
			w.write(dataexplorerPageUri);
			w.write("' ");

			if (cssClass != null)
			{
				w.write("class='" + cssClass + "' ");
			}
			w.write(">");
			body.render(w);
			w.write("</a>");
		}
		else if (params.containsKey("alternativeText"))
		{
			w.write(DataConverter.toString(params.get("alternativeText")));
		}
	}
}

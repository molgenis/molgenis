package org.molgenis.dataexplorer.freemarker;

import com.google.common.collect.Maps;
import freemarker.core.Environment;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import org.molgenis.data.DataService;
import org.molgenis.dataexplorer.controller.DataExplorerController;
import org.molgenis.framework.ui.MolgenisPlugin;
import org.molgenis.framework.ui.MolgenisPluginRegistry;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

public class DataExplorerHyperlinkDirectiveTest
{
	private DataService dataService;
	private DataExplorerHyperlinkDirective directive;
	private StringWriter envWriter;
	private Template fakeTemplate;

	@BeforeMethod
	public void setUp()
	{
		MolgenisPluginRegistry mpr = mock(MolgenisPluginRegistry.class);
		when(mpr.getPlugin(DataExplorerController.ID)).thenReturn(
				new MolgenisPlugin("dataex", "dataex", "dataex", "/menu/data/dataex"));

		dataService = mock(DataService.class);
		when(dataService.hasRepository("thedataset")).thenReturn(true);

		directive = new DataExplorerHyperlinkDirective(mpr, dataService);
		envWriter = new StringWriter();
		fakeTemplate = Template.getPlainTextTemplate("name", "content",
				new Configuration(Configuration.VERSION_2_3_21));
	}

	@Test
	public void execute() throws TemplateException, IOException
	{
		when(dataService.hasRepository("thedataset")).thenReturn(true);

		Map<String, String> params = Maps.newHashMap();
		params.put("entityTypeId", "thedataset");
		params.put("class", "class1 class2");

		directive.execute(new Environment(fakeTemplate, null, envWriter), params, new TemplateModel[0],
				out -> out.write("explore data"));

		assertEquals(envWriter.toString(),
				"<a href='/menu/data/dataex?entity=thedataset' class='class1 class2' >explore data</a>");
	}

	@Test
	public void executeWithMissingDataset() throws TemplateException, IOException
	{
		when(dataService.hasRepository("thedataset")).thenReturn(false);

		Map<String, String> params = Maps.newHashMap();
		params.put("entityTypeId", "thedataset");
		params.put("class", "class1 class2");
		params.put("alternativeText", "alt");

		directive.execute(new Environment(fakeTemplate, null, envWriter), params, new TemplateModel[0],
				out -> out.write("explore data"));

		assertEquals(envWriter.toString(), "alt");
	}
}

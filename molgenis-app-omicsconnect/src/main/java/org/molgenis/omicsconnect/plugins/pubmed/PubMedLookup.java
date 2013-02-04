/* Date:        February 2, 2010
 * Template:	PluginScreenJavaTemplateGen.java.ftl
 * generator:   org.molgenis.generators.ui.PluginScreenJavaTemplateGen 3.3.2-testing
 * 
 * THIS FILE IS A TEMPLATE. PLEASE EDIT :-)
 */

package org.molgenis.omicsconnect.plugins.pubmed;

import java.util.HashMap;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.server.MolgenisRequest;
import org.molgenis.framework.ui.PluginModel;
import org.molgenis.framework.ui.ScreenController;
import org.molgenis.framework.ui.ScreenMessage;
import org.molgenis.util.Entity;

import com.google.gson.JsonObject;

/**
 * Shows table of experiment information for WormQTL
 */
public class PubMedLookup extends PluginModel<Entity>
{

	private static final long serialVersionUID = 1L;

	private final PubMedLookupModel model = new PubMedLookupModel();

	public PubMedLookupModel getMyModel()
	{
		return model;
	}

	public PubMedLookup(String name, ScreenController<?> parent)
	{
		super(name, parent);
	}

	@Override
	public String getViewName()
	{
		return "PubMedTest";
	}

	@Override
	public String getViewTemplate()
	{
		return "templates/org/molgenis/omicsconnect/plugins/pubmed/PubMedLookup.ftl";
	}

	@Override
	public void handleRequest(Database db, MolgenisRequest request)
	{
		if (request.getString("__action") != null)
		{
			String action = request.getString("__action");
			String pubmedFromUser = request.getString("Pubmed");

			try
			{

				if (action.equals("query"))
				{

					model.setBaseURL("http://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?");
					model.setDb("pubmed");
					model.setFormat("xml");
					model.setId(pubmedFromUser);

					// no abstract 21904316
					// abstract 22457343

					HashMap<String, String> map = model.PubMedData();

					System.out.println("TEST::::::::::::::::>>>>>>>>" + map.toString());

					model.setPubmed(map);

					JsonObject json = model.getJson();
					model.setJson(json);

					System.out.println("GET::::::::::::::::>>>>>>>>" + json.toString());
				}

			}

			catch (Exception e)
			{
				e.printStackTrace();
				this.setMessages(new ScreenMessage(e.getMessage() != null ? e.getMessage() : "null", false));
			}
		}
	}

	@Override
	public void reload(Database db)
	{

	}

}

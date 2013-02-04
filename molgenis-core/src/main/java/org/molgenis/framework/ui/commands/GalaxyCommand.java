package org.molgenis.framework.ui.commands;

//import java.io.PrintWriter;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.server.MolgenisRequest;
import org.molgenis.framework.ui.ScreenController;
import org.molgenis.framework.ui.ScreenModel;
import org.molgenis.framework.ui.html.ActionInput;
import org.molgenis.framework.ui.html.HtmlInput;

/**
 * This command returns JavaScript code to download all records in CSV format.
 * 
 * @param <E>
 */
public class GalaxyCommand extends SimpleCommand
{
	public GalaxyCommand(String name, ScreenController<?> parentController)
	{
		super(name, parentController);
		if (this.getName() == "send_all_to_galaxy")
		{
			this.setLabel("Send All to Galaxy");
		}
		else
		{
			this.setLabel("Send Selected to Galaxy");
		}
		this.setIcon("img/upload2galaxy.png");
		this.setMenu("File");
	}

	// TODO: Get proper UID.
	private static final long serialVersionUID = 1L;
	private String appLoc;

	@Override
	public String getJavaScriptAction()
	{

		StringBuffer jScript = new StringBuffer();
		jScript.append("");

		if (this.getController().getApplicationController().getGalaxyUrl() != null)
		{

			String galaxy_url = this.getController().getApplicationController().getGalaxyUrl();
			String molgenis_site = appLoc;
			String molgenis_download_all = molgenis_site + "/molgenis.do?__target=" + this.getController().getName()
					+ "&__action=download_txt_all&__show=download";
			String molgenis_download_selected = molgenis_site + "/molgenis.do?__target="
					+ this.getController().getName() + "&__action=download_txt_selected&__show=download";

			jScript.append("var form = document.createElement('form');");
			jScript.append("form.setAttribute('method', 'post');");
			jScript.append("form.setAttribute('action', '" + galaxy_url + "');");

			jScript.append("var hiddenField = document.createElement('input');");
			jScript.append("hiddenField.setAttribute('type', 'hidden');");
			jScript.append("hiddenField.setAttribute('name', 'URL');");
			if (this.getName() == "send_all_to_galaxy")
			{
				jScript.append("hiddenField.setAttribute('value', '" + molgenis_download_all + "');");
			}
			else
			{
				jScript.append("hiddenField.setAttribute('value', '" + molgenis_download_selected + "');");
			}
			jScript.append("hiddenField.setAttribute('value', '" + molgenis_download_all + "');");
			jScript.append("form.appendChild(hiddenField);");

			jScript.append("document.body.appendChild(form);");
			jScript.append("form.submit();");

		}

		return jScript.toString();

	}

	@Override
	public List<HtmlInput<?>> getInputs() throws DatabaseException
	{
		return null;
	}

	@Override
	public List<ActionInput> getActions()
	{
		return new ArrayList<ActionInput>();
	}

	@Override
	public ScreenModel.Show handleRequest(Database db, MolgenisRequest request, OutputStream downloadStream)
	{
		logger.debug("galaxy button clicked: " + this.getController().getApplicationController().getGalaxyUrl());
		appLoc = request.getAppLocation();
		return ScreenModel.Show.SHOW_MAIN;
	}

	@Override
	public boolean isVisible()
	{
		// Show this menu item only if the user navigated to Molgenis from a
		// Galaxy server.
		if (this.getController().getApplicationController().getGalaxyUrl() != null)
		{
			return true;
		}
		return false;
	}
}
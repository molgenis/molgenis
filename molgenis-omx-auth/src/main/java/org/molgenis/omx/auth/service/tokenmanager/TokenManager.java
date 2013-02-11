/* Date:        February 2, 2010
 * Template:	PluginScreenJavaTemplateGen.java.ftl
 * generator:   org.molgenis.generators.ui.PluginScreenJavaTemplateGen 3.3.2-testing
 * 
 * THIS FILE IS A TEMPLATE. PLEASE EDIT :-)
 */

package org.molgenis.omx.auth.service.tokenmanager;

import java.util.Calendar;
import java.util.Date;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.security.SimpleLogin;
import org.molgenis.framework.server.MolgenisRequest;
import org.molgenis.framework.ui.PluginModel;
import org.molgenis.framework.ui.ScreenController;
import org.molgenis.framework.ui.ScreenMessage;

public class TokenManager extends PluginModel
{
	private final TokenManagerModel model = new TokenManagerModel();

	public TokenManagerModel getMyModel()
	{
		return model;
	}

	public TokenManager(String name, ScreenController<?> parent)
	{
		super(name, parent);
	}

	@Override
	public String getViewName()
	{
		return "TokenManager";
	}

	@Override
	public String getViewTemplate()
	{
		return "templates/" + TokenManager.class.getName().replace('.', '/') + ".ftl";
	}

	@Override
	public void handleRequest(Database db, MolgenisRequest request)
	{
		if (request.getString("__action") != null)
		{

			System.out.println("*** handleRequest __action: " + request.getString("__action"));
			String action = request.getString("__action");

			try
			{
				if (action.equals("createToken"))
				{
					int nrOfTokens = request.getInt("amountOfTokens");
					int nrOfDays = request.getInt("amountOfDaysValid");
					int nrOfHours = request.getInt("amountOfHoursValid");

					if (nrOfDays >= 0 && nrOfDays <= 6 && nrOfHours >= 1 && nrOfHours <= 24 && nrOfTokens >= 1
							&& nrOfTokens <= 10)
					{
						Calendar cal = Calendar.getInstance();
						cal.add(Calendar.DATE, nrOfDays);
						cal.add(Calendar.HOUR, nrOfHours);
						Date validUntil = cal.getTime();
						String userName = db.getLogin().getUserName();

						for (int i = 0; i < nrOfTokens; i++)
						{
							this.getTokenFactory().makeNewToken(userName, validUntil);
						}
					}
					else
					{
						throw new Exception("BAD REQUEST: number of days or hours not allowed!");
					}

				}
				if (action.startsWith("deleteToken_"))
				{
					String uuid = action.substring("deleteToken_".length());
					if (this.getTokenFactory().checkIfTokenExists(uuid))
					{
						this.getTokenFactory().removeToken(uuid);
					}
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

		try
		{
			String user = db.getLogin().getUserName();
			this.model.setTokens(this.getTokenFactory().getAllTokens(user));

		}
		catch (Exception e)
		{
			e.printStackTrace();
			this.setMessages(new ScreenMessage(e.getMessage() != null ? e.getMessage() : "null", false));
		}

	}

	@Override
	public boolean isVisible()
	{
		if (this.getController().getApplicationController().getLogin() instanceof SimpleLogin)
		{
			return false;
		}
		return super.isVisible();
	}

}

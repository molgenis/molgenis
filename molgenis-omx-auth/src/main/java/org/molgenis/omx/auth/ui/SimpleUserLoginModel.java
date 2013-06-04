/* Date:        May 6, 2011
 * Template:	NewPluginModelGen.java.ftl
 * generator:   org.molgenis.generators.ui.NewPluginModelGen 3.3.3
 * 
 * THIS FILE IS A TEMPLATE. PLEASE EDIT :-)
 */

package org.molgenis.omx.auth.ui;

import org.molgenis.framework.security.SimpleLogin;
import org.molgenis.framework.ui.EasyPluginModel;
import org.molgenis.framework.ui.html.Container;

public class SimpleUserLoginModel extends EasyPluginModel
{
	private static final long serialVersionUID = 1L;

	private String mailCurator;
	private String action = "init";
	private Container authenticationForm = new Container();
	private Container userAreaForm = new Container();
	private Container forgotForm = new Container();

	public SimpleUserLoginModel(SimpleUserLogin controller)
	{
		super(controller);
	}

	public String getMailCurator()
	{
		return mailCurator;
	}

	public void setMailCurator(String mailCurator)
	{
		this.mailCurator = mailCurator;
	}

	public String getAction()
	{
		return action;
	}

	public void setAction(String action)
	{
		this.action = action;
	}

	public Container getAuthenticationForm()
	{
		return authenticationForm;
	}

	public void setAuthenticationForm(Container authenticationForm)
	{
		this.authenticationForm = authenticationForm;
	}

	public Container getUserAreaForm()
	{
		return userAreaForm;
	}

	public void setUserAreaForm(Container userAreaForm)
	{
		this.userAreaForm = userAreaForm;
	}

	public void setForgotForm(Container forgotForm)
	{
		this.forgotForm = forgotForm;
	}

	public Container getForgotForm()
	{
		return forgotForm;
	}

	@Override
	public String getLabel()
	{
		if (!this.getController().getApplicationController().getLogin().isAuthenticated())
		{
			return "Login";
		}
		return super.getLabel();
	}

	@Override
	public boolean isVisible()
	{
		if (this.getController().getApplicationController().getLogin() instanceof SimpleLogin) return false;
		if (this.getController().getApplicationController().getMolgenisContext().getUsedOptions().getAuthUseDialog()) return false;
		return true;
	}
}

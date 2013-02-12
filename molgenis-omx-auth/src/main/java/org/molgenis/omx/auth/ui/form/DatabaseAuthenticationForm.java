package org.molgenis.omx.auth.ui.form;

import org.molgenis.framework.ui.html.ActionInput;
import org.molgenis.framework.ui.html.Container;
import org.molgenis.framework.ui.html.PasswordInput;
import org.molgenis.framework.ui.html.StringInput;

public class DatabaseAuthenticationForm extends Container
{

	private static final long serialVersionUID = 798533925465868923L;

	public DatabaseAuthenticationForm()
	{
		StringInput usernameInput = new StringInput("username");
		usernameInput.setNillable(false);
		usernameInput.setDescription("The name of a registered user.");
		this.add(usernameInput);
		PasswordInput passwordInput = new PasswordInput("password");
		passwordInput.setNillable(false);
		passwordInput.setDescription("The password of a registered user."); // FIXME:
																			// does
																			// not
																			// show
		this.add(passwordInput);
		ActionInput loginInput = new ActionInput("Login");
		loginInput.setTooltip("Login");
		this.add(loginInput);
		ActionInput logoutInput = new ActionInput("Logout");
		logoutInput.setTooltip("Logout");
		this.add(logoutInput);
	}
}

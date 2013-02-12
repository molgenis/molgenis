package org.molgenis.omx.auth.ui.form;

import org.molgenis.framework.ui.html.ActionInput;
import org.molgenis.framework.ui.html.Container;
import org.molgenis.framework.ui.html.TextLineInput;

public class ForgotForm extends Container
{

	private static final long serialVersionUID = -5681991905559537806L;

	public ForgotForm()
	{
		TextLineInput usernameInput = new TextLineInput("username");
		usernameInput.setLabel("Username");
		this.add(usernameInput);
		ActionInput addUserInput = new ActionInput("sendPassword");
		addUserInput.setLabel("Send new password");
		addUserInput.setTooltip("Send new password");
		this.add(addUserInput);
		ActionInput cancelInput = new ActionInput("Cancel");
		cancelInput.setLabel("Cancel");
		cancelInput.setTooltip("Cancel");
		this.add(cancelInput);
	}
}
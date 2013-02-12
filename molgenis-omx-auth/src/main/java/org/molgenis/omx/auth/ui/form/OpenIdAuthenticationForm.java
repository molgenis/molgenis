package org.molgenis.omx.auth.ui.form;

import org.molgenis.framework.ui.html.ActionInput;

public class OpenIdAuthenticationForm extends DatabaseAuthenticationForm
{

	private static final long serialVersionUID = 5869739460333081182L;

	public OpenIdAuthenticationForm()
	{
		super();
		ActionInput googleInput = new ActionInput("google");
		googleInput.setIcon("res/img/google.png");
		this.add(googleInput);
		ActionInput yahooInput = new ActionInput("yahoo");
		yahooInput.setIcon("res/img/yahoo.png");
		this.add(yahooInput);
	}
}

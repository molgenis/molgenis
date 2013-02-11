/* Date:        May 6, 2011
 * Template:	NewPluginModelGen.java.ftl
 * generator:   org.molgenis.generators.ui.NewPluginModelGen 3.3.3
 * 
 * THIS FILE IS A TEMPLATE. PLEASE EDIT :-)
 */

package org.molgenis.omx.auth.ui;

import org.molgenis.framework.ui.html.Container;

public class UserLoginModel extends SimpleUserLoginModel
{
	private static final long serialVersionUID = 1L;

	private Container registrationForm = new Container();

	public UserLoginModel(UserLogin controller)
	{
		super(controller);
	}

	public Container getRegistrationForm()
	{
		return registrationForm;
	}

	public void setRegistrationForm(Container registrationForm)
	{
		this.registrationForm = registrationForm;
	}
}

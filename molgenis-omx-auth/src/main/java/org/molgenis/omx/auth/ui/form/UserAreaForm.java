package org.molgenis.omx.auth.ui.form;

import org.molgenis.framework.ui.html.ActionInput;
import org.molgenis.framework.ui.html.Container;
import org.molgenis.framework.ui.html.PasswordInput;
import org.molgenis.framework.ui.html.TablePanel;
import org.molgenis.framework.ui.html.TextLineInput;

public class UserAreaForm extends Container
{

	private static final long serialVersionUID = 763688886819665003L;

	public UserAreaForm()
	{
		TablePanel passwordGroup = new TablePanel("login", "login");
		PasswordInput oldPwdInput = new PasswordInput("oldpwd");
		oldPwdInput.setLabel("Old password");
		passwordGroup.add(oldPwdInput);
		PasswordInput newPwdInput = new PasswordInput("newpwd");
		newPwdInput.setLabel("New password");
		passwordGroup.add(newPwdInput);
		PasswordInput newPwdInput2 = new PasswordInput("newpwd2");
		newPwdInput2.setLabel("Repeat new password");
		passwordGroup.add(newPwdInput2);
		this.add(passwordGroup);

		TablePanel addressGroup = new TablePanel("personal", "personal");
		TextLineInput email = new TextLineInput("emailaddress");
		email.setNillable(false);
		email.setLabel("Email");
		addressGroup.add(email);

		TextLineInput phone = new TextLineInput("phone");
		phone.setLabel("Phone");
		addressGroup.add(phone);
		TextLineInput fax = new TextLineInput("fax");
		fax.setLabel("Fax");
		addressGroup.add(fax);
		TextLineInput tollFreePhone = new TextLineInput("tollFreePhone");
		tollFreePhone.setLabel("TollFreePhone");
		addressGroup.add(tollFreePhone);
		TextLineInput address = new TextLineInput("address");
		address.setLabel("Address");
		addressGroup.add(address);

		TextLineInput title = new TextLineInput("title");
		title.setLabel("Title");
		addressGroup.add(title);
		TextLineInput firstname = new TextLineInput("firstname");
		firstname.setNillable(false);
		firstname.setLabel("First name");
		addressGroup.add(firstname);
		TextLineInput lastname = new TextLineInput("lastname");
		lastname.setNillable(false);
		lastname.setLabel("Last name");
		addressGroup.add(lastname);
		TextLineInput institute = new TextLineInput("institute");
		institute.setLabel("Institute");
		addressGroup.add(institute);
		TextLineInput department = new TextLineInput("department");
		department.setLabel("Department");
		addressGroup.add(department);
		TextLineInput position = new TextLineInput("position");
		position.setLabel("Position");
		addressGroup.add(position);
		TextLineInput city = new TextLineInput("city");
		city.setLabel("City");
		addressGroup.add(city);
		TextLineInput country = new TextLineInput("country");
		country.setLabel("Country");
		addressGroup.add(country);
		this.add(addressGroup);

		ActionInput changePasswordInput = new ActionInput("ChgUser", "Apply changes");
		changePasswordInput.setTooltip("Apply changes");
		this.add(changePasswordInput);
	}
}
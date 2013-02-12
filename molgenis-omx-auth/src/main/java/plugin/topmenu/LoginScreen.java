/* Date:        August 2, 2009
 * Template:	PluginScreenJavaTemplateGen.java.ftl
 * generator:   org.molgenis.generators.screen.PluginScreenJavaTemplateGen 3.3.0-testing
 * 
 * THIS FILE IS A TEMPLATE. PLEASE EDIT :-)
 */

package plugin.topmenu;

import java.util.List;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.server.MolgenisRequest;
import org.molgenis.framework.ui.PluginModel;
import org.molgenis.framework.ui.ScreenController;
import org.molgenis.framework.ui.ScreenMessage;
import org.molgenis.framework.ui.html.ActionInput;
import org.molgenis.framework.ui.html.CommandTemplate;
import org.molgenis.framework.ui.html.HtmlInput;
import org.molgenis.framework.ui.html.PasswordInput;
import org.molgenis.framework.ui.html.StringInput;
import org.molgenis.util.Entity;
import org.molgenis.util.HandleRequestDelegationException;

//import plugin.login.DatabaseLogin;

public class LoginScreen extends PluginModel<Entity>
{

	private static final long serialVersionUID = 4180722803810720253L;

	public enum State
	{
		Signup, Register, Activate, Login, Edit_profile, Save_profile, Logout, Forgot_your_password, Cancel
	};

	State state = State.Login;

	public LoginScreen(String name, ScreenController<?> parent)
	{
		super(name, parent);
	}

	@Override
	public String getViewName()
	{
		return "plugin_topmenu_LoginScreen";
	}

	@Override
	public String getViewTemplate()
	{
		return "plugin/topmenu/LoginScreen.ftl";
	}

	@Override
	public void handleRequest(Database db, MolgenisRequest request) throws HandleRequestDelegationException, Exception
	{
		// reset messages
		this.setMessages();

		// convert action to enum value
		State action = State.valueOf(request.getAction().replace(" ", "_"));

		// workflow:
		// login -> logout -> edit_profile -> save_profile -> logout
		// -> email_password -> login
		// -> register -> login
		switch (action)
		{
			case Login:
				doLogin(db, request);
				break;

			case Activate:
				doActivate(db, request);
				this.state = State.Logout;

			case Logout:
				doLogout(db, request);
				break;

			case Forgot_your_password:
				doEmailPassword(db, request);
				break;

			case Signup:
				this.state = State.Register;
				break;

			case Register:
				doRegister(db, request);
				break;

			case Edit_profile:
				this.state = State.Save_profile;
				break;

			case Save_profile:
				doSaveProfile(db, request);
				break;

			case Cancel:
			default:
				this.state = getLogin().isAuthenticated() ? State.Logout : State.Login;

		}

		// todo: add jcaptcha service
		// todo add password change options
	}

	private void doActivate(Database db, MolgenisRequest request)
	{
		this.setMessages(new ScreenMessage("TODO", false));

	}

	private void doSaveProfile(Database db, MolgenisRequest request)
	{
		// TODO: Where is MolgenisUser?
		/*
		 * // check password // change emailadress only after verification
		 * MolgenisUser user = ((DatabaseLogin) getLogin()).getMolgenisUser();
		 * user.setEmailaddress(request.getString("emailaddress"));
		 * user.setPassword(request.getString("password")); try {
		 * db.update(user); this.setMessages(new
		 * ScreenMessage("Profile updated", true)); } catch (Exception e) {
		 * this.setMessages(new
		 * ScreenMessage("Update of profile failed failed: " + e.getMessage(),
		 * false)); e.printStackTrace(); }
		 */
	}

	private void doRegister(Database db, MolgenisRequest request)
	{
		// TODO: Where is MolgenisUser?
		/*
		 * // TODO: check password try { String user =
		 * request.getString("name"); String email =
		 * request.getString("emailaddress"); String password =
		 * request.getString("password");
		 * 
		 * // check if username or email exists boolean userExists =
		 * db.query(MolgenisUser.class).equals("name", user).find().size() > 0;
		 * boolean emailExists =
		 * db.query(MolgenisUser.class).equals("emailaddress",
		 * email).find().size() > 0;
		 * 
		 * if (userExists || emailExists) { String errorMessage = "User with";
		 * if (userExists) errorMessage += " name " + user; if (userExists &&
		 * emailExists) errorMessage += " and "; if (emailExists) errorMessage
		 * += " emailadress " + email; throw new DatabaseException(errorMessage
		 * + " already exists."); }
		 * 
		 * // todo: fire up the emailing protocol or queue or something...
		 * //fixme: automate String baseUrl =
		 * "http://localhost:8080/molgenis_online/molgenis.do?__target="
		 * +this.getName()+"&select="+this.getName(); String activationCode =
		 * UUID.randomUUID().toString(); String url = baseUrl+
		 * "&__action=Activate&__activationCode=" + activationCode; String
		 * emailBody =
		 * "Dear "+user+",\n We welcome you as new user to "+this.getRootScreen
		 * ().getLabel()+". Please click on <a href=\"" + url + "\">" + url +
		 * "</a> to activate your account. Notice that this hyperlink may be spread over multiple lines.\n\nRegards,\nthe system administrators."
		 * ; this.getEmailService().email("Account activation MOLGENIS",
		 * emailBody, email, true);
		 * 
		 * // finally add user including activiation code MolgenisUser newUser =
		 * new MolgenisUser(); newUser.setName(user);
		 * newUser.setEmailaddress(email); newUser.setPassword(password);
		 * newUser.setActivationCode(activationCode);
		 * 
		 * db.add(newUser);
		 * 
		 * this.setMessages(new ScreenMessage(
		 * "Registration succesfull, an email confirmation has been sent to " +
		 * request.getString("emailaddress") + " for activation", true));
		 * this.state = State.Login; } catch (Exception e) {
		 * this.setMessages(new ScreenMessage("Registration failed: " +
		 * e.getMessage(), false)); }
		 */
	}

	private void doEmailPassword(Database db, MolgenisRequest request)
	{
		try
		{
			// getLogin().emailPassword();
			this.setMessages(new ScreenMessage("password sent", true));
			this.state = State.Login;
		}
		catch (Exception e)
		{
			this.setMessages(new ScreenMessage("sending of password failed: " + e.getMessage(), false));
		}
	}

	private void doLogout(Database db, MolgenisRequest request) throws Exception
	{
		getLogin().logout(db);
		this.state = State.Login;
	}

	private void doLogin(Database db, MolgenisRequest request) throws HandleRequestDelegationException, Exception
	{
		if (!getLogin().login(db, request.getString("name"), request.getString("password")))
		{
			this.setMessages(new ScreenMessage("login failed: username or password unknown", false));
		}
		else
		{
			this.state = State.Logout;
		}
	}

	public List<HtmlInput<?>> getInputs()
	{
		CommandTemplate f = new CommandTemplate();

		// workflow:
		// login -> logout -> edit_profile -> save_profile -> cancel -> logout
		// -> email_password -> cancel -> login
		// -> register -> cancel -> login
		String action = state.toString().replace("_", " ");
		switch (state)
		{
			case Register:
				f.add(new StringInput("name"));
				f.add(new StringInput("emailaddress"));
				f.add(new PasswordInput("password"));
				f.add(new PasswordInput("Retype new password"));
				f.add(new ActionInput(action));
				f.add(new ActionInput(State.Cancel.toString()));
				break;
			case Login:
				f.add(new StringInput("name"));
				f.add(new PasswordInput("password"));
				f.add(new ActionInput(action));
				f.add(new ActionInput(State.Signup.toString()));
				f.add(new ActionInput(State.Forgot_your_password.toString().replace("_", " ")));
				break;
			case Logout:
				// also option to move to Edit_profile
				String edit_action = State.Edit_profile.toString().replace("_", " ");
				f.add(new ActionInput(edit_action));
				f.add(new ActionInput(state.toString()));
				break;
			case Forgot_your_password:
				f.add(new StringInput("emailaddress"));
				f.add(new ActionInput(action));
				f.add(new ActionInput(State.Cancel.toString()));
				break;
			case Edit_profile:
				// identical to next so no break
				// difference is any errors
			case Save_profile:
				// TODO: Where is MolgenisUser?
				/*
				 * MolgenisUser user = ((DatabaseLogin)
				 * getLogin()).getMolgenisUser(); f.add(new StringInput("name",
				 * user.getName())); f.add(new PasswordInput("password",
				 * user.getPassword())); f.add(new
				 * PasswordInput("Retype password")); f.add(new
				 * StringInput("emailaddress", user.getEmailaddress()));
				 * f.add(new
				 * ActionInput(State.Save_profile.toString().replace("_",
				 * " "))); f.add(new ActionInput(State.Cancel.toString()));
				 */
		}

		// logger.debug("STATE: " + this.state);

		return f.getInputs();
	}

	@Override
	public void reload(Database db)
	{
		// nothing todo, Login takes care of this.
		if (getLogin().isAuthenticated())
		{
			this.setLabel("Sign out '" + getLogin().getUserName() + "'");
		}
		else
		{
			this.setLabel("Login");
		}
	}

	@Override
	public boolean isVisible()
	{
		// always visible
		return true;
	}
}

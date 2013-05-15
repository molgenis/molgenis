package org.molgenis.omx.auth.ui;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;
import org.molgenis.framework.security.Login;
import org.molgenis.framework.server.MolgenisRequest;
import org.molgenis.framework.ui.EasyPluginController;
import org.molgenis.framework.ui.FreemarkerView;
import org.molgenis.framework.ui.ScreenController;
import org.molgenis.framework.ui.ScreenMessage;
import org.molgenis.framework.ui.ScreenView;
import org.molgenis.framework.ui.html.TablePanel;
import org.molgenis.omx.auth.Institute;
import org.molgenis.omx.auth.MolgenisUser;
import org.molgenis.omx.auth.service.MolgenisUserException;
import org.molgenis.omx.auth.service.MolgenisUserService;
import org.molgenis.omx.auth.ui.form.DatabaseAuthenticationForm;
import org.molgenis.omx.auth.ui.form.ForgotForm;
import org.molgenis.omx.auth.ui.form.UserAreaForm;
import org.molgenis.omx.auth.vo.MolgenisUserSearchCriteriaVO;
import org.molgenis.omx.observ.target.OntologyTerm;
import org.molgenis.util.WebAppUtil;
import org.molgenis.util.tuple.HttpServletRequestTuple;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;

/**
 * Login box
 */
public class SimpleUserLogin extends EasyPluginController<SimpleUserLoginModel>
{
	private static final long serialVersionUID = -3084964114182861171L;

	public SimpleUserLogin(String name, ScreenController<?> parent)
	{
		super(name, parent);
		this.setModel(new SimpleUserLoginModel(this));
	}

	@Override
	public ScreenView getView()
	{
		return new FreemarkerView("templates/org/molgenis/omx/auth/ui/UserLogin.ftl", getModel());
	}

	@Override
	public String getCustomHtmlHeaders()
	{
		StringBuilder s = new StringBuilder();
		s.append("<script type=\"text/javascript\" src=\"js/jquery.autogrowinput.js\"></script>");
		s.append("<script type=\"text/javascript\" src=\"js/jquery.bt.min.js\"></script>");
		s.append("<script type=\"text/javascript\" src=\"js/jquery.validate.min.js\"></script>");
		return s.toString();
	}

	public void Login(Database db, MolgenisRequest request) throws Exception
	{
		this.getModel().setAction("Login");

		if (StringUtils.isNotEmpty(request.getString("username"))
				&& StringUtils.isNotEmpty(request.getString("password")))
		{
			String username = request.getString("username");
			String password = request.getString("password");

			boolean loggedIn = this.getApplicationController().getLogin().login(db, username, password);

			if (!loggedIn) throw new DatabaseException("Login failed: username or password unknown");

			HttpServletRequestTuple rt = request;
			HttpServletRequest httpRequest = rt.getRequest();
			HttpServletResponse httpResponse = rt.getResponse();

			if (StringUtils.isNotEmpty(this.getApplicationController().getLogin().getRedirect()))
			{
				String redirectURL = httpRequest.getRequestURL() + "?__target=main" + "&select="
						+ this.getApplicationController().getLogin().getRedirect();
				httpResponse.sendRedirect(redirectURL);
				// workaround - see comment @
				// EasyPluginController.HTML_WAS_ALREADY_SERVED
				EasyPluginController.HTML_WAS_ALREADY_SERVED = true;
				// no use: all caught as InvocationTargetException
				// throw new RedirectedException();
			}

			this.getModel().setLabel("My Account");

		}
		else
		{
			this.getModel().setLabel("Login");
			throw new DatabaseException("Login failed: username or password empty");
		}
	}

	public void Logout(Database db, MolgenisRequest request) throws Exception
	{
		this.getModel().setAction("Logout");
		this.getApplicationController().getLogin().logout(db);
		this.getApplicationController().getLogin().reload(db);
		this.getModel().setLabel("Login");
	}

	public void Cancel(Database db, MolgenisRequest request)
	{
		this.getModel().setAction("Cancel");
	}

	public void AddUser(Database db, MolgenisRequest request) throws Exception
	{
		this.getModel().setAction("AddUser");

		try
		{
			// get the http request that is encapsulated inside the tuple
			HttpServletRequestTuple rt = request;
			HttpServletRequest httpRequest = rt.getRequest();

			// save current login and then set to null, to bypass security
			Login saveLogin = db.getLogin();
			db.setLogin(null);

			MolgenisUserService userService = MolgenisUserService.getInstance(db);
			MolgenisUser user = this.toMolgenisUser(db, request);

			// Get the email address of admin user.
			MolgenisUser admin = db.query(MolgenisUser.class).eq(MolgenisUser.NAME, "admin").find().get(0);
			if (StringUtils.isEmpty(admin.getEmail())) throw new DatabaseException(
					"Registration failed: the administrator has no email address set used to confirm your registration. Please contact your administrator about this.");

			// only insert if admin has an email set
			userService.insert(user);

			// Email the admin
			String activationURL = httpRequest.getRequestURL().toString() + "?__target=" + this.getName() + "&select="
					+ this.getName() + "&__action=Activate&actCode=" + user.getActivationCode();
			String emailContents = "User registration for " + this.getRoot().getLabel() + "\n";
			emailContents += "User name: " + user.getName() + " Full name: " + user.getFirstName() + " "
					+ user.getLastName() + "\n";
			emailContents += "In order to activate the user visit the following URL:\n";
			emailContents += activationURL + "\n\n";

			SimpleMailMessage mailMessage = new SimpleMailMessage();
			mailMessage.setTo(admin.getEmail());
			mailMessage.setSubject("User registration for " + this.getRoot().getLabel());
			mailMessage.setText(emailContents);
			try
			{
				WebAppUtil.getMailSender().send(mailMessage);
				this.getModel()
						.getMessages()
						.add(new ScreenMessage(
								"Thank you for registering. Your request has been sent to the adminstrator for approval.",
								true));
			}
			catch (MailException ex)
			{
				logger.warn(ex);
				this.getModel()
						.getMessages()
						.add(new ScreenMessage(
								"Registration failed: An error occurred while e-mailing your request to the administrator.",
								false));
			}

			// restore login
			db.setLogin(saveLogin);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			this.getModel().setAction("Register");
			throw new DatabaseException(e.getMessage());
		}
		finally
		{
			this.getApplicationController().getLogin().logout(db);
			this.reload(db);
		}
	}

	public void Activate(Database db, MolgenisRequest request) throws Exception
	{
		this.getModel().setAction("Activate");

		try
		{
			// save current login and then set to null, to bypass security
			Login saveLogin = db.getLogin();
			db.setLogin(null);

			MolgenisUserSearchCriteriaVO criteria = new MolgenisUserSearchCriteriaVO();
			criteria.setActivationCode(request.getString("actCode"));

			MolgenisUserService userService = MolgenisUserService.getInstance(db);
			List<MolgenisUser> users = userService.find(criteria);

			if (users.size() != 1) throw new MolgenisUserException("No user found for activation code.");

			MolgenisUser user = users.get(0);
			user.setActive(true);
			userService.update(user);

			this.getModel().getMessages().add(new ScreenMessage("Activation successful", true));

			// Email the user
			String emailContents = "Dear " + user.getFirstName() + " " + user.getLastName() + ",\n\n";
			emailContents += "your registration request for " + this.getRoot().getLabel() + " was approved.\n";
			emailContents += "Your account is now active.\n";

			SimpleMailMessage mailMessage = new SimpleMailMessage();
			mailMessage.setTo(user.getEmail());
			mailMessage.setSubject("Your registration request");
			mailMessage.setText(emailContents);
			WebAppUtil.getMailSender().send(mailMessage);

			// restore login
			db.setLogin(saveLogin);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			this.getModel().getMessages().add(new ScreenMessage("Activation failed", false));
		}
		finally
		{
			this.getApplicationController().getLogin().logout(db);
		}
	}

	public void sendPassword(Database db, MolgenisRequest request) throws Exception
	{
		try
		{

			if (this.getApplicationController().getLogin().isAuthenticated())
			{
				// if logged in, log out first
				this.getApplicationController().getLogin().logout(db);
			}

			// login as admin
			// (a bit evil but less so than giving anonymous write-rights on the
			// MolgenisUser table)
			this.getApplicationController().getLogin().login(db, "admin", "admin"); // TODO
			this.getApplicationController().getLogin().reload(db);
			// save current login and then set to null, to bypass security
			Login saveLogin = db.getLogin();
			db.setLogin(null);

			MolgenisUserSearchCriteriaVO criteria = new MolgenisUserSearchCriteriaVO();
			criteria.setName(request.getString("username"));

			MolgenisUserService userService = MolgenisUserService.getInstance(db);
			List<MolgenisUser> users = userService.find(criteria);

			if (users.size() != 1)
			{
				throw new MolgenisUserException("No user found with this username.");
			}

			MolgenisUser user = users.get(0);

			String newPassword = UUID.randomUUID().toString().substring(0, 8);
			user.setPassword(newPassword);
			userService.update(user); // exception

			String emailContents = "Somebody, probably you, requested a new password for " + this.getRoot().getLabel()
					+ ".\n";
			emailContents += "The new password is: " + newPassword + "\n";
			emailContents += "Note: we strongly recommend you reset your password after log-in!";
			// TODO: make this mandatory (password that was sent is valid only
			// once)

			SimpleMailMessage mailMessage = new SimpleMailMessage();
			mailMessage.setTo(user.getEmail());
			mailMessage.setSubject("Your new password request");
			mailMessage.setText(emailContents);
			try
			{
				WebAppUtil.getMailSender().send(mailMessage);
				this.getModel().getMessages().add(new ScreenMessage("Sending new password successful", true));
			}
			catch (MailException e)
			{
				logger.warn(e);
				this.getModel().getMessages().add(new ScreenMessage("Error requesting new password", false));
			}

			// restore login
			db.setLogin(saveLogin);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			this.getModel().getMessages().add(new ScreenMessage("Sending new password failed", false));
		}
		finally
		{
			this.getApplicationController().getLogin().logout(db);
		}
	}

	public void ChgUser(Database db, MolgenisRequest request) throws NoSuchAlgorithmException, MolgenisUserException,
			DatabaseException, ParseException, IOException
	{
		this.getModel().setAction("ChgUser");

		MolgenisUserService userService = MolgenisUserService.getInstance(db);

		if (StringUtils.isNotEmpty(request.getString("oldpwd")) || StringUtils.isNotEmpty(request.getString("newpwd"))
				|| StringUtils.isNotEmpty(request.getString("newpwd2")))
		{
			String oldPwd = request.getString("oldpwd");
			String newPwd1 = request.getString("newpwd");
			String newPwd2 = request.getString("newpwd2");

			userService.checkPassword(this.getApplicationController().getLogin().getUserName(), oldPwd, newPwd1,
					newPwd2);
		}

		MolgenisUser user = userService.findById(this.getApplicationController().getLogin().getUserId());
		this.toMolgenisUser(request, user, db);
		userService.update(user);

		this.getModel().getMessages().add(new ScreenMessage("Changes successfully applied", true));
	}

	public void Forgot(Database db, MolgenisRequest request)
	{
		this.getModel().setAction("Forgot");
	}

	private Integer getInstitute(String instName, Database db) throws DatabaseException
	{
		if (instName != null && !instName.isEmpty())
		{
			List<Institute> institutes = db.find(Institute.class, new QueryRule(Institute.NAME, Operator.EQUALS,
					instName));
			if (institutes.size() == 0)
			{
				Institute newInst = new Institute();
				newInst.setName(instName);
				db.add(newInst);
				return newInst.getId();
			}
			else if (institutes.size() == 1)
			{
				return institutes.get(0).getId();
			}
			else
			{
				throw new DatabaseException("Multiple institutes named '" + instName + "' found");
			}
		}
		return null;
		// throw new DatabaseException("Error when finding/creating Institute");
	}

	private Integer getRole(String roleName, Database db) throws DatabaseException
	{
		if (roleName != null && !roleName.isEmpty())
		{
			List<OntologyTerm> roles = db.find(OntologyTerm.class, new QueryRule(OntologyTerm.NAME, Operator.EQUALS,
					roleName));
			if (roles.size() == 0)
			{
				OntologyTerm newRole = new OntologyTerm();
				newRole.setName(roleName);
				db.add(newRole);
				return newRole.getId();
			}
			else if (roles.size() == 1)
			{
				return roles.get(0).getId();
			}
			else
			{
				throw new DatabaseException("Multiple ontologyTerms for role '" + roleName + "' found");
			}
		}
		return null;
		// throw new DatabaseException("Error when finding/creating Role");
	}

	private MolgenisUser toMolgenisUser(Database db, MolgenisRequest request) throws MolgenisUserException,
			DatabaseException
	{
		MolgenisUser user = new MolgenisUser();
		if (!StringUtils.equals(request.getString("password"), request.getString("password2"))) throw new MolgenisUserException(
				"Passwords do not match.");

		user.setIdentifier(MolgenisUser.class.getSimpleName() + '_' + request.getString("username"));

		user.setName(request.getString("username"));
		user.setPassword(request.getString("password"));
		user.setEmail(request.getString("email"));

		user.setPhone(request.getString("phone"));
		user.setFax(request.getString("fax"));
		user.setTollFreePhone(request.getString("tollFreePhone"));
		user.setAddress(request.getString("address"));

		user.setTitle(request.getString("title"));
		user.setLastName(request.getString("lastname"));
		user.setFirstName(request.getString("firstname"));
		user.setAffiliation_Id(getInstitute(request.getString("institute"), db));
		user.setDepartment(request.getString("department"));
		user.setRoles_Id(getRole(request.getString("position"), db));
		user.setCity(request.getString("city"));
		user.setCountry(request.getString("country"));
		Calendar cal = Calendar.getInstance();
		Date now = cal.getTime();
		String actCode = Integer.toString(Math.abs(now.hashCode()));
		user.setActivationCode(actCode);
		user.setActive(false);

		return user;
	}

	private void toMolgenisUser(MolgenisRequest request, MolgenisUser user, Database db) throws DatabaseException
	{
		if (StringUtils.isNotEmpty(request.getString("newpwd"))) user.setPassword(request.getString("newpwd"));
		if (StringUtils.isNotEmpty(request.getString("emailaddress"))) user.setEmail(request.getString("emailaddress"));

		if (StringUtils.isNotEmpty(request.getString("phone"))) user.setPhone(request.getString("phone"));
		if (StringUtils.isNotEmpty(request.getString("fax"))) user.setFax(request.getString("fax"));
		if (StringUtils.isNotEmpty(request.getString("tollFreePhone"))) user.setTollFreePhone(request
				.getString("tollFreePhone"));
		if (StringUtils.isNotEmpty(request.getString("address"))) user.setAddress(request.getString("address"));

		if (StringUtils.isNotEmpty(request.getString("title"))) user.setTitle(request.getString("title"));
		if (StringUtils.isNotEmpty(request.getString("lastname"))) user.setLastName(request.getString("lastname"));
		if (StringUtils.isNotEmpty(request.getString("firstname"))) user.setFirstName(request.getString("firstname"));
		if (StringUtils.isNotEmpty(request.getString("institute"))) user.setAffiliation(getInstitute(
				request.getString("institute"), db));
		if (StringUtils.isNotEmpty(request.getString("department"))) user
				.setDepartment(request.getString("department"));
		if (StringUtils.isNotEmpty(request.getString("position"))) user.setRoles(getRole(request.getString("position"),
				db));
		if (StringUtils.isNotEmpty(request.getString("city"))) user.setCity(request.getString("city"));
		if (StringUtils.isNotEmpty(request.getString("country"))) user.setCountry(request.getString("country"));
	}

	@Override
	public void reload(Database db)
	{
		this.populateAuthenticationForm();
		this.populateUserAreaForm(db);
		this.populateForgotForm();
	}

	private void populateAuthenticationForm()
	{
		this.getModel().setAuthenticationForm(new DatabaseAuthenticationForm());
	}

	@SuppressWarnings("unchecked")
	private void populateUserAreaForm(Database db)
	{
		try
		{
			MolgenisUserService userService = MolgenisUserService.getInstance(db);
			MolgenisUser user = userService.findById(this.getApplicationController().getLogin().getUserId());

			UserAreaForm userAreaForm = new UserAreaForm();
			((TablePanel) userAreaForm.get("personal")).get("emailaddress").setValue(user.getEmail());

			((TablePanel) userAreaForm.get("personal")).get("phone").setValue(user.getPhone());
			((TablePanel) userAreaForm.get("personal")).get("fax").setValue(user.getFax());
			((TablePanel) userAreaForm.get("personal")).get("tollFreePhone").setValue(user.getTollFreePhone());
			((TablePanel) userAreaForm.get("personal")).get("address").setValue(user.getAddress());

			((TablePanel) userAreaForm.get("personal")).get("title").setValue(user.getTitle());
			((TablePanel) userAreaForm.get("personal")).get("firstname").setValue(user.getFirstName());
			((TablePanel) userAreaForm.get("personal")).get("lastname").setValue(user.getLastName());
			((TablePanel) userAreaForm.get("personal")).get("institute").setValue(user.getAffiliation_Name());
			((TablePanel) userAreaForm.get("personal")).get("department").setValue(user.getDepartment());
			((TablePanel) userAreaForm.get("personal")).get("position").setValue(user.getRoles_Identifier());
			((TablePanel) userAreaForm.get("personal")).get("city").setValue(user.getCity());
			((TablePanel) userAreaForm.get("personal")).get("country").setValue(user.getCountry());

			this.getModel().setUserAreaForm(userAreaForm);
		}
		catch (Exception e)
		{
			this.getModel().setUserAreaForm(new UserAreaForm());
		}
	}

	private void populateForgotForm()
	{
		this.getModel().setForgotForm(new ForgotForm());
	}

}

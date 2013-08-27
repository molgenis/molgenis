package org.molgenis.omx.auth.controller;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseAccessException;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;
import org.molgenis.framework.server.MolgenisRequest;
import org.molgenis.framework.ui.ApplicationController;
import org.molgenis.omx.auth.Institute;
import org.molgenis.omx.auth.MolgenisUser;
import org.molgenis.omx.auth.PersonRole;
import org.molgenis.omx.auth.service.AccountService;
import org.molgenis.omx.auth.service.CaptchaService;
import org.molgenis.omx.auth.service.CaptchaService.CaptchaException;
import org.molgenis.omx.auth.service.MolgenisUserException;
import org.molgenis.omx.auth.service.MolgenisUserService;
import org.molgenis.omx.auth.vo.CaptchaRequest;
import org.molgenis.omx.auth.vo.PasswordResetRequest;
import org.molgenis.omx.auth.vo.RegisterRequest;
import org.molgenis.omx.observ.target.OntologyTerm;
import org.molgenis.util.CountryCodes;
import org.molgenis.util.HandleRequestDelegationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Controller
@RequestMapping("/account")
public class AccountController
{
	@Autowired
	private Database database;

	@Autowired
	@Qualifier("unauthorizedDatabase")
	private Database unauthorizedDatabase;

	@Autowired
	private AccountService accountService;

	@Autowired
	private CaptchaService captchaService;

	@RequestMapping(method = GET)
	public String init(Model model) throws DatabaseException
	{	
		model.addAttribute("user", getCurrentUser());

		return "view-account";
	}
	
	@RequestMapping(value = "/login", method = RequestMethod.GET)
	public String getLoginForm()
	{
		return "login-modal";
	}
	
	@RequestMapping(value = "/loginform", method = RequestMethod.GET)
	public String showLoginForm(Model model) throws DatabaseException
	{
		if(database.getLogin().isAuthenticated()){
			model.addAttribute("user", getCurrentUser());
			return "view-account";
		}
		return "view-login";
	}

	@RequestMapping(value = "/register", method = RequestMethod.GET)
	public ModelAndView getRegisterForm() throws DatabaseException
	{
		ModelAndView model = new ModelAndView("register-modal");
		model.addObject("institutes", unauthorizedDatabase.find(Institute.class));
		model.addObject("personroles", unauthorizedDatabase.find(PersonRole.class));
		model.addObject("countries", CountryCodes.get());
		return model;
	}

	@RequestMapping(value = "/password/reset", method = RequestMethod.GET)
	public String getPasswordResetForm()
	{
		return "resetpassword-modal";
	}

	@RequestMapping(value = "/login", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void loginUser(@RequestParam("username")
	String username, @RequestParam("password")
	String password) throws HandleRequestDelegationException, Exception
	{
		boolean ok = database.getLogin().login(database, username, password);
		if (!ok) throw new DatabaseAccessException("Login failed: username or password unknown");
	}

	@RequestMapping(value = "/logout", method = RequestMethod.GET)
	public String logoutUser() throws Exception
	{
		database.getLogin().logout(database);
		database.getLogin().reload(database);
		return "redirect:" + ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
	}

	// Spring's FormHttpMessageConverter cannot bind target classes (as ModelAttribute can)
	@RequestMapping(value = "/register", method = RequestMethod.POST, headers = "Content-Type=application/x-www-form-urlencoded")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void registerUser(@Valid
	@ModelAttribute
	RegisterRequest registerRequest, @Valid
	@ModelAttribute
	CaptchaRequest captchaRequest) throws DatabaseException, CaptchaException, BindException
	{
		if (!captchaService.validateCaptcha(captchaRequest.getCaptcha()))
		{
			throw new CaptchaException("invalid captcha answer");
		}
		if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword()))
		{
			throw new BindException(RegisterRequest.class, "password does not match confirm password");
		}
		MolgenisUser molgenisUser = toMolgenisUser(registerRequest);
		URI activationUri = ServletUriComponentsBuilder.fromCurrentContextPath().path("/account/activate").build()
				.toUri();
		accountService.createUser(molgenisUser, activationUri);
	}

	@RequestMapping(value = "/activate/{activationCode}", method = RequestMethod.GET)
	public String activateUser(@Valid
	@NotNull
	@PathVariable
	String activationCode) throws DatabaseException
	{
		accountService.activateUser(activationCode);
		return "redirect:" + ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
	}

	// Spring's FormHttpMessageConverter cannot bind target classes (as ModelAttribute can)
	@RequestMapping(value = "/password/reset", method = RequestMethod.POST, headers = "Content-Type=application/x-www-form-urlencoded")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void resetPassword(@Valid
	@ModelAttribute
	PasswordResetRequest passwordResetRequest) throws DatabaseException
	{
		List<MolgenisUser> molgenisUsers = database.find(MolgenisUser.class, new QueryRule(MolgenisUser.EMAIL,
				Operator.EQUALS, passwordResetRequest.getEmail()));
		if (molgenisUsers != null && !molgenisUsers.isEmpty()) accountService.resetPassword(molgenisUsers.get(0));
	}

	@RequestMapping(value = "/uppdateuser", method = RequestMethod.POST, headers = "Content-Type=application/x-www-form-urlencoded")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void ChgUser(HttpServletRequest request) throws Exception
	{
		final HttpSession session = request.getSession();
		ApplicationController applicationController = (ApplicationController) session.getAttribute("application");

		MolgenisUserService userService = MolgenisUserService.getInstance(database);

		if (StringUtils.isNotEmpty(request.getParameter("oldpwd")) || StringUtils.isNotEmpty(request.getParameter("newpwd"))
				|| StringUtils.isNotEmpty(request.getParameter("newpwd2")))
		{
			String oldPwd = request.getParameter("oldpwd");
			String newPwd1 = request.getParameter("newpwd");
			String newPwd2 = request.getParameter("newpwd2");

			userService.checkPassword(applicationController.getLogin().getUserName(), oldPwd, newPwd1, newPwd2);
		}

		MolgenisUser user = userService.findById(applicationController.getLogin().getUserId());
		MolgenisRequest molgenisRequest = new MolgenisRequest(request);
		this.updateMolgenisUser(user, molgenisRequest);
		userService.update(user);
	}
	
	@ExceptionHandler(DatabaseAccessException.class)
	@ResponseStatus(value = HttpStatus.UNAUTHORIZED)
	private void handleDatabaseAccessException(DatabaseAccessException e)
	{
	}

	@ExceptionHandler(CaptchaException.class)
	@ResponseStatus(value = HttpStatus.BAD_REQUEST)
	private void handleCaptchaException(CaptchaException e)
	{
	}

	private MolgenisUser toMolgenisUser(RegisterRequest request)
	{
		MolgenisUser user = new MolgenisUser();
		user.setName(request.getUsername());
		user.setIdentifier(UUID.randomUUID().toString());
		user.setPassword(request.getPassword());
		user.setEmail(request.getEmail());
		user.setPhone(request.getPhone());
		user.setFax(request.getFax());
		user.setTollFreePhone(request.getTollFreePhone());
		user.setAddress(request.getAddress());
		user.setTitle(request.getTitle());
		user.setLastName(request.getLastname());
		user.setFirstName(request.getFirstname());
		user.setAffiliation_Id(request.getInstitute());
		user.setDepartment(request.getDepartment());
		user.setRoles_Id(request.getPosition());
		user.setCity(request.getCity());
		user.setCountry(CountryCodes.get(request.getCountry()));
		return user;
	}

	private MolgenisUser updateMolgenisUser(MolgenisUser user, MolgenisRequest request) throws MolgenisUserException,
			DatabaseException
	{
		if (!StringUtils.equals(request.getString("password"), request.getString("password2"))) throw new MolgenisUserException(
				"Passwords do not match.");

		if(notNullOrEmpty(request.getString("password"))) user.setPassword(request.getString("password"));
		if(notNullOrEmpty(request.getString("emailaddress"))) user.setEmail(request.getString("emailaddress"));

		if(notNullOrEmpty(request.getString("phone"))) user.setPhone(request.getString("phone"));
		if(notNullOrEmpty(request.getString("fax"))) user.setFax(request.getString("fax"));
		if(notNullOrEmpty(request.getString("tollFreePhone"))) user.setTollFreePhone(request.getString("tollFreePhone"));
		if(notNullOrEmpty(request.getString("address"))) user.setAddress(request.getString("address"));

		if(notNullOrEmpty(request.getString("title"))) user.setTitle(request.getString("title"));
		if(notNullOrEmpty(request.getString("lastname"))) user.setLastName(request.getString("lastname"));
		if(notNullOrEmpty(request.getString("firstname"))) user.setFirstName(request.getString("firstname"));
		if(notNullOrEmpty(request.getString("institute"))) user.setAffiliation_Id(getInstitute(request.getString("institute"), database));
		if(notNullOrEmpty(request.getString("department"))) user.setDepartment(request.getString("department"));
		if(notNullOrEmpty(request.getString("position"))) user.setRoles_Id(getRole(request.getString("position"), database));
		if(notNullOrEmpty(request.getString("city"))) user.setCity(request.getString("city"));
		if(notNullOrEmpty(request.getString("country"))) user.setCountry(request.getString("country"));

		return user;
	}
	
	private boolean notNullOrEmpty(String input){
		return (input!=null&&!"".equals(input));
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
	
	private MolgenisUser getCurrentUser() throws DatabaseException
	{
		return getMolgenisUserService().findById(database.getLogin().getUserId());
	}
	
	public MolgenisUserService getMolgenisUserService()
	{
		return MolgenisUserService.getInstance(database);
	}
}

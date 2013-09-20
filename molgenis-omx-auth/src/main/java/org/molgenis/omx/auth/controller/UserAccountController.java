package org.molgenis.omx.auth.controller;

import static org.molgenis.omx.auth.controller.UserAccountController.URI;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseAccessException;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;
import org.molgenis.framework.ui.MolgenisPlugin;
import org.molgenis.omx.auth.Institute;
import org.molgenis.omx.auth.MolgenisUser;
import org.molgenis.omx.auth.service.AccountService;
import org.molgenis.omx.auth.service.CaptchaService;
import org.molgenis.omx.auth.service.CaptchaService.CaptchaException;
import org.molgenis.omx.auth.service.MolgenisUserException;
import org.molgenis.omx.auth.service.MolgenisUserService;
import org.molgenis.omx.observ.target.OntologyTerm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
@RequestMapping(URI)
public class UserAccountController extends MolgenisPlugin
{
	public static final String URI = MolgenisPlugin.PLUGIN_URI_PREFIX + "useraccount";

	@Autowired
	private Database database;

	@Autowired
	@Qualifier("unauthorizedDatabase")
	private Database unauthorizedDatabase;

	@Autowired
	private AccountService accountService;

	@Autowired
	private CaptchaService captchaService;

	public UserAccountController()
	{
		super(URI);
	}

	@RequestMapping(method = RequestMethod.GET)
	public String showAccount(Model model) throws DatabaseException
	{
		model.addAttribute("user", getCurrentUser());

		return "view-useraccount";
	}

	@RequestMapping(value = "/update", method = RequestMethod.POST, headers = "Content-Type=application/x-www-form-urlencoded")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void updateAccount(HttpServletRequest request) throws Exception
	{
		MolgenisUserService userService = MolgenisUserService.getInstance(database);

		if (StringUtils.isNotEmpty(request.getParameter("oldpwd"))
				|| StringUtils.isNotEmpty(request.getParameter("newpwd"))
				|| StringUtils.isNotEmpty(request.getParameter("newpwd2")))
		{
			String oldPwd = request.getParameter("oldpwd");
			String newPwd1 = request.getParameter("newpwd");
			String newPwd2 = request.getParameter("newpwd2");

			userService.checkPassword(database.getLogin().getUserName(), oldPwd, newPwd1, newPwd2);
		}

		MolgenisUser user = userService.findById(database.getLogin().getUserId());
		this.updateMolgenisUser(user, request);
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

	private MolgenisUser updateMolgenisUser(MolgenisUser user, HttpServletRequest request)
			throws MolgenisUserException, DatabaseException
	{
		if (!StringUtils.equals(request.getParameter("newpwd"), request.getParameter("newpwd2"))) throw new MolgenisUserException(
				"Passwords do not match.");

		if (StringUtils.isNotEmpty(request.getParameter("newpwd"))) user.setPassword(request.getParameter("newpwd"));
		if (StringUtils.isNotEmpty(request.getParameter("emailaddress"))) user.setEmail(request
				.getParameter("emailaddress"));

		if (StringUtils.isNotEmpty(request.getParameter("phone"))) user.setPhone(request.getParameter("phone"));
		if (StringUtils.isNotEmpty(request.getParameter("fax"))) user.setFax(request.getParameter("fax"));
		if (StringUtils.isNotEmpty(request.getParameter("tollFreePhone"))) user.setTollFreePhone(request
				.getParameter("tollFreePhone"));
		if (StringUtils.isNotEmpty(request.getParameter("address"))) user.setAddress(request.getParameter("address"));

		if (StringUtils.isNotEmpty(request.getParameter("title"))) user.setTitle(request.getParameter("title"));
		if (StringUtils.isNotEmpty(request.getParameter("lastname"))) user
				.setLastName(request.getParameter("lastname"));
		if (StringUtils.isNotEmpty(request.getParameter("firstname"))) user.setFirstName(request
				.getParameter("firstname"));
		if (StringUtils.isNotEmpty(request.getParameter("institute"))) user.setAffiliation_Id(getInstitute(
				request.getParameter("institute"), database));
		if (StringUtils.isNotEmpty(request.getParameter("department"))) user.setDepartment(request
				.getParameter("department"));
		if (StringUtils.isNotEmpty(request.getParameter("position"))) user.setRoles_Id(getRole(
				request.getParameter("position"), database));
		if (StringUtils.isNotEmpty(request.getParameter("city"))) user.setCity(request.getParameter("city"));
		if (StringUtils.isNotEmpty(request.getParameter("country"))) user.setCountry(request.getParameter("country"));

		return user;
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
	}

	private MolgenisUser getCurrentUser() throws DatabaseException
	{
		return getMolgenisUserService().findById(database.getLogin().getUserId());
	}

	private MolgenisUserService getMolgenisUserService()
	{
		return MolgenisUserService.getInstance(database);
	}
}

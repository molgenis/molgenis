package org.molgenis.security.user;

import static org.molgenis.security.user.UserAccountController.URI;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseAccessException;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.molgenis.omx.auth.MolgenisUser;
import org.molgenis.security.SecurityUtils;
import org.molgenis.security.account.AccountService;
import org.molgenis.security.captcha.CaptchaException;
import org.molgenis.security.captcha.CaptchaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
@RequestMapping(URI)
public class UserAccountController extends MolgenisPluginController
{
	public static final String ID = "useraccount";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;

	@Autowired
	private Database database;

	@Autowired
	private AccountService accountService;

	@Autowired
	private CaptchaService captchaService;

	@Autowired
	private MolgenisUserService molgenisUserService;

	public UserAccountController()
	{
		super(URI);
	}

	@RequestMapping(method = RequestMethod.GET)
	public String showAccount(Model model) throws DatabaseException
	{
		model.addAttribute("user", molgenisUserService.getCurrentUser());
		return "view-useraccount";
	}

	@RequestMapping(value = "/update", method = RequestMethod.POST, headers = "Content-Type=application/x-www-form-urlencoded")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void updateAccount(HttpServletRequest request) throws Exception
	{
		if (StringUtils.isNotEmpty(request.getParameter("oldpwd"))
				|| StringUtils.isNotEmpty(request.getParameter("newpwd"))
				|| StringUtils.isNotEmpty(request.getParameter("newpwd2")))
		{
			String oldPwd = request.getParameter("oldpwd");
			String newPwd1 = request.getParameter("newpwd");
			String newPwd2 = request.getParameter("newpwd2");

			molgenisUserService.checkPassword(SecurityUtils.getCurrentUsername(), oldPwd, newPwd1, newPwd2);
		}

		MolgenisUser user = MolgenisUser.findByUsername(database, SecurityUtils.getCurrentUsername());
		this.updateMolgenisUser(user, request);
		molgenisUserService.update(user);
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
		if (StringUtils.isNotEmpty(request.getParameter("department"))) user.setDepartment(request
				.getParameter("department"));
		if (StringUtils.isNotEmpty(request.getParameter("city"))) user.setCity(request.getParameter("city"));
		if (StringUtils.isNotEmpty(request.getParameter("country"))) user.setCountry(request.getParameter("country"));
		return user;
	}
}

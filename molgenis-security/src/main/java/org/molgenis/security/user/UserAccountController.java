package org.molgenis.security.user;

import static org.molgenis.security.user.UserAccountController.URI;

import java.util.Collections;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.molgenis.omx.auth.MolgenisUser;
import org.molgenis.util.CountryCodes;
import org.molgenis.util.ErrorMessageResponse;
import org.molgenis.util.ErrorMessageResponse.ErrorMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
@RequestMapping(URI)
public class UserAccountController extends MolgenisPluginController
{
	private static final Logger logger = Logger.getLogger(UserAccountController.class);

	public static final int MIN_PASSWORD_LENGTH = 6;

	public static final String ID = "useraccount";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;

	private final UserAccountService userAccountService;

	@Autowired
	public UserAccountController(UserAccountService userAccountService)
	{
		super(URI);
		if (userAccountService == null) throw new IllegalArgumentException("UserAccountService is null");
		this.userAccountService = userAccountService;
	}

	@RequestMapping(method = RequestMethod.GET)
	public String showAccount(Model model) throws DatabaseException
	{
		model.addAttribute("user", userAccountService.getCurrentUser());
		model.addAttribute("countries", CountryCodes.get());
		model.addAttribute("min_password_length", MIN_PASSWORD_LENGTH);
		return "view-useraccount";
	}

	@RequestMapping(value = "/update", method = RequestMethod.POST, headers = "Content-Type=application/x-www-form-urlencoded")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void updateAccount(@Valid @NotNull AccountUpdateRequest updateRequest) throws DatabaseException
	{
		// validate new password
		String newPassword = updateRequest.getNewpwd();
		if (!StringUtils.isEmpty(newPassword))
		{
			String oldPassword = updateRequest.getOldpwd();
			String newPasswordConfirm = updateRequest.getNewpwd2();

			// validate password for current user
			if (oldPassword == null || oldPassword.isEmpty())
			{
				throw new MolgenisUserException("Please enter old password to update your password.");
			}
			boolean valid = userAccountService.validateCurrentUserPassword(oldPassword);
			if (!valid) throw new MolgenisUserException("The password you entered is incorrect.");

			// validate new password against new password confirmation
			if (!newPassword.equals(newPasswordConfirm))
			{
				throw new MolgenisUserException("'New password' does not match 'Repeat new password'.");
			}

			// TODO implement http://www.molgenis.org/ticket/2145
			// TODO define minimum password length in one location (org.molgenis.security.account.RegisterRequest)
			if (newPassword.length() < MIN_PASSWORD_LENGTH)
			{
				throw new MolgenisUserException("New password must consist of at least 6 characters.");
			}
		}

		// update current user
		MolgenisUser user = userAccountService.getCurrentUser();

		if (StringUtils.isNotEmpty(newPassword)) user.setPassword(newPassword);
		if (StringUtils.isNotEmpty(updateRequest.getPhone())) user.setPhone(updateRequest.getPhone());
		if (StringUtils.isNotEmpty(updateRequest.getFax())) user.setFax(updateRequest.getFax());
		if (StringUtils.isNotEmpty(updateRequest.getTollFreePhone()))
		{
			user.setTollFreePhone(updateRequest.getTollFreePhone());
		}
		if (StringUtils.isNotEmpty(updateRequest.getAddress())) user.setAddress(updateRequest.getAddress());
		if (StringUtils.isNotEmpty(updateRequest.getTitle())) user.setTitle(updateRequest.getTitle());
		if (StringUtils.isNotEmpty(updateRequest.getFirstname())) user.setFirstName(updateRequest.getFirstname());
		if (StringUtils.isNotEmpty(updateRequest.getLastname())) user.setLastName(updateRequest.getLastname());
		if (StringUtils.isNotEmpty(updateRequest.getInstitute())) user.setAffiliation(updateRequest.getInstitute());
		if (StringUtils.isNotEmpty(updateRequest.getDepartment())) user.setDepartment(updateRequest.getDepartment());
		if (StringUtils.isNotEmpty(updateRequest.getPosition())) user.setRole(updateRequest.getPosition());
		if (StringUtils.isNotEmpty(updateRequest.getCity())) user.setCity(updateRequest.getCity());
		if (StringUtils.isNotEmpty(updateRequest.getCountry()))
		{
			user.setCountry(CountryCodes.get(updateRequest.getCountry()));
		}

		userAccountService.updateCurrentUser(user);
	}

	@ExceptionHandler(DatabaseException.class)
	@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
	@ResponseBody
	private ErrorMessageResponse handleDatabaseException(DatabaseException e)
	{
		logger.error("", e);
		return new ErrorMessageResponse(Collections.singletonList(new ErrorMessage(e.getMessage())));
	}

	@ExceptionHandler(MolgenisUserException.class)
	@ResponseStatus(value = HttpStatus.BAD_REQUEST)
	@ResponseBody
	private ErrorMessageResponse handleMolgenisUserException(MolgenisUserException e)
	{
		logger.debug("", e);
		return new ErrorMessageResponse(Collections.singletonList(new ErrorMessage(e.getMessage())));
	}

	@ExceptionHandler(RuntimeException.class)
	@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
	@ResponseBody
	private ErrorMessageResponse handleRuntimeException(RuntimeException e)
	{
		logger.error("", e);
		return new ErrorMessageResponse(Collections.singletonList(new ErrorMessage(e.getMessage())));
	}

	private static class AccountUpdateRequest
	{
		private String oldpwd;
		private String newpwd;
		private String newpwd2;
		private String phone;
		private String fax;
		private String tollFreePhone;
		private String address;
		private String title;
		@NotNull
		private String firstname;
		@NotNull
		private String lastname;
		private String institute;
		private String department;
		private String position;
		private String city;
		private String country;

		public String getOldpwd()
		{
			return oldpwd;
		}

		@SuppressWarnings("unused")
		public void setOldpwd(String oldpwd)
		{
			this.oldpwd = oldpwd;
		}

		public String getNewpwd()
		{
			return newpwd;
		}

		@SuppressWarnings("unused")
		public void setNewpwd(String newpwd)
		{
			this.newpwd = newpwd;
		}

		public String getNewpwd2()
		{
			return newpwd2;
		}

		@SuppressWarnings("unused")
		public void setNewpwd2(String newpwd2)
		{
			this.newpwd2 = newpwd2;
		}

		public String getPhone()
		{
			return phone;
		}

		@SuppressWarnings("unused")
		public void setPhone(String phone)
		{
			this.phone = phone;
		}

		public String getFax()
		{
			return fax;
		}

		@SuppressWarnings("unused")
		public void setFax(String fax)
		{
			this.fax = fax;
		}

		public String getTollFreePhone()
		{
			return tollFreePhone;
		}

		@SuppressWarnings("unused")
		public void setTollFreePhone(String tollFreePhone)
		{
			this.tollFreePhone = tollFreePhone;
		}

		public String getAddress()
		{
			return address;
		}

		@SuppressWarnings("unused")
		public void setAddress(String address)
		{
			this.address = address;
		}

		public String getTitle()
		{
			return title;
		}

		@SuppressWarnings("unused")
		public void setTitle(String title)
		{
			this.title = title;
		}

		public String getFirstname()
		{
			return firstname;
		}

		@SuppressWarnings("unused")
		public void setFirstname(String firstname)
		{
			this.firstname = firstname;
		}

		public String getLastname()
		{
			return lastname;
		}

		@SuppressWarnings("unused")
		public void setLastname(String lastname)
		{
			this.lastname = lastname;
		}

		public String getInstitute()
		{
			return institute;
		}

		@SuppressWarnings("unused")
		public void setInstitute(String institute)
		{
			this.institute = institute;
		}

		public String getDepartment()
		{
			return department;
		}

		@SuppressWarnings("unused")
		public void setDepartment(String department)
		{
			this.department = department;
		}

		public String getPosition()
		{
			return position;
		}

		@SuppressWarnings("unused")
		public void setPosition(String position)
		{
			this.position = position;
		}

		public String getCity()
		{
			return city;
		}

		@SuppressWarnings("unused")
		public void setCity(String city)
		{
			this.city = city;
		}

		public String getCountry()
		{
			return country;
		}

		@SuppressWarnings("unused")
		public void setCountry(String country)
		{
			this.country = country;
		}
	}
}

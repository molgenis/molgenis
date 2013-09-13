package org.molgenis.omx.auth.controller;

import java.net.URI;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.molgenis.framework.db.DatabaseAccessException;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.omx.auth.MolgenisUser;
import org.molgenis.omx.auth.service.AccountService;
import org.molgenis.omx.auth.service.CaptchaService;
import org.molgenis.omx.auth.service.CaptchaService.CaptchaException;
import org.molgenis.omx.auth.vo.CaptchaRequest;
import org.molgenis.omx.auth.vo.PasswordResetRequest;
import org.molgenis.omx.auth.vo.RegisterRequest;
import org.molgenis.util.CountryCodes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Controller
@RequestMapping("/account")
public class AccountController
{
	@Autowired
	private AccountService accountService;

	@Autowired
	private CaptchaService captchaService;

	@RequestMapping(value = "/login", method = RequestMethod.GET)
	public String getLoginForm()
	{
		return "login-modal";
	}

	@RequestMapping(value = "/register", method = RequestMethod.GET)
	public ModelAndView getRegisterForm() throws DatabaseException
	{
		ModelAndView model = new ModelAndView("register-modal");
		model.addObject("countries", CountryCodes.get());
		return model;
	}

	@RequestMapping(value = "/password/reset", method = RequestMethod.GET)
	public String getPasswordResetForm()
	{
		return "resetpassword-modal";
	}

	// Spring's FormHttpMessageConverter cannot bind target classes (as ModelAttribute can)
	@RequestMapping(value = "/register", method = RequestMethod.POST, headers = "Content-Type=application/x-www-form-urlencoded")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void registerUser(@Valid @ModelAttribute RegisterRequest registerRequest,
			@Valid @ModelAttribute CaptchaRequest captchaRequest) throws DatabaseException, CaptchaException,
			BindException
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
	public String activateUser(@Valid @NotNull @PathVariable String activationCode) throws DatabaseException
	{
		accountService.activateUser(activationCode);
		return "redirect:" + ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
	}

	// Spring's FormHttpMessageConverter cannot bind target classes (as ModelAttribute can)
	@RequestMapping(value = "/password/reset", method = RequestMethod.POST, headers = "Content-Type=application/x-www-form-urlencoded")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void resetPassword(@Valid @ModelAttribute PasswordResetRequest passwordResetRequest)
			throws DatabaseException
	{
		accountService.resetPassword(passwordResetRequest.getEmail());
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
		user.setUsername(request.getUsername());
		user.setPassword(request.getPassword());
		user.setEmail(request.getEmail());
		user.setPhone(request.getPhone());
		user.setFax(request.getFax());
		user.setTollFreePhone(request.getTollFreePhone());
		user.setAddress(request.getAddress());
		user.setTitle(request.getTitle());
		user.setLastName(request.getLastname());
		user.setFirstName(request.getFirstname());
		user.setDepartment(request.getDepartment());
		user.setCity(request.getCity());
		user.setCountry(CountryCodes.get(request.getCountry()));
		return user;
	}
}

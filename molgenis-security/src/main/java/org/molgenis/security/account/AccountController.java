package org.molgenis.security.account;

import static org.molgenis.security.account.AccountController.URI;
import static org.molgenis.security.user.UserAccountController.MIN_PASSWORD_LENGTH;

import java.util.Collections;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.log4j.Logger;
import org.molgenis.data.MolgenisDataAccessException;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.omx.auth.MolgenisUser;
import org.molgenis.security.captcha.CaptchaException;
import org.molgenis.security.captcha.CaptchaRequest;
import org.molgenis.security.captcha.CaptchaService;
import org.molgenis.security.user.MolgenisUserException;
import org.molgenis.util.CountryCodes;
import org.molgenis.util.ErrorMessageResponse;
import org.molgenis.util.ErrorMessageResponse.ErrorMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Controller
@RequestMapping(URI)
public class AccountController
{
	public static final String URI = "/account";

	private static final Logger logger = Logger.getLogger(AccountController.class);

	static final String REGISTRATION_SUCCESS_MESSAGE_USER = "You have successfully registered, an activation e-mail has been send to your email.";
	static final String REGISTRATION_SUCCESS_MESSAGE_ADMIN = "You have successfully registered, your request has been forwarded to the administrator.";

	@Autowired
	private AccountService accountService;

	@Autowired
	private CaptchaService captchaService;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@RequestMapping(value = "/login", method = RequestMethod.GET)
	public String getLoginForm()
	{
		return "login-modal";
	}

	@RequestMapping(value = "/register", method = RequestMethod.GET)
	public ModelAndView getRegisterForm()
	{
		ModelAndView model = new ModelAndView("register-modal");
		model.addObject("countries", CountryCodes.get());
		model.addObject("min_password_length", MIN_PASSWORD_LENGTH);
		return model;
	}

	@RequestMapping(value = "/password/reset", method = RequestMethod.GET)
	public String getPasswordResetForm()
	{
		return "resetpassword-modal";
	}

	// Spring's FormHttpMessageConverter cannot bind target classes (as ModelAttribute can)
	@RequestMapping(value = "/register", method = RequestMethod.POST, headers = "Content-Type=application/x-www-form-urlencoded")
	@ResponseBody
	public Map<String, String> registerUser(@Valid
	@ModelAttribute
	RegisterRequest registerRequest, @Valid
	@ModelAttribute
	CaptchaRequest captchaRequest, HttpServletRequest request) throws CaptchaException, BindException
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
		String activationUri = null;
		if (StringUtils.isEmpty(request.getHeader("X-Forwarded-Host")))
		{
			activationUri = ServletUriComponentsBuilder.fromCurrentRequest().path(URI + "/activate").build()
					.toUriString();
		}
		else
		{
			activationUri = request.getScheme() + "://" + request.getHeader("X-Forwarded-Host") + URI + "/activate";
		}
		accountService.createUser(molgenisUser, activationUri);

		String successMessage;
		switch (accountService.getActivationMode())
		{
			case ADMIN:
				successMessage = REGISTRATION_SUCCESS_MESSAGE_ADMIN;
				break;
			case USER:
				successMessage = REGISTRATION_SUCCESS_MESSAGE_USER;
				break;
			default:
				throw new RuntimeException("Unknown activation mode " + accountService.getActivationMode());
		}
		return Collections.singletonMap("message", successMessage);
	}

	@RequestMapping(value = "/activate/{activationCode}", method = RequestMethod.GET)
	public String activateUser(@Valid @NotNull @PathVariable String activationCode, Model model)
	{
		try
		{
			accountService.activateUser(activationCode);
			model.addAttribute("successMessage", "Your account has been activated, you can now sign in.");
		}
		catch (RuntimeException e)
		{
			model.addAttribute("errorMessage", e.getMessage());
		}
		return "forward:/";
	}

	// Spring's FormHttpMessageConverter cannot bind target classes (as ModelAttribute can)
	@RequestMapping(value = "/password/reset", method = RequestMethod.POST, headers = "Content-Type=application/x-www-form-urlencoded")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void resetPassword(@Valid @ModelAttribute PasswordResetRequest passwordResetRequest)
	{
		accountService.resetPassword(passwordResetRequest.getEmail());
	}

	@ExceptionHandler(MolgenisDataAccessException.class)
	@ResponseStatus(value = HttpStatus.UNAUTHORIZED)
	private void handleMolgenisDataAccessException(MolgenisDataAccessException e)
	{
	}

	@ExceptionHandler(CaptchaException.class)
	@ResponseStatus(value = HttpStatus.BAD_REQUEST)
	private void handleCaptchaException(CaptchaException e)
	{
	}

	@ExceptionHandler(MolgenisUserException.class)
	@ResponseStatus(value = HttpStatus.BAD_REQUEST)
	@ResponseBody
	public ErrorMessageResponse handleMolgenisUserException(MolgenisUserException e)
	{
		logger.debug("", e);
		return new ErrorMessageResponse(Collections.singletonList(new ErrorMessage(e.getMessage())));
	}

	@ExceptionHandler(MolgenisDataException.class)
	@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
	@ResponseBody
	public ErrorMessageResponse handleMolgenisDataException(MolgenisDataException e)
	{
		logger.error("", e);
		return new ErrorMessageResponse(Collections.singletonList(new ErrorMessage(e.getMessage())));
	}

	@ExceptionHandler(RuntimeException.class)
	@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
	@ResponseBody
	public ErrorMessageResponse handleRuntimeException(RuntimeException e)
	{
		logger.error("", e);
		return new ErrorMessageResponse(Collections.singletonList(new ErrorMessage(e.getMessage())));
	}

	private MolgenisUser toMolgenisUser(RegisterRequest request)
	{
		MolgenisUser user = new MolgenisUser();
		user.setUsername(request.getUsername());
		user.setPassword(passwordEncoder.encode(request.getPassword()));
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

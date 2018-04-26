package org.molgenis.security.account;

import org.molgenis.core.util.CountryCodes;
import org.molgenis.data.MolgenisDataAccessException;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.security.auth.User;
import org.molgenis.data.security.auth.UserFactory;
import org.molgenis.security.captcha.CaptchaException;
import org.molgenis.security.captcha.CaptchaRequest;
import org.molgenis.security.captcha.CaptchaService;
import org.molgenis.security.settings.AuthenticationSettings;
import org.molgenis.security.user.MolgenisUserException;
import org.molgenis.web.ErrorMessageResponse;
import org.molgenis.web.ErrorMessageResponse.ErrorMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.naming.NoPermissionException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import static java.util.Objects.requireNonNull;
import static org.molgenis.security.account.AccountController.URI;
import static org.molgenis.security.user.UserAccountService.MIN_PASSWORD_LENGTH;

@Controller
@RequestMapping(URI)
public class AccountController
{
	private static final Logger LOG = LoggerFactory.getLogger(AccountController.class);

	public static final String URI = "/account";
	private static final String CHANGE_PASSWORD_RELATIVE_URI = "/password/change";
	public static final String CHANGE_PASSWORD_URI = URI + CHANGE_PASSWORD_RELATIVE_URI;

	static final String REGISTRATION_SUCCESS_MESSAGE_USER = "You have successfully registered, an activation e-mail has been sent to your email.";
	static final String REGISTRATION_SUCCESS_MESSAGE_ADMIN = "You have successfully registered, your request has been forwarded to the administrator.";

	private final AccountService accountService;
	private final CaptchaService captchaService;
	private final RedirectStrategy redirectStrategy;
	private final AuthenticationSettings authenticationSettings;
	private final UserFactory userFactory;

	public AccountController(AccountService accountService, CaptchaService captchaService,
			RedirectStrategy redirectStrategy, AuthenticationSettings authenticationSettings, UserFactory userFactory)
	{
		this.accountService = requireNonNull(accountService);
		this.captchaService = requireNonNull(captchaService);
		this.redirectStrategy = requireNonNull(redirectStrategy);
		this.authenticationSettings = requireNonNull(authenticationSettings);
		this.userFactory = requireNonNull(userFactory);
	}

	@GetMapping("/login")
	public String getLoginForm()
	{
		return "login-modal";
	}

	@GetMapping("/register")
	public ModelAndView getRegisterForm()
	{
		ModelAndView model = new ModelAndView("register-modal");
		model.addObject("countries", CountryCodes.get());
		model.addObject("min_password_length", MIN_PASSWORD_LENGTH);
		return model;
	}

	@GetMapping("/password/reset")
	public String getPasswordResetForm()
	{
		return "resetpassword-modal";
	}

	@GetMapping(CHANGE_PASSWORD_RELATIVE_URI)
	public ModelAndView getChangePasswordForm()
	{
		ModelAndView model = new ModelAndView("view-change-password");
		model.addObject("min_password_length", MIN_PASSWORD_LENGTH);
		return model;
	}

	@PostMapping(CHANGE_PASSWORD_RELATIVE_URI)
	public void changePassword(@Valid ChangePasswordForm form, HttpServletRequest request, HttpServletResponse response)
			throws IOException
	{
		try
		{
			// Change password of current user
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			if (authentication != null)
			{
				accountService.changePassword(authentication.getName(), form.getPassword1());
			}

			// Redirect to homepage
			redirectStrategy.sendRedirect(request, response, "/");
		}
		catch (Exception e)
		{
			LOG.error("Error changing password", e);
		}
	}

	// Spring's FormHttpMessageConverter cannot bind target classes (as ModelAttribute can)
	@PostMapping(value = "/register", headers = "Content-Type=application/x-www-form-urlencoded")
	@ResponseBody
	public Map<String, String> registerUser(@Valid @ModelAttribute RegisterRequest registerRequest,
			@Valid @ModelAttribute CaptchaRequest captchaRequest, HttpServletRequest request) throws Exception
	{
		if (authenticationSettings.getSignUp())
		{
			if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword()))
			{
				throw new BindException(RegisterRequest.class, "password does not match confirm password");
			}
			if (!captchaService.validateCaptcha(captchaRequest.getCaptcha()))
			{
				throw new CaptchaException("invalid captcha answer");
			}
			User user = toUser(registerRequest);
			String activationUri = null;
			if (StringUtils.isEmpty(request.getHeader("X-Forwarded-Host")))
			{
				activationUri = ServletUriComponentsBuilder.fromCurrentRequest()
														   .replacePath(URI + "/activate")
														   .build()
														   .toUriString();
			}
			else
			{
				String scheme = request.getHeader("X-Forwarded-Proto");
				if (scheme == null) scheme = request.getScheme();
				activationUri = scheme + "://" + request.getHeader("X-Forwarded-Host") + URI + "/activate";
			}
			accountService.createUser(user, activationUri);

			String successMessage = authenticationSettings.getSignUpModeration() ? REGISTRATION_SUCCESS_MESSAGE_ADMIN : REGISTRATION_SUCCESS_MESSAGE_USER;
			captchaService.removeCaptcha();
			return Collections.singletonMap("message", successMessage);
		}
		else
		{
			throw new NoPermissionException("Self registration is disabled");
		}
	}

	@GetMapping("/activate/{activationCode}")
	public String activateUser(@Valid @NotNull @PathVariable String activationCode, Model model)
	{
		try
		{
			accountService.activateUser(activationCode);
			model.addAttribute("successMessage", "Your account has been activated, you can now sign in.");
		}
		catch (MolgenisUserException e)
		{
			model.addAttribute("warningMessage", e.getMessage());
		}
		catch (RuntimeException e)
		{
			model.addAttribute("errorMessage", e.getMessage());
		}
		return "forward:/";
	}

	// Spring's FormHttpMessageConverter cannot bind target classes (as ModelAttribute can)
	@PostMapping(value = "/password/reset", headers = "Content-Type=application/x-www-form-urlencoded")
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
		LOG.debug("", e);
		return new ErrorMessageResponse(Collections.singletonList(new ErrorMessage(e.getMessage())));
	}

	@ExceptionHandler(UsernameAlreadyExistsException.class)
	@ResponseStatus(value = HttpStatus.BAD_REQUEST)
	@ResponseBody
	public ErrorMessageResponse handleUsernameAlreadyExistsException(UsernameAlreadyExistsException e)
	{
		LOG.debug("", e);
		return new ErrorMessageResponse(Collections.singletonList(new ErrorMessage(e.getMessage())));
	}

	@ExceptionHandler(EmailAlreadyExistsException.class)
	@ResponseStatus(value = HttpStatus.BAD_REQUEST)
	@ResponseBody
	public ErrorMessageResponse handleEmailAlreadyExistsException(EmailAlreadyExistsException e)
	{
		LOG.debug("", e);
		return new ErrorMessageResponse(Collections.singletonList(new ErrorMessage(e.getMessage())));
	}

	@ExceptionHandler(MolgenisDataException.class)
	@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
	@ResponseBody
	public ErrorMessageResponse handleMolgenisDataException(MolgenisDataException e)
	{
		LOG.error("", e);
		return new ErrorMessageResponse(Collections.singletonList(new ErrorMessage(e.getMessage())));
	}

	@ExceptionHandler(RuntimeException.class)
	@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
	@ResponseBody
	public ErrorMessageResponse handleRuntimeException(RuntimeException e)
	{
		LOG.error("", e);
		return new ErrorMessageResponse(Collections.singletonList(new ErrorMessage(e.getMessage())));
	}

	private User toUser(RegisterRequest request)
	{
		User user = userFactory.create();
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
		user.setChangePassword(false);
		user.setSuperuser(false);
		return user;
	}
}

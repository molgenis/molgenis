package org.molgenis.core.ui.admin.user;

import com.google.common.collect.Lists;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.commons.lang3.StringUtils;
import org.molgenis.core.util.CountryCodes;
import org.molgenis.data.security.auth.User;
import org.molgenis.i18n.LanguageService;
import org.molgenis.security.login.MolgenisLoginController;
import org.molgenis.security.settings.AuthenticationSettings;
import org.molgenis.security.twofactor.TwoFactorAuthenticationController;
import org.molgenis.security.twofactor.auth.TwoFactorAuthenticationSetting;
import org.molgenis.security.twofactor.model.RecoveryCode;
import org.molgenis.security.twofactor.service.RecoveryService;
import org.molgenis.security.twofactor.service.TwoFactorAuthenticationService;
import org.molgenis.security.user.MolgenisUserException;
import org.molgenis.security.user.UserAccountService;
import org.molgenis.web.ErrorMessageResponse;
import org.molgenis.web.ErrorMessageResponse.ErrorMessage;
import org.molgenis.web.PluginController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.LocaleResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Stream;

import static java.text.MessageFormat.format;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.molgenis.core.ui.admin.user.UserAccountController.URI;
import static org.molgenis.security.user.UserAccountService.MIN_PASSWORD_LENGTH;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

@Api("User account")
@Controller
@RequestMapping(URI)
public class UserAccountController extends PluginController
{
	private static final Logger LOG = LoggerFactory.getLogger(UserAccountController.class);

	public static final String ID = "useraccount";
	public static final String URI = PluginController.PLUGIN_URI_PREFIX + ID;

	private final UserAccountService userAccountService;
	private final RecoveryService recoveryService;
	private final TwoFactorAuthenticationService twoFactorAuthenticationService;
	private final AuthenticationSettings authenticationSettings;
	private final LocaleResolver localeResolver;

	public UserAccountController(UserAccountService userAccountService, RecoveryService recoveryService,
			TwoFactorAuthenticationService twoFactorAuthenticationService,
			AuthenticationSettings authenticationSettings, LocaleResolver localeResolver)
	{
		super(URI);
		this.userAccountService = requireNonNull(userAccountService);
		this.recoveryService = requireNonNull(recoveryService);
		this.twoFactorAuthenticationService = requireNonNull(twoFactorAuthenticationService);
		this.authenticationSettings = requireNonNull(authenticationSettings);
		this.localeResolver = requireNonNull(localeResolver);
	}

	@ApiOperation("Show account")
	@ApiResponses({ @ApiResponse(code = 200, message = "Return the view of the account", response = String.class) })
	@GetMapping
	public String showAccount(Model model, @RequestParam(value = "showCodes", defaultValue = "false") boolean showCodes)
	{
		TwoFactorAuthenticationSetting twoFactorAuthenticationApp = authenticationSettings.getTwoFactorAuthentication();
		boolean isTwoFactorAuthenticationEnableForUser = userAccountService.getCurrentUser()
																		   .isTwoFactorAuthentication();

		model.addAttribute("user", userAccountService.getCurrentUser());
		model.addAttribute("countries", CountryCodes.get());
		model.addAttribute("groups", Lists.newArrayList(userAccountService.getCurrentUserGroups()));
		model.addAttribute("min_password_length", MIN_PASSWORD_LENGTH);
		model.addAttribute("two_factor_authentication_app_option", twoFactorAuthenticationApp);
		model.addAttribute("two_factor_authentication_user_enabled", isTwoFactorAuthenticationEnableForUser);
		model.addAttribute("show_recovery_codes", showCodes);
		return "view-useraccount";
	}

	@ApiOperation("Updates the selected user language")
	@ApiResponses({ @ApiResponse(code = 204, message = "Update succeeded"),
			@ApiResponse(code = 403, message = "Access denied. You need write permission on the UserAccount plugin", response = ErrorMessageResponse.class),
			@ApiResponse(code = 400, message = "Bad request. You need to provide a valid language code", response = ErrorMessageResponse.class) })
	@PostMapping(value = "/language/update", produces = APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void updateUserLanguage(HttpServletRequest request, HttpServletResponse response,
			@RequestParam("languageCode") String languageCode)
	{
		if (!LanguageService.hasLanguageCode(languageCode))
		{
			throw new MolgenisUserException(format("Unknown language code ''{0}''", languageCode));
		}
		localeResolver.setLocale(request, response, new Locale(languageCode));
	}

	@ApiOperation("Updated the useraccount")
	@ApiResponses({ @ApiResponse(code = 200, message = "Account is updated"),
			@ApiResponse(code = 400, message = "Invalid password entered", response = ErrorMessageResponse.class) })
	@PostMapping(value = "/update", headers = "Content-Type=application/x-www-form-urlencoded")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void updateAccount(@Valid @NotNull AccountUpdateRequest updateRequest)
	{
		String newPassword = validatePasswordInUpdateRequest(updateRequest);

		// update current user
		User user = userAccountService.getCurrentUser();

		if (isNotEmpty(newPassword)) user.setPassword(newPassword);
		if (isNotEmpty(updateRequest.getPhone())) user.setPhone(updateRequest.getPhone());
		if (isNotEmpty(updateRequest.getFax())) user.setFax(updateRequest.getFax());
		if (isNotEmpty(updateRequest.getTollFreePhone()))
		{
			user.setTollFreePhone(updateRequest.getTollFreePhone());
		}
		if (isNotEmpty(updateRequest.getAddress())) user.setAddress(updateRequest.getAddress());
		if (isNotEmpty(updateRequest.getTitle())) user.setTitle(updateRequest.getTitle());
		if (isNotEmpty(updateRequest.getFirstname())) user.setFirstName(updateRequest.getFirstname());
		if (isNotEmpty(updateRequest.getMiddleNames())) user.setMiddleNames(updateRequest.getMiddleNames());
		if (isNotEmpty(updateRequest.getLastname())) user.setLastName(updateRequest.getLastname());
		if (isNotEmpty(updateRequest.getInstitute())) user.setAffiliation(updateRequest.getInstitute());
		if (isNotEmpty(updateRequest.getDepartment())) user.setDepartment(updateRequest.getDepartment());
		if (isNotEmpty(updateRequest.getPosition())) user.setRole(updateRequest.getPosition());
		if (isNotEmpty(updateRequest.getCity())) user.setCity(updateRequest.getCity());
		if (isNotEmpty(updateRequest.getCountry()))
		{
			user.setCountry(CountryCodes.get(updateRequest.getCountry()));
		}

		userAccountService.updateCurrentUser(user);
	}

	private String validatePasswordInUpdateRequest(@Valid @NotNull AccountUpdateRequest updateRequest)
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
			if (newPassword.length() < MIN_PASSWORD_LENGTH)
			{
				throw new MolgenisUserException("New password must consist of at least 6 characters.");
			}
		}
		return newPassword;
	}

	@ApiOperation("Enable two factor authentication")
	@ApiResponses({ @ApiResponse(code = 200, message = "Two factor authentication enabled"),
			@ApiResponse(code = 400, message = "Could not enable two factor authentication for user") })
	@PostMapping(TwoFactorAuthenticationController.URI + "/enable")
	public String enableTwoFactorAuthentication()
	{
		twoFactorAuthenticationService.enableForUser();

		return "redirect:" + MolgenisLoginController.URI;
	}

	@ApiOperation("Disable two factor authentication")
	@ApiResponses({ @ApiResponse(code = 200, message = "Two factor authentication disabled"),
			@ApiResponse(code = 400, message = "Could not disable two factor authentication for user", response = ErrorMessageResponse.class) })
	@PostMapping(TwoFactorAuthenticationController.URI + "/disable")
	public String disableTwoFactorAuthentication(Model model)
	{
		twoFactorAuthenticationService.disableForUser();
		downgradeUserAuthentication();

		return showAccount(model, false);
	}

	@ApiOperation("Reset two factor authentication")
	@ApiResponses({ @ApiResponse(code = 200, message = "Two factor authentication is reset"),
			@ApiResponse(code = 400, message = "Could not rest two factor authentication for user", response = ErrorMessageResponse.class) })
	@PostMapping(TwoFactorAuthenticationController.URI + "/reset")
	public String resetTwoFactorAuthentication()
	{
		twoFactorAuthenticationService.resetSecretForUser();

		return "redirect:" + TwoFactorAuthenticationController.URI
				+ TwoFactorAuthenticationController.TWO_FACTOR_ACTIVATION_URI;
	}

	/**
	 * <p>Set AuthenticationToken back to {@link UsernamePasswordAuthenticationToken} generated with default DaoAuthenticationProvider</p>
	 */
	private void downgradeUserAuthentication()
	{
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(auth.getPrincipal(),
				auth.getCredentials(), auth.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(token);
	}

	@ApiOperation("Get recoverycodes for two factor authentication")
	@ApiResponses({ @ApiResponse(code = 200, message = "Returns a list of recoverycodes", response = Map.class),
			@ApiResponse(code = 500, message = "Something went wrong retrieving the recoverycodes", response = ErrorMessageResponse.class) })
	@GetMapping("recoveryCodes")
	@ResponseBody
	public Map<String, List<String>> getRecoveryCodes()
	{
		return convertToRecoveryCodesMap(
				recoveryService.getRecoveryCodes().map(RecoveryCode::getCode).collect(toList()));
	}

	@ApiOperation("Generate recoverycodes for two factor authentication")
	@ApiResponses({
			@ApiResponse(code = 200, message = "Returns a list of generated recoverycodes", response = Map.class),
			@ApiResponse(code = 500, message = "Something went wrong generating the recoverycodes", response = ErrorMessageResponse.class) })
	@GetMapping("generateRecoveryCodes")
	@ResponseBody
	public Map<String, List<String>> generateRecoveryCodes()
	{
		Stream<RecoveryCode> recoveryCodes = recoveryService.generateRecoveryCodes();
		return convertToRecoveryCodesMap(recoveryCodes.map(RecoveryCode::getCode).collect(toList()));
	}

	/**
	 * Convert the list from the recoveryService to a Map for usability in client
	 *
	 * @param recoveryCodesList list from recoveryService
	 * @return Map&lt;String, List&lt;String&gt;&gt;
	 */
	private Map<String, List<String>> convertToRecoveryCodesMap(List<String> recoveryCodesList)
	{
		Map<String, List<String>> recoveryCodes = new HashMap<>();
		recoveryCodes.put("recoveryCodes", recoveryCodesList);
		return recoveryCodes;
	}

	@ExceptionHandler(MolgenisUserException.class)
	@ResponseStatus(value = HttpStatus.BAD_REQUEST)
	@ResponseBody
	private ErrorMessageResponse handleMolgenisUserException(MolgenisUserException e)
	{
		LOG.debug("", e);
		return new ErrorMessageResponse(Collections.singletonList(new ErrorMessage(e.getMessage())));
	}

	@ExceptionHandler(AccessDeniedException.class)
	@ResponseStatus(value = HttpStatus.FORBIDDEN)
	@ResponseBody
	private ErrorMessageResponse handleAccessDeniedException(AccessDeniedException e)
	{
		LOG.warn("Access denied", e);
		return new ErrorMessageResponse(Collections.singletonList(new ErrorMessage(e.getMessage())));
	}

	@ExceptionHandler(RuntimeException.class)
	@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
	@Order(Ordered.HIGHEST_PRECEDENCE)
	@ResponseBody
	private ErrorMessageResponse handleRuntimeException(RuntimeException e)
	{
		LOG.error("", e);
		return new ErrorMessageResponse(Collections.singletonList(new ErrorMessage(e.getMessage())));
	}
}
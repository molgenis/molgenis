package org.molgenis.ui.admin.user;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.i18n.LanguageService;
import org.molgenis.security.core.model.User;
import org.molgenis.security.core.service.GroupService;
import org.molgenis.security.core.service.MolgenisUserException;
import org.molgenis.security.core.service.UserAccountService;
import org.molgenis.security.login.MolgenisLoginController;
import org.molgenis.security.settings.AuthenticationSettings;
import org.molgenis.security.twofactor.TwoFactorAuthenticationController;
import org.molgenis.security.twofactor.auth.TwoFactorAuthenticationSetting;
import org.molgenis.security.twofactor.model.RecoveryCode;
import org.molgenis.security.twofactor.service.RecoveryService;
import org.molgenis.security.twofactor.service.TwoFactorAuthenticationService;
import org.molgenis.util.CountryCodes;
import org.molgenis.util.ErrorMessageResponse;
import org.molgenis.util.ErrorMessageResponse.ErrorMessage;
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

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.text.MessageFormat.format;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.molgenis.security.core.service.UserAccountService.MIN_PASSWORD_LENGTH;
import static org.molgenis.ui.admin.user.UserAccountController.URI;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

@Controller
@RequestMapping(URI)
public class UserAccountController extends PluginController
{
	private static final Logger LOG = LoggerFactory.getLogger(UserAccountController.class);

	public static final String ID = "useraccount";
	public static final String URI = PluginController.PLUGIN_URI_PREFIX + ID;

	private final UserAccountService userAccountService;
	private final GroupService groupService;

	private final RecoveryService recoveryService;
	private final TwoFactorAuthenticationService twoFactorAuthenticationService;
	private final AuthenticationSettings authenticationSettings;

	public UserAccountController(UserAccountService userAccountService, RecoveryService recoveryService,
			TwoFactorAuthenticationService twoFactorAuthenticationService,
			AuthenticationSettings authenticationSettings, GroupService groupService)
	{
		super(URI);
		this.userAccountService = requireNonNull(userAccountService);
		this.recoveryService = requireNonNull(recoveryService);
		this.twoFactorAuthenticationService = requireNonNull(twoFactorAuthenticationService);
		this.authenticationSettings = requireNonNull(authenticationSettings);
		this.groupService = requireNonNull(groupService);
	}

	@GetMapping
	public String showAccount(Model model, @RequestParam(defaultValue = "false") boolean showCodes)
	{
		TwoFactorAuthenticationSetting twoFactorAuthenticationApp = authenticationSettings.getTwoFactorAuthentication();

		User user = userAccountService.getCurrentUser();
		boolean isTwoFactorAuthenticationEnableForUser = user.isTwoFactorAuthentication();

		model.addAttribute("user", user);
		model.addAttribute("countries", CountryCodes.get());
		model.addAttribute("groups", groupService.getCurrentGroups(user));
		model.addAttribute("min_password_length", MIN_PASSWORD_LENGTH);
		model.addAttribute("two_factor_authentication_app_option", twoFactorAuthenticationApp);
		model.addAttribute("two_factor_authentication_user_enabled", isTwoFactorAuthenticationEnableForUser);
		model.addAttribute("show_recovery_codes", showCodes);
		return "view-useraccount";
	}

	@PostMapping(value = "/language/update", produces = APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void updateUserLanguage(@RequestParam("languageCode") String languageCode)
	{
		if (!LanguageService.hasLanguageCode(languageCode))
		{
			throw new MolgenisUserException(format("Unknown language code ''{0}''", languageCode));
		}
		userAccountService.updateCurrentUser(
				userAccountService.getCurrentUser().toBuilder().languageCode(languageCode).build());
	}

	@PostMapping(value = "/update", headers = "Content-Type=application/x-www-form-urlencoded")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void updateAccount(@Valid @NotNull AccountUpdateRequest updateRequest)
	{
		String newPassword = validatePasswordInUpdateRequest(updateRequest);

		// update current user
		User.Builder user = userAccountService.getCurrentUser().toBuilder();
		if (isNotEmpty(newPassword)) user.password(newPassword);
		if (isNotEmpty(updateRequest.getPhone())) user.phone(updateRequest.getPhone());
		if (isNotEmpty(updateRequest.getFax())) user.fax(updateRequest.getFax());
		if (isNotEmpty(updateRequest.getTollFreePhone()))
		{
			user.tollFreePhone(updateRequest.getTollFreePhone());
		}
		if (isNotEmpty(updateRequest.getAddress())) user.address(updateRequest.getAddress());
		if (isNotEmpty(updateRequest.getTitle())) user.title(updateRequest.getTitle());
		if (isNotEmpty(updateRequest.getFirstname())) user.firstName(updateRequest.getFirstname());
		if (isNotEmpty(updateRequest.getMiddleNames())) user.middleNames(updateRequest.getMiddleNames());
		if (isNotEmpty(updateRequest.getLastname())) user.lastName(updateRequest.getLastname());
		if (isNotEmpty(updateRequest.getInstitute())) user.affiliation(updateRequest.getInstitute());
		if (isNotEmpty(updateRequest.getDepartment())) user.department(updateRequest.getDepartment());
		if (isNotEmpty(updateRequest.getPosition())) user.role(updateRequest.getPosition());
		if (isNotEmpty(updateRequest.getCity())) user.city(updateRequest.getCity());
		if (isNotEmpty(updateRequest.getCountry()))
		{
			user.country(CountryCodes.get(updateRequest.getCountry()));
		}
		userAccountService.updateCurrentUser(user.build());
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

	@PostMapping(TwoFactorAuthenticationController.URI + "/enable")
	public String enableTwoFactorAuthentication()
	{
		twoFactorAuthenticationService.enableForUser();

		return "redirect:" + MolgenisLoginController.URI;
	}

	@PostMapping(TwoFactorAuthenticationController.URI + "/disable")
	public String disableTwoFactorAuthentication(Model model)
	{
		twoFactorAuthenticationService.disableForUser();
		downgradeUserAuthentication();

		return showAccount(model, false);
	}

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

	@GetMapping("recoveryCodes")
	@ResponseBody
	public Map<String, List<String>> getRecoveryCodes()
	{
		return convertToRecoveryCodesMap(recoveryService.getRecoveryCodes());
	}

	@GetMapping("generateRecoveryCodes")
	@ResponseBody
	public Map<String, List<String>> generateRecoveryCodes()
	{
		return convertToRecoveryCodesMap(recoveryService.generateRecoveryCodes());
	}

	/**
	 * Convert the RecoveryCodes from the recoveryService to a Map for usability in client
	 *
	 * @param recoveryCodes stream of RecoveryCodes from recoveryService
	 * @return Map&lt;String, List&lt;String&gt;&gt;
	 */
	private Map<String, List<String>> convertToRecoveryCodesMap(Stream<RecoveryCode> recoveryCodes)
	{
		return ImmutableMap.of("recoveryCodes", recoveryCodes.map(RecoveryCode::getCode).collect(toList()));
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
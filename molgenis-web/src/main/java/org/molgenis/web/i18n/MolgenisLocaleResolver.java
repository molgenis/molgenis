package org.molgenis.web.i18n;

import org.molgenis.data.DataService;
import org.molgenis.data.security.auth.User;
import org.molgenis.data.security.auth.UserMetaData;
import org.molgenis.i18n.LanguageService;
import org.springframework.web.servlet.LocaleResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.molgenis.data.security.auth.UserMetaData.USER;
import static org.molgenis.i18n.LanguageService.DEFAULT_LANGUAGE_CODE;
import static org.molgenis.security.core.runas.RunAsSystemAspect.runAsSystem;
import static org.molgenis.security.core.utils.SecurityUtils.ANONYMOUS_USERNAME;
import static org.molgenis.security.core.utils.SecurityUtils.getCurrentUsername;

/**
 * Resolves the locale.
 */
public class MolgenisLocaleResolver implements LocaleResolver
{
	private final DataService dataService;
	private final Supplier<Locale> fallbackLocaleSupplier;

	public MolgenisLocaleResolver(DataService dataService, Supplier<Locale> fallbackLocaleSupplier)
	{
		this.dataService = dataService;
		this.fallbackLocaleSupplier = Objects.requireNonNull(fallbackLocaleSupplier);
	}

	@Override
	public Locale resolveLocale(HttpServletRequest request)
	{
		Stream<Supplier<Optional<String>>> candidates = Stream.of(() -> getCurrentUser().map(User::getLanguageCode),
				() -> Optional.of(fallbackLocaleSupplier.get().getLanguage()));
		String languageCode = candidates.map(Supplier::get)
										.map(candidate -> candidate.filter(LanguageService::hasLanguageCode))
										.filter(Optional::isPresent)
										.map(Optional::get)
										.findFirst()
										.orElse(DEFAULT_LANGUAGE_CODE);
		return Locale.forLanguageTag(languageCode);
	}

	private Optional<User> getCurrentUser()
	{
		Optional<String> currentUserName = Optional.ofNullable(getCurrentUsername())
												   .filter(username -> !ANONYMOUS_USERNAME.equals(username));
		return runAsSystem(() -> currentUserName.flatMap(name -> Optional.ofNullable(
				dataService.query(USER, User.class).eq(UserMetaData.USERNAME, name).findOne())));
	}

	@Override
	public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale)
	{
		String languageCode = locale.getLanguage();
		if (!LanguageService.hasLanguageCode(languageCode))
		{
			throw new UnsupportedOperationException("Cannot set language to unsupported languageCode");
		}
		User user = getCurrentUser().orElseThrow(
				() -> new UnsupportedOperationException("Cannot change language if not logged in"));
		user.setLanguageCode(locale.getLanguage());
		runAsSystem(() -> dataService.update(USER, user));
	}
}

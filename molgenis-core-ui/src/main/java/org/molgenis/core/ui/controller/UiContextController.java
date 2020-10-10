package org.molgenis.core.ui.controller;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.molgenis.security.core.utils.SecurityUtils.currentUserIsAuthenticated;

import com.google.common.collect.ImmutableMap;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.List;
import java.util.Optional;
import org.molgenis.core.ui.cookiewall.CookieWallService;
import org.molgenis.data.security.auth.User;
import org.molgenis.security.user.UserAccountService;
import org.molgenis.settings.AppSettings;
import org.molgenis.web.menu.MenuReaderService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Api("UI Context")
@Controller
@RequestMapping(UiContextController.ID)
public class UiContextController {

  public static final String ID = "app-ui-context";

  public static final String LOGIN_HREF = "/login";
  public static final String HELP_HREF = "https://molgenis.gitbook.io/molgenis/";

  private final AppSettings appSettings;
  private final CookieWallService cookieWallService;
  private final MenuReaderService menuReaderService;
  private final String molgenisVersion;
  private final String molgenisBuildDate;
  private final UserAccountService userAccountService;

  public UiContextController(
      AppSettings appSettings,
      CookieWallService cookieWallService,
      MenuReaderService menuReaderService,
      UserAccountService userAccountService,
      @Value("${molgenis.version}") String molgenisVersion,
      @Value("${molgenis.build.date}") String molgenisBuildDate) {
    this.appSettings = requireNonNull(appSettings);
    this.cookieWallService = requireNonNull(cookieWallService);
    this.menuReaderService = requireNonNull(menuReaderService);
    this.molgenisVersion = requireNonNull(molgenisVersion);
    this.molgenisBuildDate = requireNonNull(molgenisBuildDate);
    this.userAccountService = requireNonNull(userAccountService);
  }

  @ApiOperation(value = "Returns the ui context object", response = ResponseEntity.class)
  @ApiResponses({
    @ApiResponse(
        code = 200,
        message = "Returns object containing settings relevant for user interface ",
        response = ResponseEntity.class)
  })
  private static List<String> getCurrentUserRoles() {
    return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
        .map(Authentication::getAuthorities)
        .map(
            authorities ->
                authorities.stream().map(GrantedAuthority::getAuthority).collect(toList()))
        .orElse(emptyList());
  }

  @GetMapping("/**")
  @ResponseBody
  public UiContextResponse getContext() {
    User user = userAccountService.getCurrentUser();

    return UiContextResponse.builder()
        .setMenu(menuReaderService.getMenu().orElse(null))
        .setSelectedTheme(appSettings.getThemeURL())
        .setCssHref(appSettings.getCssHref())
        .setNavBarLogo(appSettings.getLogoNavBarHref())
        .setLogoTop(appSettings.getLogoTopHref())
        .setLogoTopMaxHeight(appSettings.getLogoTopMaxHeight())
        .setLoginHref(LOGIN_HREF)
        .setHelpLink(ImmutableMap.of("label", "Help", "href", HELP_HREF))
        .setShowCookieWall(cookieWallService.showCookieWall())
        .setAuthenticated(currentUserIsAuthenticated())
        .setUsername(user.getUsername())
        .setEmail(user.getEmail())
        .setRoles(getCurrentUserRoles())
        .setAdditionalMessage(appSettings.getFooter())
        .setVersion(this.molgenisVersion)
        .setBuildDate(this.molgenisBuildDate)
        .build();
  }
}

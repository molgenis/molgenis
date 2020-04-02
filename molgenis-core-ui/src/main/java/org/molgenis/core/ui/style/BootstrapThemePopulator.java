package org.molgenis.core.ui.style;

import static java.util.Objects.requireNonNull;
import static org.molgenis.core.ui.style.StyleSheetMetadata.STYLE_SHEET;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.molgenis.data.DataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

@Component
public class BootstrapThemePopulator {

  private static final Logger LOG = LoggerFactory.getLogger(BootstrapThemePopulator.class);

  private static final String LOCAL_CSS_BOOTSTRAP_3_THEME_LOCATION =
      "classpath*:css/bootstrap-*.min.css";
  private static final String LOCAL_CSS_BOOTSTRAP_4_THEME_LOCATION =
      "classpath*:css/bootstrap-4/bootstrap-*.min.css";

  private final StyleService styleService;
  private final DataService dataService;

  public BootstrapThemePopulator(StyleService styleService, DataService dataService) {

    this.styleService = requireNonNull(styleService);
    this.dataService = requireNonNull(dataService);
  }

  /**
   * Populate the database with the available bootstrap themes found in the jar. This enables the
   * release of the application with a set of predefined bootstrap themes.
   *
   * <p>If a given bootstrap 3 theme is located a matching bootstrap 4 theme is added the the
   * styleSheet row is present.
   */
  public void populate() {
    PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
    try {
      Resource[] bootstrap3Themes = resolver.getResources(LOCAL_CSS_BOOTSTRAP_3_THEME_LOCATION);
      Resource[] bootstrap4Themes = resolver.getResources(LOCAL_CSS_BOOTSTRAP_4_THEME_LOCATION);

      // filter out themes already stored in the database
      List<Resource> newThemes =
          Arrays.stream(bootstrap3Themes)
              .filter(
                  theme ->
                      dataService.getRepository(STYLE_SHEET).findOneById(theme.getFilename())
                          == null)
              .collect(Collectors.toList());

      newThemes.forEach(nt -> addNewTheme(nt, bootstrap4Themes));

    } catch (Exception e) {
      LOG.error("error populating bootstrap themes", e);
    }
  }

  /**
   * Guess witch bootstrap4 theme file to combine with the bootstrap 3 theme file based on the file
   * name. It is assumed all resources where resolved using the "bootstrap-[theme-name].min.css"
   * template.
   *
   * <p>Not all of the bootstrap 3 themes have a matching bootstrap 4 theme.
   */
  private Optional<Resource> guessMatchingBootstrap4File(
      Resource[] bootstrap4Themes, String bootstrap3ThemeFileName) {
    return Arrays.stream(bootstrap4Themes)
        .filter(bootstrap4Theme -> bootstrap3ThemeFileName.equals(bootstrap4Theme.getFilename()))
        .findFirst();
  }

  private void addNewTheme(Resource bootstrap3Resource, Resource[] bootstrap4Themes) {
    String bootstrap3FileName = bootstrap3Resource.getFilename();

    LOG.debug("Add theme with name {}", bootstrap3FileName);

    try (InputStream bootstrap3Data = bootstrap3Resource.getInputStream()) {
      Optional<Resource> bootstrap4Optional =
          guessMatchingBootstrap4File(bootstrap4Themes, bootstrap3FileName);

      if (bootstrap4Optional.isPresent()) {
        Resource bootstrap4resource = bootstrap4Optional.get();
        addBootstrap3And4Style(bootstrap3FileName, bootstrap3Data, bootstrap4resource);
      } else {
        LOG.debug("No matching bootstrap 4 theme found, falling back to default");
        styleService.addStyle(bootstrap3FileName, bootstrap3FileName, bootstrap3Data, null, null);
      }

    } catch (Exception e) {
      LOG.error("error adding new bootstrap themes", e);
    }
  }

  private void addBootstrap3And4Style(
      String bootstrap3FileName, InputStream bootstrap3Data, Resource bootstrap4resource) {
    try (InputStream bootstrap4Data = bootstrap4resource.getInputStream()) {
      String bootstrap4FileName = bootstrap4resource.getFilename();
      LOG.debug("Adding matching bootstrap 4 theme with name {}", bootstrap4FileName);
      styleService.addStyle(
          bootstrap3FileName,
          bootstrap3FileName,
          bootstrap3Data,
          bootstrap4FileName,
          bootstrap4Data);
    } catch (Exception e) {
      LOG.error("error adding new bootstrap 4 theme", e);
    }
  }
}

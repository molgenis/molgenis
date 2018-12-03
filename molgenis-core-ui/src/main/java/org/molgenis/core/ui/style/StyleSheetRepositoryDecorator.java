package org.molgenis.core.ui.style;

import java.util.stream.Stream;
import org.molgenis.data.AbstractRepositoryDecorator;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Repository;
import org.molgenis.settings.AppSettings;

/** Repository decorator that updates {@link StyleSheet} on {@link StyleSheet} changes. */
public class StyleSheetRepositoryDecorator extends AbstractRepositoryDecorator<StyleSheet> {
  private final AppSettings appSettings;

  public StyleSheetRepositoryDecorator(
      Repository<StyleSheet> delegateRepository, AppSettings appSettings) {
    super(delegateRepository);
    this.appSettings = appSettings;
  }

  @Override
  public void delete(StyleSheet sheet) {
    checkAndUpdateAppSettings(sheet.getId());
    super.delete(sheet);
  }

  @Override
  public void deleteById(Object id) {
    checkAndUpdateAppSettings(id);
    super.deleteById(id);
  }

  @Override
  public void deleteAll() {
    throw new MolgenisDataException(
        "Cannot delete all boostrap themes, at least one theme is needed for the application");
  }

  @Override
  public void delete(Stream<StyleSheet> styleSheetStream) {
    styleSheetStream.forEach(sheet -> checkAndUpdateAppSettings(sheet.getId()));
    super.delete(styleSheetStream);
  }

  @Override
  public void deleteAll(Stream<Object> ids) {
    ids.forEach(this::checkAndUpdateAppSettings);
    super.deleteAll(ids);
  }

  private void checkAndUpdateAppSettings(Object id) {
    if (appSettings.getBootstrapTheme().equals(id)) {
      throw new MolgenisDataException("Cannot delete the currently selected bootstrap theme");
    }
  }
}

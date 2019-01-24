package org.molgenis.data.i18n;

import java.util.stream.Stream;
import org.molgenis.data.AbstractRepositoryDecorator;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Repository;
import org.molgenis.data.i18n.model.Language;
import org.molgenis.i18n.LanguageService;

class LanguageRepositoryDecorator extends AbstractRepositoryDecorator<Language> {
  LanguageRepositoryDecorator(Repository<Language> delegateRepository) {
    super(delegateRepository);
  }

  @Override
  public void delete(Language language) {
    throw new LanguageModificationException();
  }

  @Override
  public void delete(Stream<Language> entities) {
    entities.forEach(this::delete);
    throw new MolgenisDataException();
  }

  @Override
  public void deleteById(Object id) {
    Language entity = findOneById(id);
    if (entity != null) delete(entity);
  }

  @Override
  public void deleteAll(Stream<Object> ids) {
    ids.forEach(this::deleteById);
  }

  @Override
  public void deleteAll() {
    forEachBatched(entities -> delete(entities.stream()), 1000);
  }

  @Override
  public void add(Language language) {
    validateLanguage(language);
    delegate().add(language);
  }

  @Override
  public Integer add(Stream<Language> languageStream) {
    return delegate().add(languageStream.filter(this::validateLanguage));
  }

  private boolean validateLanguage(Language language) {
    String languageCode = language.getCode();
    return validateLanguage(languageCode);
  }

  private boolean validateLanguage(String languageCode) {
    if (!LanguageService.hasLanguageCode(languageCode)) {
      throw new LanguageModificationException();
    }
    return true;
  }
}

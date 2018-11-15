package org.molgenis.jobs;

import com.google.auto.value.AutoValue;
import java.util.Locale;
import org.springframework.security.core.Authentication;

@AutoValue
@SuppressWarnings(
    "squid:S1610") // Abstract classes without fields should be converted to interfaces
abstract class JobExecutionContext {
  abstract Authentication getAuthentication();

  abstract Locale getLocale();

  static JobExecutionContext create(Authentication newAuthentication, Locale newLocale) {
    return builder().setAuthentication(newAuthentication).setLocale(newLocale).build();
  }

  static Builder builder() {
    return new AutoValue_JobExecutionContext.Builder();
  }

  @AutoValue.Builder
  abstract static class Builder {

    abstract Builder setAuthentication(Authentication newAuthentication);

    abstract Builder setLocale(Locale newLocale);

    abstract JobExecutionContext build();
  }
}

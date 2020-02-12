package org.molgenis.bootstrap;

import static java.util.Objects.requireNonNull;

import io.micrometer.core.annotation.Timed;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.PriorityOrdered;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/** Application bootstrapper listener */
@SuppressWarnings("unused")
@Component
class BootstrapperListener implements ApplicationListener<ContextRefreshedEvent>, PriorityOrdered {
  private final Bootstrapper bootstrapper;

  BootstrapperListener(Bootstrapper bootstrapper) {
    this.bootstrapper = requireNonNull(bootstrapper);
  }

  @Timed(value = "bootstrap", description = "Timing information for the bootstrapping event.")
  @Override
  public void onApplicationEvent(ContextRefreshedEvent event) {
    // bootstrap only for root context, not for child contexts
    if (event.getApplicationContext().getParent() != null) {
      return;
    }

    bootstrapper.bootstrap(event);

    // prevent memory leak
    SecurityContextHolder.clearContext();
  }

  @Override
  public int getOrder() {
    // bootstrap application before doing anything else
    return HIGHEST_PRECEDENCE;
  }
}

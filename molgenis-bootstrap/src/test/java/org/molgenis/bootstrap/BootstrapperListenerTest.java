package org.molgenis.bootstrap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;

class BootstrapperListenerTest extends AbstractMockitoTest {
  @Mock private Bootstrapper bootstrapper;
  private BootstrapperListener bootstrapperListener;

  @BeforeEach
  void setUpBeforeEach() {
    bootstrapperListener = new BootstrapperListener(bootstrapper);
  }

  @Test
  void testOnApplicationEventParent() {
    ApplicationContext applicationContext = mock(ApplicationContext.class);
    ContextRefreshedEvent contextRefreshedEvent = mock(ContextRefreshedEvent.class);
    when(contextRefreshedEvent.getApplicationContext()).thenReturn(applicationContext);
    bootstrapperListener.onApplicationEvent(contextRefreshedEvent);

    verify(bootstrapper).bootstrap(contextRefreshedEvent);
  }

  @Test
  void testOnApplicationEventChild() {
    ApplicationContext parentApplicationContext = mock(ApplicationContext.class);
    ApplicationContext applicationContext = mock(ApplicationContext.class);
    when(applicationContext.getParent()).thenReturn(parentApplicationContext);
    ContextRefreshedEvent contextRefreshedEvent = mock(ContextRefreshedEvent.class);
    when(contextRefreshedEvent.getApplicationContext()).thenReturn(applicationContext);
    bootstrapperListener.onApplicationEvent(contextRefreshedEvent);

    verifyNoInteractions(bootstrapper);
  }

  @Test
  void testGetOrder() {
    assertEquals(Ordered.HIGHEST_PRECEDENCE, bootstrapperListener.getOrder());
  }
}

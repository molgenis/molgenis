package org.molgenis.test;

import static java.util.Objects.requireNonNull;
import static org.mockito.Mockito.mockitoSession;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.MockitoSession;
import org.mockito.quality.Strictness;

public class AbstractMockitoTest {
  private final Strictness strictness;

  private MockitoSession mockitoSession;

  public AbstractMockitoTest() {
    this.strictness = Strictness.STRICT_STUBS;
  }

  /** @deprecated use {{@link #AbstractMockitoTest()}} */
  @Deprecated
  public AbstractMockitoTest(Strictness strictness) {
    this.strictness = requireNonNull(strictness);
  }

  @BeforeEach
  protected void initMocks() {
    mockitoSession = mockitoSession().initMocks(this).strictness(strictness).startMocking();
  }

  @AfterEach
  protected void tearDownAfterMethod() {
    mockitoSession.finishMocking();
  }
}

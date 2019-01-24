package org.molgenis.test;

import static java.util.Objects.requireNonNull;
import static org.mockito.Mockito.mockitoSession;

import org.mockito.MockitoSession;
import org.mockito.quality.Strictness;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

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

  @BeforeMethod
  public void initMocks() {
    mockitoSession = mockitoSession().initMocks(this).strictness(strictness).startMocking();
  }

  @AfterMethod
  public void tearDownAfterMethod() {
    mockitoSession.finishMocking();
  }
}

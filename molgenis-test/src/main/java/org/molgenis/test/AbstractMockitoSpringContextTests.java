package org.molgenis.test;

import static java.util.Objects.requireNonNull;
import static org.mockito.Mockito.mockitoSession;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockitoSession;
import org.mockito.quality.Strictness;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
public class AbstractMockitoSpringContextTests {
  private final Strictness strictness;

  private MockitoSession mockitoSession;

  public AbstractMockitoSpringContextTests() {
    this(Strictness.STRICT_STUBS);
  }

  public AbstractMockitoSpringContextTests(Strictness strictness) {
    this.strictness = requireNonNull(strictness);
  }

  @BeforeEach
  public void initMocks() {
    mockitoSession = mockitoSession().initMocks(this).strictness(strictness).startMocking();
  }

  @AfterEach
  public void tearDownAfterMethod() {
    mockitoSession.finishMocking();
  }
}

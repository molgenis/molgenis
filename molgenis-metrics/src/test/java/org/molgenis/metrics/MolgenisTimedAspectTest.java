package org.molgenis.metrics;

import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleConfig;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.aspectj.lang.JoinPoint.StaticPart;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.test.AbstractMockitoTest;

class MolgenisTimedAspectTest extends AbstractMockitoTest {

  private MolgenisTimedAspect timedAspect;
  private SimpleMeterRegistry meterRegistry;
  @Mock private Clock clock;
  @Mock private ProceedingJoinPoint proceedingJoinPoint;
  @Mock private MethodSignature signature;
  @Mock private StaticPart staticPart;

  @BeforeEach
  void beforeMethod() {
    meterRegistry = new SimpleMeterRegistry(SimpleConfig.DEFAULT, clock);
    timedAspect = new MolgenisTimedAspect(meterRegistry);
  }

  @Test
  void testExecuteAnnotatedMethod() throws Throwable {
    when(clock.monotonicTime()).thenReturn(0L, 100L);
    when(proceedingJoinPoint.getSignature()).thenReturn(signature);
    when(signature.getMethod()).thenReturn(AnnotatedMethod.class.getMethod("annotatedMethod"));
    when(proceedingJoinPoint.getStaticPart()).thenReturn(staticPart);
    when(staticPart.getSignature()).thenReturn(signature);
    when(proceedingJoinPoint.getSignature()).thenReturn(signature);
    when(signature.getDeclaringTypeName()).thenReturn("AnnotatedMethod");
    when(signature.getName()).thenReturn("annotatedMethod");
    when(proceedingJoinPoint.proceed()).thenReturn(42);

    assertEquals(42, timedAspect.timedMethod(proceedingJoinPoint));

    verify(proceedingJoinPoint, times(1)).proceed();
    Timer timer = meterRegistry.get("test.method").timer();
    assertEquals(1, timer.count());
    assertEquals(100.0, timer.max(NANOSECONDS));
  }

  @Test
  void testExecuteAnnotatedClass() throws Throwable {
    doReturn(0L, 100L).when(clock).monotonicTime();
    when(proceedingJoinPoint.getSignature()).thenReturn(signature);
    when(signature.getMethod()).thenReturn(AnnotatedClass.class.getMethod("annotatedMethod"));
    when(proceedingJoinPoint.getStaticPart()).thenReturn(staticPart);
    when(staticPart.getSignature()).thenReturn(signature);
    when(proceedingJoinPoint.getSignature()).thenReturn(signature);
    when(signature.getDeclaringTypeName()).thenReturn("AnnotatedMethod");
    when(signature.getName()).thenReturn("annotatedMethod");
    when(proceedingJoinPoint.proceed()).thenReturn(42);

    assertEquals(42, timedAspect.timedClassMethod(proceedingJoinPoint));

    verify(proceedingJoinPoint, times(1)).proceed();
    Timer timer = meterRegistry.get("test.class").timer();
    assertEquals(1, timer.count());
    assertEquals(100.0, timer.max(NANOSECONDS));
  }

  /*
   A Method cannnot be mocked so we need to use a real one
  */
  public class AnnotatedMethod {
    @Timed(value = "test.method", description = "description")
    public int annotatedMethod() {
      return 1;
    }
  }

  @Timed(value = "test.class", description = "description")
  public class AnnotatedClass {
    public int annotatedMethod() {
      return 1;
    }
  }
}

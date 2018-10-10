package org.molgenis.metrics;

import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleConfig;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.aspectj.lang.JoinPoint.StaticPart;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.mockito.Mock;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class MolgenisTimedAspectTest extends AbstractMockitoTest {

  private MolgenisTimedAspect timedAspect;
  private SimpleMeterRegistry meterRegistry;
  @Mock private Clock clock;
  @Mock private ProceedingJoinPoint proceedingJoinPoint;
  @Mock private MethodSignature signature;
  @Mock private StaticPart staticPart;

  @BeforeMethod
  public void beforeMethod() {
    meterRegistry = new SimpleMeterRegistry(SimpleConfig.DEFAULT, clock);
    timedAspect = new MolgenisTimedAspect(meterRegistry);
  }

  @Test
  public void testExecuteAnnotatedMethod() throws Throwable {
    when(clock.monotonicTime()).thenReturn(0L, 100L);
    when(proceedingJoinPoint.getSignature()).thenReturn(signature);
    when(signature.getMethod()).thenReturn(AnnotatedMethod.class.getMethod("annotatedMethod"));
    when(proceedingJoinPoint.getStaticPart()).thenReturn(staticPart);
    when(staticPart.getSignature()).thenReturn(signature);
    when(proceedingJoinPoint.getSignature()).thenReturn(signature);
    when(signature.getDeclaringTypeName()).thenReturn("AnnotatedMethod");
    when(signature.getName()).thenReturn("annotatedMethod");
    when(proceedingJoinPoint.proceed()).thenReturn(42);

    assertEquals(timedAspect.timedMethod(proceedingJoinPoint), 42);

    verify(proceedingJoinPoint, times(1)).proceed();
    Timer timer = meterRegistry.get("test.method").timer();
    assertEquals(timer.count(), 1);
    assertEquals(timer.max(NANOSECONDS), 100.0);
  }

  @Test
  public void testExecuteAnnotatedClass() throws Throwable {
    when(clock.monotonicTime()).thenReturn(0L, 100L);
    when(proceedingJoinPoint.getSignature()).thenReturn(signature);
    when(signature.getMethod()).thenReturn(AnnotatedClass.class.getMethod("annotatedMethod"));
    when(proceedingJoinPoint.getStaticPart()).thenReturn(staticPart);
    when(staticPart.getSignature()).thenReturn(signature);
    when(proceedingJoinPoint.getSignature()).thenReturn(signature);
    when(signature.getDeclaringTypeName()).thenReturn("AnnotatedMethod");
    when(signature.getName()).thenReturn("annotatedMethod");
    when(proceedingJoinPoint.proceed()).thenReturn(42);

    assertEquals(timedAspect.timedClassMethod(proceedingJoinPoint), 42);

    verify(proceedingJoinPoint, times(1)).proceed();
    Timer timer = meterRegistry.get("test.class").timer();
    assertEquals(timer.count(), 1);
    assertEquals(timer.max(NANOSECONDS), 100.0);
  }

  /*
   A Method cannnot be mocked so we need to use a real one
  */
  private class AnnotatedMethod {
    @Timed(value = "test.method", description = "description")
    public int annotatedMethod() {
      return 1;
    }
  }

  @Timed(value = "test.class", description = "description")
  private class AnnotatedClass {
    public int annotatedMethod() {
      return 1;
    }
  }
}

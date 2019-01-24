/*
 * Copyright 2017 Pivotal Software, Inc.
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.molgenis.metrics;

import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.lang.NonNullApi;
import java.lang.reflect.Method;
import java.util.function.Function;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

/**
 * AspectJ aspect for intercepting types or method annotated with @Timed.
 *
 * <p>Copied from micrometer.io and adapted to also work for annotated classes.
 *
 * @author David J. M. Karlsen
 * @author Jon Schneider
 * @author Fleur Kelpin
 */
@Aspect
@NonNullApi
public class MolgenisTimedAspect {

  private final MeterRegistry registry;
  private final Function<ProceedingJoinPoint, Iterable<Tag>> tagsBasedOnJoinpoint;

  MolgenisTimedAspect(MeterRegistry registry) {
    this(
        registry,
        pjp ->
            Tags.of(
                "class",
                pjp.getStaticPart().getSignature().getDeclaringTypeName(),
                "method",
                pjp.getStaticPart().getSignature().getName()));
  }

  MolgenisTimedAspect(
      MeterRegistry registry, Function<ProceedingJoinPoint, Iterable<Tag>> tagsBasedOnJoinpoint) {
    this.registry = registry;
    this.tagsBasedOnJoinpoint = tagsBasedOnJoinpoint;
  }

  @Around("execution(* (@io.micrometer.core.annotation.Timed *).*(..))")
  public Object timedClassMethod(ProceedingJoinPoint pjp) throws Throwable {
    Method method = ((MethodSignature) pjp.getSignature()).getMethod();
    return timedProceed(pjp, method.getDeclaringClass().getAnnotation(Timed.class));
  }

  @Around("execution (@io.micrometer.core.annotation.Timed * *.*(..))")
  public Object timedMethod(ProceedingJoinPoint pjp) throws Throwable {
    Method method = ((MethodSignature) pjp.getSignature()).getMethod();
    return timedProceed(pjp, method.getAnnotation(Timed.class));
  }

  private Object timedProceed(ProceedingJoinPoint pjp, Timed timed) throws Throwable {
    if (timed.value().isEmpty()) {
      return pjp.proceed();
    }
    Timer.Sample sample = Timer.start(registry);
    try {
      return pjp.proceed();
    } finally {
      sample.stop(
          Timer.builder(timed.value())
              .description(timed.description().isEmpty() ? null : timed.description())
              .tags(timed.extraTags())
              .tags(tagsBasedOnJoinpoint.apply(pjp))
              .publishPercentileHistogram(timed.histogram())
              .publishPercentiles(timed.percentiles().length == 0 ? null : timed.percentiles())
              .register(registry));
    }
  }
}

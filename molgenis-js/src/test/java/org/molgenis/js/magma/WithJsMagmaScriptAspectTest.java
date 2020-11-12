package org.molgenis.js.magma;

import static org.junit.jupiter.api.Assertions.*;

import org.graalvm.polyglot.PolyglotException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.molgenis.js.graal.GraalScriptEngine;
import org.molgenis.test.AbstractMockitoSpringContextTests;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(
    classes = {WithJsMagmaScriptAspect.class, WithJsMagmaScriptAspectTest.Config.class})
class WithJsMagmaScriptAspectTest extends AbstractMockitoSpringContextTests {

  @Autowired TestBean testBean;

  @BeforeEach
  void beforeEach() {
    JsMagmaScriptContextHolder.clearContext();
  }

  @Test
  void testRunsWithinSameContext() {
    assertEquals("Hello", testBean.execute("const a = 'Hello'", "a"));
    assertNull(JsMagmaScriptContextHolder.getContext());
  }

  @Test
  void testThrow() {
    assertThrows(PolyglotException.class, () -> testBean.execute("'Hello'", "throw(new Error())"));
    assertNull(JsMagmaScriptContextHolder.getContext());
  }

  @Test
  void testContextIsEntered() {
    assertDoesNotThrow(testBean::checkThatContextIsEntered);
  }

  @Configuration
  public static class Config {
    @Bean
    public GraalScriptEngine engine() {
      return new GraalScriptEngine();
    }

    @Bean
    public TestBean testBean() {
      return new TestBean();
    }
  }

  public static class TestBean {
    @WithJsMagmaScriptContext
    public Object execute(String expression1, String expression2) {
      JsMagmaScriptContext context = JsMagmaScriptContextHolder.getContext();
      context.eval(expression1);
      return context.eval(expression2);
    }

    @WithJsMagmaScriptContext
    public void checkThatContextIsEntered() {
      JsMagmaScriptContext context = JsMagmaScriptContextHolder.getContext();
      context.leave();
      context.enter();
    }
  }
}

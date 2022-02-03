package org.molgenis.data.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.HandlebarsException;
import com.github.jknack.handlebars.Template;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.test.AbstractMockitoTest;

class HandlebarsMathHelperTest extends AbstractMockitoTest {
  private static String[][] exceptionInputProvider() {
    String[] notEnoughParameters = {
      "{{math this \"+\" }}", "TemplateExpressionMathNotEnoughParametersException"
    };
    String[] operatorIsNull = {
      "{{math 37 \"$\" 42}}", "TemplateExpressionMathUnknownOperatorException"
    };
    String[] firstValueIsNull = {
      "{{math nullVar \"+\" 42}}", "TemplateExpressionMathInvalidParameterException"
    };
    String[] secondValueIsNull = {
      "{{math 42 \"+\" nullVar}}", "TemplateExpressionMathInvalidParameterException"
    };
    String[] valueIsNotNumeric = {"{{math \"this\" \"+\" \"foo\"}}", "NumberFormatException"};
    return new String[][] {
      notEnoughParameters, operatorIsNull, firstValueIsNull, secondValueIsNull, valueIsNotNumeric
    };
  }

  @ParameterizedTest
  @MethodSource("exceptionInputProvider")
  void testExceptions(String templateString, String exception) throws IOException {
    Handlebars handlebars = new Handlebars();
    handlebars.registerHelper("math", new HandlebarsMathHelper());
    Template template = handlebars.compileInline(templateString);
    HandlebarsException thrown =
        assertThrows(HandlebarsException.class, () -> template.apply(null));
    assertThat(thrown.getCause().toString()).contains(exception);
  }

  @Test
  void addTest() throws IOException {
    shouldCompileTo("{{math this \"+\" 0}}", "2", "2");
    shouldCompileTo("{{math this \"+\" 2}}", "1", "3");
    shouldCompileTo("{{math this \"+\" \"4.5\"}}", "4.5", "9.0");
    shouldCompileTo("{{math this \"+\" \"4.5\"}}", "4", "8.5");
  }

  @Test
  void subtractTest() throws IOException {
    shouldCompileTo("{{math this \"-\" 0}}", "2", "2");
    shouldCompileTo("{{math this \"-\" 2}}", "2", "0");
    shouldCompileTo("{{math this \"-\" 3}}", "2", "-1");
    shouldCompileTo("{{math this \"-\" \".5\"}}", "2", "1.5");
    shouldCompileTo("{{math this \"-\" \"-5\"}}", "2", "7");
  }

  @Test
  void multiplyTest() throws IOException {
    shouldCompileTo("{{math this \"*\" 2}}", "1", "2");
    shouldCompileTo("{{math this \"*\" 2}}", "2", "4");
    shouldCompileTo("{{math this \"*\" 2}}", ".5", "1.0");
    shouldCompileTo("{{math this \"*\" \"2.5\"}}", "4", "10.0");
    shouldCompileTo("{{math this \"*\" \"100\"}}", "4", "400");
  }

  @Test
  void divideTest() throws IOException {
    shouldCompileTo("{{math this \"/\" 2}}", "2", "1");
    shouldCompileTo("{{math this \"/\" 2}}", "1", "0.5");
    shouldCompileTo("{{math this \"/\" 3}}", "1", "0.3333333333333333");
    shouldCompileTo("{{math this \"/\" \".5\"}}", "2", "4");
    shouldCompileTo("{{math this \"/\" 1}}", "2", "2");
  }

  @Test
  void modulusTest() throws IOException {
    shouldCompileTo("{{math this \"%\" 2}}", "3", "1");
    shouldCompileTo("{{math this \"%\" 2}}", "10.5", "0.5");
  }

  private void shouldCompileTo(String templateString, String varValue, String expected)
      throws IOException {
    Handlebars handlebars = new Handlebars();
    handlebars.registerHelper("math", new HandlebarsMathHelper());
    Template template = handlebars.compileInline(templateString);

    String result = template.apply(varValue);
    assertEquals(expected, result);
  }
}

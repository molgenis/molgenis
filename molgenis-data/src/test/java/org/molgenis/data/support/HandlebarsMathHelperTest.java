package org.molgenis.data.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.HandlebarsException;
import com.github.jknack.handlebars.Template;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.molgenis.test.AbstractMockitoTest;

class HandlebarsMathHelperTest extends AbstractMockitoTest {

  protected Handlebars newHandlebars() {
    Handlebars handlebars = new Handlebars();
    handlebars.registerHelper("math", new HandlebarsMathHelper());
    return handlebars;
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

  @Test
  void notEnoughParameters() throws IOException {
    Handlebars handlebars = new Handlebars();
    handlebars.registerHelper("math", new HandlebarsMathHelper());
    Template template = handlebars.compileInline("{{math this \"+\" }}");
    String varValue = "37";
    HandlebarsException thrown =
        assertThrows(HandlebarsException.class, () -> template.apply(varValue));

    // Handlebars adds hash of the thrown IOException to the error
    assertEquals(
        "inline@783389cf:1:2: "
            + "org.molgenis.data.support.TemplateExpressionMathNotEnoughParametersException: "
            + "FAILED TO FORMAT LOCALIZED MESSAGE FOR ERROR CODE D12f.%nFallback message: \n"
            + "    inline@783389cf:1:2",
        thrown.getMessage());
  }

  @Test
  void operatorIsNull() throws IOException {
    Handlebars handlebars = new Handlebars();
    handlebars.registerHelper("math", new HandlebarsMathHelper());
    Template template = handlebars.compileInline("{{math this \"$\" 42}}");
    HandlebarsException thrown =
        assertThrows(HandlebarsException.class, () -> template.apply("37"));

    // Handlebars adds hash of the thrown IOException to the error
    assertEquals(
        "inline@5419d628:1:2: "
            + "org.molgenis.data.support.TemplateExpressionMathUnknownOperatorException: "
            + "FAILED TO FORMAT LOCALIZED MESSAGE FOR ERROR CODE D12f.%nFallback message: Unknown operator 'Unknown operator '$' for molgenis-math' for molgenis-math\n"
            + "    inline@5419d628:1:2",
        thrown.getMessage());
  }

  @Test
  void firstValueIsNull() throws IOException {
    Handlebars handlebars = new Handlebars();
    handlebars.registerHelper("math", new HandlebarsMathHelper());
    Template template = handlebars.compileInline("{{math this \"+\" 42}}");
    HandlebarsException thrown =
        assertThrows(HandlebarsException.class, () -> template.apply(null));

    // Handlebars adds hash of the thrown IOException to the error
    assertEquals(
        "inline@399a8b11:1:2: "
            + "org.molgenis.data.support.TemplateExpressionMathInvalidParameterException: "
            + "FAILED TO FORMAT LOCALIZED MESSAGE FOR ERROR CODE D12h.%nFallback message: \n"
            + "    inline@399a8b11:1:2",
        thrown.getMessage());
  }

  @Test
  void secondValueIsNull() throws IOException {
    Handlebars handlebars = new Handlebars();
    handlebars.registerHelper("math", new HandlebarsMathHelper());
    Template template = handlebars.compileInline("{{math this \"+\" foo}}");
    HandlebarsException thrown =
        assertThrows(HandlebarsException.class, () -> template.apply("37"));

    // Handlebars adds hash of the thrown IOException to the error
    assertEquals(
        "inline@92546b5:1:2: "
            + "org.molgenis.data.support.TemplateExpressionMathInvalidParameterException: "
            + "FAILED TO FORMAT LOCALIZED MESSAGE FOR ERROR CODE D12h.%nFallback message: \n"
            + "    inline@92546b5:1:2",
        thrown.getMessage());
  }

  @Test
  void valueIsNotNumeric() throws IOException {
    Handlebars handlebars = new Handlebars();
    handlebars.registerHelper("math", new HandlebarsMathHelper());
    Template template = handlebars.compileInline("{{math this \"+\" foo}}");
    HandlebarsException thrown =
        assertThrows(HandlebarsException.class, () -> template.apply("NotANumber!"));

    // Handlebars adds hash of the thrown IOException to the error
    assertEquals(
        "inline@92546b5:1:2: "
            + "org.molgenis.data.support.TemplateExpressionMathInvalidParameterException: "
            + "FAILED TO FORMAT LOCALIZED MESSAGE FOR ERROR CODE D12h.%nFallback message: \n"
            + "    inline@92546b5:1:2",
        thrown.getMessage());
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

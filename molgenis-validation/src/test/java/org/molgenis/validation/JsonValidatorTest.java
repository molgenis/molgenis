package org.molgenis.validation;

import static com.google.common.collect.ImmutableMap.of;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.gson.Gson;
import org.everit.json.schema.Schema;
import org.junit.jupiter.api.Test;
import org.molgenis.test.AbstractMockitoSpringContextTests;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = {JsonValidator.class})
class JsonValidatorTest extends AbstractMockitoSpringContextTests {
  @Autowired private JsonValidator jsonValidator;

  private Gson gson = new Gson();

  @Test
  void testLoadSchemaInvalid() {
    assertThrows(InvalidJsonSchemaException.class, () -> jsonValidator.loadSchema(""));
  }

  @Test
  void testLoadAndValidate() {
    String schemaJson =
        gson.toJson(
            of(
                "title",
                "Hello World Job",
                "type",
                "object",
                "properties",
                of("delay", of("type", "integer")),
                "required",
                singletonList("delay")));
    Schema schema = jsonValidator.loadSchema(schemaJson);
    jsonValidator.validate("{\"delay\":10}", schema);
  }

  @Test
  void testLoadAndValidateInvalid() {
    String schemaJson =
        gson.toJson(
            of(
                "title",
                "Hello World Job",
                "type",
                "object",
                "properties",
                of("p1", of("type", "integer"), "p2", of("type", "integer")),
                "required",
                singletonList("p2")));
    Schema schema = jsonValidator.loadSchema(schemaJson);
    try {
      jsonValidator.validate("{\"p1\":\"10\"}", schema);
    } catch (JsonValidationException expected) {
      assertEquals(
          expected.getViolations(),
          newHashSet(
              new ConstraintViolation("#/p1: expected type: Number, found: String"),
              new ConstraintViolation("#: required key [p2] not found")));
    }
  }
}

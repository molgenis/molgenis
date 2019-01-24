package org.molgenis.validation;

import static java.util.stream.Collectors.toSet;

import org.everit.json.schema.Schema;
import org.everit.json.schema.SchemaException;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.stereotype.Component;

@Component
public class JsonValidator {
  /**
   * Loads JSON schema. The schema may contain remote references.
   *
   * @param schema String containing the JSON schema to load
   * @return loaded {@link Schema}
   * @throws InvalidJsonSchemaException if the schema fails to load
   */
  public Schema loadSchema(String schema) {
    try {
      JSONObject rawSchema = new JSONObject(new JSONTokener(schema));
      return SchemaLoader.load(rawSchema);
    } catch (JSONException | SchemaException e) {
      throw new InvalidJsonSchemaException(e);
    }
  }

  /**
   * Validates that a JSON string conforms to a {@link Schema}.
   *
   * @param json the JSON string to check
   * @param schema the {@link Schema} that the JSON string should conform to
   * @throws JsonValidationException if the JSON doesn't conform to the schema, containing a {@link
   *     ConstraintViolation} for each message
   */
  public void validate(String json, Schema schema) {
    try {
      schema.validate(new JSONObject(json));
    } catch (ValidationException validationException) {
      throw new JsonValidationException(
          validationException
              .getAllMessages()
              .stream()
              .map(ConstraintViolation::new)
              .collect(toSet()));
    }
  }

  /**
   * Validates that a JSON string conforms to a JSON schema.
   *
   * @param json the JSON string to check
   * @param schemaJson the JSON string for the schema
   * @throws InvalidJsonSchemaException if the JSON schema cannot be loaded
   * @throws JsonValidationException if the JSON string doesn't conform to the schema
   */
  public void validate(String json, String schemaJson) {
    Schema schema = loadSchema(schemaJson);
    validate(json, schema);
  }
}

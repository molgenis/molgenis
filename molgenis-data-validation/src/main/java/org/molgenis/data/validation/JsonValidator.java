package org.molgenis.data.validation;

import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.molgenis.data.MolgenisDataException;
import org.springframework.stereotype.Component;

@Component
public class JsonValidator
{
	public void validateJson(String json, String schema)
	{
		try
		{
			JSONObject rawSchema = new JSONObject(new JSONTokener(schema));
			Schema jsonSchema = SchemaLoader.load(rawSchema);
			jsonSchema.validate(new JSONObject(json));
		}
		catch (ValidationException validationException)
		{
			throw new MolgenisDataException(String.join(", ", validationException.getAllMessages()));
		}
	}
}

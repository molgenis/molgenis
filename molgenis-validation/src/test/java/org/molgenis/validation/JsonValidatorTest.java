package org.molgenis.validation;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;

@ContextConfiguration(classes = { JsonValidator.class })
public class JsonValidatorTest extends AbstractTestNGSpringContextTests
{
	//TODO
	//	@Autowired
	//	private JsonValidator jsonValidator;
	//
	//	private Gson gson = new Gson();
	//
	//	@Test(expectedExceptions = MolgenisDataException.class)
	//	public void testLoadSchemaInvalid() throws Exception
	//	{
	//		jsonValidator.loadSchema("");
	//	}
	//
	//	@Test
	//	public void testLoadAndValidate() throws Exception
	//	{
	//		String schemaJson = gson.toJson(
	//				of("title", "Hello World Job", "type", "object", "properties", of("delay", of("type", "integer")),
	//						"required", singletonList("delay")));
	//		Schema schema = jsonValidator.loadSchema(schemaJson);
	//		jsonValidator.validate("{\"delay\":10}", schema);
	//	}
	//
	//	@Test
	//	public void testLoadAndValidateInvalid() throws Exception
	//	{
	//		String schemaJson = gson.toJson(of("title", "Hello World Job", "type", "object", "properties",
	//				of("p1", of("type", "integer"), "p2", of("type", "integer")), "required", singletonList("p2")));
	//		Schema schema = jsonValidator.loadSchema(schemaJson);
	//		try
	//		{
	//			jsonValidator.validate("{\"p1\":\"10\"}", schema);
	//		}
	//		catch (MolgenisValidationException expected)
	//		{
	//			Assert.assertEquals(expected.getViolations(),
	//					Sets.newHashSet(new ConstraintViolation("#/p1: expected type: Number, found: String"),
	//							new ConstraintViolation("#: required key [p2] not found")));
	//		}
	//	}
}
package org.molgenis.integrationtest.platform.datatypeediting;

import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.validation.ValidationException;
import org.molgenis.integrationtest.platform.PlatformITConfig;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.molgenis.data.meta.AttributeType.*;
import static org.testng.Assert.*;

@ContextConfiguration(classes = { PlatformITConfig.class })
public class EnumAttributeTypeUpdateIT extends AbstractAttributeTypeUpdateIT
{
	@BeforeClass
	private void setup()
	{
		super.setup(ENUM, STRING);
	}

	@AfterMethod
	private void afterMethod()
	{
		super.afterMethod(ENUM);
	}

	@AfterClass
	public void afterClass()
	{
		super.afterClass();
	}

	@DataProvider(name = "validConversionData")
	public Object[][] validConversionData()
	{
		return new Object[][] { { "1", STRING, "1" }, { "1", TEXT, "1" }, { "1", INT, 1 }, { "1", LONG, 1L } };
	}

	/**
	 * Valid conversion cases for ENUM to:
	 * STRING, TEXT, INT, LONG
	 *
	 * @param valueToConvert  The value that will be converted
	 * @param typeToConvertTo The type to convert to
	 * @param convertedValue  The expected value after converting the type
	 */
	@Test(dataProvider = "validConversionData")
	public void testValidConversion(String valueToConvert, AttributeType typeToConvertTo, Object convertedValue)
	{
		testTypeConversion(valueToConvert, typeToConvertTo);

		// Assert if conversion was successful
		assertEquals(getActualDataType(), typeToConvertTo);
		assertEquals(getActualValue(), convertedValue);
	}

	@DataProvider(name = "invalidValueTestCases")
	public Object[][] invalidValueTestCases()
	{
		Entity entity2 = dataService.findOneById("REFERENCEENTITY", "molgenis@test.org");
		return new Object[][] { { entity2, INT,
				"Value [REFERENCEENTITY{id=molgenis@test.org&label=email label}] is of type [DynamicEntity] instead of [String] for attribute: [mainAttribute]" },
				{ entity2, LONG,
						"Value [REFERENCEENTITY{id=molgenis@test.org&label=email label}] is of type [DynamicEntity] instead of [String] for attribute: [mainAttribute]" } };
	}

	@DataProvider(name = "invalidConversionTestCases")
	public Object[][] invalidConversionTestCases()
	{
		return new Object[][] { { "1", DECIMAL, "V94" },
				{ "1", XREF, "V94" },
				{ "1", CATEGORICAL, "V94" },
				{ "1", DATE, "V94" },
				{ "1", DATE_TIME, "V94" },
				{ "1", MREF, "V94" },
				{ "1", CATEGORICAL_MREF, "V94" },
				{ "1", EMAIL, "V94" },
				{ "1", HTML, "V94" },
				{ "1", HYPERLINK, "V94" },
				{ "1", COMPOUND, "V94" },
				{ "1", FILE, "V94" },
				{ "1", BOOL, "V94" },
				{ "1", SCRIPT, "V94" },
				{ "1", ONE_TO_MANY, "V94" } };
	}

	/**
	 * Invalid conversions cases for ENUM to:
	 * INT, LONG, DECIMAL, XREF, CATEGORICAL, DATE, DATE_TIME, MREF, CATEGORICAL_MREF, EMAIL, HTML, HYPERLINK, COMPOUND, FILE, BOOL, STRING, SCRIPT, ONE_TO_MANY
	 *
	 * @param valueToConvert   The value that will be converted
	 * @param typeToConvertTo  The type to convert to
	 *                            * @param errorCode       The expected errorCode
	 */
	@Test(dataProvider = "invalidConversionTestCases")
	public void testInvalidConversion(String valueToConvert, AttributeType typeToConvertTo, String errorCode)
	{
		try
		{
			testTypeConversion(valueToConvert, typeToConvertTo);
			fail("Conversion should have failed");
		}
		catch (ValidationException exception)
		{
			//match on error code only since the message has no parameters
			List<String> messageList = exception.getValidationMessages()
												.map(message -> message.getErrorCode())
												.collect(Collectors.toList());
			assertTrue(messageList.contains(errorCode));
		}
	}

	@Test(dataProvider = "invalidValueTestCases")
	public void testInvalidValue(Entity valueToConvert, AttributeType typeToConvertTo, String message)
	{
		try
		{
			testTypeConversion(valueToConvert, typeToConvertTo);
			fail("Conversion should have failed");
		}
		catch (MolgenisDataException exception)
		{
			assertEquals(exception.getMessage(), message);
		}
	}

}

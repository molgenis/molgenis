package org.molgenis.integrationtest.platform.datatypeediting;

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
public class EmailAttributeTypeUpdateIT extends AbstractAttributeTypeUpdateIT
{
	@BeforeClass
	private void setup()
	{
		super.setup(EMAIL, STRING);
	}

	@AfterMethod
	private void afterMethod()
	{
		super.afterMethod(EMAIL);
	}

	@AfterClass
	public void afterClass()
	{
		super.afterClass();
	}

	@DataProvider(name = "validConversionData")
	public Object[][] validConversionData()
	{
		return new Object[][] { { "molgenis@test.org", STRING, "molgenis@test.org" },
				{ "molgenis@test.org", TEXT, "molgenis@test.org" }, { "molgenis@test.org", CATEGORICAL, "email label" },
				{ "molgenis@test.org", XREF, "email label" } };
	}

	/**
	 * Valid conversion cases for EMAIL to:
	 * STRING, TEXT, CATEGORICAL, XREF
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

	@DataProvider(name = "invalidConversionTestCases")
	public Object[][] invalidConversionTestCases()
	{
		return new Object[][] { { "molgenis@test.org", BOOL, "V94" },
				{ "molgenis@test.org", INT, "V94" },
				{ "molgenis@test.org", LONG, "V94" },
				{ "molgenis@test.org", DECIMAL, "V94" },
				{ "molgenis@test.org", SCRIPT, "V94" },
				{ "molgenis@test.org", HYPERLINK, "V94" },
				{ "molgenis@test.org", HTML, "V94" },
				{ "molgenis@test.org", ENUM, "V94" },
				{ "molgenis@test.org", DATE, "V94" },
				{ "molgenis@test.org", DATE_TIME, "V94" },
				{ "molgenis@test.org", MREF, "V94" },
				{ "molgenis@test.org", CATEGORICAL_MREF, "V94" },
				{ "molgenis@test.org", FILE, "V94" },
				{ "molgenis@test.org", COMPOUND, "V94" },
				{ "molgenis@test.org", ONE_TO_MANY, "V94" } };
	}

	/**
	 * Invalid conversions cases for EMAIL to:
	 * BOOL, INT, LONG, DECIMAL, XREF, CATEGORICAL, SCRIPT, HYPERLINK, HTML, ENUM, DATE, DATE_TIME, MREF, CATEGORICAL_MREF, FILE, COMPOUND
	 *
	 * @param valueToConvert   The value that will be converted
	 * @param typeToConvertTo  The type to convert to
	 * @param errorCode       The expected errorCode
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
}

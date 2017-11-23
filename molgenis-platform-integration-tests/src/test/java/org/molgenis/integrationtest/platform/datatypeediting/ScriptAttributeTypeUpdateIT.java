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
public class ScriptAttributeTypeUpdateIT extends AbstractAttributeTypeUpdateIT
{
	@BeforeClass
	public void setup()
	{
		super.setup(SCRIPT, STRING);
	}

	@AfterMethod
	public void afterMethod()
	{
		super.afterMethod(SCRIPT);
	}

	@AfterClass
	public void afterClass()
	{
		super.afterClass();
	}

	@DataProvider(name = "validConversionData")
	public Object[][] validConversionData()
	{
		return new Object[][] {
				{ "import test; function(){this is a test script in STRING format; return MOLGENIS}", STRING,
						"import test; function(){this is a test script in STRING format; return MOLGENIS}" },
				{ "import test; function(){this is a test script in TEXT format; return MOLGENIS}", TEXT,
						"import test; function(){this is a test script in TEXT format; return MOLGENIS}" } };
	}

	/**
	 * Valid conversion cases for SCRIPT to:
	 * STRING, TEXT
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
		return new Object[][] { { "function log(data){console.log(data)}", BOOL, "V94" },
				{ "function log(data){console.log(data)}", INT, "V94" },
				{ "function log(data){console.log(data)}", LONG, "V94" },
				{ "function log(data){console.log(data)}", DECIMAL, "V94" },
				{ "function log(data){console.log(data)}", XREF, "V94" },
				{ "function log(data){console.log(data)}", CATEGORICAL, "V94" },
				{ "function log(data){console.log(data)}", EMAIL, "V94" },
				{ "function log(data){console.log(data)}", HYPERLINK, "V94" },
				{ "function log(data){console.log(data)}", HTML, "V94" },
				{ "function log(data){console.log(data)}", ENUM, "V94" },
				{ "function log(data){console.log(data)}", DATE, "V94" },
				{ "function log(data){console.log(data)}", DATE_TIME, "V94" },
				{ "function log(data){console.log(data)}", MREF, "V94" },
				{ "function log(data){console.log(data)}", CATEGORICAL_MREF, "V94" },
				{ "function log(data){console.log(data)}", FILE, "V94" },
				{ "function log(data){console.log(data)}", COMPOUND, "V94" },
				{ "function log(data){console.log(data)}", ONE_TO_MANY, "V94" } };
	}

	/**
	 * Invalid conversion cases for SCRIPT to:
	 * BOOL, INT, LONG, DECIMAL, XREF, CATEGORICAL, EMAIL, HYPERLINK, HTML, ENUM, DATE, DATE_TIME, MREF, CATEGORICAL_MREF, FILE, COMPOUND, ONE_TO_MANY
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

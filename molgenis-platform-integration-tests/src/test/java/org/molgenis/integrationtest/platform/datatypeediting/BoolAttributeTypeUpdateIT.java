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
public class BoolAttributeTypeUpdateIT extends AbstractAttributeTypeUpdateIT
{
	@BeforeClass
	public void setUp()
	{
		super.setup(BOOL, STRING);
	}

	@AfterMethod
	public void afterMethod()
	{
		super.afterMethod(BOOL);
	}

	@AfterClass
	public void afterClass()
	{
		super.afterClass();
	}

	@DataProvider(name = "validConversionData")
	public Object[][] validConversionData()
	{
		return new Object[][] { { true, STRING, "true" }, { false, STRING, "false" }, { true, TEXT, "true" },
				{ false, TEXT, "false" }, { true, INT, 1 }, { false, INT, 0 } };
	}

	/**
	 * Valid conversion cases for BOOLEAN to:
	 * STRING, TEXT, INT
	 *
	 * @param valueToConvert  The value that will be converted
	 * @param typeToConvertTo The type to convert to
	 * @param convertedValue  The expected value after converting the type
	 */
	@Test(dataProvider = "validConversionData")
	public void testValidConversion(boolean valueToConvert, AttributeType typeToConvertTo, Object convertedValue)
	{
		testTypeConversion(valueToConvert, typeToConvertTo);

		// Assert if conversion was successful
		assertEquals(getActualDataType(), typeToConvertTo);
		assertEquals(getActualValue(), convertedValue);
	}

	@DataProvider(name = "invalidConversionTestCases")
	public Object[][] invalidConversionTestCases()
	{
		return new Object[][] { { true, DECIMAL, "V94" }, { true, LONG, "V94" }, { true, MREF, "V94" },
				{ true, XREF, "V94" }, { true, CATEGORICAL, "V94" }, { true, CATEGORICAL_MREF, "V94" },
				{ true, FILE, "V94" }, { true, COMPOUND, "V94" }, { true, EMAIL, "V94" }, { true, HTML, "V94" },
				{ true, HYPERLINK, "V94" }, { true, DATE, "V94" }, { true, DATE_TIME, "V94" }, { true, ENUM, "V94" },
				{ true, SCRIPT, "V94" }, { true, ONE_TO_MANY, "V94" } };
	}

	/**
	 * Invalid conversion cases for BOOL to:
	 * DECIMAL, LONG, MREF, XREF, CATEGORICAL, CATEGORICAL_MREF, FILE, COMPOUND, EMAIL, HTML, HYPERLINK, DATE, DATE_TIME, ENUM, ONE_TO_MANY, SCRIPT
	 *
	 * @param valueToConvert   The value that will be converted
	 * @param typeToConvertTo  The type to convert to
	 * @param errorCode The expected errorCode
	 */
	@Test(dataProvider = "invalidConversionTestCases")
	public void testInvalidConversion(boolean valueToConvert, AttributeType typeToConvertTo, String errorCode)
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

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
public class CompoundAttributeTypeUpdateIT extends AbstractAttributeTypeUpdateIT
{
	@BeforeClass
	public void setup()
	{
		super.setup(COMPOUND, STRING);
	}

	@AfterMethod
	public void afterMethod()
	{
		super.afterMethod(COMPOUND);
	}

	@AfterClass
	public void afterClass()
	{
		super.afterClass();
	}

	@DataProvider(name = "validConversionData")
	public Object[][] validConversionData()
	{
		return new Object[][] { { null, STRING, null } };
	}

	/**
	 * Valid conversion cases for COMPOUND to:
	 * STRING
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
		return new Object[][] { { null, BOOL, "V94" },
				{ null, TEXT, "V94" },
				{ null, SCRIPT, "V94" },
				{ null, INT, "V94" },
				{ null, LONG, "V94" },
				{ null, DECIMAL, "V94" },
				{ null, XREF, "V94" },
				{ null, CATEGORICAL, "V94" },
				{ null, EMAIL, "V94" },
				{ null, HYPERLINK, "V94" },
				{ null, HTML, "V94" },
				{ null, ENUM, "V94" },
				{ null, DATE, "V94" },
				{ null, DATE_TIME, "V94" },
				{ null, MREF, "V94" },
				{ null, CATEGORICAL_MREF, "V94" },
				{ null, FILE, "V94" },
				{ null, ONE_TO_MANY, "V94" } };
	}

	/**
	 * Invalid conversion cases for COMPOUND to:
	 * BOOL, TEXT, INT, LONG, DECIMAL, XREF, CATEGORICAL, EMAIL, HYPERLINK, HTML, ENUM, DATE, DATE_TIME, MREF, CATEGORICAL_MREF, FILE, ONE_TO_MANY
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

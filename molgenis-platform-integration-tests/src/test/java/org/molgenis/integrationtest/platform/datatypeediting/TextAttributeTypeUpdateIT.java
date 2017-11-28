package org.molgenis.integrationtest.platform.datatypeediting;

import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.validation.DataTypeConstraintViolationException;
import org.molgenis.data.validation.EnumConstraintModificationException;
import org.molgenis.data.validation.ValidationException;
import org.molgenis.integrationtest.platform.PlatformITConfig;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.*;

import java.text.ParseException;
import java.util.List;
import java.util.stream.Collectors;

import static org.molgenis.data.meta.AttributeType.*;
import static org.molgenis.util.MolgenisDateFormat.parseInstant;
import static org.molgenis.util.MolgenisDateFormat.parseLocalDate;
import static org.testng.Assert.*;

@ContextConfiguration(classes = { PlatformITConfig.class })
public class TextAttributeTypeUpdateIT extends AbstractAttributeTypeUpdateIT
{
	@BeforeClass
	public void setUp()
	{
		super.setup(TEXT, STRING);
	}

	@AfterMethod
	public void afterMethod()
	{
		super.afterMethod(TEXT);
	}

	@AfterClass
	public void afterClass()
	{
		super.afterClass();
	}

	@DataProvider(name = "validConversionTestCases")
	public Object[][] validConversionTestCases()
	{
		return new Object[][] { { "true", BOOL, true }, { "1", INT, 1 }, { "4243298", LONG, 4243298L },
				{ "1.234", DECIMAL, 1.234 },
				{ "A VERY LONG TEXT!!!!@#$#{@}{@}{#%$#*($&@#", STRING, "A VERY LONG TEXT!!!!@#$#{@}{@}{#%$#*($&@#" },
				{ "1", ENUM, "1" }, { "<h1>Hello World</h1>", HTML, "<h1>Hello World</h1>" },
				{ "Compounds go!", COMPOUND, null }, { "1990-11-13", DATE, "1990-11-13" },
				{ "2016-11-13T20:20:20+0100", DATE_TIME, "2016-11-13T20:20:20+0100" } };
	}

	/**
	 * Valid conversion cases for TEXT to:
	 * INT, STRING, BOOL, DECIMAL, LONG, XREF, CATEGORICAL, COMPOUND, ENUM, HTML, DATE, DATE_TIME
	 *
	 * @param valueToConvert  The value that will be converted
	 * @param typeToConvertTo The type to convert to
	 * @param convertedValue  The expected value after converting the type
	 */
	@Test(dataProvider = "validConversionTestCases")
	public void testValidConversion(String valueToConvert, AttributeType typeToConvertTo, Object convertedValue)
			throws ParseException
	{
		testTypeConversion(valueToConvert, typeToConvertTo);

		if (typeToConvertTo.equals(DATE)) convertedValue = parseLocalDate(convertedValue.toString());
		if (typeToConvertTo.equals(DATE_TIME)) convertedValue = parseInstant(convertedValue.toString());

		// Assert if conversion was successful
		assertEquals(getActualDataType(), typeToConvertTo);
		assertEquals(getActualValue(), convertedValue);
	}

	@DataProvider(name = "invalidConversionTestCases")
	public Object[][] invalidConversionTestCases()
	{
		return new Object[][] { { "ref123", XREF, "V94" },
				{ "ref123", CATEGORICAL, "V94" },
				{ "Test@Test.Test", EMAIL, "V94" },
				{ "https://www.google.com", HYPERLINK, "V94" }, { "ref123", MREF, "V94" },
				{ "ref123", CATEGORICAL_MREF, "V94" },
				{ "ref123", FILE, "V94" },
				{ "ref123", ONE_TO_MANY, "V94" } };
	}

	/**
	 * Invalid conversion cases for TEXT to:
	 * STRING, BOOL, INT, LONG, DECIMAL, XREF, CATEGORICAL, EMAIL, HYPERLINK, ENUM, DATE, DATE_TIME, MREF, CATEGORICAL_MREF, FILE
	 *
	 * @param valueToConvert   The value that will be converted
	 * @param typeToConvertTo  The type to convert to
	 * @param errorCode       The expected errorCode
	 */
	@Test(dataProvider = "invalidConversionTestCases")
	public void testInvalidConversions(String valueToConvert, AttributeType typeToConvertTo, String errorCode)
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

	@DataProvider(name = "invalidValueTestCases")
	public Object[][] invalidValueTestCases()
	{
		return new Object[][] { { "1b", INT, "V03" }, { "1234567890b", LONG, "V03" }, { "1.123b", DECIMAL, "V03" },
				{ "Not a date", DATE, "V03" }, { "Not a date time", DATE_TIME, "V03" } };
	}

	@Test(dataProvider = "invalidValueTestCases")
	public void testInvalidValue(String valueToConvert, AttributeType typeToConvertTo, String errorCode)
	{
		try
		{
			testTypeConversion(valueToConvert, typeToConvertTo);
			fail("Conversion should have failed");
		}
		catch (DataTypeConstraintViolationException exception)
		{
			assertEquals(exception.getErrorCode(), errorCode);
		}
	}

	@Test
	public void testInvalidEnumValue()
	{
		try
		{
			testTypeConversion("enumOption100", ENUM);
			fail("Conversion should have failed");
		}
		catch (EnumConstraintModificationException exception)
		{
			assertEquals(exception.getErrorCode(), "V09");
		}
	}
}

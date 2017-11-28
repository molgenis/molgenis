package org.molgenis.integrationtest.platform.datatypeediting;

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
public class DecimalAttributeTypeUpdateIT extends AbstractAttributeTypeUpdateIT
{
	@BeforeClass
	private void setup()
	{
		super.setup(DECIMAL, INT);
	}

	@AfterMethod
	private void afterMethod()
	{
		super.afterMethod(DECIMAL);
	}

	@AfterClass
	public void afterClass()
	{
		super.afterClass();
	}

	@DataProvider(name = "validConversionData")
	public Object[][] validConversionData()
	{
		return new Object[][] { { 1.123, STRING, "1.123" }, { 1.123, TEXT, "1.123" }, { 1.123, INT, 1 },
				{ 1.123, LONG, 1L }, { 1.0, ENUM, "1" } };
	}

	/**
	 * Valid conversion cases for DECIMAL to:
	 * STRING, TEXT, INT, LONG, ENUM
	 *
	 * @param valueToConvert  The value that will be converted
	 * @param typeToConvertTo The type to convert to
	 * @param convertedValue  The expected value after converting the type
	 */
	@Test(dataProvider = "validConversionData")
	public void testValidConversion(double valueToConvert, AttributeType typeToConvertTo, Object convertedValue)
	{
		testTypeConversion(valueToConvert, typeToConvertTo);

		// Assert if conversion was successful
		assertEquals(getActualDataType(), typeToConvertTo);
		assertEquals(getActualValue(), convertedValue);
	}

	@DataProvider(name = "invalidConversionTestCases")
	public Object[][] invalidConversionTestCases()
	{
		return new Object[][] { { 2L, XREF, "V94" },
				{ 2.0, CATEGORICAL, "V94" }, { 1.0, DATE, "V94" },
				{ 1.0, DATE_TIME, "V94" },
				{ 1.0, MREF, "V94" },
				{ 1.0, CATEGORICAL_MREF, "V94" },
				{ 1.0, EMAIL, "V94" },
				{ 1.0, HTML, "V94" },
				{ 1.0, HYPERLINK, "V94" },
				{ 1.0, COMPOUND, "V94" },
				{ 1.0, FILE, "V94" },
				{ 1.0, BOOL, "V94" },
				{ 1.0, SCRIPT, "V94" },
				{ 1.0, ONE_TO_MANY, "V94" } };
	}

	/**
	 * Invalid conversions cases for DECIMAL to:
	 * XREF, CATEGORICAL, ENUM, DATE, DATE_TIME, MREF, CATEGORICAL_MREF, EMAIL, HTML, HYPERLINK, COMPOUND, FILE, BOOL, SCRIPT, ONE_TO_MANY
	 *
	 * @param valueToConvert   The value that will be converted
	 * @param typeToConvertTo  The type to convert to
	 * @param errorCode       The expected errorCode
	 */
	@Test(dataProvider = "invalidConversionTestCases")
	public void testInvalidConversion(double valueToConvert, AttributeType typeToConvertTo, String errorCode)
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

	@Test
	public void testInvalidEnumValue()
	{
		try
		{
			testTypeConversion("enumOption100", ENUM);
			fail("Conversion should have failed");
		}
		catch (MolgenisDataException exception)
		{
			assertEquals(exception.getMessage(),
					"Value [enumOption100] is of type [String] instead of [Double] for attribute: [mainAttribute]");
		}
	}
}

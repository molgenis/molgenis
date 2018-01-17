package org.molgenis.integrationtest.platform.datatypeediting;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.validation.DataTypeConstraintViolationException;
import org.molgenis.data.validation.ValidationException;
import org.molgenis.integrationtest.platform.PlatformITConfig;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.molgenis.data.meta.AttributeType.*;
import static org.testng.Assert.*;

@ContextConfiguration(classes = { PlatformITConfig.class })
public class XrefAttributeTypeUpdateIT extends AbstractAttributeTypeUpdateIT
{
	@BeforeClass
	public void setup()
	{
		super.setup(XREF, STRING);
	}

	@AfterMethod
	public void afterMethod()
	{
		super.afterMethod(XREF);
	}

	@AfterClass
	public void afterClass()
	{
		super.afterClass();
	}

	@DataProvider(name = "validConversionData")
	public Object[][] validConversionData()
	{
		Entity entity = dataService.findOneById("REFERENCEENTITY", "1");
		return new Object[][] { { entity, STRING, "1" }, { entity, INT, 1 }, { entity, LONG, 1L },
				{ entity, CATEGORICAL, "label1" } };
	}

	/**
	 * Valid conversion cases for XREF to:
	 * STRING, INT, LONG, CATEGORICAL
	 *
	 * @param valueToConvert  The value that will be converted
	 * @param typeToConvertTo The type to convert to
	 * @param convertedValue  The expected value after converting the type
	 */
	@Test(dataProvider = "validConversionData")
	public void testValidConversion(Entity valueToConvert, AttributeType typeToConvertTo, Object convertedValue)
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
		return new Object[][] { { entity2, INT, "V03" }, { entity2, LONG, "V03" } };
	}

	@DataProvider(name = "invalidConversionTestCases")
	public Object[][] invalidConversionTestCases()
	{
		Entity entity1 = dataService.findOneById("REFERENCEENTITY", "1");
		return new Object[][] { { entity1, BOOL, "V94" },
				{ entity1, TEXT, "V94" },
				{ entity1, SCRIPT, "V94" }, { entity1, DECIMAL, "V94" },
				{ entity1, EMAIL, "V94" },
				{ entity1, HYPERLINK, "V94" },
				{ entity1, HTML, "V94" },
				{ entity1, ENUM, "V94" },
				{ entity1, DATE, "V94" },
				{ entity1, DATE_TIME, "V94" },
				{ entity1, MREF, "V94" },
				{ entity1, CATEGORICAL_MREF, "V94" },
				{ entity1, FILE, "V94" },
				{ entity1, COMPOUND, "V94" },
				{ entity1, ONE_TO_MANY, "V94" } };
	}

	/**
	 * Invalid conversion cases for XREF to:
	 * BOOL, TEXT, SCRIPT INT, LONG, DECIMAL, EMAIL, HYPERLINK, HTML, ENUM, DATE, DATE_TIME, MREF, CATEGORICAL_MREF, FILE, COMPOUND, ONE_TO_MANY
	 *
	 * @param valueToConvert   The value that will be converted
	 * @param typeToConvertTo  The type to convert to
	 * @param errorCode       The expected errorCode
	 */
	@Test(dataProvider = "invalidConversionTestCases")
	public void testInvalidConversion(Entity valueToConvert, AttributeType typeToConvertTo, String errorCode)
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

	/**
	 * Invalid conversion cases for CATEGORICAL to:
	 * BOOL, TEXT, SCRIPT INT, LONG, DECIMAL, EMAIL, HYPERLINK, HTML, ENUM, DATE, DATE_TIME, MREF, CATEGORICAL_MREF, FILE, COMPOUND, ONE_TO_MANY
	 *
	 * @param valueToConvert  The value that will be converted
	 * @param typeToConvertTo The type to convert to
	 * @param errorCode       The expected errorCode
	 */
	@Test(dataProvider = "invalidValueTestCases")
	public void testInvalidValue(Entity valueToConvert, AttributeType typeToConvertTo, String errorCode)
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
}

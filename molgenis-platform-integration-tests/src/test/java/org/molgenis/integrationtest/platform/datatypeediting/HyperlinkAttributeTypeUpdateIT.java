package org.molgenis.integrationtest.platform.datatypeediting;

import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.validation.MolgenisValidationException;
import org.molgenis.data.validation.ValidationException;
import org.molgenis.integrationtest.platform.PlatformITConfig;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.molgenis.data.meta.AttributeType.*;
import static org.testng.Assert.*;

@ContextConfiguration(classes = { PlatformITConfig.class })
public class HyperlinkAttributeTypeUpdateIT extends AbstractAttributeTypeUpdateIT
{
	@BeforeClass
	private void setup()
	{
		super.setup(HYPERLINK, STRING);
	}

	@AfterMethod
	private void afterMethod()
	{
		super.afterMethod(HYPERLINK);
	}

	@AfterClass
	public void afterClass()
	{
		super.afterClass();
	}

	@DataProvider(name = "validConversionData")
	public Object[][] validConversionData()
	{
		return new Object[][] { { "https://www.google.com", STRING, "https://www.google.com" },
				{ "https://www.google.com", TEXT, "https://www.google.com" },
				{ "https://www.google.com", CATEGORICAL, "hyperlink label" },
				{ "https://www.google.com", XREF, "hyperlink label" } };
	}

	/**
	 * Valid conversion cases for HYPERLINK to:
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
		return new Object[][] { { "https://www.google.com", BOOL, MolgenisDataException.class,
				"Attribute data type update from [HYPERLINK] to [BOOL] not allowed, allowed types are [CATEGORICAL, STRING, TEXT, XREF]" },
				{ "https://www.google.com", INT, MolgenisDataException.class,
						"Attribute data type update from [HYPERLINK] to [INT] not allowed, allowed types are [CATEGORICAL, STRING, TEXT, XREF]" },
				{ "https://www.google.com", LONG, MolgenisDataException.class,
						"Attribute data type update from [HYPERLINK] to [LONG] not allowed, allowed types are [CATEGORICAL, STRING, TEXT, XREF]" },
				{ "https://www.google.com", DECIMAL, MolgenisDataException.class,
						"Attribute data type update from [HYPERLINK] to [DECIMAL] not allowed, allowed types are [CATEGORICAL, STRING, TEXT, XREF]" },
				{ "molgenis@test.nl", XREF,
						org.molgenis.data.validation.EntityReferenceUnknownConstraintViolationException.class,
						"type:MAINENTITY attribute:mainAttribute value: molgenis@test.nl" },
				{ "molgenis@test.nl", CATEGORICAL,
						org.molgenis.data.validation.EntityReferenceUnknownConstraintViolationException.class,
						"type:MAINENTITY attribute:mainAttribute value: molgenis@test.nl" },
				{ "https://www.google.com", SCRIPT, MolgenisDataException.class,
						"Attribute data type update from [HYPERLINK] to [SCRIPT] not allowed, allowed types are [CATEGORICAL, STRING, TEXT, XREF]" },
				{ "https://www.google.com", EMAIL, MolgenisDataException.class,
						"Attribute data type update from [HYPERLINK] to [EMAIL] not allowed, allowed types are [CATEGORICAL, STRING, TEXT, XREF]" },
				{ "https://www.google.com", HTML, MolgenisDataException.class,
						"Attribute data type update from [HYPERLINK] to [HTML] not allowed, allowed types are [CATEGORICAL, STRING, TEXT, XREF]" },
				{ "https://www.google.com", ENUM, MolgenisDataException.class,
						"Attribute data type update from [HYPERLINK] to [ENUM] not allowed, allowed types are [CATEGORICAL, STRING, TEXT, XREF]" },
				{ "https://www.google.com", DATE, MolgenisDataException.class,
						"Attribute data type update from [HYPERLINK] to [DATE] not allowed, allowed types are [CATEGORICAL, STRING, TEXT, XREF]" },
				{ "https://www.google.com", DATE_TIME, MolgenisDataException.class,
						"Attribute data type update from [HYPERLINK] to [DATE_TIME] not allowed, allowed types are [CATEGORICAL, STRING, TEXT, XREF]" },
				{ "https://www.google.com", MREF, MolgenisDataException.class,
						"Attribute data type update from [HYPERLINK] to [MREF] not allowed, allowed types are [CATEGORICAL, STRING, TEXT, XREF]" },
				{ "https://www.google.com", CATEGORICAL_MREF, MolgenisDataException.class,
						"Attribute data type update from [HYPERLINK] to [CATEGORICAL_MREF] not allowed, allowed types are [CATEGORICAL, STRING, TEXT, XREF]" },
				{ "https://www.google.com", FILE, MolgenisDataException.class,
						"Attribute data type update from [HYPERLINK] to [FILE] not allowed, allowed types are [CATEGORICAL, STRING, TEXT, XREF]" },
				{ "https://www.google.com", COMPOUND, MolgenisDataException.class,
						"Attribute data type update from [HYPERLINK] to [COMPOUND] not allowed, allowed types are [CATEGORICAL, STRING, TEXT, XREF]" },
				{ "https://www.google.com", ONE_TO_MANY, MolgenisValidationException.class,
						"Invalid [xref] value [] for attribute [Referenced entity] of entity [mainAttribute] with type [sys_md_Attribute]. Offended validation expression: $('refEntityType').isNull().and($('type').matches(/^(categorical|categoricalmref|file|mref|onetomany|xref)$/).not()).or($('refEntityType').isNull().not().and($('type').matches(/^(categorical|categoricalmref|file|mref|onetomany|xref)$/))).value().Invalid [xref] value [] for attribute [Mapped by] of entity [mainAttribute] with type [sys_md_Attribute]. Offended validation expression: $('mappedBy').isNull().and($('type').eq('onetomany').not()).or($('mappedBy').isNull().not().and($('type').eq('onetomany'))).value()" } };
	}

	/**
	 * Invalid conversions cases for HYPERLINK to:
	 * BOOL, INT, LONG, DECIMAL, XREF, CATEGORICAL, SCRIPT, EMAIL, HTML, ENUM, DATE, DATE_TIME, MREF, CATEGORICAL_MREF, FILE, COMPOUND, ONE_TO_MANY
	 *
	 * @param valueToConvert   The value that will be converted
	 * @param typeToConvertTo  The type to convert to
	 * @param exceptionClass   The expected class of the exception that will be thrown
	 * @param exceptionMessage The expected exception message
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
			List<String> messageList = exception.getValidationMessages().map(message -> message.getErrorCode()).collect(Collectors.toList());
			assertTrue(messageList.contains(errorCode));
		}
	}
}

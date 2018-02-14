package org.molgenis.integrationtest.platform.datatypeediting;

import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.validation.MolgenisValidationException;
import org.molgenis.integrationtest.platform.PlatformITConfig;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.transaction.annotation.Transactional;
import org.testng.annotations.*;

import static org.molgenis.data.meta.AttributeType.*;
import static org.testng.Assert.*;

@ContextConfiguration(classes = { PlatformITConfig.class })
@TestExecutionListeners(listeners = { WithSecurityContextTestExecutionListener.class })
@Transactional
public class EmailAttributeTypeUpdateIT extends AbstractAttributeTypeUpdateIT
{
	private static final String USERNAME = "email-attribute-type-update-user";

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
	@WithMockUser(username = USERNAME)
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
		return new Object[][] { { "molgenis@test.org", BOOL, MolgenisValidationException.class,
				"Attribute data type update from [EMAIL] to [BOOL] not allowed, allowed types are [CATEGORICAL, STRING, TEXT, XREF]" },
				{ "molgenis@test.org", INT, MolgenisValidationException.class,
						"Attribute data type update from [EMAIL] to [INT] not allowed, allowed types are [CATEGORICAL, STRING, TEXT, XREF]" },
				{ "molgenis@test.org", LONG, MolgenisValidationException.class,
						"Attribute data type update from [EMAIL] to [LONG] not allowed, allowed types are [CATEGORICAL, STRING, TEXT, XREF]" },
				{ "molgenis@test.org", DECIMAL, MolgenisValidationException.class,
						"Attribute data type update from [EMAIL] to [DECIMAL] not allowed, allowed types are [CATEGORICAL, STRING, TEXT, XREF]" },
				{ "molgenis@test.nl", XREF, MolgenisValidationException.class,
						"Unknown xref value 'molgenis@test.nl' for attribute 'mainAttribute' of entity 'MAINENTITY'." },
				{ "molgenis@test.nl", CATEGORICAL, MolgenisValidationException.class,
						"Unknown xref value 'molgenis@test.nl' for attribute 'mainAttribute' of entity 'MAINENTITY'." },
				{ "molgenis@test.org", SCRIPT, MolgenisValidationException.class,
						"Attribute data type update from [EMAIL] to [SCRIPT] not allowed, allowed types are [CATEGORICAL, STRING, TEXT, XREF]" },
				{ "molgenis@test.org", HYPERLINK, MolgenisValidationException.class,
						"Attribute data type update from [EMAIL] to [HYPERLINK] not allowed, allowed types are [CATEGORICAL, STRING, TEXT, XREF]" },
				{ "molgenis@test.org", HTML, MolgenisValidationException.class,
						"Attribute data type update from [EMAIL] to [HTML] not allowed, allowed types are [CATEGORICAL, STRING, TEXT, XREF]" },
				{ "molgenis@test.org", ENUM, MolgenisValidationException.class,
						"Attribute data type update from [EMAIL] to [ENUM] not allowed, allowed types are [CATEGORICAL, STRING, TEXT, XREF]" },
				{ "molgenis@test.org", DATE, MolgenisValidationException.class,
						"Attribute data type update from [EMAIL] to [DATE] not allowed, allowed types are [CATEGORICAL, STRING, TEXT, XREF]" },
				{ "molgenis@test.org", DATE_TIME, MolgenisValidationException.class,
						"Attribute data type update from [EMAIL] to [DATE_TIME] not allowed, allowed types are [CATEGORICAL, STRING, TEXT, XREF]" },
				{ "molgenis@test.org", MREF, MolgenisValidationException.class,
						"Attribute data type update from [EMAIL] to [MREF] not allowed, allowed types are [CATEGORICAL, STRING, TEXT, XREF]" },
				{ "molgenis@test.org", CATEGORICAL_MREF, MolgenisValidationException.class,
						"Attribute data type update from [EMAIL] to [CATEGORICAL_MREF] not allowed, allowed types are [CATEGORICAL, STRING, TEXT, XREF]" },
				{ "molgenis@test.org", FILE, MolgenisValidationException.class,
						"Attribute data type update from [EMAIL] to [FILE] not allowed, allowed types are [CATEGORICAL, STRING, TEXT, XREF]" },
				{ "molgenis@test.org", COMPOUND, MolgenisValidationException.class,
						"Attribute data type update from [EMAIL] to [COMPOUND] not allowed, allowed types are [CATEGORICAL, STRING, TEXT, XREF]" },
				{ "molgenis@test.org", ONE_TO_MANY, MolgenisValidationException.class,
						"Invalid [xref] value [] for attribute [Referenced entity] of entity [mainAttribute] with type [sys_md_Attribute]. Offended validation expression: $('refEntityType').isNull().and($('type').matches(/^(categorical|categoricalmref|file|mref|onetomany|xref)$/).not()).or($('refEntityType').isNull().not().and($('type').matches(/^(categorical|categoricalmref|file|mref|onetomany|xref)$/))).value().Invalid [xref] value [] for attribute [Mapped by] of entity [mainAttribute] with type [sys_md_Attribute]. Offended validation expression: $('mappedBy').isNull().and($('type').eq('onetomany').not()).or($('mappedBy').isNull().not().and($('type').eq('onetomany'))).value()" } };
	}

	/**
	 * Invalid conversions cases for EMAIL to:
	 * BOOL, INT, LONG, DECIMAL, XREF, CATEGORICAL, SCRIPT, HYPERLINK, HTML, ENUM, DATE, DATE_TIME, MREF, CATEGORICAL_MREF, FILE, COMPOUND
	 *
	 * @param valueToConvert   The value that will be converted
	 * @param typeToConvertTo  The type to convert to
	 * @param exceptionClass   The expected class of the exception that will be thrown
	 * @param exceptionMessage The expected exception message
	 */
	@WithMockUser(username = USERNAME)
	@Test(dataProvider = "invalidConversionTestCases")
	public void testInvalidConversion(String valueToConvert, AttributeType typeToConvertTo, Class exceptionClass,
			String exceptionMessage)
	{
		try
		{
			testTypeConversion(valueToConvert, typeToConvertTo);
			fail("Conversion should have failed");
		}
		catch (Exception exception)
		{
			assertTrue(exception.getClass().isAssignableFrom(exceptionClass));
			assertEquals(exception.getMessage(), exceptionMessage);
		}
	}
}

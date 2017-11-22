package org.molgenis.integrationtest.platform.datatypeediting;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.validation.ValidationException;
import org.molgenis.integrationtest.platform.PlatformITConfig;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.*;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toList;
import static org.molgenis.data.meta.AttributeType.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

@ContextConfiguration(classes = { PlatformITConfig.class })
public class MrefAttributeTypeUpdateIT extends AbstractAttributeTypeUpdateIT
{
	@BeforeClass
	public void setup()
	{
		super.setup(MREF, INT);
	}

	@AfterMethod
	public void afterMethod()
	{
		super.afterMethod(MREF);
	}

	@AfterClass
	public void afterClass()
	{
		super.afterClass();
	}

	@DataProvider(name = "validConversionData")
	public Object[][] validConversionData()
	{
		List<Entity> entities = dataService.findAll("REFERENCEENTITY").collect(toList());
		return new Object[][] {
				{ entities, CATEGORICAL_MREF, newArrayList("label1", "email label", "hyperlink label") } };
	}

	/**
	 * Valid conversion cases for MREF to:
	 * CATEGORICAL_MREF
	 *
	 * @param valueToConvert  The value that will be converted
	 * @param typeToConvertTo The type to convert to
	 * @param convertedValue  The expected value after converting the type
	 */
	@Test(dataProvider = "validConversionData")
	public void testValidConversion(List<Entity> valueToConvert, AttributeType typeToConvertTo,
			List<String> convertedValue)
	{
		testTypeConversion(valueToConvert, typeToConvertTo);

		// Assert if conversion was successful
		assertEquals(getActualDataType(), typeToConvertTo);

		List<String> actualValues = newArrayList();
		Entity entity1 = dataService.findOneById("MAINENTITY", "1");
		entity1.getEntities("mainAttribute").forEach(entity -> actualValues.add(entity.getLabelValue().toString()));
		assertEquals(actualValues, convertedValue);
	}

	@DataProvider(name = "invalidConversionTestCases")
	public Object[][] invalidConversionTestCases()
	{
		List<Entity> entities = dataService.findAll("REFERENCEENTITY").collect(toList());
		return new Object[][] { { entities, BOOL,
				"Attribute data type update from [MREF] to [BOOL] not allowed, allowed types are [CATEGORICAL_MREF]" },
				{ entities, STRING,
						"Attribute data type update from [MREF] to [STRING] not allowed, allowed types are [CATEGORICAL_MREF]" },
				{ entities, TEXT,
						"Attribute data type update from [MREF] to [TEXT] not allowed, allowed types are [CATEGORICAL_MREF]" },
				{ entities, SCRIPT,
						"Attribute data type update from [MREF] to [SCRIPT] not allowed, allowed types are [CATEGORICAL_MREF]" },
				{ entities, INT,
						"Attribute data type update from [MREF] to [INT] not allowed, allowed types are [CATEGORICAL_MREF]" },
				{ entities, LONG,
						"Attribute data type update from [MREF] to [LONG] not allowed, allowed types are [CATEGORICAL_MREF]" },
				{ entities, DECIMAL,
						"Attribute data type update from [MREF] to [DECIMAL] not allowed, allowed types are [CATEGORICAL_MREF]" },
				{ entities, XREF,
						"Attribute data type update from [MREF] to [XREF] not allowed, allowed types are [CATEGORICAL_MREF]" },
				{ entities, CATEGORICAL,
						"Attribute data type update from [MREF] to [CATEGORICAL] not allowed, allowed types are [CATEGORICAL_MREF]" },
				{ entities, EMAIL,
						"Attribute data type update from [MREF] to [EMAIL] not allowed, allowed types are [CATEGORICAL_MREF]" },
				{ entities, HYPERLINK,
						"Attribute data type update from [MREF] to [HYPERLINK] not allowed, allowed types are [CATEGORICAL_MREF]" },
				{ entities, HTML,
						"Attribute data type update from [MREF] to [HTML] not allowed, allowed types are [CATEGORICAL_MREF]" },
				{ entities, ENUM,
						"Attribute data type update from [MREF] to [ENUM] not allowed, allowed types are [CATEGORICAL_MREF]" },
				{ entities, DATE,
						"Attribute data type update from [MREF] to [DATE] not allowed, allowed types are [CATEGORICAL_MREF]" },
				{ entities, DATE_TIME,
						"Attribute data type update from [MREF] to [DATE_TIME] not allowed, allowed types are [CATEGORICAL_MREF]" },
				{ entities, FILE,
						"Attribute data type update from [MREF] to [FILE] not allowed, allowed types are [CATEGORICAL_MREF]" },
				{ entities, COMPOUND,
						"Attribute data type update from [MREF] to [COMPOUND] not allowed, allowed types are [CATEGORICAL_MREF]" },
				{ entities, ONE_TO_MANY,
						"Invalid [xref] value [] for attribute [Referenced entity] of entity [mainAttribute] with type [sys_md_Attribute]. Offended validation expression: $('refEntityType').isNull().and($('type').matches(/^(categorical|categoricalmref|file|mref|onetomany|xref)$/).not()).or($('refEntityType').isNull().not().and($('type').matches(/^(categorical|categoricalmref|file|mref|onetomany|xref)$/))).value().Invalid [xref] value [] for attribute [Mapped by] of entity [mainAttribute] with type [sys_md_Attribute]. Offended validation expression: $('mappedBy').isNull().and($('type').eq('onetomany').not()).or($('mappedBy').isNull().not().and($('type').eq('onetomany'))).value()" } };
	}

	/**
	 * Invalid conversion cases for MREF to:
	 * BOOL, STRING, TEXT, SCRIPT INT, LONG, DECIMAL, XREF, CATEGORICAL, EMAIL, HYPERLINK, HTML, ENUM, DATE, DATE_TIME, FILE, COMPOUND, ONE_TO_MANY
	 *
	 * @param valueToConvert   The value that will be converted
	 * @param typeToConvertTo  The type to convert to
	 * @param exceptionMessage The expected exception message
	 */
	@Test(dataProvider = "invalidConversionTestCases")
	public void testInvalidConversion(List<Object> valueToConvert, AttributeType typeToConvertTo,
			String exceptionMessage)
	{
		try
		{
			testTypeConversion(valueToConvert, typeToConvertTo);
			fail("Conversion should have failed");
		}
		catch (ValidationException e)
		{
			assertEquals(e.getMessage(), exceptionMessage);
		}
	}
}

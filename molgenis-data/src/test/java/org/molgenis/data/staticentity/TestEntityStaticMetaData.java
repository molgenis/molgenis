package org.molgenis.data.staticentity;

import org.molgenis.data.EntityTestHarness;
import org.molgenis.data.TestPackage;
import org.molgenis.data.meta.SystemEntityType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.Package;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.lang.String.valueOf;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.AttributeType.*;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_LABEL;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;

@Component
public class TestEntityStaticMetaData extends SystemEntityType
{
	private static final String SIMPLE_NAME = "TypeTestStatic";
	public static final String TEST_ENTITY = TestPackage.PACKAGE_TEST_ENTITY + PACKAGE_SEPARATOR + SIMPLE_NAME;

	private final Package testPackage;
	private final TestRefEntityStaticMetaData testRefEntityStaticMetaData;

	@Autowired
	public TestEntityStaticMetaData(TestPackage testPackage, TestRefEntityStaticMetaData testRefEntityStaticMetaData)
	{
		super(SIMPLE_NAME, TestPackage.PACKAGE_TEST_ENTITY);
		this.testPackage = requireNonNull(testPackage);
		this.testRefEntityStaticMetaData = testRefEntityStaticMetaData;
	}

	@Override
	public void init()
	{
		setLabel("TypeTestStatic");
		setPackage(testPackage);
		addAttribute(EntityTestHarness.ATTR_ID, ROLE_ID).setAuto(true);
		addAttribute(EntityTestHarness.ATTR_STRING, ROLE_LABEL).setNillable(false);
		addAttribute(EntityTestHarness.ATTR_BOOL).setDataType(BOOL)
												 .setAggregatable(true)
												 .setNillable(false)
												 .setDefaultValue(valueOf(true));
		addAttribute(EntityTestHarness.ATTR_CATEGORICAL).setDataType(CATEGORICAL)
														.setRefEntity(testRefEntityStaticMetaData);
		addAttribute(EntityTestHarness.ATTR_CATEGORICAL_MREF).setDataType(CATEGORICAL_MREF)
															 .setRefEntity(testRefEntityStaticMetaData);
		addAttribute(EntityTestHarness.ATTR_ENUM).setDataType(ENUM)
												 .setEnumOptions(asList("option1", "option2"))
												 .setAggregatable(true)
												 .setNillable(false)
												 .setDefaultValue("option1");
		addAttribute(EntityTestHarness.ATTR_DATE).setDataType(DATE);
		addAttribute(EntityTestHarness.ATTR_DATETIME).setDataType(DATE_TIME);
		addAttribute(EntityTestHarness.ATTR_EMAIL).setDataType(EMAIL);
		addAttribute(EntityTestHarness.ATTR_DECIMAL).setDataType(DECIMAL);
		addAttribute(EntityTestHarness.ATTR_HTML).setDataType(HTML);
		addAttribute(EntityTestHarness.ATTR_HYPERLINK).setDataType(HYPERLINK);
		addAttribute(EntityTestHarness.ATTR_LONG).setDataType(LONG);
		addAttribute(EntityTestHarness.ATTR_INT).setDataType(INT)
												.setAggregatable(true)
												.setNillable(false)
												.setDefaultValue(valueOf(1));
		addAttribute(EntityTestHarness.ATTR_SCRIPT).setDataType(SCRIPT);
		addAttribute(EntityTestHarness.ATTR_XREF).setDataType(XREF).setRefEntity(testRefEntityStaticMetaData);
		addAttribute(EntityTestHarness.ATTR_MREF).setDataType(MREF).setRefEntity(testRefEntityStaticMetaData);
		Attribute compound = addAttribute(EntityTestHarness.ATTR_COMPOUND).setDataType(COMPOUND);
		addAttribute(EntityTestHarness.ATTR_COMPOUND_CHILD_INT).setDataType(INT).setParent(compound);
	}
}

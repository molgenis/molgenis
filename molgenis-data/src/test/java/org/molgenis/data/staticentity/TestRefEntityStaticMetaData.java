package org.molgenis.data.staticentity;

import org.molgenis.data.EntityTestHarness;
import org.molgenis.data.TestPackage;
import org.molgenis.data.meta.SystemEntityType;
import org.molgenis.data.meta.model.Package;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_LABEL;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;

@Component
public class TestRefEntityStaticMetaData extends SystemEntityType
{
	private static final String SIMPLE_NAME = "TypeTestRefStatic";
	public static final String TEST_REF_ENTITY = TestPackage.PACKAGE_TEST_ENTITY + PACKAGE_SEPARATOR + SIMPLE_NAME;

	private Package testPackage;

	@Autowired
	public TestRefEntityStaticMetaData(TestPackage testPackage)
	{
		super(SIMPLE_NAME, TestPackage.PACKAGE_TEST_ENTITY);
		this.testPackage = requireNonNull(testPackage);
	}

	@Override
	public void init()
	{
		setLabel("TypeTestRefStatic");
		setPackage(testPackage);
		addAttribute(EntityTestHarness.ATTR_REF_ID, ROLE_ID);
		addAttribute(EntityTestHarness.ATTR_REF_STRING, ROLE_LABEL).setNillable(false);
	}
}

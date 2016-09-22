package org.molgenis.test.data.staticentity.bidirectional.test4;

import org.molgenis.data.meta.SystemEntityMetaData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.molgenis.MolgenisFieldTypes.AttributeType.XREF;
import static org.molgenis.data.meta.model.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.EntityMetaData.AttributeRole.ROLE_LABEL;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.data.system.model.RootSystemPackage.PACKAGE_SYSTEM;

@Component
public class BookMetaData4 extends SystemEntityMetaData
{
	private static final String SIMPLE_NAME = "Book4";
	public static final String MY_REF_ENTITY = PACKAGE_SYSTEM + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public static final String ID = "id";
	public static final String LABEL = "label";
	public static final String AUTHOR = "author";

	private AuthorMetaData4 authorMetaData;

	BookMetaData4()
	{
		super(SIMPLE_NAME, PACKAGE_SYSTEM);
	}

	@Override
	public void init()
	{
		setLabel("Book");

		addAttribute(ID, ROLE_ID).setAuto(true).setLabel("Identifier");
		addAttribute(LABEL, ROLE_LABEL).setNillable(true).setLabel("Label");
		addAttribute(AUTHOR).setDataType(XREF).setRefEntity(authorMetaData).setNillable(false);
	}

	@Autowired
	public void setAuthorMetaData(AuthorMetaData4 authorMetaData)
	{
		this.authorMetaData = requireNonNull(authorMetaData);
	}
}

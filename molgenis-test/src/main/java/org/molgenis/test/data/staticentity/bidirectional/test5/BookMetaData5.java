package org.molgenis.test.data.staticentity.bidirectional.test5;

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
public class BookMetaData5 extends SystemEntityMetaData
{
	private static final String SIMPLE_NAME = "Book1";
	public static final String MY_REF_ENTITY = PACKAGE_SYSTEM + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public static final String ID = "id";
	public static final String LABEL = "label";
	public static final String AUTHOR = "author";

	private AuthorMetaData5 authorMetaData;

	BookMetaData5()
	{
		super(SIMPLE_NAME, PACKAGE_SYSTEM);
	}

	@Override
	public void init()
	{
		setLabel("Book");

		addAttribute(ID, ROLE_ID).setLabel("Identifier");
		addAttribute(LABEL, ROLE_LABEL).setNillable(true).setLabel("Label");
		addAttribute(AUTHOR).setDataType(XREF).setRefEntity(authorMetaData);
	}

	@Autowired
	public void setAuthorMetaData(AuthorMetaData5 authorMetaData)
	{
		this.authorMetaData = requireNonNull(authorMetaData);
	}
}

package org.molgenis.data.staticentity.bidirectional.authorbook2;

import org.molgenis.data.meta.SystemEntityType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.AttributeType.XREF;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_LABEL;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.data.system.model.RootSystemPackage.PACKAGE_SYSTEM;

/**
 * AuthorMetaData2 and BookMetaData2 define two entities with a OneToMany relation with a required XREF.
 */
@Component
public class BookMetaData2 extends SystemEntityType
{
	private static final String SIMPLE_NAME = "Book2";
	public static final String MY_REF_ENTITY = PACKAGE_SYSTEM + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public static final String ID = "id";
	public static final String LABEL = "label";
	public static final String AUTHOR = "author";

	private AuthorMetaData2 authorMetaData;

	BookMetaData2()
	{
		super(SIMPLE_NAME, PACKAGE_SYSTEM);
	}

	@Override
	public void init()
	{
		setLabel("Book");

		addAttribute(ID, ROLE_ID).setLabel("Identifier");
		addAttribute(LABEL, ROLE_LABEL).setNillable(false).setLabel("Label");
		addAttribute(AUTHOR).setDataType(XREF).setRefEntity(authorMetaData).setNillable(false);
	}

	@Autowired
	public void setAuthorMetaData(AuthorMetaData2 authorMetaData)
	{
		this.authorMetaData = requireNonNull(authorMetaData);
	}
}

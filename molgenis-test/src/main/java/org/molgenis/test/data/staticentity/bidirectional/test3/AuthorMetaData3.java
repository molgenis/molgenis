package org.molgenis.test.data.staticentity.bidirectional.test3;

import org.molgenis.data.meta.SystemEntityMetaData;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

import static java.util.Collections.singleton;
import static java.util.Objects.requireNonNull;
import static org.molgenis.MolgenisFieldTypes.AttributeType.ONE_TO_MANY;
import static org.molgenis.data.meta.model.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.EntityMetaData.AttributeRole.ROLE_LABEL;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.data.system.model.RootSystemPackage.PACKAGE_SYSTEM;

@Component
public class AuthorMetaData3 extends SystemEntityMetaData
{
	private static final String SIMPLE_NAME = "Author3";
	public static final String MY_ENTITY = PACKAGE_SYSTEM + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public static final String ID = "id";
	public static final String LABEL = "label";
	public static final String ATTR_BOOKS = "books";

	private BookMetaData3 bookMetaData;

	AuthorMetaData3()
	{
		super(SIMPLE_NAME, PACKAGE_SYSTEM);
	}

	@Override
	public void init()
	{
		setLabel("Author");

		addAttribute(ID, ROLE_ID).setAuto(true).setLabel("Identifier");
		addAttribute(LABEL, ROLE_LABEL).setNillable(true).setLabel("Label");
		AttributeMetaData attribute = bookMetaData.getAttribute(BookMetaData3.AUTHOR);
		addAttribute(ATTR_BOOKS).setDataType(ONE_TO_MANY).setRefEntity(bookMetaData).setMappedBy(attribute).setNillable(false);
	}

	@Autowired
	public void setBookMetaData(BookMetaData3 bookMetaData)
	{
		this.bookMetaData = requireNonNull(bookMetaData);
	}

	@Override
	public Set<SystemEntityMetaData> getDependencies()
	{
		return singleton(bookMetaData);
	}
}

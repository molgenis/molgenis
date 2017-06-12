package org.molgenis.data.staticentity.bidirectional.authorbook4;

import org.molgenis.data.Sort;
import org.molgenis.data.meta.SystemEntityType;
import org.molgenis.data.meta.model.Attribute;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

import static java.util.Collections.singleton;
import static java.util.Objects.requireNonNull;
import static org.molgenis.data.Sort.Direction.DESC;
import static org.molgenis.data.meta.AttributeType.ONE_TO_MANY;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_LABEL;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.data.system.model.RootSystemPackage.PACKAGE_SYSTEM;

/**
 * AuthorMetaData4 and BookMetaData4 define two entities having a nullable OneToMany relation with a descending order.
 */
@Component
public class AuthorMetaData4 extends SystemEntityType
{
	private static final String SIMPLE_NAME = "Author4";
	public static final String MY_ENTITY = PACKAGE_SYSTEM + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public static final String ID = "id";
	public static final String LABEL = "label";
	public static final String ATTR_BOOKS = "books";

	private BookMetaData4 bookMetaData;

	AuthorMetaData4()
	{
		super(SIMPLE_NAME, PACKAGE_SYSTEM);
	}

	@Override
	public void init()
	{
		setLabel("Author");

		addAttribute(ID, ROLE_ID).setAuto(true).setLabel("Identifier");
		addAttribute(LABEL, ROLE_LABEL).setNillable(false).setLabel("Label");
		Attribute attribute = bookMetaData.getAttribute(BookMetaData4.AUTHOR);
		addAttribute(ATTR_BOOKS).setDataType(ONE_TO_MANY)
								.setRefEntity(bookMetaData)
								.setMappedBy(attribute)
								.setOrderBy(new Sort(BookMetaData4.ID, DESC));
	}

	@Autowired
	public void setBookMetaData(BookMetaData4 bookMetaData)
	{
		this.bookMetaData = requireNonNull(bookMetaData);
	}

	@Override
	public Set<SystemEntityType> getDependencies()
	{
		return singleton(bookMetaData);
	}
}

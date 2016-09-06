package org.molgenis.data.validation.meta;

import org.molgenis.data.DataService;
import org.molgenis.data.Query;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.validation.MolgenisValidationException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.MolgenisFieldTypes.AttributeType.STRING;
import static org.molgenis.MolgenisFieldTypes.AttributeType.XREF;
import static org.molgenis.data.meta.model.AttributeMetaDataMetaData.ATTRIBUTE_META_DATA;
import static org.molgenis.data.meta.model.AttributeMetaDataMetaData.PARTS;
import static org.molgenis.data.meta.model.EntityMetaDataMetaData.ATTRIBUTES;
import static org.molgenis.data.meta.model.EntityMetaDataMetaData.ENTITY_META_DATA;

public class EntityMetaDataValidatorTest
{
	private EntityMetaDataValidator entityMetaDataValidator;
	private DataService dataService;

	private EntityMetaData entityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("entity").getMock();
	private AttributeMetaData idAttr;
	private AttributeMetaData labelAttr;
	private Query<EntityMetaData> entityQ;
	private Query<AttributeMetaData> attrQ;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		dataService = mock(DataService.class);
		MetaDataService metaDataService = mock(MetaDataService.class);
		when(dataService.getMeta()).thenReturn(metaDataService);
		entityMetaDataValidator = new EntityMetaDataValidator(dataService);

		String backendName = "backend";
		RepositoryCollection repoCollection = mock(RepositoryCollection.class);
		when(metaDataService.getBackend(backendName)).thenReturn(repoCollection);

		// valid entity meta
		entityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("entity").getMock();

		idAttr = when(mock(AttributeMetaData.class).getName()).thenReturn("idAttr").getMock();
		when(idAttr.getIdentifier()).thenReturn("#idAttr");
		when(idAttr.getDataType()).thenReturn(STRING);
		when(idAttr.isUnique()).thenReturn(true);
		when(idAttr.isNillable()).thenReturn(false);
		labelAttr = when(mock(AttributeMetaData.class).getName()).thenReturn("labelAttr").getMock();
		when(labelAttr.getIdentifier()).thenReturn("#labelAttr");
		when(labelAttr.getDataType()).thenReturn(STRING);

		//noinspection unchecked
		entityQ = mock(Query.class);
		when(dataService.query(ENTITY_META_DATA, EntityMetaData.class)).thenReturn(entityQ);
		//noinspection unchecked
		Query<EntityMetaData> entityQ0 = mock(Query.class);
		//noinspection unchecked
		Query<EntityMetaData> entityQ1 = mock(Query.class);
		when(entityQ.eq(ATTRIBUTES, idAttr)).thenReturn(entityQ0);
		when(entityQ.eq(ATTRIBUTES, labelAttr)).thenReturn(entityQ1);
		when(entityQ0.findOne()).thenReturn(null);
		when(entityQ1.findOne()).thenReturn(null);

		//noinspection unchecked
		attrQ = mock(Query.class);
		//noinspection unchecked
		Query<AttributeMetaData> attrQ0 = mock(Query.class);
		//noinspection unchecked
		Query<AttributeMetaData> attrQ1 = mock(Query.class);
		when(dataService.query(ATTRIBUTE_META_DATA, AttributeMetaData.class)).thenReturn(attrQ);
		when(attrQ.eq(PARTS, idAttr)).thenReturn(attrQ0);
		when(attrQ.eq(PARTS, labelAttr)).thenReturn(attrQ1);
		when(attrQ0.findOne()).thenReturn(null);
		when(attrQ1.findOne()).thenReturn(null);

		String packageName = "package";
		Package package_ = when(mock(Package.class).getName()).thenReturn(packageName).getMock();
		when(entityMeta.getPackage()).thenReturn(package_);
		String name = "name";
		when(entityMeta.getName()).thenReturn(packageName + '_' + name);
		when(entityMeta.getSimpleName()).thenReturn(name);
		when(entityMeta.getOwnAllAttributes()).thenReturn(newArrayList(idAttr, labelAttr));
		when(entityMeta.getOwnIdAttribute()).thenReturn(idAttr);
		when(entityMeta.getOwnLabelAttribute()).thenReturn(labelAttr);
		when(entityMeta.getOwnLookupAttributes()).thenReturn(singletonList(labelAttr));
		when(entityMeta.isAbstract()).thenReturn(false);
		when(entityMeta.getExtends()).thenReturn(null);
		when(entityMeta.getBackend()).thenReturn(backendName);
	}

	@Test(expectedExceptions = MolgenisValidationException.class, expectedExceptionsMessageRegExp = "Name \\[abstract\\] is not allowed because it is a reserved keyword.")
	public void testValidateNameIsReservedKeyword() throws Exception
	{
		when(entityMeta.getSimpleName()).thenReturn("abstract");
		entityMetaDataValidator.validate(entityMeta);
	}

	@Test(expectedExceptions = MolgenisValidationException.class, expectedExceptionsMessageRegExp = "Name \\[attributeWithNameExceedingMaxSize\\] is too long: maximum length is 30 characters.")
	public void testValidateNameIsTooLong() throws Exception
	{
		when(entityMeta.getSimpleName()).thenReturn("attributeWithNameExceedingMaxSize");
		entityMetaDataValidator.validate(entityMeta);
	}

	@Test(expectedExceptions = MolgenisValidationException.class, expectedExceptionsMessageRegExp = "Qualified entity name \\[package_name\\] not equal to entity package name \\[package\\] underscore entity name \\[invalidName\\]")
	public void testValidateFullNameDoesNotMatchPackageAndSimpleName()
	{
		when(entityMeta.getSimpleName()).thenReturn("invalidName");
		entityMetaDataValidator.validate(entityMeta);
	}

	@Test(expectedExceptions = MolgenisValidationException.class, expectedExceptionsMessageRegExp = "Qualified entity name \\[name\\] not equal to entity name \\[invalidName\\]")
	public void testValidateFullNameDoesNotMatchSimpleName()
	{
		when(entityMeta.getPackage()).thenReturn(null);
		when(entityMeta.getName()).thenReturn("name");
		when(entityMeta.getSimpleName()).thenReturn("invalidName");
		entityMetaDataValidator.validate(entityMeta);
	}

	@Test(expectedExceptions = MolgenisValidationException.class, expectedExceptionsMessageRegExp = "Attribute \\[labelAttr\\] is owned by entity \\[ownerEntity\\]")
	public void testValidateAttributeOwnedByOtherEntity()
	{
		//noinspection unchecked
		Query<EntityMetaData> entityQ0 = mock(Query.class);
		//noinspection unchecked
		Query<EntityMetaData> entityQ1 = mock(Query.class);
		when(entityQ.eq(ATTRIBUTES, idAttr)).thenReturn(entityQ0);
		when(entityQ.eq(ATTRIBUTES, labelAttr)).thenReturn(entityQ1);
		when(entityQ0.findOne()).thenReturn(null);
		EntityMetaData ownerEntityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("ownerEntity").getMock();
		when(entityQ1.findOne()).thenReturn(ownerEntityMeta);
		when(dataService.query(ATTRIBUTE_META_DATA, AttributeMetaData.class)).thenReturn(attrQ);
		when(attrQ.eq(PARTS, idAttr)).thenReturn(attrQ);
		when(attrQ.findOne()).thenReturn(null);
		entityMetaDataValidator.validate(entityMeta);
	}

	@Test
	public void testValidateAttributeOwnedBySameEntity()
	{
		//noinspection unchecked
		Query<EntityMetaData> entityQ0 = mock(Query.class);
		//noinspection unchecked
		Query<EntityMetaData> entityQ1 = mock(Query.class);
		when(entityQ.eq(ATTRIBUTES, idAttr)).thenReturn(entityQ0);
		when(entityQ.eq(ATTRIBUTES, labelAttr)).thenReturn(entityQ1);
		when(entityQ0.findOne()).thenReturn(null);
		when(entityQ1.findOne()).thenReturn(entityMeta); // same entity
		when(dataService.query(ATTRIBUTE_META_DATA, AttributeMetaData.class)).thenReturn(attrQ);
		when(attrQ.eq(PARTS, idAttr)).thenReturn(attrQ);
		when(attrQ.findOne()).thenReturn(null);
		entityMetaDataValidator.validate(entityMeta); // should not throw an exception
	}

	@Test(expectedExceptions = MolgenisValidationException.class, expectedExceptionsMessageRegExp = "Attribute \\[labelAttr\\] is owned by entity \\[ownerEntity\\]")
	public void testValidateAttributePartOwnedByOtherEntity()
	{
		when(entityQ.eq(ATTRIBUTES, idAttr)).thenReturn(entityQ);
		when(entityQ.eq(ATTRIBUTES, labelAttr)).thenReturn(entityQ);
		when(entityQ.findOne()).thenReturn(null);
		//noinspection unchecked
		Query<AttributeMetaData> attrQ0 = mock(Query.class);
		//noinspection unchecked
		Query<AttributeMetaData> attrQ1 = mock(Query.class);
		when(dataService.query(ATTRIBUTE_META_DATA, AttributeMetaData.class)).thenReturn(attrQ);
		when(attrQ.eq(PARTS, idAttr)).thenReturn(attrQ0);
		when(attrQ.eq(PARTS, labelAttr)).thenReturn(attrQ1);
		when(attrQ0.findOne()).thenReturn(null);
		AttributeMetaData attrParent = when(mock(AttributeMetaData.class).getName()).thenReturn("attrParent").getMock();
		when(attrQ1.findOne()).thenReturn(attrParent);
		//noinspection unchecked
		Query<EntityMetaData> entityQ0 = mock(Query.class);
		when(entityQ.eq(ATTRIBUTES, attrParent)).thenReturn(entityQ0);
		EntityMetaData ownerEntityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("ownerEntity").getMock();
		when(entityQ0.findOne()).thenReturn(ownerEntityMeta);
		entityMetaDataValidator.validate(entityMeta);
	}

	@Test
	public void testValidateAttributePartOwnedBySameEntity()
	{
		when(entityQ.eq(ATTRIBUTES, idAttr)).thenReturn(entityQ);
		when(entityQ.eq(ATTRIBUTES, labelAttr)).thenReturn(entityQ);
		when(entityQ.findOne()).thenReturn(null);
		//noinspection unchecked
		Query<AttributeMetaData> attrQ0 = mock(Query.class);
		//noinspection unchecked
		Query<AttributeMetaData> attrQ1 = mock(Query.class);
		when(dataService.query(ATTRIBUTE_META_DATA, AttributeMetaData.class)).thenReturn(attrQ);
		when(attrQ.eq(PARTS, idAttr)).thenReturn(attrQ0);
		when(attrQ.eq(PARTS, labelAttr)).thenReturn(attrQ1);
		when(attrQ0.findOne()).thenReturn(null);
		AttributeMetaData attrParent = when(mock(AttributeMetaData.class).getName()).thenReturn("attrParent").getMock();
		when(attrQ1.findOne()).thenReturn(attrParent);
		//noinspection unchecked
		Query<EntityMetaData> entityQ0 = mock(Query.class);
		when(entityQ.eq(ATTRIBUTES, attrParent)).thenReturn(entityQ0);
		when(entityQ0.findOne()).thenReturn(entityMeta);
		entityMetaDataValidator.validate(entityMeta); // should not throw an exception
	}

	@Test
	public void testValidateAttributeNotOwnedByExtendedEntity()
	{
		EntityMetaData extendsEntityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("entity").getMock();
		when(extendsEntityMeta.getAllAttributes()).thenReturn(emptyList());
		when(entityMeta.getExtends()).thenReturn(extendsEntityMeta);
		entityMetaDataValidator.validate(entityMeta); // should not throw an exception
	}

	@Test(expectedExceptions = MolgenisValidationException.class, expectedExceptionsMessageRegExp = "An attribute with name \\[idAttr\\] already exists in entity \\[extendsEntity\\] or one of its parents")
	public void testValidateAttributeOwnedByExtendedEntity()
	{
		EntityMetaData extendsEntityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("extendsEntity")
				.getMock();
		when(extendsEntityMeta.getAllAttributes()).thenReturn(singletonList(idAttr));
		when(entityMeta.getExtends()).thenReturn(extendsEntityMeta);
		entityMetaDataValidator.validate(entityMeta);
	}

	@Test(expectedExceptions = MolgenisValidationException.class, expectedExceptionsMessageRegExp = "ID attribute \\[idAttr\\] is not part of the entity attributes")
	public void testValidateOwnIdAttributeInAttributes()
	{
		when(entityMeta.getOwnAllAttributes()).thenReturn(singletonList(labelAttr));
		entityMetaDataValidator.validate(entityMeta);
	}

	@Test(expectedExceptions = MolgenisValidationException.class, expectedExceptionsMessageRegExp = "ID attribute \\[idAttr\\] type \\[XREF\\] is not allowed")
	public void testValidateOwnIdAttributeTypeAllowed()
	{
		when(idAttr.getDataType()).thenReturn(XREF);
		entityMetaDataValidator.validate(entityMeta);
	}

	@Test(expectedExceptions = MolgenisValidationException.class, expectedExceptionsMessageRegExp = "ID attribute \\[idAttr\\] is not a unique attribute")
	public void testValidateOwnIdAttributeUnique()
	{
		when(idAttr.isUnique()).thenReturn(false);
		entityMetaDataValidator.validate(entityMeta);
	}

	@Test(expectedExceptions = MolgenisValidationException.class, expectedExceptionsMessageRegExp = "ID attribute \\[idAttr\\] is not a non-nillable attribute")
	public void testValidateOwnIdAttributeNonNillable()
	{
		when(idAttr.isNillable()).thenReturn(true);
		entityMetaDataValidator.validate(entityMeta);
	}

	@Test(expectedExceptions = MolgenisValidationException.class, expectedExceptionsMessageRegExp = "Missing required ID attribute")
	public void testValidateOwnIdAttributeNullIdAttributeNull()
	{
		when(entityMeta.getOwnIdAttribute()).thenReturn(null);
		when(entityMeta.getIdAttribute()).thenReturn(null);
		entityMetaDataValidator.validate(entityMeta);
	}

	@Test
	public void testValidateOwnIdAttributeNullIdAttributeNullAbstract()
	{
		when(entityMeta.isAbstract()).thenReturn(true);
		when(entityMeta.getOwnIdAttribute()).thenReturn(null);
		when(entityMeta.getIdAttribute()).thenReturn(null);
		entityMetaDataValidator.validate(entityMeta); // valid
	}

	@Test
	public void testValidateOwnIdAttributeNullIdAttributeNotNull()
	{
		when(entityMeta.getOwnIdAttribute()).thenReturn(null);
		AttributeMetaData parentIdAttr = mock(AttributeMetaData.class);
		when(entityMeta.getIdAttribute()).thenReturn(parentIdAttr);
		entityMetaDataValidator.validate(entityMeta); // valid
	}

	@Test(expectedExceptions = MolgenisValidationException.class, expectedExceptionsMessageRegExp = "Label attribute \\[labelAttr\\] is not part of the entity attributes")
	public void testValidateOwnLabelAttributeInAttributes()
	{
		when(entityMeta.getOwnAllAttributes()).thenReturn(singletonList(idAttr));
		entityMetaDataValidator.validate(entityMeta);
	}

	@Test(expectedExceptions = MolgenisValidationException.class, expectedExceptionsMessageRegExp = "Lookup attribute \\[labelAttr\\] is not part of the entity attributes")
	public void testValidateOwnLookupAttributesInAttributes()
	{
		when(entityMeta.getOwnAllAttributes()).thenReturn(singletonList(idAttr));
		when(entityMeta.getOwnLabelAttribute()).thenReturn(null);
		entityMetaDataValidator.validate(entityMeta);
	}

	@Test(expectedExceptions = MolgenisValidationException.class, expectedExceptionsMessageRegExp = "Unknown backend \\[invalidBackend\\]")
	public void testValidateBackend()
	{
		when(entityMeta.getBackend()).thenReturn("invalidBackend");
		entityMetaDataValidator.validate(entityMeta);
	}
}
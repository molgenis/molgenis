package org.molgenis.data.validation.meta;

import org.molgenis.data.DataService;
import org.molgenis.data.Query;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.system.SystemEntityTypeRegistry;
import org.molgenis.data.validation.MolgenisValidationException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.AttributeType.STRING;
import static org.molgenis.data.meta.AttributeType.XREF;
import static org.molgenis.data.meta.model.AttributeMetadata.ATTRIBUTE_META_DATA;
import static org.molgenis.data.meta.model.AttributeMetadata.CHILDREN;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ATTRIBUTES;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ENTITY_TYPE_META_DATA;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.data.system.model.RootSystemPackage.PACKAGE_SYSTEM;

public class EntityTypeValidatorTest
{
	private EntityTypeValidator entityTypeValidator;
	private DataService dataService;

	private EntityType entityType = when(mock(EntityType.class).getName()).thenReturn("entity").getMock();
	private Attribute idAttr;
	private Attribute labelAttr;
	private Query<EntityType> entityQ;
	private Query<Attribute> attrQ;
	private SystemEntityTypeRegistry systemEntityTypeRegistry;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		dataService = mock(DataService.class);
		MetaDataService metaDataService = mock(MetaDataService.class);
		when(dataService.getMeta()).thenReturn(metaDataService);
		systemEntityTypeRegistry = mock(SystemEntityTypeRegistry.class);
		entityTypeValidator = new EntityTypeValidator(dataService, systemEntityTypeRegistry);

		String backendName = "backend";
		RepositoryCollection repoCollection = mock(RepositoryCollection.class);
		when(metaDataService.getBackend(backendName)).thenReturn(repoCollection);

		// valid entity meta
		entityType = when(mock(EntityType.class).getName()).thenReturn("entity").getMock();

		idAttr = when(mock(Attribute.class).getName()).thenReturn("idAttr").getMock();
		when(idAttr.getIdentifier()).thenReturn("#idAttr");
		when(idAttr.getDataType()).thenReturn(STRING);
		when(idAttr.isUnique()).thenReturn(true);
		when(idAttr.isNillable()).thenReturn(false);
		labelAttr = when(mock(Attribute.class).getName()).thenReturn("labelAttr").getMock();
		when(labelAttr.getIdentifier()).thenReturn("#labelAttr");
		when(labelAttr.getDataType()).thenReturn(STRING);

		//noinspection unchecked
		entityQ = mock(Query.class);
		when(dataService.query(ENTITY_TYPE_META_DATA, EntityType.class)).thenReturn(entityQ);
		//noinspection unchecked
		Query<EntityType> entityQ0 = mock(Query.class);
		//noinspection unchecked
		Query<EntityType> entityQ1 = mock(Query.class);
		when(entityQ.eq(ATTRIBUTES, idAttr)).thenReturn(entityQ0);
		when(entityQ.eq(ATTRIBUTES, labelAttr)).thenReturn(entityQ1);
		when(entityQ0.findOne()).thenReturn(null);
		when(entityQ1.findOne()).thenReturn(null);

		//noinspection unchecked
		attrQ = mock(Query.class);
		//noinspection unchecked
		Query<Attribute> attrQ0 = mock(Query.class);
		//noinspection unchecked
		Query<Attribute> attrQ1 = mock(Query.class);
		when(dataService.query(ATTRIBUTE_META_DATA, Attribute.class)).thenReturn(attrQ);
		when(attrQ.eq(CHILDREN, idAttr)).thenReturn(attrQ0);
		when(attrQ.eq(CHILDREN, labelAttr)).thenReturn(attrQ1);
		when(attrQ0.findOne()).thenReturn(null);
		when(attrQ1.findOne()).thenReturn(null);

		String packageName = "package";
		Package package_ = when(mock(Package.class).getName()).thenReturn(packageName).getMock();
		when(entityType.getPackage()).thenReturn(package_);
		String name = "name";
		when(entityType.getName()).thenReturn(packageName + PACKAGE_SEPARATOR + name);
		when(entityType.getSimpleName()).thenReturn(name);
		when(entityType.getOwnAllAttributes()).thenReturn(newArrayList(idAttr, labelAttr));
		when(entityType.getAllAttributes()).thenReturn(newArrayList(idAttr, labelAttr));
		when(entityType.getOwnIdAttribute()).thenReturn(idAttr);
		when(entityType.getOwnLabelAttribute()).thenReturn(labelAttr);
		when(entityType.getOwnLookupAttributes()).thenReturn(singletonList(labelAttr));
		when(entityType.isAbstract()).thenReturn(false);
		when(entityType.getExtends()).thenReturn(null);
		when(entityType.getBackend()).thenReturn(backendName);
	}

	@Test(expectedExceptions = MolgenisValidationException.class, expectedExceptionsMessageRegExp = "Name \\[logout\\] is not allowed because it is a reserved keyword.")
	public void testValidateNameIsReservedKeyword() throws Exception
	{
		when(entityType.getSimpleName()).thenReturn("logout");
		entityTypeValidator.validate(entityType);
	}

	@Test(expectedExceptions = MolgenisValidationException.class, expectedExceptionsMessageRegExp = "Name \\[attributeWithNameExceedingMaxSize\\] is too long: maximum length is 30 characters.")
	public void testValidateNameIsTooLong() throws Exception
	{
		when(entityType.getSimpleName()).thenReturn("attributeWithNameExceedingMaxSize");
		entityTypeValidator.validate(entityType);
	}

	@Test(expectedExceptions = MolgenisValidationException.class, expectedExceptionsMessageRegExp = "Qualified entity name \\[package_name\\] not equal to entity package name \\[package\\] underscore entity name \\[invalidName\\]")
	public void testValidateFullNameDoesNotMatchPackageAndSimpleName()
	{
		when(entityType.getSimpleName()).thenReturn("invalidName");
		entityTypeValidator.validate(entityType);
	}

	@Test(expectedExceptions = MolgenisValidationException.class, expectedExceptionsMessageRegExp = "Qualified entity name \\[name\\] not equal to entity name \\[invalidName\\]")
	public void testValidateFullNameDoesNotMatchSimpleName()
	{
		when(entityType.getPackage()).thenReturn(null);
		when(entityType.getName()).thenReturn("name");
		when(entityType.getSimpleName()).thenReturn("invalidName");
		entityTypeValidator.validate(entityType);
	}

	@Test
	public void testValidateAttributeOwnedBySameEntity()
	{
		//noinspection unchecked
		Query<EntityType> entityQ0 = mock(Query.class);
		//noinspection unchecked
		Query<EntityType> entityQ1 = mock(Query.class);
		when(entityQ.eq(ATTRIBUTES, idAttr)).thenReturn(entityQ0);
		when(entityQ.eq(ATTRIBUTES, labelAttr)).thenReturn(entityQ1);
		when(entityQ0.findOne()).thenReturn(null);
		when(entityQ1.findOne()).thenReturn(entityType); // same entity
		when(dataService.query(ATTRIBUTE_META_DATA, Attribute.class)).thenReturn(attrQ);
		when(attrQ.eq(CHILDREN, idAttr)).thenReturn(attrQ);
		when(attrQ.findOne()).thenReturn(null);
		entityTypeValidator.validate(entityType); // should not throw an exception
	}

	@Test
	public void testValidateAttributePartOwnedBySameEntity()
	{
		when(entityQ.eq(ATTRIBUTES, idAttr)).thenReturn(entityQ);
		when(entityQ.eq(ATTRIBUTES, labelAttr)).thenReturn(entityQ);
		when(entityQ.findOne()).thenReturn(null);
		//noinspection unchecked
		Query<Attribute> attrQ0 = mock(Query.class);
		//noinspection unchecked
		Query<Attribute> attrQ1 = mock(Query.class);
		when(dataService.query(ATTRIBUTE_META_DATA, Attribute.class)).thenReturn(attrQ);
		when(attrQ.eq(CHILDREN, idAttr)).thenReturn(attrQ0);
		when(attrQ.eq(CHILDREN, labelAttr)).thenReturn(attrQ1);
		when(attrQ0.findOne()).thenReturn(null);
		Attribute attrParent = when(mock(Attribute.class).getName()).thenReturn("attrParent").getMock();
		when(attrQ1.findOne()).thenReturn(attrParent);
		//noinspection unchecked
		Query<EntityType> entityQ0 = mock(Query.class);
		when(entityQ.eq(ATTRIBUTES, attrParent)).thenReturn(entityQ0);
		when(entityQ0.findOne()).thenReturn(entityType);
		entityTypeValidator.validate(entityType); // should not throw an exception
	}

	@Test
	public void testValidateAttributeNotOwnedByExtendedEntity()
	{
		EntityType extendsEntityType = when(mock(EntityType.class).getName()).thenReturn("entity").getMock();
		when(extendsEntityType.getAllAttributes()).thenReturn(emptyList());
		when(extendsEntityType.isAbstract()).thenReturn(true);
		when(entityType.getExtends()).thenReturn(extendsEntityType);
		entityTypeValidator.validate(entityType); // should not throw an exception
	}

	@Test(expectedExceptions = MolgenisValidationException.class, expectedExceptionsMessageRegExp = "An attribute with name \\[idAttr\\] already exists in entity \\[extendsEntity\\] or one of its parents")
	public void testValidateAttributeOwnedByExtendedEntity()
	{
		EntityType extendsEntityType = when(mock(EntityType.class).getName()).thenReturn("extendsEntity").getMock();
		when(extendsEntityType.getAllAttributes()).thenReturn(singletonList(idAttr));
		when(extendsEntityType.isAbstract()).thenReturn(true);
		when(entityType.getExtends()).thenReturn(extendsEntityType);
		entityTypeValidator.validate(entityType);
	}

	@Test(expectedExceptions = MolgenisValidationException.class, expectedExceptionsMessageRegExp = "Entity \\[package_name\\] ID attribute \\[idAttr\\] is not part of the entity attributes")
	public void testValidateOwnIdAttributeInAttributes()
	{
		when(entityType.getOwnAllAttributes()).thenReturn(singletonList(labelAttr));
		entityTypeValidator.validate(entityType);
	}

	@Test(expectedExceptions = MolgenisValidationException.class, expectedExceptionsMessageRegExp = "Entity \\[package_name\\] ID attribute \\[idAttr\\] type \\[XREF\\] is not allowed")
	public void testValidateOwnIdAttributeTypeAllowed()
	{
		when(idAttr.getDataType()).thenReturn(XREF);
		entityTypeValidator.validate(entityType);
	}

	@Test(expectedExceptions = MolgenisValidationException.class, expectedExceptionsMessageRegExp = "Entity \\[package_name\\] ID attribute \\[idAttr\\] is not a unique attribute")
	public void testValidateOwnIdAttributeUnique()
	{
		when(idAttr.isUnique()).thenReturn(false);
		entityTypeValidator.validate(entityType);
	}

	@Test(expectedExceptions = MolgenisValidationException.class, expectedExceptionsMessageRegExp = "Entity \\[package_name\\] ID attribute \\[idAttr\\] is not a non-nillable attribute")
	public void testValidateOwnIdAttributeNonNillable()
	{
		when(idAttr.isNillable()).thenReturn(true);
		entityTypeValidator.validate(entityType);
	}

	@Test(expectedExceptions = MolgenisValidationException.class, expectedExceptionsMessageRegExp = "Entity \\[package_name\\] is missing required ID attribute")
	public void testValidateOwnIdAttributeNullIdAttributeNull()
	{
		when(entityType.getOwnIdAttribute()).thenReturn(null);
		when(entityType.getIdAttribute()).thenReturn(null);
		entityTypeValidator.validate(entityType);
	}

	@Test
	public void testValidateOwnIdAttributeNullIdAttributeNullAbstract()
	{
		when(entityType.isAbstract()).thenReturn(true);
		when(entityType.getOwnIdAttribute()).thenReturn(null);
		when(entityType.getIdAttribute()).thenReturn(null);
		entityTypeValidator.validate(entityType); // valid
	}

	@Test
	public void testValidateOwnIdAttributeNullIdAttributeNotNull()
	{
		when(entityType.getOwnIdAttribute()).thenReturn(null);
		Attribute parentIdAttr = mock(Attribute.class);
		when(entityType.getIdAttribute()).thenReturn(parentIdAttr);
		entityTypeValidator.validate(entityType); // valid
	}

	@Test(expectedExceptions = MolgenisValidationException.class, expectedExceptionsMessageRegExp = "Label attribute \\[labelAttr\\] is not part of the entity attributes")
	public void testValidateOwnLabelAttributeInAttributes()
	{
		when(entityType.getOwnAllAttributes()).thenReturn(singletonList(idAttr));
		entityTypeValidator.validate(entityType);
	}

	@Test(expectedExceptions = MolgenisValidationException.class, expectedExceptionsMessageRegExp = "Lookup attribute \\[labelAttr\\] is not part of the entity attributes")
	public void testValidateOwnLookupAttributesInAttributes()
	{
		when(entityType.getOwnAllAttributes()).thenReturn(singletonList(idAttr));
		when(entityType.getOwnLabelAttribute()).thenReturn(null);
		entityTypeValidator.validate(entityType);
	}

	@Test(expectedExceptions = MolgenisValidationException.class, expectedExceptionsMessageRegExp = "Unknown backend \\[invalidBackend\\]")
	public void testValidateBackend()
	{
		when(entityType.getBackend()).thenReturn("invalidBackend");
		entityTypeValidator.validate(entityType);
	}

	@Test
	public void testValidateExtendsFromAbstract()
	{
		EntityType extendsEntityType = mock(EntityType.class);
		when(extendsEntityType.getName()).thenReturn("abstractEntity");
		when(extendsEntityType.isAbstract()).thenReturn(true);
		when(extendsEntityType.getAllAttributes()).thenReturn(emptyList());
		when(entityType.getExtends()).thenReturn(extendsEntityType);
		entityTypeValidator.validate(entityType); // valid
	}

	@Test(expectedExceptions = MolgenisValidationException.class, expectedExceptionsMessageRegExp = "EntityType \\[concreteEntity\\] is not abstract; EntityType \\[package_name\\] can't extend it")
	public void testValidateExtendsFromNonAbstract()
	{
		EntityType extendsEntityType = mock(EntityType.class);
		when(extendsEntityType.getName()).thenReturn("concreteEntity");
		when(extendsEntityType.isAbstract()).thenReturn(false);
		when(extendsEntityType.getAllAttributes()).thenReturn(emptyList());
		when(entityType.getExtends()).thenReturn(extendsEntityType);
		entityTypeValidator.validate(entityType);
	}

	@Test
	public void testValidateSystemPackageValid()
	{
		String packageName = PACKAGE_SYSTEM;
		Package rootSystemPackage = mock(Package.class);
		when(rootSystemPackage.getName()).thenReturn(packageName);

		String entityName = "entity";
		String qualifiedEntityName = packageName + '_' + entityName;
		when(entityType.getName()).thenReturn(qualifiedEntityName);
		when(entityType.getSimpleName()).thenReturn(entityName);
		when(entityType.getPackage()).thenReturn(rootSystemPackage);

		when(systemEntityTypeRegistry.hasSystemEntityType(qualifiedEntityName)).thenReturn(true);
		entityTypeValidator.validate(entityType); // valid
	}

	@Test(expectedExceptions = MolgenisValidationException.class, expectedExceptionsMessageRegExp = "Adding entity \\[sys_myEntity\\] to system package \\[sys\\] is not allowed")
	public void testValidateSystemPackageInvalid()
	{
		String packageName = PACKAGE_SYSTEM;
		Package rootSystemPackage = mock(Package.class);
		when(rootSystemPackage.getName()).thenReturn(packageName);

		String entityName = "myEntity";
		String qualifiedEntityName = packageName + '_' + entityName;
		when(entityType.getName()).thenReturn(qualifiedEntityName);
		when(entityType.getSimpleName()).thenReturn(entityName);
		when(entityType.getPackage()).thenReturn(rootSystemPackage);

		when(systemEntityTypeRegistry.hasSystemEntityType(qualifiedEntityName)).thenReturn(false);
		entityTypeValidator.validate(entityType);
	}

	@Test(expectedExceptions = MolgenisValidationException.class, expectedExceptionsMessageRegExp = "Entity \\[package_name\\] contains multiple attributes with name \\[idAttr\\]")
	public void testValidateAttributeWithDuplicateName()
	{
		when(entityType.getAllAttributes()).thenReturn(asList(idAttr, idAttr));
		entityTypeValidator.validate(entityType);
	}
}
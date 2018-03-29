package org.molgenis.data.validation.meta;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.mockito.Mock;
import org.molgenis.data.DataService;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.system.SystemEntityTypeRegistry;
import org.molgenis.data.validation.MolgenisValidationException;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static java.util.Collections.*;
import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.AttributeType.COMPOUND;
import static org.molgenis.data.meta.AttributeType.STRING;
import static org.molgenis.data.system.model.RootSystemPackage.PACKAGE_SYSTEM;

public class EntityTypeValidatorTest extends AbstractMockitoTest
{
	private EntityTypeValidator entityTypeValidator;
	@Mock
	private DataService dataService;
	@Mock
	private EntityType entityType;
	@Mock
	private EntityType parent;
	@Mock
	private Attribute idAttr;
	@Mock
	private Attribute labelAttr;
	@Mock
	private Attribute lookupAttr1;
	@Mock
	private Attribute lookupAttr2;
	@Mock
	private Package aPackage;
	@Mock
	private SystemEntityTypeRegistry systemEntityTypeRegistry;
	@Mock
	private MetaDataService metaDataService;

	@SuppressWarnings("unchecked")
	@BeforeMethod
	public void setUpBeforeMethod()
	{
		entityTypeValidator = new EntityTypeValidator(dataService, systemEntityTypeRegistry);
	}

	@Test(expectedExceptions = MolgenisValidationException.class, expectedExceptionsMessageRegExp = "Name \\[logout\\] is not allowed because it is a reserved keyword.")
	public void testValidateEntityIdIsReservedKeyword()
	{
		when(entityType.getId()).thenReturn("logout");
		EntityTypeValidator.validateEntityId(entityType);
	}

	@Test
	public void testValidateEntityIdValid()
	{
		when(entityType.getId()).thenReturn("entity");
		EntityTypeValidator.validateEntityId(entityType);
	}

	@Test(expectedExceptions = MolgenisValidationException.class, expectedExceptionsMessageRegExp = "Label of EntityType \\[entity\\] is empty")
	public void testValidateLabelIsEmpty()
	{
		when(entityType.getId()).thenReturn("entity");
		when(entityType.getLabel()).thenReturn("");
		EntityTypeValidator.validateEntityLabel(entityType);
	}

	@Test(expectedExceptions = MolgenisValidationException.class, expectedExceptionsMessageRegExp = "Label of EntityType \\[entity\\] contains only white space")
	public void testValidateLabelIsWhiteSpace()
	{
		when(entityType.getId()).thenReturn("entity");
		when(entityType.getLabel()).thenReturn("  ");
		EntityTypeValidator.validateEntityLabel(entityType);
	}

	@Test
	public void testValidateLabelIsValid()
	{
		when(entityType.getLabel()).thenReturn(" Label ");
		EntityTypeValidator.validateEntityLabel(entityType);
	}

	@Test
	public void testValidatePackageNonSystem()
	{
		when(entityType.getPackage()).thenReturn(aPackage);
		when(aPackage.getId()).thenReturn("nosys");

		entityTypeValidator.validatePackage(entityType);
	}

	@Test
	public void testValidatePackageNull()
	{
		when(entityType.getPackage()).thenReturn(null);

		entityTypeValidator.validatePackage(entityType);
	}

	@Test(expectedExceptions = MolgenisValidationException.class, expectedExceptionsMessageRegExp = "Adding entity \\[entity\\] to system package \\[sys\\] is not allowed")
	public void testValidateSystemPackageInvalid()
	{
		when(entityType.getId()).thenReturn("entity");
		when(entityType.getPackage()).thenReturn(aPackage);
		when(aPackage.getId()).thenReturn(PACKAGE_SYSTEM);
		when(systemEntityTypeRegistry.hasSystemEntityType("entity")).thenReturn(false);

		entityTypeValidator.validatePackage(entityType);
	}

	@Test
	public void testValidateSystemPackageValid()
	{
		when(entityType.getId()).thenReturn("entity");
		when(entityType.getPackage()).thenReturn(aPackage);
		when(aPackage.getId()).thenReturn(PACKAGE_SYSTEM);
		when(systemEntityTypeRegistry.hasSystemEntityType("entity")).thenReturn(true);

		entityTypeValidator.validatePackage(entityType);
	}

	@Test(expectedExceptions = MolgenisValidationException.class, expectedExceptionsMessageRegExp =
			"EntityType \\[entity\\] does not contain any attributes. "
					+ "Did you use the correct package\\+entity name combination in both the entities as well as the attributes sheet\\?")
	public void testValidateOwnAttributesNoAttributes()
	{
		when(entityType.getId()).thenReturn("entity");
		when(entityType.getAllAttributes()).thenReturn(emptyList());

		EntityTypeValidator.validateOwnAttributes(entityType);
	}

	@Test(expectedExceptions = MolgenisValidationException.class, expectedExceptionsMessageRegExp = "EntityType \\[entity\\] contains multiple attributes with name \\[id\\]")
	public void testValidateOwnAttributesAttributesWithSameName()
	{
		when(entityType.getId()).thenReturn("entity");
		when(entityType.getAllAttributes()).thenReturn(newArrayList(idAttr, labelAttr));
		when(idAttr.getName()).thenReturn("id");
		when(labelAttr.getName()).thenReturn("id");

		EntityTypeValidator.validateOwnAttributes(entityType);
	}

	@Test(expectedExceptions = MolgenisValidationException.class, expectedExceptionsMessageRegExp = "EntityType \\[entity\\] is missing required ID attribute")
	public void testValidateOwnIdAttributeNull()
	{
		when(entityType.getId()).thenReturn("entity");
		when(entityType.getOwnIdAttribute()).thenReturn(null);

		EntityTypeValidator.validateOwnIdAttribute(entityType, ImmutableMap.of("id", idAttr, "label", labelAttr));
	}

	@Test(expectedExceptions = MolgenisValidationException.class, expectedExceptionsMessageRegExp = "EntityType \\[entity\\] ID attribute \\[id\\] is not part of the entity attributes")
	public void testValidateOwnIdAttributeNotListedInAllAttributes()
	{
		when(entityType.getId()).thenReturn("entity");
		when(entityType.getOwnIdAttribute()).thenReturn(idAttr);
		String idAttributeIdentifier = "abcde";
		String labelAttributeIdentifier = "defgh";
		when(idAttr.getIdentifier()).thenReturn(idAttributeIdentifier);
		when(idAttr.getName()).thenReturn("id");

		EntityTypeValidator.validateOwnIdAttribute(entityType, ImmutableMap.of(labelAttributeIdentifier, labelAttr));
	}

	@Test(expectedExceptions = MolgenisValidationException.class, expectedExceptionsMessageRegExp = "EntityType \\[entity\\] ID attribute \\[id\\] type \\[COMPOUND\\] is not allowed")
	public void testValidateOwnIdAttributeInvalidType()
	{
		when(entityType.getId()).thenReturn("entity");
		when(entityType.getOwnIdAttribute()).thenReturn(idAttr);
		String idAttributeIdentifier = "abcde";
		when(idAttr.getIdentifier()).thenReturn(idAttributeIdentifier);
		when(idAttr.getName()).thenReturn("id");
		when(idAttr.getDataType()).thenReturn(COMPOUND);

		EntityTypeValidator.validateOwnIdAttribute(entityType, ImmutableMap.of(idAttributeIdentifier, idAttr));
	}

	@Test(expectedExceptions = MolgenisValidationException.class, expectedExceptionsMessageRegExp = "EntityType \\[entity\\] ID attribute \\[id\\] is not a unique attribute")
	public void testValidateOwnIdAttributeNotUnique()
	{
		when(entityType.getId()).thenReturn("entity");
		when(entityType.getOwnIdAttribute()).thenReturn(idAttr);
		String idAttributeIdentifier = "abcde";
		when(idAttr.getIdentifier()).thenReturn(idAttributeIdentifier);
		when(idAttr.getName()).thenReturn("id");
		when(idAttr.getDataType()).thenReturn(STRING);
		when(idAttr.isUnique()).thenReturn(false);

		EntityTypeValidator.validateOwnIdAttribute(entityType, ImmutableMap.of(idAttributeIdentifier, idAttr));
	}

	@Test(expectedExceptions = MolgenisValidationException.class, expectedExceptionsMessageRegExp = "EntityType \\[entity\\] ID attribute \\[id\\] is not a non-nillable attribute")
	public void testValidateOwnIdAttributeNillable()
	{
		when(entityType.getId()).thenReturn("entity");
		when(entityType.getOwnIdAttribute()).thenReturn(idAttr);
		String idAttributeIdentifier = "abcde";
		when(idAttr.getIdentifier()).thenReturn(idAttributeIdentifier);
		when(idAttr.getName()).thenReturn("id");
		when(idAttr.getDataType()).thenReturn(STRING);
		when(idAttr.isUnique()).thenReturn(true);
		when(idAttr.isNillable()).thenReturn(true);

		EntityTypeValidator.validateOwnIdAttribute(entityType, ImmutableMap.of(idAttributeIdentifier, idAttr));
	}

	@Test
	public void testValidateOwnIdAttributeNullAbstract()
	{
		when(entityType.getOwnIdAttribute()).thenReturn(null);
		when(entityType.isAbstract()).thenReturn(true);

		EntityTypeValidator.validateOwnIdAttribute(entityType, emptyMap());
	}

	@Test(expectedExceptions = MolgenisValidationException.class, expectedExceptionsMessageRegExp = "EntityType \\[entity\\] is missing required ID attribute")
	public void testValidateOwnIdAttributeNullParentIdNull()
	{
		when(entityType.getId()).thenReturn("entity");
		EntityTypeValidator.validateOwnIdAttribute(entityType, emptyMap());
	}

	@Test
	public void testValidateOwnIdAttributeParentHasIdAttribute()
	{
		when(entityType.getOwnIdAttribute()).thenReturn(null);
		when(entityType.getIdAttribute()).thenReturn(idAttr);
		EntityTypeValidator.validateOwnIdAttribute(entityType, emptyMap());
	}

	@Test
	public void testValidateOwnLabelAttributeNullIdAttributeVisible()
	{
		when(entityType.getIdAttribute()).thenReturn(idAttr);
		when(idAttr.isVisible()).thenReturn(true);
		EntityTypeValidator.validateOwnLabelAttribute(entityType, emptyMap());
	}

	@Test(expectedExceptions = MolgenisValidationException.class, expectedExceptionsMessageRegExp = "EntityType \\[entity\\] must define a label attribute because the identifier is hidden")
	public void testValidateOwnLabelAttributeNullIdAttributeInvisible()
	{
		when(entityType.getIdAttribute()).thenReturn(idAttr);
		when(idAttr.isVisible()).thenReturn(false);
		when(entityType.getId()).thenReturn("entity");
		EntityTypeValidator.validateOwnLabelAttribute(entityType, emptyMap());
	}

	@Test(expectedExceptions = MolgenisValidationException.class, expectedExceptionsMessageRegExp = "Label attribute \\[label\\] is not is not one of the attributes of entity \\[entity\\]")
	public void testValidateOwnLabelAttributeNotInAttributeMap()
	{
		when(entityType.getOwnLabelAttribute()).thenReturn(labelAttr);
		when(entityType.getId()).thenReturn("entity");
		when(labelAttr.getName()).thenReturn("label");
		EntityTypeValidator.validateOwnLabelAttribute(entityType, emptyMap());
	}

	@Test
	public void testValidateOwnLabelAttributeValid()
	{
		when(entityType.getOwnLabelAttribute()).thenReturn(labelAttr);
		String labelAttributeId = "bcdef";
		when(labelAttr.getIdentifier()).thenReturn(labelAttributeId);
		EntityTypeValidator.validateOwnLabelAttribute(entityType, ImmutableMap.of(labelAttributeId, labelAttr));
	}

	@Test(expectedExceptions = MolgenisValidationException.class, expectedExceptionsMessageRegExp = "EntityType \\[package_name\\] must define a label attribute because the identifier is hidden")
	public void testValidateOwnLabelAttributeIdHidden()
	{
		when(entityType.getOwnLabelAttribute()).thenReturn(null);
		when(entityType.getId()).thenReturn("package_name");
		when(entityType.getIdAttribute()).thenReturn(idAttr);
		EntityTypeValidator.validateOwnLabelAttribute(entityType, newHashMap());
	}

	@Test
	public void testValidateOwnLabelAttributeAbstractEntity()
	{
		when(entityType.getOwnLabelAttribute()).thenReturn(null);
		when(entityType.isAbstract()).thenReturn(true);
		EntityTypeValidator.validateOwnLabelAttribute(entityType, newHashMap());
	}

	@Test
	public void testValidateOwnLookupAttributesEmpty()
	{
		when(entityType.getOwnLookupAttributes()).thenReturn(emptyList());
		EntityTypeValidator.validateOwnLookupAttributes(entityType, emptyMap());
	}

	@Test(expectedExceptions = MolgenisValidationException.class, expectedExceptionsMessageRegExp = "Lookup attribute \\[lookup1\\] is not one of the attributes of entity \\[entity\\]")
	public void testValidateOwnLookupAttributesNotInAttributeMap()
	{
		when(entityType.getId()).thenReturn("entity");
		when(entityType.getOwnLookupAttributes()).thenReturn(singletonList(lookupAttr1));
		when(lookupAttr1.getName()).thenReturn("lookup1");
		EntityTypeValidator.validateOwnLookupAttributes(entityType, emptyMap());
	}

	@Test(expectedExceptions = MolgenisValidationException.class, expectedExceptionsMessageRegExp = "Lookup attribute \\[lookup2\\] of entity type \\[entity\\] must be visible")
	public void testValidateOwnLookupAttributesInvisible()
	{
		when(entityType.getOwnLookupAttributes()).thenReturn(ImmutableList.of(lookupAttr1, lookupAttr2));
		String lookupAttr1Id = "abcde";
		String lookupAttr2Id = "defgh";
		when(lookupAttr1.getIdentifier()).thenReturn(lookupAttr1Id);
		when(lookupAttr2.getIdentifier()).thenReturn(lookupAttr2Id);
		when(lookupAttr1.isVisible()).thenReturn(true);
		when(lookupAttr2.isVisible()).thenReturn(false);
		when(entityType.getId()).thenReturn("entity");
		when(lookupAttr2.getName()).thenReturn("lookup2");
		EntityTypeValidator.validateOwnLookupAttributes(entityType,
				ImmutableMap.of(lookupAttr1Id, lookupAttr1, lookupAttr2Id, lookupAttr2));
	}

	@Test
	public void testValidateOwnLookupAttributesValid()
	{
		when(entityType.getOwnLookupAttributes()).thenReturn(ImmutableList.of(lookupAttr1, lookupAttr2));
		String lookupAttr1Id = "abcde";
		String lookupAttr2Id = "defgh";
		when(lookupAttr1.getIdentifier()).thenReturn(lookupAttr1Id);
		when(lookupAttr2.getIdentifier()).thenReturn(lookupAttr2Id);
		when(lookupAttr1.isVisible()).thenReturn(true);
		when(lookupAttr2.isVisible()).thenReturn(true);
		EntityTypeValidator.validateOwnLookupAttributes(entityType,
				ImmutableMap.of(lookupAttr1Id, lookupAttr1, lookupAttr2Id, lookupAttr2));
	}

	@Test(expectedExceptions = MolgenisValidationException.class, expectedExceptionsMessageRegExp = "EntityType \\[parent\\] is not abstract; EntityType \\[entity\\] can't extend it")
	public void testValidateExtendsNonAbstract()
	{
		when(entityType.getExtends()).thenReturn(parent);
		when(parent.isAbstract()).thenReturn(false);
		when(entityType.getId()).thenReturn("entity");
		when(parent.getId()).thenReturn("parent");
		EntityTypeValidator.validateExtends(entityType);
	}

	@Test
	public void testValidateExtendsValid()
	{
		when(entityType.getExtends()).thenReturn(parent);
		when(parent.isAbstract()).thenReturn(true);
		EntityTypeValidator.validateExtends(entityType);
	}

	@Test(expectedExceptions = MolgenisValidationException.class, expectedExceptionsMessageRegExp = "Unknown backend \\[BackendName\\]")
	public void testValidateBackendInvalid()
	{
		when(entityType.getBackend()).thenReturn("BackendName");
		when(dataService.getMeta()).thenReturn(metaDataService);
		when(metaDataService.hasBackend("BackendName")).thenReturn(false);
		entityTypeValidator.validateBackend(entityType);
	}

	@Test
	public void testValidateBackendValid()
	{
		when(entityType.getBackend()).thenReturn("BackendName");
		when(dataService.getMeta()).thenReturn(metaDataService);
		when(metaDataService.hasBackend("BackendName")).thenReturn(true);
		entityTypeValidator.validateBackend(entityType);
	}

	@Test
	public void testValidateValid()
	{
		when(entityType.getId()).thenReturn("entity");
		when(entityType.getLabel()).thenReturn("Label");
		when(entityType.getPackage()).thenReturn(null);
		when(entityType.getExtends()).thenReturn(null);
		when(idAttr.getIdentifier()).thenReturn("abcde");
		when(idAttr.getDataType()).thenReturn(STRING);
		when(idAttr.isUnique()).thenReturn(true);
		when(idAttr.isVisible()).thenReturn(true);
		when(entityType.getAllAttributes()).thenReturn(singletonList(idAttr));
		when(entityType.getOwnAllAttributes()).thenReturn(singletonList(idAttr));
		when(entityType.getOwnIdAttribute()).thenReturn(idAttr);
		when(entityType.getIdAttribute()).thenReturn(idAttr);
		when(entityType.getBackend()).thenReturn("PostgreSQL");
		when(dataService.getMeta()).thenReturn(metaDataService);
		when(metaDataService.hasBackend("PostgreSQL")).thenReturn(true);
		entityTypeValidator.validate(entityType);
	}
}
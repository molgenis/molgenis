package org.molgenis.data.validation;

import org.mockito.ArgumentCaptor;
import org.molgenis.data.*;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.QueryImpl;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static org.mockito.Mockito.*;
import static org.molgenis.data.meta.AttributeType.*;
import static org.testng.Assert.assertEquals;

public class RepositoryValidationDecoratorTest
{
	private String refEntityName;
	private String refAttrIdName;
	private EntityType refEntityType;
	private String entityTypeId;
	private String attrIdName;
	private String attrXrefName;
	private String attrNillableXrefName;
	private String attrMrefName;
	private String attrNillableMrefName;
	private String attrUniqueStringName;
	private String attrUniqueXrefName;
	private Attribute idAttr;
	private Attribute xrefAttr;
	private Attribute nillableXrefAttr;
	private Attribute mrefAttr;
	private Attribute nillableMrefAttr;
	private Attribute uniqueStringAttr;
	private Attribute uniqueXrefAttr;
	private EntityType entityType;
	private Repository<Entity> decoratedRepo;
	private Repository<Entity> refRepo;
	private DataService dataService;
	private MetaDataService metaDataService;
	private EntityAttributesValidator entityAttributesValidator;
	private ExpressionValidator expressionValidator;
	private RepositoryValidationDecorator repositoryValidationDecorator;
	private Entity refEntity0;
	private Entity refEntity0Clone;
	private Entity refEntity1;
	private Entity refEntity1Clone;

	@SuppressWarnings("unchecked")
	@BeforeMethod
	public void beforeMethod()
	{
		// ref entity meta
		refEntityName = "refEntity";

		refAttrIdName = "refId";

		Attribute refIdAttr = when(mock(Attribute.class).getName()).thenReturn(refAttrIdName).getMock();
		when(refIdAttr.getDataType()).thenReturn(STRING);

		refEntityType = mock(EntityType.class);
		when(refEntityType.getId()).thenReturn(refEntityName);
		when(refEntityType.getLabel()).thenReturn(refEntityName);
		when(refEntityType.getIdAttribute()).thenReturn(refIdAttr);
		when(refEntityType.getAtomicAttributes()).thenReturn(Arrays.asList(refIdAttr));

		// entity meta
		entityTypeId = "entity";

		attrIdName = "id";
		attrXrefName = "xrefAttr";
		attrNillableXrefName = "nillableXrefAttr";
		attrMrefName = "mrefAttr";
		attrNillableMrefName = "nillableMrefAttr";
		attrUniqueStringName = "uniqueStringAttr";
		attrUniqueXrefName = "uniqueXrefAttr";

		idAttr = when(mock(Attribute.class).getName()).thenReturn(attrIdName).getMock();
		when(idAttr.getDataType()).thenReturn(STRING);

		xrefAttr = when(mock(Attribute.class).getName()).thenReturn(attrXrefName).getMock();
		when(xrefAttr.getRefEntity()).thenReturn(refEntityType);
		when(xrefAttr.getDataType()).thenReturn(XREF);
		when(xrefAttr.isNillable()).thenReturn(false);

		nillableXrefAttr = when(mock(Attribute.class).getName()).thenReturn(attrNillableXrefName).getMock();
		when(nillableXrefAttr.getRefEntity()).thenReturn(refEntityType);
		when(nillableXrefAttr.getDataType()).thenReturn(XREF);
		when(nillableXrefAttr.isNillable()).thenReturn(true);

		mrefAttr = when(mock(Attribute.class).getName()).thenReturn(attrMrefName).getMock();
		when(mrefAttr.getRefEntity()).thenReturn(refEntityType);
		when(mrefAttr.getDataType()).thenReturn(MREF);
		when(mrefAttr.isNillable()).thenReturn(false);

		nillableMrefAttr = when(mock(Attribute.class).getName()).thenReturn(attrNillableMrefName).getMock();
		when(nillableMrefAttr.getRefEntity()).thenReturn(refEntityType);
		when(nillableMrefAttr.getDataType()).thenReturn(MREF);
		when(nillableMrefAttr.isNillable()).thenReturn(true);

		uniqueStringAttr = when(mock(Attribute.class).getName()).thenReturn(attrUniqueStringName).getMock();
		when(uniqueStringAttr.getDataType()).thenReturn(STRING);
		when(uniqueStringAttr.isUnique()).thenReturn(true);

		uniqueXrefAttr = when(mock(Attribute.class).getName()).thenReturn(attrUniqueXrefName).getMock();
		when(uniqueXrefAttr.getRefEntity()).thenReturn(refEntityType);
		when(uniqueXrefAttr.getDataType()).thenReturn(XREF);
		when(uniqueXrefAttr.isUnique()).thenReturn(true);

		entityType = mock(EntityType.class);
		when(entityType.getId()).thenReturn(entityTypeId);
		when(entityType.getLabel()).thenReturn(entityTypeId);
		when(entityType.getIdAttribute()).thenReturn(idAttr);
		when(entityType.getAttribute(attrIdName)).thenReturn(idAttr);
		when(entityType.getAttribute(attrXrefName)).thenReturn(xrefAttr);
		when(entityType.getAttribute(attrNillableXrefName)).thenReturn(nillableXrefAttr);
		when(entityType.getAttribute(attrMrefName)).thenReturn(mrefAttr);
		when(entityType.getAttribute(attrNillableMrefName)).thenReturn(nillableMrefAttr);
		when(entityType.getAttribute(attrUniqueStringName)).thenReturn(uniqueStringAttr);
		when(entityType.getAttribute(attrUniqueXrefName)).thenReturn(uniqueXrefAttr);
		when(entityType.getAtomicAttributes()).thenReturn(
				Arrays.asList(idAttr, xrefAttr, nillableXrefAttr, mrefAttr, nillableMrefAttr, uniqueStringAttr,
						uniqueXrefAttr));

		// ref entities
		String refEntity0Id = "idref0";
		refEntity0 = mock(Entity.class);
		when(refEntity0.getEntityType()).thenReturn(refEntityType);
		when(refEntity0.getIdValue()).thenReturn(refEntity0Id);
		when(refEntity0.get(refAttrIdName)).thenReturn(refEntity0Id);
		when(refEntity0.getString(refAttrIdName)).thenReturn(refEntity0Id);

		refEntity0Clone = mock(Entity.class);
		when(refEntity0Clone.getEntityType()).thenReturn(refEntityType);
		when(refEntity0Clone.getIdValue()).thenReturn(refEntity0Id);
		when(refEntity0Clone.get(refAttrIdName)).thenReturn(refEntity0Id);
		when(refEntity0Clone.getString(refAttrIdName)).thenReturn(refEntity0Id);

		String refEntity1Id = "idref1";
		refEntity1 = mock(Entity.class);
		when(refEntity1.getEntityType()).thenReturn(refEntityType);
		when(refEntity1.getIdValue()).thenReturn(refEntity1Id);
		when(refEntity1.get(refAttrIdName)).thenReturn(refEntity1Id);
		when(refEntity1.getString(refAttrIdName)).thenReturn(refEntity1Id);

		refEntity1Clone = mock(Entity.class);
		when(refEntity1Clone.getEntityType()).thenReturn(refEntityType);
		when(refEntity1Clone.getIdValue()).thenReturn(refEntity1Id);
		when(refEntity1Clone.get(refAttrIdName)).thenReturn(refEntity1Id);
		when(refEntity1Clone.getString(refAttrIdName)).thenReturn(refEntity1Id);

		// beans
		decoratedRepo = mock(Repository.class);
		when(decoratedRepo.getEntityType()).thenReturn(entityType);
		when(decoratedRepo.getName()).thenReturn(entityTypeId);
		when(decoratedRepo.findAll(new QueryImpl<>().fetch(
				new Fetch().field(attrUniqueStringName).field(attrUniqueXrefName)))).thenReturn(Stream.empty());
		refRepo = mock(Repository.class);
		when(refRepo.getEntityType()).thenReturn(refEntityType);

		dataService = mock(DataService.class);
		when(dataService.getRepository(entityTypeId)).thenReturn(decoratedRepo);
		when(dataService.getRepository(refEntityName)).thenReturn(refRepo);
		when(dataService.findAll(refEntityName, new QueryImpl<>().fetch(new Fetch().field(refAttrIdName)))).thenReturn(
				Stream.of(refEntity0, refEntity1));

		metaDataService = mock(MetaDataService.class);
		when(dataService.getMeta()).thenReturn(metaDataService);

		expressionValidator = mock(ExpressionValidator.class);
		entityAttributesValidator = mock(EntityAttributesValidator.class);
		repositoryValidationDecorator = new RepositoryValidationDecorator(dataService, decoratedRepo,
				entityAttributesValidator, expressionValidator);
	}

	@Test
	public void query() throws Exception
	{
		assertEquals(repositoryValidationDecorator.query().getRepository(), repositoryValidationDecorator);
	}

	@Test
	public void add()
	{
		// entities
		Entity entity0 = mock(Entity.class);
		when(entity0.getEntityType()).thenReturn(entityType);

		when(entity0.getIdValue()).thenReturn("id0");
		when(entity0.getEntity(attrXrefName)).thenReturn(refEntity0);
		when(entity0.getEntity(attrNillableXrefName)).thenReturn(null);
		when(entity0.getEntities(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.getEntities(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.getString(attrUniqueStringName)).thenReturn("unique0");
		when(entity0.getEntity(attrUniqueXrefName)).thenReturn(refEntity0);

		when(entity0.get(attrIdName)).thenReturn("id0");
		when(entity0.get(attrXrefName)).thenReturn(refEntity0);
		when(entity0.get(attrNillableXrefName)).thenReturn(null);
		when(entity0.get(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.get(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.get(attrUniqueStringName)).thenReturn("unique0");
		when(entity0.get(attrUniqueXrefName)).thenReturn(refEntity0);

		// actual tests
		repositoryValidationDecorator.add(entity0);
		verify(decoratedRepo, times(1)).add(entity0);
		verify(entityAttributesValidator, times(1)).validate(entity0, entityType);
	}

	@Test
	public void addEntityDoesNotRequireValidationDueToRepoCapabilities()
	{
		when(decoratedRepo.getCapabilities()).thenReturn(
				new HashSet<>(Arrays.asList(RepositoryCapability.VALIDATE_NOTNULL_CONSTRAINT)));

		// entities
		Entity entity0 = mock(Entity.class);
		when(entity0.getEntityType()).thenReturn(entityType);

		when(entity0.getIdValue()).thenReturn("id0");
		when(entity0.getEntity(attrXrefName)).thenReturn(null); // valid, because entity is validated by decorated repo
		when(entity0.getEntity(attrNillableXrefName)).thenReturn(null);
		when(entity0.getEntities(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.getEntities(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.getString(attrUniqueStringName)).thenReturn("unique0");
		when(entity0.getEntity(attrUniqueXrefName)).thenReturn(refEntity0);

		when(entity0.get(attrIdName)).thenReturn("id0");
		when(entity0.get(attrXrefName)).thenReturn(null); // valid, because entity is validated by decorated repo
		when(entity0.get(attrNillableXrefName)).thenReturn(null);
		when(entity0.get(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.get(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.get(attrUniqueStringName)).thenReturn("unique0");
		when(entity0.get(attrUniqueXrefName)).thenReturn(refEntity0);

		// actual tests
		repositoryValidationDecorator.add(entity0);
		verify(decoratedRepo, times(1)).add(entity0);
	}

	@Test
	public void addEntityCrossRepositoryCollectionReference()
	{
		when(decoratedRepo.getCapabilities()).thenReturn(
				new HashSet<>(Arrays.asList(RepositoryCapability.VALIDATE_REFERENCE_CONSTRAINT)));
		when(dataService.getMeta()).thenReturn(metaDataService);
		// references need to be validated because they are stored in another repository collection
		RepositoryCollection thisBackend = mock(RepositoryCollection.class);
		when(thisBackend.getName()).thenReturn("thisBackend");
		RepositoryCollection otherBackend = mock(RepositoryCollection.class);
		when(otherBackend.getName()).thenReturn("otherBackend");
		when(metaDataService.getBackend(entityType)).thenReturn(thisBackend);
		when(metaDataService.getBackend(refEntityType)).thenReturn(otherBackend);

		String refEntityDoesNotExistId = "id1";
		Entity refEntityDoesNotExist = mock(Entity.class);
		when(refEntityDoesNotExist.getEntityType()).thenReturn(refEntityType);
		when(refEntityDoesNotExist.getIdValue()).thenReturn(refEntityDoesNotExistId);
		when(refEntityDoesNotExist.get(refAttrIdName)).thenReturn(refEntityDoesNotExistId);
		when(refEntityDoesNotExist.getString(refAttrIdName)).thenReturn(refEntityDoesNotExistId);

		// entities
		Entity entity0 = mock(Entity.class);
		when(entity0.getEntityType()).thenReturn(entityType);

		when(entity0.getIdValue()).thenReturn("id0");
		when(entity0.getEntity(attrXrefName)).thenReturn(refEntityDoesNotExist); // validation error
		when(entity0.getEntity(attrNillableXrefName)).thenReturn(null);
		when(entity0.getEntities(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.getEntities(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.getString(attrUniqueStringName)).thenReturn("unique0");
		when(entity0.getEntity(attrUniqueXrefName)).thenReturn(refEntity0);

		when(entity0.get(attrIdName)).thenReturn("id0");
		when(entity0.get(attrXrefName)).thenReturn(refEntity0);
		when(entity0.get(attrNillableXrefName)).thenReturn(null);
		when(entity0.get(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.get(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.get(attrUniqueStringName)).thenReturn("unique0");
		when(entity0.get(attrUniqueXrefName)).thenReturn(refEntity0);

		// actual tests
		try
		{
			repositoryValidationDecorator.add(entity0);
			throw new RuntimeException("Expected MolgenisValidationException instead of no exception");
		}
		catch (MolgenisValidationException e)
		{
			verify(entityAttributesValidator, times(1)).validate(entity0, entityType);
			assertEquals(e.getMessage(),
					"Unknown xref value 'id1' for attribute 'xrefAttr' of entity 'entity'. (entity 1)");
		}
	}

	@Test
	public void addEntityAttributesValidationError()
	{
		// entities
		Entity entity0 = mock(Entity.class);
		when(entity0.getEntityType()).thenReturn(entityType);

		when(entity0.getIdValue()).thenReturn("id0");
		when(entity0.getEntity(attrXrefName)).thenReturn(refEntity0);
		when(entity0.getEntity(attrNillableXrefName)).thenReturn(null);
		when(entity0.getEntities(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.getEntities(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.getString(attrUniqueStringName)).thenReturn("unique0");
		when(entity0.getEntity(attrUniqueXrefName)).thenReturn(refEntity0);

		when(entity0.get(attrIdName)).thenReturn("id0");
		when(entity0.get(attrXrefName)).thenReturn(refEntity0);
		when(entity0.get(attrNillableXrefName)).thenReturn(null);
		when(entity0.get(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.get(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.get(attrUniqueStringName)).thenReturn("unique0");
		when(entity0.get(attrUniqueXrefName)).thenReturn(refEntity0);

		Set<ConstraintViolation> violations = singleton(new ConstraintViolation("violation", 2L));
		when(entityAttributesValidator.validate(entity0, entityType)).thenReturn(violations);

		// actual tests
		try
		{
			repositoryValidationDecorator.add(entity0);
			throw new RuntimeException("Expected MolgenisValidationException instead of no exception");
		}
		catch (MolgenisValidationException e)
		{
			verify(entityAttributesValidator, times(1)).validate(entity0, entityType);
			assertEquals(e.getViolations(), violations);
		}
	}

	@Test
	public void addRequiredValueValidationError()
	{
		// entities
		Entity entity0 = mock(Entity.class);
		when(entity0.getEntityType()).thenReturn(entityType);

		when(entity0.getIdValue()).thenReturn("id0");
		when(entity0.getEntity(attrXrefName)).thenReturn(null); // violation error
		when(entity0.getEntity(attrNillableXrefName)).thenReturn(null);
		when(entity0.getEntities(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.getEntities(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.getString(attrUniqueStringName)).thenReturn("unique0");
		when(entity0.getEntity(attrUniqueXrefName)).thenReturn(refEntity0);

		when(entity0.get(attrIdName)).thenReturn("id0");
		when(entity0.get(attrXrefName)).thenReturn(null); // violation error
		when(entity0.get(attrNillableXrefName)).thenReturn(null);
		when(entity0.get(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.get(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.get(attrUniqueStringName)).thenReturn("unique0");
		when(entity0.get(attrUniqueXrefName)).thenReturn(refEntity0);

		// actual tests
		try
		{
			repositoryValidationDecorator.add(entity0);
			throw new RuntimeException("Expected MolgenisValidationException instead of no exception");
		}
		catch (MolgenisValidationException e)
		{
			verify(entityAttributesValidator, times(1)).validate(entity0, entityType);
			assertEquals(e.getMessage(), "The attribute 'xrefAttr' of entity 'entity' can not be null. (entity 1)");
		}
	}

	@Test
	public void addRequiredValueWithExpression()
	{
		when(xrefAttr.getExpression()).thenReturn("expr");

		// entities
		Entity entity0 = mock(Entity.class);
		when(entity0.getEntityType()).thenReturn(entityType);

		when(entity0.getIdValue()).thenReturn("id0");
		when(entity0.getEntity(attrXrefName)).thenReturn(null); // valid, because the value is 'computed'
		when(entity0.getEntity(attrNillableXrefName)).thenReturn(null);
		when(entity0.getEntities(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.getEntities(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.getString(attrUniqueStringName)).thenReturn("unique0");
		when(entity0.getEntity(attrUniqueXrefName)).thenReturn(refEntity0);

		when(entity0.get(attrIdName)).thenReturn("id0");
		when(entity0.get(attrXrefName)).thenReturn(null); // valid, because the value is 'computed'
		when(entity0.get(attrNillableXrefName)).thenReturn(null);
		when(entity0.get(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.get(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.get(attrUniqueStringName)).thenReturn("unique0");
		when(entity0.get(attrUniqueXrefName)).thenReturn(refEntity0);

		// actual tests
		repositoryValidationDecorator.add(entity0);
		verify(decoratedRepo, times(1)).add(entity0);
		verify(entityAttributesValidator, times(1)).validate(entity0, entityType);
	}

	@Test
	public void addRequiredMrefValueValidationError()
	{
		// entities
		Entity entity0 = mock(Entity.class);
		when(entity0.getEntityType()).thenReturn(entityType);

		when(entity0.getIdValue()).thenReturn("id0");
		when(entity0.getEntity(attrXrefName)).thenReturn(refEntity0);
		when(entity0.getEntity(attrNillableXrefName)).thenReturn(null);
		when(entity0.getEntities(attrMrefName)).thenReturn(emptyList());
		when(entity0.getEntities(attrNillableMrefName)).thenReturn(emptyList()); // violation error
		when(entity0.getString(attrUniqueStringName)).thenReturn("unique0");
		when(entity0.getEntity(attrUniqueXrefName)).thenReturn(refEntity0);

		when(entity0.get(attrIdName)).thenReturn("id0");
		when(entity0.get(attrXrefName)).thenReturn(refEntity0);
		when(entity0.get(attrNillableXrefName)).thenReturn(null);
		when(entity0.get(attrMrefName)).thenReturn(emptyList());
		when(entity0.get(attrNillableMrefName)).thenReturn(emptyList()); // violation error
		when(entity0.get(attrUniqueStringName)).thenReturn("unique0");
		when(entity0.get(attrUniqueXrefName)).thenReturn(refEntity0);

		// actual tests
		try
		{
			repositoryValidationDecorator.add(entity0);
			throw new RuntimeException("Expected MolgenisValidationException instead of no exception");
		}
		catch (MolgenisValidationException e)
		{
			verify(entityAttributesValidator, times(1)).validate(entity0, entityType);
			assertEquals(e.getMessage(), "The attribute 'mrefAttr' of entity 'entity' can not be null. (entity 1)");
		}
	}

	@Test
	public void addReferenceXrefSelfReferenceToPreviouslyAddedEntity()
	{
		when(xrefAttr.getRefEntity()).thenReturn(entityType);

		// entities
		Entity entity0 = mock(Entity.class);
		when(entity0.getEntityType()).thenReturn(entityType);

		when(entity0.getIdValue()).thenReturn("id0");
		when(entity0.getEntity(attrXrefName)).thenReturn(entity0); // self reference
		when(entity0.getEntity(attrNillableXrefName)).thenReturn(null);
		when(entity0.getEntities(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.getEntities(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.getString(attrUniqueStringName)).thenReturn("unique0");
		when(entity0.getEntity(attrUniqueXrefName)).thenReturn(refEntity0);

		when(entity0.get(attrIdName)).thenReturn("id0");
		when(entity0.get(attrXrefName)).thenReturn(entity0); // self reference
		when(entity0.get(attrNillableXrefName)).thenReturn(null);
		when(entity0.get(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.get(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.get(attrUniqueStringName)).thenReturn("unique0");
		when(entity0.get(attrUniqueXrefName)).thenReturn(refEntity0);

		// actual tests
		when(dataService.findAll(entityTypeId,
				new QueryImpl<>().fetch(new Fetch().field(attrIdName)))).thenReturn(Stream.of(entity0));
		repositoryValidationDecorator.add(entity0);

		verify(decoratedRepo, times(1)).add(entity0);
		verify(entityAttributesValidator, times(1)).validate(entity0, entityType);
	}

	@Test
	public void addReferenceXrefDoesNotExistsValidationError()
	{
		String refEntityDoesNotExistId = "id1";
		Entity refEntityDoesNotExist = mock(Entity.class);
		when(refEntityDoesNotExist.getEntityType()).thenReturn(refEntityType);
		when(refEntityDoesNotExist.getIdValue()).thenReturn(refEntityDoesNotExistId);
		when(refEntityDoesNotExist.get(refAttrIdName)).thenReturn(refEntityDoesNotExistId);
		when(refEntityDoesNotExist.getString(refAttrIdName)).thenReturn(refEntityDoesNotExistId);

		// entities
		Entity entity0 = mock(Entity.class);
		when(entity0.getEntityType()).thenReturn(entityType);

		when(entity0.getIdValue()).thenReturn("id1");
		when(entity0.getEntity(attrXrefName)).thenReturn(refEntityDoesNotExist);
		when(entity0.getEntity(attrNillableXrefName)).thenReturn(null);
		when(entity0.getEntities(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.getEntities(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.getString(attrUniqueStringName)).thenReturn("unique1");
		when(entity0.getEntity(attrUniqueXrefName)).thenReturn(refEntity1);

		when(entity0.get(attrIdName)).thenReturn("id1");
		when(entity0.get(attrXrefName)).thenReturn(refEntityDoesNotExist);
		when(entity0.get(attrNillableXrefName)).thenReturn(null);
		when(entity0.get(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.get(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.get(attrUniqueStringName)).thenReturn("unique1");
		when(entity0.get(attrUniqueXrefName)).thenReturn(refEntity1);

		// actual tests
		try
		{
			repositoryValidationDecorator.add(entity0);
			throw new RuntimeException("Expected MolgenisValidationException instead of no exception");
		}
		catch (MolgenisValidationException e)
		{
			verify(entityAttributesValidator, times(1)).validate(entity0, entityType);
			assertEquals(e.getMessage(),
					"Unknown xref value 'id1' for attribute 'xrefAttr' of entity 'entity'. (entity 1)");
		}
	}

	@Test
	public void addReferenceAttrWithExpression()
	{
		when(xrefAttr.getExpression()).thenReturn("expr");

		String refEntityDoesNotExistId = "id1";
		Entity refEntityDoesNotExist = mock(Entity.class);
		when(refEntityDoesNotExist.getEntityType()).thenReturn(refEntityType);
		when(refEntityDoesNotExist.getIdValue()).thenReturn(refEntityDoesNotExistId);
		when(refEntityDoesNotExist.get(refAttrIdName)).thenReturn(refEntityDoesNotExistId);
		when(refEntityDoesNotExist.getString(refAttrIdName)).thenReturn(refEntityDoesNotExistId);

		// entities
		Entity entity0 = mock(Entity.class);
		when(entity0.getEntityType()).thenReturn(entityType);

		when(entity0.getIdValue()).thenReturn("id1");
		when(entity0.getEntity(attrXrefName)).thenReturn(refEntityDoesNotExist); // valid, because the value is computed
		when(entity0.getEntity(attrNillableXrefName)).thenReturn(null);
		when(entity0.getEntities(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.getEntities(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.getString(attrUniqueStringName)).thenReturn("unique1");
		when(entity0.getEntity(attrUniqueXrefName)).thenReturn(refEntity1);

		when(entity0.get(attrIdName)).thenReturn("id1");
		when(entity0.get(attrXrefName)).thenReturn(refEntityDoesNotExist);
		when(entity0.get(attrNillableXrefName)).thenReturn(null);
		when(entity0.get(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.get(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.get(attrUniqueStringName)).thenReturn("unique1");
		when(entity0.get(attrUniqueXrefName)).thenReturn(refEntity1);

		// actual tests
		repositoryValidationDecorator.add(entity0);
		verify(decoratedRepo, times(1)).add(entity0);
		verify(entityAttributesValidator, times(1)).validate(entity0, entityType);
	}

	@Test
	public void addReferenceMrefDoesNotExistsValidationError()
	{
		String refEntityDoesNotExistId = "id1";
		Entity refEntityDoesNotExist = mock(Entity.class);
		when(refEntityDoesNotExist.getEntityType()).thenReturn(refEntityType);
		when(refEntityDoesNotExist.getIdValue()).thenReturn(refEntityDoesNotExistId);
		when(refEntityDoesNotExist.get(refAttrIdName)).thenReturn(refEntityDoesNotExistId);
		when(refEntityDoesNotExist.getString(refAttrIdName)).thenReturn(refEntityDoesNotExistId);

		// entities
		Entity entity0 = mock(Entity.class);
		when(entity0.getEntityType()).thenReturn(entityType);

		when(entity0.getIdValue()).thenReturn("id1");
		when(entity0.getEntity(attrXrefName)).thenReturn(refEntity0);
		when(entity0.getEntity(attrNillableXrefName)).thenReturn(null);
		when(entity0.getEntities(attrMrefName)).thenReturn(Arrays.asList(refEntity0, refEntityDoesNotExist));
		when(entity0.getEntities(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.getString(attrUniqueStringName)).thenReturn("unique1");
		when(entity0.getEntity(attrUniqueXrefName)).thenReturn(refEntity1);

		when(entity0.get(attrIdName)).thenReturn("id1");
		when(entity0.get(attrXrefName)).thenReturn(refEntityDoesNotExist);
		when(entity0.get(attrNillableXrefName)).thenReturn(null);
		when(entity0.get(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.get(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.get(attrUniqueStringName)).thenReturn("unique1");
		when(entity0.get(attrUniqueXrefName)).thenReturn(refEntity1);

		// actual tests
		try
		{
			repositoryValidationDecorator.add(entity0);
			throw new RuntimeException("Expected MolgenisValidationException instead of no exception");
		}
		catch (MolgenisValidationException e)
		{
			verify(entityAttributesValidator, times(1)).validate(entity0, entityType);
			assertEquals(e.getMessage(),
					"Unknown xref value 'id1' for attribute 'mrefAttr' of entity 'entity'. (entity 1)");
		}
	}

	@Test
	public void addUniqueStringValueExistsInBackendValidationError()
	{
		Entity entityInBackend0 = mock(Entity.class);
		when(entityInBackend0.getEntityType()).thenReturn(entityType);

		when(entityInBackend0.getIdValue()).thenReturn("idbackend0");
		when(entityInBackend0.getEntity(attrXrefName)).thenReturn(refEntity0);
		when(entityInBackend0.getEntity(attrNillableXrefName)).thenReturn(null);
		when(entityInBackend0.getEntities(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entityInBackend0.getEntities(attrNillableMrefName)).thenReturn(emptyList());
		when(entityInBackend0.getString(attrUniqueStringName)).thenReturn("unique0");
		when(entityInBackend0.getEntity(attrUniqueXrefName)).thenReturn(refEntity0);

		when(entityInBackend0.get(attrIdName)).thenReturn("idbackend0");
		when(entityInBackend0.get(attrXrefName)).thenReturn(refEntity0);
		when(entityInBackend0.get(attrNillableXrefName)).thenReturn(null);
		when(entityInBackend0.get(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entityInBackend0.get(attrNillableMrefName)).thenReturn(emptyList());
		when(entityInBackend0.get(attrUniqueStringName)).thenReturn("unique0");
		when(entityInBackend0.get(attrUniqueXrefName)).thenReturn(refEntity0);

		when(decoratedRepo.findAll(new QueryImpl<>().fetch(new Fetch().field(attrIdName)))).thenReturn(
				Stream.of(entityInBackend0));

		// entities
		Entity entity0 = mock(Entity.class);
		when(entity0.getEntityType()).thenReturn(entityType);

		when(entity0.getIdValue()).thenReturn("id0");
		when(entity0.getEntity(attrXrefName)).thenReturn(refEntity0);
		when(entity0.getEntity(attrNillableXrefName)).thenReturn(null);
		when(entity0.getEntities(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.getEntities(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.getString(attrUniqueStringName)).thenReturn("unique0"); // duplicate
		when(entity0.getEntity(attrUniqueXrefName)).thenReturn(refEntity1);

		when(entity0.get(attrIdName)).thenReturn("id0");
		when(entity0.get(attrXrefName)).thenReturn(refEntity0);
		when(entity0.get(attrNillableXrefName)).thenReturn(null);
		when(entity0.get(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.get(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.get(attrUniqueStringName)).thenReturn("unique0"); // duplicate
		when(entity0.get(attrUniqueXrefName)).thenReturn(refEntity1);

		// actual tests
		try
		{
			repositoryValidationDecorator.add(entity0);
			throw new RuntimeException("Expected MolgenisValidationException instead of no exception");
		}
		catch (MolgenisValidationException e)
		{
			verify(entityAttributesValidator, times(1)).validate(entity0, entityType);
			assertEquals(e.getMessage(),
					"Duplicate value 'unique0' for unique attribute 'uniqueStringAttr' from entity 'entity' (entity 1)");
		}
	}

	@Test
	public void addUniqueValueWithExpression()
	{
		when(uniqueStringAttr.getExpression()).thenReturn("expr");

		Entity entityInBackend0 = mock(Entity.class);
		when(entityInBackend0.getEntityType()).thenReturn(entityType);

		when(entityInBackend0.getIdValue()).thenReturn("idbackend0");
		when(entityInBackend0.getEntity(attrXrefName)).thenReturn(refEntity0);
		when(entityInBackend0.getEntity(attrNillableXrefName)).thenReturn(null);
		when(entityInBackend0.getEntities(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entityInBackend0.getEntities(attrNillableMrefName)).thenReturn(emptyList());
		when(entityInBackend0.getString(attrUniqueStringName)).thenReturn("unique0");
		when(entityInBackend0.getEntity(attrUniqueXrefName)).thenReturn(refEntity0);

		when(entityInBackend0.get(attrIdName)).thenReturn("idbackend0");
		when(entityInBackend0.get(attrXrefName)).thenReturn(refEntity0);
		when(entityInBackend0.get(attrNillableXrefName)).thenReturn(null);
		when(entityInBackend0.get(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entityInBackend0.get(attrNillableMrefName)).thenReturn(emptyList());
		when(entityInBackend0.get(attrUniqueStringName)).thenReturn("unique0");
		when(entityInBackend0.get(attrUniqueXrefName)).thenReturn(refEntity0);

		when(decoratedRepo.findAll(new QueryImpl<>().fetch(new Fetch().field(attrIdName)))).thenReturn(
				Stream.of(entityInBackend0));

		// entities
		Entity entity0 = mock(Entity.class);
		when(entity0.getEntityType()).thenReturn(entityType);

		when(entity0.getIdValue()).thenReturn("id0");
		when(entity0.getEntity(attrXrefName)).thenReturn(refEntity0);
		when(entity0.getEntity(attrNillableXrefName)).thenReturn(null);
		when(entity0.getEntities(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.getEntities(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.getString(attrUniqueStringName)).thenReturn("unique0"); // valid, because value is 'computed'
		when(entity0.getEntity(attrUniqueXrefName)).thenReturn(refEntity1);

		when(entity0.get(attrIdName)).thenReturn("id0");
		when(entity0.get(attrXrefName)).thenReturn(refEntity0);
		when(entity0.get(attrNillableXrefName)).thenReturn(null);
		when(entity0.get(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.get(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.get(attrUniqueStringName)).thenReturn("unique0"); // valid, because value is 'computed'
		when(entity0.get(attrUniqueXrefName)).thenReturn(refEntity1);

		// actual tests
		repositoryValidationDecorator.add(entity0);
		verify(decoratedRepo, times(1)).add(entity0);
		verify(entityAttributesValidator, times(1)).validate(entity0, entityType);
	}

	@Test
	public void addUniqueXrefValueExistsInBackendValidationError()
	{
		Entity entityInBackend0 = mock(Entity.class);
		when(entityInBackend0.getEntityType()).thenReturn(entityType);

		when(entityInBackend0.getIdValue()).thenReturn("idbackend0");
		when(entityInBackend0.getEntity(attrXrefName)).thenReturn(refEntity0);
		when(entityInBackend0.getEntity(attrNillableXrefName)).thenReturn(null);
		when(entityInBackend0.getEntities(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entityInBackend0.getEntities(attrNillableMrefName)).thenReturn(emptyList());
		when(entityInBackend0.getString(attrUniqueStringName)).thenReturn("unique0");
		when(entityInBackend0.getEntity(attrUniqueXrefName)).thenReturn(refEntity0);

		when(entityInBackend0.get(attrIdName)).thenReturn("idbackend0");
		when(entityInBackend0.get(attrXrefName)).thenReturn(refEntity0);
		when(entityInBackend0.get(attrNillableXrefName)).thenReturn(null);
		when(entityInBackend0.get(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entityInBackend0.get(attrNillableMrefName)).thenReturn(emptyList());
		when(entityInBackend0.get(attrUniqueStringName)).thenReturn("unique0");
		when(entityInBackend0.get(attrUniqueXrefName)).thenReturn(refEntity0);

		when(decoratedRepo.findAll(new QueryImpl<>().fetch(new Fetch().field(attrIdName)))).thenReturn(
				Stream.of(entityInBackend0));

		// entities
		Entity entity0 = mock(Entity.class);
		when(entity0.getEntityType()).thenReturn(entityType);

		when(entity0.getIdValue()).thenReturn("id0");
		when(entity0.getEntity(attrXrefName)).thenReturn(refEntity0);
		when(entity0.getEntity(attrNillableXrefName)).thenReturn(null);
		when(entity0.getEntities(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.getEntities(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.getString(attrUniqueStringName)).thenReturn("unique1");
		when(entity0.getEntity(attrUniqueXrefName)).thenReturn(refEntity0); // duplicate

		when(entity0.get(attrIdName)).thenReturn("id0");
		when(entity0.get(attrXrefName)).thenReturn(refEntity0);
		when(entity0.get(attrNillableXrefName)).thenReturn(null);
		when(entity0.get(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.get(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.get(attrUniqueStringName)).thenReturn("unique1");
		when(entity0.get(attrUniqueXrefName)).thenReturn(refEntity0); // duplicate

		// actual tests
		try
		{
			repositoryValidationDecorator.add(entity0);
			throw new RuntimeException("Expected MolgenisValidationException instead of no exception");
		}
		catch (MolgenisValidationException e)
		{
			verify(entityAttributesValidator, times(1)).validate(entity0, entityType);
			assertEquals(e.getMessage(),
					"Duplicate value 'idref0' for unique attribute 'uniqueXrefAttr' from entity 'entity' (entity 1)");
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void addStream()
	{
		// entities
		Entity entity0 = mock(Entity.class);
		when(entity0.getEntityType()).thenReturn(entityType);

		when(entity0.getIdValue()).thenReturn("id0");
		when(entity0.getEntity(attrXrefName)).thenReturn(refEntity0);
		when(entity0.getEntity(attrNillableXrefName)).thenReturn(null);
		when(entity0.getEntities(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.getEntities(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.getString(attrUniqueStringName)).thenReturn("unique0");
		when(entity0.getEntity(attrUniqueXrefName)).thenReturn(refEntity0);

		when(entity0.get(attrIdName)).thenReturn("id0");
		when(entity0.get(attrXrefName)).thenReturn(refEntity0);
		when(entity0.get(attrNillableXrefName)).thenReturn(null);
		when(entity0.get(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.get(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.get(attrUniqueStringName)).thenReturn("unique0");
		when(entity0.get(attrUniqueXrefName)).thenReturn(refEntity0);

		// actual tests
		List<Entity> entities = Arrays.asList(entity0);
		repositoryValidationDecorator.add(entities.stream());

		ArgumentCaptor<Stream<Entity>> captor = ArgumentCaptor.forClass(Stream.class);
		verify(decoratedRepo, times(1)).add(captor.capture());
		Stream<Entity> stream = captor.getValue();
		stream.collect(Collectors.toList()); // process stream to enable validation

		verify(entityAttributesValidator, times(1)).validate(entity0, entityType);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void addStreamEntityAttributesValidationError()
	{
		// entities
		Entity entity0 = mock(Entity.class);
		when(entity0.getEntityType()).thenReturn(entityType);

		when(entity0.getIdValue()).thenReturn("id0");
		when(entity0.getEntity(attrXrefName)).thenReturn(refEntity0);
		when(entity0.getEntity(attrNillableXrefName)).thenReturn(null);
		when(entity0.getEntities(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.getEntities(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.getString(attrUniqueStringName)).thenReturn("unique0");
		when(entity0.getEntity(attrUniqueXrefName)).thenReturn(refEntity0);

		when(entity0.get(attrIdName)).thenReturn("id0");
		when(entity0.get(attrXrefName)).thenReturn(refEntity0);
		when(entity0.get(attrNillableXrefName)).thenReturn(null);
		when(entity0.get(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.get(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.get(attrUniqueStringName)).thenReturn("unique0");
		when(entity0.get(attrUniqueXrefName)).thenReturn(refEntity0);

		Entity entity1 = mock(Entity.class);
		when(entity1.getIdValue()).thenReturn("id1");
		when(entity1.getEntityType()).thenReturn(entityType);
		when(entity1.getEntity(attrXrefName)).thenReturn(refEntity0);
		when(entity1.getEntity(attrNillableXrefName)).thenReturn(null);
		when(entity1.getEntities(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity1.getEntities(attrNillableMrefName)).thenReturn(emptyList());
		when(entity1.getString(attrUniqueStringName)).thenReturn("unique1");
		when(entity1.getEntity(attrUniqueXrefName)).thenReturn(refEntity1);

		when(entity1.get(attrIdName)).thenReturn("id1");
		when(entity1.get(attrXrefName)).thenReturn(refEntity0);
		when(entity1.get(attrNillableXrefName)).thenReturn(null);
		when(entity1.get(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity1.get(attrNillableMrefName)).thenReturn(emptyList());
		when(entity1.get(attrUniqueStringName)).thenReturn("unique1");
		when(entity1.get(attrUniqueXrefName)).thenReturn(refEntity1);

		Set<ConstraintViolation> violations = singleton(new ConstraintViolation("violation", 2L));
		when(entityAttributesValidator.validate(entity1, entityType)).thenReturn(violations);

		// actual tests
		List<Entity> entities = Arrays.asList(entity0, entity1);
		repositoryValidationDecorator.add(entities.stream());

		ArgumentCaptor<Stream<Entity>> captor = ArgumentCaptor.forClass(Stream.class);
		verify(decoratedRepo, times(1)).add(captor.capture());
		Stream<Entity> stream = captor.getValue();
		try
		{
			stream.collect(Collectors.toList()); // process stream to enable validation

			throw new RuntimeException("Expected MolgenisValidationException instead of no exception");
		}
		catch (MolgenisValidationException e)
		{
			verify(entityAttributesValidator, times(1)).validate(entity0, entityType);
			verify(entityAttributesValidator, times(1)).validate(entity1, entityType);
			assertEquals(e.getViolations(), violations);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void addStreamRequiredValueValidationError()
	{
		// entities
		Entity entity0 = mock(Entity.class);
		when(entity0.getEntityType()).thenReturn(entityType);

		when(entity0.getIdValue()).thenReturn("id0");
		when(entity0.getEntity(attrXrefName)).thenReturn(null); // violation error
		when(entity0.getEntity(attrNillableXrefName)).thenReturn(null);
		when(entity0.getEntities(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.getEntities(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.getString(attrUniqueStringName)).thenReturn("unique0");
		when(entity0.getEntity(attrUniqueXrefName)).thenReturn(refEntity0);

		when(entity0.get(attrIdName)).thenReturn("id0");
		when(entity0.get(attrXrefName)).thenReturn(null); // violation error
		when(entity0.get(attrNillableXrefName)).thenReturn(null);
		when(entity0.get(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.get(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.get(attrUniqueStringName)).thenReturn("unique0");
		when(entity0.get(attrUniqueXrefName)).thenReturn(refEntity0);

		// actual tests
		List<Entity> entities = Arrays.asList(entity0);
		repositoryValidationDecorator.add(entities.stream());

		ArgumentCaptor<Stream<Entity>> captor = ArgumentCaptor.forClass(Stream.class);
		verify(decoratedRepo, times(1)).add(captor.capture());
		Stream<Entity> stream = captor.getValue();
		try
		{
			stream.collect(Collectors.toList()); // process stream to enable validation

			throw new RuntimeException("Expected MolgenisValidationException instead of no exception");
		}
		catch (MolgenisValidationException e)
		{
			verify(entityAttributesValidator, times(1)).validate(entity0, entityType);
			assertEquals(e.getMessage(), "The attribute 'xrefAttr' of entity 'entity' can not be null. (entity 1)");
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void addStreamRequiredValueWithExpression()
	{
		when(xrefAttr.getExpression()).thenReturn("expr");

		// entities
		Entity entity0 = mock(Entity.class);
		when(entity0.getEntityType()).thenReturn(entityType);

		when(entity0.getIdValue()).thenReturn("id0");
		when(entity0.getEntity(attrXrefName)).thenReturn(null); // valid, because the value is 'computed'
		when(entity0.getEntity(attrNillableXrefName)).thenReturn(null);
		when(entity0.getEntities(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.getEntities(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.getString(attrUniqueStringName)).thenReturn("unique0");
		when(entity0.getEntity(attrUniqueXrefName)).thenReturn(refEntity0);

		when(entity0.get(attrIdName)).thenReturn("id0");
		when(entity0.get(attrXrefName)).thenReturn(null); // valid, because the value is 'computed'
		when(entity0.get(attrNillableXrefName)).thenReturn(null);
		when(entity0.get(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.get(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.get(attrUniqueStringName)).thenReturn("unique0");
		when(entity0.get(attrUniqueXrefName)).thenReturn(refEntity0);

		// actual tests
		List<Entity> entities = Arrays.asList(entity0);
		repositoryValidationDecorator.add(entities.stream());

		ArgumentCaptor<Stream<Entity>> captor = ArgumentCaptor.forClass(Stream.class);
		verify(decoratedRepo, times(1)).add(captor.capture());
		Stream<Entity> stream = captor.getValue();

		stream.collect(Collectors.toList()); // process stream to enable validation
		verify(entityAttributesValidator, times(1)).validate(entity0, entityType);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void addStreamRequiredMrefValueValidationError()
	{
		// entities
		Entity entity0 = mock(Entity.class);
		when(entity0.getEntityType()).thenReturn(entityType);

		when(entity0.getIdValue()).thenReturn("id0");
		when(entity0.getEntity(attrXrefName)).thenReturn(refEntity0);
		when(entity0.getEntity(attrNillableXrefName)).thenReturn(null);
		when(entity0.getEntities(attrMrefName)).thenReturn(emptyList());
		when(entity0.getEntities(attrNillableMrefName)).thenReturn(emptyList()); // violation error
		when(entity0.getString(attrUniqueStringName)).thenReturn("unique0");
		when(entity0.getEntity(attrUniqueXrefName)).thenReturn(refEntity0);

		when(entity0.get(attrIdName)).thenReturn("id0");
		when(entity0.get(attrXrefName)).thenReturn(refEntity0);
		when(entity0.get(attrNillableXrefName)).thenReturn(null);
		when(entity0.get(attrMrefName)).thenReturn(emptyList());
		when(entity0.get(attrNillableMrefName)).thenReturn(emptyList()); // violation error
		when(entity0.get(attrUniqueStringName)).thenReturn("unique0");
		when(entity0.get(attrUniqueXrefName)).thenReturn(refEntity0);

		// actual tests
		List<Entity> entities = Arrays.asList(entity0);
		repositoryValidationDecorator.add(entities.stream());

		ArgumentCaptor<Stream<Entity>> captor = ArgumentCaptor.forClass(Stream.class);
		verify(decoratedRepo, times(1)).add(captor.capture());
		Stream<Entity> stream = captor.getValue();
		try
		{
			stream.collect(Collectors.toList()); // process stream to enable validation

			throw new RuntimeException("Expected MolgenisValidationException instead of no exception");
		}
		catch (MolgenisValidationException e)
		{
			verify(entityAttributesValidator, times(1)).validate(entity0, entityType);
			assertEquals(e.getMessage(), "The attribute 'mrefAttr' of entity 'entity' can not be null. (entity 1)");
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void addStreamReferenceXrefSelfReferenceToPreviouslyAddedEntity()
	{
		when(xrefAttr.getRefEntity()).thenReturn(entityType);

		// entities
		Entity entity0 = mock(Entity.class);
		when(entity0.getEntityType()).thenReturn(entityType);

		when(entity0.getIdValue()).thenReturn("id0");
		when(entity0.getEntity(attrXrefName)).thenReturn(entity0); // self reference
		when(entity0.getEntity(attrNillableXrefName)).thenReturn(null);
		when(entity0.getEntities(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.getEntities(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.getString(attrUniqueStringName)).thenReturn("unique0");
		when(entity0.getEntity(attrUniqueXrefName)).thenReturn(refEntity0);

		when(entity0.get(attrIdName)).thenReturn("id0");
		when(entity0.get(attrXrefName)).thenReturn(entity0); // self reference
		when(entity0.get(attrNillableXrefName)).thenReturn(null);
		when(entity0.get(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.get(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.get(attrUniqueStringName)).thenReturn("unique0");
		when(entity0.get(attrUniqueXrefName)).thenReturn(refEntity0);

		// actual tests
		List<Entity> entities = Arrays.asList(entity0);
		when(dataService.findAll(entityTypeId,
				new QueryImpl<>().fetch(new Fetch().field(attrIdName)))).thenReturn(Stream.of(entity0));
		repositoryValidationDecorator.add(entities.stream());

		ArgumentCaptor<Stream<Entity>> captor = ArgumentCaptor.forClass(Stream.class);
		verify(decoratedRepo, times(1)).add(captor.capture());
		Stream<Entity> stream = captor.getValue();

		stream.collect(Collectors.toList()); // process stream to enable validation
		verify(entityAttributesValidator, times(1)).validate(entity0, entityType);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void addStreamReferenceXrefSelfReferenceToSelf()
	{
		when(xrefAttr.getRefEntity()).thenReturn(entityType);

		// entities
		Entity entity0 = mock(Entity.class);
		when(entity0.getEntityType()).thenReturn(entityType);

		when(entity0.getIdValue()).thenReturn("id0");
		when(entity0.getEntity(attrXrefName)).thenReturn(entity0); // self reference
		when(entity0.getEntity(attrNillableXrefName)).thenReturn(null);
		when(entity0.getEntities(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.getEntities(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.getString(attrUniqueStringName)).thenReturn("unique0");
		when(entity0.getEntity(attrUniqueXrefName)).thenReturn(refEntity0);

		when(entity0.get(attrIdName)).thenReturn("id0");
		when(entity0.get(attrXrefName)).thenReturn(entity0); // self reference
		when(entity0.get(attrNillableXrefName)).thenReturn(null);
		when(entity0.get(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.get(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.get(attrUniqueStringName)).thenReturn("unique0");
		when(entity0.get(attrUniqueXrefName)).thenReturn(refEntity0);

		Entity entity1 = mock(Entity.class);
		when(entity1.getEntityType()).thenReturn(entityType);

		when(entity1.getIdValue()).thenReturn("id1");
		when(entity1.getEntity(attrXrefName)).thenReturn(entity0); // reference to previously added entity
		when(entity1.getEntity(attrNillableXrefName)).thenReturn(null);
		when(entity1.getEntities(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity1.getEntities(attrNillableMrefName)).thenReturn(emptyList());
		when(entity1.getString(attrUniqueStringName)).thenReturn("unique1");
		when(entity1.getEntity(attrUniqueXrefName)).thenReturn(refEntity1);

		when(entity1.get(attrIdName)).thenReturn("id1");
		when(entity1.get(attrXrefName)).thenReturn(entity0); // reference to previously added entity
		when(entity1.get(attrNillableXrefName)).thenReturn(null);
		when(entity1.get(attrMrefName)).thenReturn(Arrays.asList(refEntity1));
		when(entity1.get(attrNillableMrefName)).thenReturn(emptyList());
		when(entity1.get(attrUniqueStringName)).thenReturn("unique1");
		when(entity1.get(attrUniqueXrefName)).thenReturn(refEntity1);

		// actual tests
		List<Entity> entities = Arrays.asList(entity0, entity1);
		when(dataService.findAll(entityTypeId,
				new QueryImpl<>().fetch(new Fetch().field(attrIdName)))).thenReturn(Stream.empty());
		repositoryValidationDecorator.add(entities.stream());

		ArgumentCaptor<Stream<Entity>> captor = ArgumentCaptor.forClass(Stream.class);
		verify(decoratedRepo, times(1)).add(captor.capture());
		Stream<Entity> stream = captor.getValue();

		stream.collect(Collectors.toList()); // process stream to enable validation
		verify(entityAttributesValidator, times(1)).validate(entity0, entityType);
		verify(entityAttributesValidator, times(1)).validate(entity1, entityType);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void addStreamReferenceXrefDoesNotExistsValidationError()
	{
		String refEntityDoesNotExistId = "id1";
		Entity refEntityDoesNotExist = mock(Entity.class);
		when(refEntityDoesNotExist.getEntityType()).thenReturn(refEntityType);
		when(refEntityDoesNotExist.getIdValue()).thenReturn(refEntityDoesNotExistId);
		when(refEntityDoesNotExist.get(refAttrIdName)).thenReturn(refEntityDoesNotExistId);
		when(refEntityDoesNotExist.getString(refAttrIdName)).thenReturn(refEntityDoesNotExistId);

		// entities
		Entity entity0 = mock(Entity.class);
		when(entity0.getEntityType()).thenReturn(entityType);

		when(entity0.getIdValue()).thenReturn("id0");
		when(entity0.getEntity(attrXrefName)).thenReturn(refEntity0);
		when(entity0.getEntity(attrNillableXrefName)).thenReturn(null);
		when(entity0.getEntities(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.getEntities(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.getString(attrUniqueStringName)).thenReturn("unique0");
		when(entity0.getEntity(attrUniqueXrefName)).thenReturn(refEntity0);

		when(entity0.get(attrIdName)).thenReturn("id0");
		when(entity0.get(attrXrefName)).thenReturn(refEntity0);
		when(entity0.get(attrNillableXrefName)).thenReturn(null);
		when(entity0.get(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.get(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.get(attrUniqueStringName)).thenReturn("unique0");
		when(entity0.get(attrUniqueXrefName)).thenReturn(refEntity0);

		Entity entity1 = mock(Entity.class);
		when(entity1.getEntityType()).thenReturn(entityType);

		when(entity1.getIdValue()).thenReturn("id1");
		when(entity1.getEntity(attrXrefName)).thenReturn(refEntityDoesNotExist);
		when(entity1.getEntity(attrNillableXrefName)).thenReturn(null);
		when(entity1.getEntities(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity1.getEntities(attrNillableMrefName)).thenReturn(emptyList());
		when(entity1.getString(attrUniqueStringName)).thenReturn("unique1");
		when(entity1.getEntity(attrUniqueXrefName)).thenReturn(refEntity1);

		when(entity1.get(attrIdName)).thenReturn("id1");
		when(entity1.get(attrXrefName)).thenReturn(refEntityDoesNotExist);
		when(entity1.get(attrNillableXrefName)).thenReturn(null);
		when(entity1.get(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity1.get(attrNillableMrefName)).thenReturn(emptyList());
		when(entity1.get(attrUniqueStringName)).thenReturn("unique1");
		when(entity1.get(attrUniqueXrefName)).thenReturn(refEntity1);

		// actual tests
		List<Entity> entities = Arrays.asList(entity0, entity1);
		repositoryValidationDecorator.add(entities.stream());

		ArgumentCaptor<Stream<Entity>> captor = ArgumentCaptor.forClass(Stream.class);
		verify(decoratedRepo, times(1)).add(captor.capture());
		Stream<Entity> stream = captor.getValue();
		try
		{
			stream.collect(Collectors.toList()); // process stream to enable validation

			throw new RuntimeException("Expected MolgenisValidationException instead of no exception");
		}
		catch (MolgenisValidationException e)
		{
			verify(entityAttributesValidator, times(1)).validate(entity0, entityType);
			verify(entityAttributesValidator, times(1)).validate(entity1, entityType);
			assertEquals(e.getMessage(),
					"Unknown xref value 'id1' for attribute 'xrefAttr' of entity 'entity'. (entity 2)");
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void addStreamReferenceAttrWithExpression()
	{
		when(xrefAttr.getExpression()).thenReturn("expr");

		String refEntityDoesNotExistId = "id1";
		Entity refEntityDoesNotExist = mock(Entity.class);
		when(refEntityDoesNotExist.getEntityType()).thenReturn(refEntityType);
		when(refEntityDoesNotExist.getIdValue()).thenReturn(refEntityDoesNotExistId);
		when(refEntityDoesNotExist.get(refAttrIdName)).thenReturn(refEntityDoesNotExistId);
		when(refEntityDoesNotExist.getString(refAttrIdName)).thenReturn(refEntityDoesNotExistId);

		// entities
		Entity entity0 = mock(Entity.class);
		when(entity0.getEntityType()).thenReturn(entityType);

		when(entity0.getIdValue()).thenReturn("id0");
		when(entity0.getEntity(attrXrefName)).thenReturn(refEntity0);
		when(entity0.getEntity(attrNillableXrefName)).thenReturn(null);
		when(entity0.getEntities(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.getEntities(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.getString(attrUniqueStringName)).thenReturn("unique0");
		when(entity0.getEntity(attrUniqueXrefName)).thenReturn(refEntity0);

		when(entity0.get(attrIdName)).thenReturn("id0");
		when(entity0.get(attrXrefName)).thenReturn(refEntity0);
		when(entity0.get(attrNillableXrefName)).thenReturn(null);
		when(entity0.get(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.get(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.get(attrUniqueStringName)).thenReturn("unique0");
		when(entity0.get(attrUniqueXrefName)).thenReturn(refEntity0);

		Entity entity1 = mock(Entity.class);
		when(entity1.getEntityType()).thenReturn(entityType);

		when(entity1.getIdValue()).thenReturn("id1");
		when(entity1.getEntity(attrXrefName)).thenReturn(refEntityDoesNotExist); // valid, because the value is computed
		when(entity1.getEntity(attrNillableXrefName)).thenReturn(null);
		when(entity1.getEntities(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity1.getEntities(attrNillableMrefName)).thenReturn(emptyList());
		when(entity1.getString(attrUniqueStringName)).thenReturn("unique1");
		when(entity1.getEntity(attrUniqueXrefName)).thenReturn(refEntity1);

		when(entity1.get(attrIdName)).thenReturn("id1");
		when(entity1.get(attrXrefName)).thenReturn(refEntityDoesNotExist);
		when(entity1.get(attrNillableXrefName)).thenReturn(null);
		when(entity1.get(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity1.get(attrNillableMrefName)).thenReturn(emptyList());
		when(entity1.get(attrUniqueStringName)).thenReturn("unique1");
		when(entity1.get(attrUniqueXrefName)).thenReturn(refEntity1);

		// actual tests
		List<Entity> entities = Arrays.asList(entity0, entity1);
		repositoryValidationDecorator.add(entities.stream());

		ArgumentCaptor<Stream<Entity>> captor = ArgumentCaptor.forClass(Stream.class);
		verify(decoratedRepo, times(1)).add(captor.capture());
		Stream<Entity> stream = captor.getValue();

		stream.collect(Collectors.toList()); // process stream to enable validation
		verify(entityAttributesValidator, times(1)).validate(entity0, entityType);
		verify(entityAttributesValidator, times(1)).validate(entity1, entityType);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void addStreamReferenceMrefDoesNotExistsValidationError()
	{
		String refEntityDoesNotExistId = "id1";
		Entity refEntityDoesNotExist = mock(Entity.class);
		when(refEntityDoesNotExist.getEntityType()).thenReturn(refEntityType);
		when(refEntityDoesNotExist.getIdValue()).thenReturn(refEntityDoesNotExistId);
		when(refEntityDoesNotExist.get(refAttrIdName)).thenReturn(refEntityDoesNotExistId);
		when(refEntityDoesNotExist.getString(refAttrIdName)).thenReturn(refEntityDoesNotExistId);

		// entities
		Entity entity0 = mock(Entity.class);
		when(entity0.getEntityType()).thenReturn(entityType);

		when(entity0.getIdValue()).thenReturn("id0");
		when(entity0.getEntity(attrXrefName)).thenReturn(refEntity0);
		when(entity0.getEntity(attrNillableXrefName)).thenReturn(null);
		when(entity0.getEntities(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.getEntities(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.getString(attrUniqueStringName)).thenReturn("unique0");
		when(entity0.getEntity(attrUniqueXrefName)).thenReturn(refEntity0);

		when(entity0.get(attrIdName)).thenReturn("id0");
		when(entity0.get(attrXrefName)).thenReturn(refEntity0);
		when(entity0.get(attrNillableXrefName)).thenReturn(null);
		when(entity0.get(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.get(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.get(attrUniqueStringName)).thenReturn("unique0");
		when(entity0.get(attrUniqueXrefName)).thenReturn(refEntity0);

		Entity entity1 = mock(Entity.class);
		when(entity1.getEntityType()).thenReturn(entityType);

		when(entity1.getIdValue()).thenReturn("id1");
		when(entity1.getEntity(attrXrefName)).thenReturn(refEntity0);
		when(entity1.getEntity(attrNillableXrefName)).thenReturn(null);
		when(entity1.getEntities(attrMrefName)).thenReturn(Arrays.asList(refEntity0, refEntityDoesNotExist));
		when(entity1.getEntities(attrNillableMrefName)).thenReturn(emptyList());
		when(entity1.getString(attrUniqueStringName)).thenReturn("unique1");
		when(entity1.getEntity(attrUniqueXrefName)).thenReturn(refEntity1);

		when(entity1.get(attrIdName)).thenReturn("id1");
		when(entity1.get(attrXrefName)).thenReturn(refEntityDoesNotExist);
		when(entity1.get(attrNillableXrefName)).thenReturn(null);
		when(entity1.get(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity1.get(attrNillableMrefName)).thenReturn(emptyList());
		when(entity1.get(attrUniqueStringName)).thenReturn("unique1");
		when(entity1.get(attrUniqueXrefName)).thenReturn(refEntity1);

		// actual tests
		List<Entity> entities = Arrays.asList(entity0, entity1);
		repositoryValidationDecorator.add(entities.stream());

		ArgumentCaptor<Stream<Entity>> captor = ArgumentCaptor.forClass(Stream.class);
		verify(decoratedRepo, times(1)).add(captor.capture());
		Stream<Entity> stream = captor.getValue();
		try
		{
			stream.collect(Collectors.toList()); // process stream to enable validation

			throw new RuntimeException("Expected MolgenisValidationException instead of no exception");
		}
		catch (MolgenisValidationException e)
		{
			verify(entityAttributesValidator, times(1)).validate(entity0, entityType);
			verify(entityAttributesValidator, times(1)).validate(entity1, entityType);
			assertEquals(e.getMessage(),
					"Unknown xref value 'id1' for attribute 'mrefAttr' of entity 'entity'. (entity 2)");
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void addStreamUniqueStringValueExistsInBackendValidationError()
	{
		Entity entityInBackend0 = mock(Entity.class);
		when(entityInBackend0.getEntityType()).thenReturn(entityType);

		when(entityInBackend0.getIdValue()).thenReturn("idbackend0");
		when(entityInBackend0.getEntity(attrXrefName)).thenReturn(refEntity0);
		when(entityInBackend0.getEntity(attrNillableXrefName)).thenReturn(null);
		when(entityInBackend0.getEntities(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entityInBackend0.getEntities(attrNillableMrefName)).thenReturn(emptyList());
		when(entityInBackend0.getString(attrUniqueStringName)).thenReturn("unique0");
		when(entityInBackend0.getEntity(attrUniqueXrefName)).thenReturn(refEntity0);

		when(entityInBackend0.get(attrIdName)).thenReturn("idbackend0");
		when(entityInBackend0.get(attrXrefName)).thenReturn(refEntity0);
		when(entityInBackend0.get(attrNillableXrefName)).thenReturn(null);
		when(entityInBackend0.get(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entityInBackend0.get(attrNillableMrefName)).thenReturn(emptyList());
		when(entityInBackend0.get(attrUniqueStringName)).thenReturn("unique0");
		when(entityInBackend0.get(attrUniqueXrefName)).thenReturn(refEntity0);

		when(decoratedRepo.findAll(new QueryImpl<>().fetch(new Fetch().field(attrIdName)))).thenReturn(
				Stream.of(entityInBackend0));

		// entities
		Entity entity0 = mock(Entity.class);
		when(entity0.getEntityType()).thenReturn(entityType);

		when(entity0.getIdValue()).thenReturn("id0");
		when(entity0.getEntity(attrXrefName)).thenReturn(refEntity0);
		when(entity0.getEntity(attrNillableXrefName)).thenReturn(null);
		when(entity0.getEntities(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.getEntities(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.getString(attrUniqueStringName)).thenReturn("unique0"); // duplicate
		when(entity0.getEntity(attrUniqueXrefName)).thenReturn(refEntity1);

		when(entity0.get(attrIdName)).thenReturn("id0");
		when(entity0.get(attrXrefName)).thenReturn(refEntity0);
		when(entity0.get(attrNillableXrefName)).thenReturn(null);
		when(entity0.get(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.get(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.get(attrUniqueStringName)).thenReturn("unique0"); // duplicate
		when(entity0.get(attrUniqueXrefName)).thenReturn(refEntity1);

		// actual tests
		List<Entity> entities = Arrays.asList(entity0);
		repositoryValidationDecorator.add(entities.stream());

		ArgumentCaptor<Stream<Entity>> captor = ArgumentCaptor.forClass(Stream.class);
		verify(decoratedRepo, times(1)).add(captor.capture());
		Stream<Entity> stream = captor.getValue();
		try
		{
			stream.collect(Collectors.toList()); // process stream to enable validation

			throw new RuntimeException("Expected MolgenisValidationException instead of no exception");
		}
		catch (MolgenisValidationException e)
		{
			verify(entityAttributesValidator, times(1)).validate(entity0, entityType);
			assertEquals(e.getMessage(),
					"Duplicate value 'unique0' for unique attribute 'uniqueStringAttr' from entity 'entity' (entity 1)");
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void addStreamUniqueValueWithExpression()
	{
		when(uniqueStringAttr.getExpression()).thenReturn("expr");

		Entity entityInBackend0 = mock(Entity.class);
		when(entityInBackend0.getEntityType()).thenReturn(entityType);

		when(entityInBackend0.getIdValue()).thenReturn("idbackend0");
		when(entityInBackend0.getEntity(attrXrefName)).thenReturn(refEntity0);
		when(entityInBackend0.getEntity(attrNillableXrefName)).thenReturn(null);
		when(entityInBackend0.getEntities(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entityInBackend0.getEntities(attrNillableMrefName)).thenReturn(emptyList());
		when(entityInBackend0.getString(attrUniqueStringName)).thenReturn("unique0");
		when(entityInBackend0.getEntity(attrUniqueXrefName)).thenReturn(refEntity0);

		when(entityInBackend0.get(attrIdName)).thenReturn("idbackend0");
		when(entityInBackend0.get(attrXrefName)).thenReturn(refEntity0);
		when(entityInBackend0.get(attrNillableXrefName)).thenReturn(null);
		when(entityInBackend0.get(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entityInBackend0.get(attrNillableMrefName)).thenReturn(emptyList());
		when(entityInBackend0.get(attrUniqueStringName)).thenReturn("unique0");
		when(entityInBackend0.get(attrUniqueXrefName)).thenReturn(refEntity0);

		when(decoratedRepo.findAll(new QueryImpl<>().fetch(new Fetch().field(attrIdName)))).thenReturn(
				Stream.of(entityInBackend0));

		// entities
		Entity entity0 = mock(Entity.class);
		when(entity0.getEntityType()).thenReturn(entityType);

		when(entity0.getIdValue()).thenReturn("id0");
		when(entity0.getEntity(attrXrefName)).thenReturn(refEntity0);
		when(entity0.getEntity(attrNillableXrefName)).thenReturn(null);
		when(entity0.getEntities(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.getEntities(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.getString(attrUniqueStringName)).thenReturn("unique0"); // valid, because value is 'computed'
		when(entity0.getEntity(attrUniqueXrefName)).thenReturn(refEntity1);

		when(entity0.get(attrIdName)).thenReturn("id0");
		when(entity0.get(attrXrefName)).thenReturn(refEntity0);
		when(entity0.get(attrNillableXrefName)).thenReturn(null);
		when(entity0.get(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.get(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.get(attrUniqueStringName)).thenReturn("unique0"); // valid, because value is 'computed'
		when(entity0.get(attrUniqueXrefName)).thenReturn(refEntity1);

		// actual tests
		List<Entity> entities = Arrays.asList(entity0);
		repositoryValidationDecorator.add(entities.stream());

		ArgumentCaptor<Stream<Entity>> captor = ArgumentCaptor.forClass(Stream.class);
		verify(decoratedRepo, times(1)).add(captor.capture());
		Stream<Entity> stream = captor.getValue();

		stream.collect(Collectors.toList()); // process stream to enable validation
		verify(entityAttributesValidator, times(1)).validate(entity0, entityType);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void addStreamUniqueStringValueExistsInSourceValidationError()
	{
		// entities
		Entity entity0 = mock(Entity.class);
		when(entity0.getEntityType()).thenReturn(entityType);

		when(entity0.getIdValue()).thenReturn("id0");
		when(entity0.getEntity(attrXrefName)).thenReturn(refEntity0);
		when(entity0.getEntity(attrNillableXrefName)).thenReturn(null);
		when(entity0.getEntities(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.getEntities(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.getString(attrUniqueStringName)).thenReturn("unique0");
		when(entity0.getEntity(attrUniqueXrefName)).thenReturn(refEntity0);

		when(entity0.get(attrIdName)).thenReturn("id0");
		when(entity0.get(attrXrefName)).thenReturn(refEntity0);
		when(entity0.get(attrNillableXrefName)).thenReturn(null);
		when(entity0.get(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.get(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.get(attrUniqueStringName)).thenReturn("unique0");
		when(entity0.get(attrUniqueXrefName)).thenReturn(refEntity0);

		Entity entity1 = mock(Entity.class);
		when(entity1.getEntityType()).thenReturn(entityType);

		when(entity1.getIdValue()).thenReturn("id1");
		when(entity1.getEntity(attrXrefName)).thenReturn(refEntity0);
		when(entity1.getEntity(attrNillableXrefName)).thenReturn(null);
		when(entity1.getEntities(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity1.getEntities(attrNillableMrefName)).thenReturn(emptyList());
		when(entity1.getString(attrUniqueStringName)).thenReturn("unique0"); // duplicate
		when(entity1.getEntity(attrUniqueXrefName)).thenReturn(refEntity1);

		when(entity1.get(attrIdName)).thenReturn("id1");
		when(entity1.get(attrXrefName)).thenReturn(refEntity0);
		when(entity1.get(attrNillableXrefName)).thenReturn(null);
		when(entity1.get(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity1.get(attrNillableMrefName)).thenReturn(emptyList());
		when(entity1.get(attrUniqueStringName)).thenReturn("unique0"); // duplicate
		when(entity1.get(attrUniqueXrefName)).thenReturn(refEntity1);

		// actual tests
		List<Entity> entities = Arrays.asList(entity0, entity1);
		repositoryValidationDecorator.add(entities.stream());

		ArgumentCaptor<Stream<Entity>> captor = ArgumentCaptor.forClass(Stream.class);
		verify(decoratedRepo, times(1)).add(captor.capture());
		Stream<Entity> stream = captor.getValue();
		try
		{
			stream.collect(Collectors.toList()); // process stream to enable validation

			throw new RuntimeException("Expected MolgenisValidationException instead of no exception");
		}
		catch (MolgenisValidationException e)
		{
			verify(entityAttributesValidator, times(1)).validate(entity0, entityType);
			verify(entityAttributesValidator, times(1)).validate(entity1, entityType);
			assertEquals(e.getMessage(),
					"Duplicate value 'unique0' for unique attribute 'uniqueStringAttr' from entity 'entity' (entity 2)");
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void addStreamUniqueXrefValueExistsInBackendValidationError()
	{
		Entity entityInBackend0 = mock(Entity.class);
		when(entityInBackend0.getEntityType()).thenReturn(entityType);

		when(entityInBackend0.getIdValue()).thenReturn("idbackend0");
		when(entityInBackend0.getEntity(attrXrefName)).thenReturn(refEntity0);
		when(entityInBackend0.getEntity(attrNillableXrefName)).thenReturn(null);
		when(entityInBackend0.getEntities(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entityInBackend0.getEntities(attrNillableMrefName)).thenReturn(emptyList());
		when(entityInBackend0.getString(attrUniqueStringName)).thenReturn("unique0");
		when(entityInBackend0.getEntity(attrUniqueXrefName)).thenReturn(refEntity0);

		when(entityInBackend0.get(attrIdName)).thenReturn("idbackend0");
		when(entityInBackend0.get(attrXrefName)).thenReturn(refEntity0);
		when(entityInBackend0.get(attrNillableXrefName)).thenReturn(null);
		when(entityInBackend0.get(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entityInBackend0.get(attrNillableMrefName)).thenReturn(emptyList());
		when(entityInBackend0.get(attrUniqueStringName)).thenReturn("unique0");
		when(entityInBackend0.get(attrUniqueXrefName)).thenReturn(refEntity0);

		when(decoratedRepo.findAll(new QueryImpl<>().fetch(new Fetch().field(attrIdName)))).thenReturn(
				Stream.of(entityInBackend0));

		// entities
		Entity entity0 = mock(Entity.class);
		when(entity0.getEntityType()).thenReturn(entityType);

		when(entity0.getIdValue()).thenReturn("id0");
		when(entity0.getEntity(attrXrefName)).thenReturn(refEntity0);
		when(entity0.getEntity(attrNillableXrefName)).thenReturn(null);
		when(entity0.getEntities(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.getEntities(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.getString(attrUniqueStringName)).thenReturn("unique1");
		when(entity0.getEntity(attrUniqueXrefName)).thenReturn(refEntity0); // duplicate

		when(entity0.get(attrIdName)).thenReturn("id0");
		when(entity0.get(attrXrefName)).thenReturn(refEntity0);
		when(entity0.get(attrNillableXrefName)).thenReturn(null);
		when(entity0.get(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.get(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.get(attrUniqueStringName)).thenReturn("unique1");
		when(entity0.get(attrUniqueXrefName)).thenReturn(refEntity0); // duplicate

		// actual tests
		List<Entity> entities = Arrays.asList(entity0);
		repositoryValidationDecorator.add(entities.stream());

		ArgumentCaptor<Stream<Entity>> captor = ArgumentCaptor.forClass(Stream.class);
		verify(decoratedRepo, times(1)).add(captor.capture());
		Stream<Entity> stream = captor.getValue();
		try
		{
			stream.collect(Collectors.toList()); // process stream to enable validation

			throw new RuntimeException("Expected MolgenisValidationException instead of no exception");
		}
		catch (MolgenisValidationException e)
		{
			verify(entityAttributesValidator, times(1)).validate(entity0, entityType);
			assertEquals(e.getMessage(),
					"Duplicate value 'idref0' for unique attribute 'uniqueXrefAttr' from entity 'entity' (entity 1)");
		}
	}

	@Test
	public void deleteStream()
	{
		Stream<Entity> stream = Stream.empty();
		repositoryValidationDecorator.delete(stream);
		verify(decoratedRepo, times(1)).delete(stream);
	}

	@Test
	public void update()
	{
		// entities
		Entity entity0 = mock(Entity.class);
		when(entity0.getEntityType()).thenReturn(entityType);

		when(entity0.getIdValue()).thenReturn("id0");
		when(entity0.getEntity(attrXrefName)).thenReturn(refEntity0);
		when(entity0.getEntity(attrNillableXrefName)).thenReturn(null);
		when(entity0.getEntities(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.getEntities(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.getString(attrUniqueStringName)).thenReturn("unique0");
		when(entity0.getEntity(attrUniqueXrefName)).thenReturn(refEntity0);

		when(entity0.get(attrIdName)).thenReturn("id0");
		when(entity0.get(attrXrefName)).thenReturn(refEntity0);
		when(entity0.get(attrNillableXrefName)).thenReturn(null);
		when(entity0.get(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.get(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.get(attrUniqueStringName)).thenReturn("unique0");
		when(entity0.get(attrUniqueXrefName)).thenReturn(refEntity0);

		// actual tests
		repositoryValidationDecorator.update(entity0);
		verify(decoratedRepo, times(1)).update(entity0);
		verify(entityAttributesValidator, times(1)).validate(entity0, entityType);
	}

	@Test
	public void updateEntityAttributesValidationError()
	{
		// entities
		Entity entity0 = mock(Entity.class);
		when(entity0.getIdValue()).thenReturn("id1");
		when(entity0.getEntityType()).thenReturn(entityType);
		when(entity0.getEntity(attrXrefName)).thenReturn(refEntity0);
		when(entity0.getEntity(attrNillableXrefName)).thenReturn(null);
		when(entity0.getEntities(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.getEntities(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.getString(attrUniqueStringName)).thenReturn("unique1");
		when(entity0.getEntity(attrUniqueXrefName)).thenReturn(refEntity1);

		when(entity0.get(attrIdName)).thenReturn("id1");
		when(entity0.get(attrXrefName)).thenReturn(refEntity0);
		when(entity0.get(attrNillableXrefName)).thenReturn(null);
		when(entity0.get(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.get(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.get(attrUniqueStringName)).thenReturn("unique1");
		when(entity0.get(attrUniqueXrefName)).thenReturn(refEntity1);

		Set<ConstraintViolation> violations = singleton(new ConstraintViolation("violation", 2L));
		when(entityAttributesValidator.validate(entity0, entityType)).thenReturn(violations);

		// actual tests
		try
		{
			repositoryValidationDecorator.update(entity0);
			throw new RuntimeException("Expected MolgenisValidationException instead of no exception");
		}
		catch (MolgenisValidationException e)
		{
			verify(entityAttributesValidator, times(1)).validate(entity0, entityType);
			assertEquals(e.getViolations(), violations);
		}
	}

	@Test
	public void updateRequiredValueValidationError()
	{
		// entities
		Entity entity0 = mock(Entity.class);
		when(entity0.getEntityType()).thenReturn(entityType);

		when(entity0.getIdValue()).thenReturn("id0");
		when(entity0.getEntity(attrXrefName)).thenReturn(null); // violation error
		when(entity0.getEntity(attrNillableXrefName)).thenReturn(null);
		when(entity0.getEntities(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.getEntities(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.getString(attrUniqueStringName)).thenReturn("unique0");
		when(entity0.getEntity(attrUniqueXrefName)).thenReturn(refEntity0);

		when(entity0.get(attrIdName)).thenReturn("id0");
		when(entity0.get(attrXrefName)).thenReturn(null); // violation error
		when(entity0.get(attrNillableXrefName)).thenReturn(null);
		when(entity0.get(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.get(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.get(attrUniqueStringName)).thenReturn("unique0");
		when(entity0.get(attrUniqueXrefName)).thenReturn(refEntity0);

		// actual tests
		try
		{
			repositoryValidationDecorator.update(entity0);
			throw new RuntimeException("Expected MolgenisValidationException instead of no exception");
		}
		catch (MolgenisValidationException e)
		{
			verify(entityAttributesValidator, times(1)).validate(entity0, entityType);
			assertEquals(e.getMessage(), "The attribute 'xrefAttr' of entity 'entity' can not be null. (entity 1)");
		}
	}

	@Test
	public void updateRequiredValueWithExpression()
	{
		when(xrefAttr.getExpression()).thenReturn("expr");

		// entities
		Entity entity0 = mock(Entity.class);
		when(entity0.getEntityType()).thenReturn(entityType);

		when(entity0.getIdValue()).thenReturn("id0");
		when(entity0.getEntity(attrXrefName)).thenReturn(null); // valid, because the value is 'computed'
		when(entity0.getEntity(attrNillableXrefName)).thenReturn(null);
		when(entity0.getEntities(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.getEntities(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.getString(attrUniqueStringName)).thenReturn("unique0");
		when(entity0.getEntity(attrUniqueXrefName)).thenReturn(refEntity0);

		when(entity0.get(attrIdName)).thenReturn("id0");
		when(entity0.get(attrXrefName)).thenReturn(null); // valid, because the value is 'computed'
		when(entity0.get(attrNillableXrefName)).thenReturn(null);
		when(entity0.get(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.get(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.get(attrUniqueStringName)).thenReturn("unique0");
		when(entity0.get(attrUniqueXrefName)).thenReturn(refEntity0);

		// actual tests
		repositoryValidationDecorator.update(entity0);
		verify(decoratedRepo, times(1)).update(entity0);
		verify(entityAttributesValidator, times(1)).validate(entity0, entityType);
	}

	@Test
	public void updateRequiredMrefValueValidationError()
	{
		// entities
		Entity entity0 = mock(Entity.class);
		when(entity0.getEntityType()).thenReturn(entityType);

		when(entity0.getIdValue()).thenReturn("id0");
		when(entity0.getEntity(attrXrefName)).thenReturn(refEntity0);
		when(entity0.getEntity(attrNillableXrefName)).thenReturn(null);
		when(entity0.getEntities(attrMrefName)).thenReturn(emptyList());
		when(entity0.getEntities(attrNillableMrefName)).thenReturn(emptyList()); // violation error
		when(entity0.getString(attrUniqueStringName)).thenReturn("unique0");
		when(entity0.getEntity(attrUniqueXrefName)).thenReturn(refEntity0);

		when(entity0.get(attrIdName)).thenReturn("id0");
		when(entity0.get(attrXrefName)).thenReturn(refEntity0);
		when(entity0.get(attrNillableXrefName)).thenReturn(null);
		when(entity0.get(attrMrefName)).thenReturn(emptyList());
		when(entity0.get(attrNillableMrefName)).thenReturn(emptyList()); // violation error
		when(entity0.get(attrUniqueStringName)).thenReturn("unique0");
		when(entity0.get(attrUniqueXrefName)).thenReturn(refEntity0);

		// actual tests
		try
		{
			repositoryValidationDecorator.update(entity0);
			throw new RuntimeException("Expected MolgenisValidationException instead of no exception");
		}
		catch (MolgenisValidationException e)
		{
			verify(entityAttributesValidator, times(1)).validate(entity0, entityType);
			assertEquals(e.getMessage(), "The attribute 'mrefAttr' of entity 'entity' can not be null. (entity 1)");
		}
	}

	@Test
	public void updateReferenceXrefSelfReferenceToPreviouslyAddedEntity()
	{
		when(xrefAttr.getRefEntity()).thenReturn(entityType);

		// entities
		Entity entity0 = mock(Entity.class);
		when(entity0.getEntityType()).thenReturn(entityType);

		when(entity0.getIdValue()).thenReturn("id0");
		when(entity0.getEntity(attrXrefName)).thenReturn(entity0); // self reference
		when(entity0.getEntity(attrNillableXrefName)).thenReturn(null);
		when(entity0.getEntities(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.getEntities(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.getString(attrUniqueStringName)).thenReturn("unique0");
		when(entity0.getEntity(attrUniqueXrefName)).thenReturn(refEntity0);

		when(entity0.get(attrIdName)).thenReturn("id0");
		when(entity0.get(attrXrefName)).thenReturn(entity0); // self reference
		when(entity0.get(attrNillableXrefName)).thenReturn(null);
		when(entity0.get(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.get(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.get(attrUniqueStringName)).thenReturn("unique0");
		when(entity0.get(attrUniqueXrefName)).thenReturn(refEntity0);

		// actual tests
		when(dataService.findAll(entityTypeId,
				new QueryImpl<>().fetch(new Fetch().field(attrIdName)))).thenReturn(Stream.of(entity0));
		repositoryValidationDecorator.update(entity0);
		verify(decoratedRepo, times(1)).update(entity0);
		verify(entityAttributesValidator, times(1)).validate(entity0, entityType);
	}

	@Test
	public void updateReferenceXrefDoesNotExistsValidationError()
	{
		String refEntityDoesNotExistId = "id1";
		Entity refEntityDoesNotExist = mock(Entity.class);
		when(refEntityDoesNotExist.getEntityType()).thenReturn(refEntityType);
		when(refEntityDoesNotExist.getIdValue()).thenReturn(refEntityDoesNotExistId);
		when(refEntityDoesNotExist.get(refAttrIdName)).thenReturn(refEntityDoesNotExistId);
		when(refEntityDoesNotExist.getString(refAttrIdName)).thenReturn(refEntityDoesNotExistId);

		// entities
		Entity entity0 = mock(Entity.class);
		when(entity0.getEntityType()).thenReturn(entityType);

		when(entity0.getIdValue()).thenReturn("id1");
		when(entity0.getEntity(attrXrefName)).thenReturn(refEntityDoesNotExist);
		when(entity0.getEntity(attrNillableXrefName)).thenReturn(null);
		when(entity0.getEntities(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.getEntities(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.getString(attrUniqueStringName)).thenReturn("unique1");
		when(entity0.getEntity(attrUniqueXrefName)).thenReturn(refEntity1);

		when(entity0.get(attrIdName)).thenReturn("id1");
		when(entity0.get(attrXrefName)).thenReturn(refEntityDoesNotExist);
		when(entity0.get(attrNillableXrefName)).thenReturn(null);
		when(entity0.get(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.get(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.get(attrUniqueStringName)).thenReturn("unique1");
		when(entity0.get(attrUniqueXrefName)).thenReturn(refEntity1);

		// actual tests
		try
		{
			repositoryValidationDecorator.update(entity0);
			throw new RuntimeException("Expected MolgenisValidationException instead of no exception");
		}
		catch (MolgenisValidationException e)
		{
			verify(entityAttributesValidator, times(1)).validate(entity0, entityType);
			assertEquals(e.getMessage(),
					"Unknown xref value 'id1' for attribute 'xrefAttr' of entity 'entity'. (entity 1)");
		}
	}

	@Test
	public void updateReferenceAttrWithExpression()
	{
		when(xrefAttr.getExpression()).thenReturn("expr");

		String refEntityDoesNotExistId = "id1";
		Entity refEntityDoesNotExist = mock(Entity.class);
		when(refEntityDoesNotExist.getEntityType()).thenReturn(refEntityType);
		when(refEntityDoesNotExist.getIdValue()).thenReturn(refEntityDoesNotExistId);
		when(refEntityDoesNotExist.get(refAttrIdName)).thenReturn(refEntityDoesNotExistId);
		when(refEntityDoesNotExist.getString(refAttrIdName)).thenReturn(refEntityDoesNotExistId);

		// entities
		Entity entity1 = mock(Entity.class);
		when(entity1.getEntityType()).thenReturn(entityType);

		when(entity1.getIdValue()).thenReturn("id1");
		when(entity1.getEntity(attrXrefName)).thenReturn(refEntityDoesNotExist); // valid, because the value is computed
		when(entity1.getEntity(attrNillableXrefName)).thenReturn(null);
		when(entity1.getEntities(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity1.getEntities(attrNillableMrefName)).thenReturn(emptyList());
		when(entity1.getString(attrUniqueStringName)).thenReturn("unique1");
		when(entity1.getEntity(attrUniqueXrefName)).thenReturn(refEntity1);

		when(entity1.get(attrIdName)).thenReturn("id1");
		when(entity1.get(attrXrefName)).thenReturn(refEntityDoesNotExist);
		when(entity1.get(attrNillableXrefName)).thenReturn(null);
		when(entity1.get(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity1.get(attrNillableMrefName)).thenReturn(emptyList());
		when(entity1.get(attrUniqueStringName)).thenReturn("unique1");
		when(entity1.get(attrUniqueXrefName)).thenReturn(refEntity1);

		// actual tests
		repositoryValidationDecorator.update(entity1);
		verify(decoratedRepo, times(1)).update(entity1);
		verify(entityAttributesValidator, times(1)).validate(entity1, entityType);
	}

	@Test
	public void updateReferenceMrefDoesNotExistsValidationError()
	{
		String refEntityDoesNotExistId = "id1";
		Entity refEntityDoesNotExist = mock(Entity.class);
		when(refEntityDoesNotExist.getEntityType()).thenReturn(refEntityType);
		when(refEntityDoesNotExist.getIdValue()).thenReturn(refEntityDoesNotExistId);
		when(refEntityDoesNotExist.get(refAttrIdName)).thenReturn(refEntityDoesNotExistId);
		when(refEntityDoesNotExist.getString(refAttrIdName)).thenReturn(refEntityDoesNotExistId);

		// entities
		Entity entity0 = mock(Entity.class);
		when(entity0.getEntityType()).thenReturn(entityType);

		when(entity0.getIdValue()).thenReturn("id1");
		when(entity0.getEntity(attrXrefName)).thenReturn(refEntity0);
		when(entity0.getEntity(attrNillableXrefName)).thenReturn(null);
		when(entity0.getEntities(attrMrefName)).thenReturn(Arrays.asList(refEntity0, refEntityDoesNotExist));
		when(entity0.getEntities(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.getString(attrUniqueStringName)).thenReturn("unique1");
		when(entity0.getEntity(attrUniqueXrefName)).thenReturn(refEntity1);

		when(entity0.get(attrIdName)).thenReturn("id1");
		when(entity0.get(attrXrefName)).thenReturn(refEntityDoesNotExist);
		when(entity0.get(attrNillableXrefName)).thenReturn(null);
		when(entity0.get(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.get(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.get(attrUniqueStringName)).thenReturn("unique1");
		when(entity0.get(attrUniqueXrefName)).thenReturn(refEntity1);

		// actual tests
		try
		{
			repositoryValidationDecorator.update(entity0);
			throw new RuntimeException("Expected MolgenisValidationException instead of no exception");
		}
		catch (MolgenisValidationException e)
		{
			verify(entityAttributesValidator, times(1)).validate(entity0, entityType);
			assertEquals(e.getMessage(),
					"Unknown xref value 'id1' for attribute 'mrefAttr' of entity 'entity'. (entity 1)");
		}
	}

	@Test
	public void updateUniqueStringValueExistsInBackendValidationError()
	{
		Entity entityInBackend0 = mock(Entity.class);
		when(entityInBackend0.getEntityType()).thenReturn(entityType);

		when(entityInBackend0.getIdValue()).thenReturn("idbackend0");
		when(entityInBackend0.getEntity(attrXrefName)).thenReturn(refEntity0);
		when(entityInBackend0.getEntity(attrNillableXrefName)).thenReturn(null);
		when(entityInBackend0.getEntities(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entityInBackend0.getEntities(attrNillableMrefName)).thenReturn(emptyList());
		when(entityInBackend0.getString(attrUniqueStringName)).thenReturn("unique0");
		when(entityInBackend0.getEntity(attrUniqueXrefName)).thenReturn(refEntity0);

		when(entityInBackend0.get(attrIdName)).thenReturn("idbackend0");
		when(entityInBackend0.get(attrXrefName)).thenReturn(refEntity0);
		when(entityInBackend0.get(attrNillableXrefName)).thenReturn(null);
		when(entityInBackend0.get(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entityInBackend0.get(attrNillableMrefName)).thenReturn(emptyList());
		when(entityInBackend0.get(attrUniqueStringName)).thenReturn("unique0");
		when(entityInBackend0.get(attrUniqueXrefName)).thenReturn(refEntity0);

		when(decoratedRepo.findAll(new QueryImpl<>().fetch(new Fetch().field(attrIdName)))).thenReturn(
				Stream.of(entityInBackend0));

		// entities
		Entity entity0 = mock(Entity.class);
		when(entity0.getEntityType()).thenReturn(entityType);

		when(entity0.getIdValue()).thenReturn("id0");
		when(entity0.getEntity(attrXrefName)).thenReturn(refEntity0);
		when(entity0.getEntity(attrNillableXrefName)).thenReturn(null);
		when(entity0.getEntities(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.getEntities(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.getString(attrUniqueStringName)).thenReturn("unique0"); // duplicate
		when(entity0.getEntity(attrUniqueXrefName)).thenReturn(refEntity1);

		when(entity0.get(attrIdName)).thenReturn("id0");
		when(entity0.get(attrXrefName)).thenReturn(refEntity0);
		when(entity0.get(attrNillableXrefName)).thenReturn(null);
		when(entity0.get(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.get(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.get(attrUniqueStringName)).thenReturn("unique0"); // duplicate
		when(entity0.get(attrUniqueXrefName)).thenReturn(refEntity1);

		// actual tests
		try
		{
			repositoryValidationDecorator.update(entity0);
			throw new RuntimeException("Expected MolgenisValidationException instead of no exception");
		}
		catch (MolgenisValidationException e)
		{
			verify(entityAttributesValidator, times(1)).validate(entity0, entityType);
			assertEquals(e.getMessage(),
					"Duplicate value 'unique0' for unique attribute 'uniqueStringAttr' from entity 'entity' (entity 1)");
		}
	}

	@Test
	public void updateUniqueValueWithExpression()
	{
		when(uniqueStringAttr.getExpression()).thenReturn("expr");

		Entity entityInBackend0 = mock(Entity.class);
		when(entityInBackend0.getEntityType()).thenReturn(entityType);

		when(entityInBackend0.getIdValue()).thenReturn("idbackend0");
		when(entityInBackend0.getEntity(attrXrefName)).thenReturn(refEntity0);
		when(entityInBackend0.getEntity(attrNillableXrefName)).thenReturn(null);
		when(entityInBackend0.getEntities(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entityInBackend0.getEntities(attrNillableMrefName)).thenReturn(emptyList());
		when(entityInBackend0.getString(attrUniqueStringName)).thenReturn("unique0");
		when(entityInBackend0.getEntity(attrUniqueXrefName)).thenReturn(refEntity0);

		when(entityInBackend0.get(attrIdName)).thenReturn("idbackend0");
		when(entityInBackend0.get(attrXrefName)).thenReturn(refEntity0);
		when(entityInBackend0.get(attrNillableXrefName)).thenReturn(null);
		when(entityInBackend0.get(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entityInBackend0.get(attrNillableMrefName)).thenReturn(emptyList());
		when(entityInBackend0.get(attrUniqueStringName)).thenReturn("unique0");
		when(entityInBackend0.get(attrUniqueXrefName)).thenReturn(refEntity0);

		when(decoratedRepo.findAll(new QueryImpl<>().fetch(new Fetch().field(attrIdName)))).thenReturn(
				Stream.of(entityInBackend0));

		// entities
		Entity entity0 = mock(Entity.class);
		when(entity0.getEntityType()).thenReturn(entityType);

		when(entity0.getIdValue()).thenReturn("id0");
		when(entity0.getEntity(attrXrefName)).thenReturn(refEntity0);
		when(entity0.getEntity(attrNillableXrefName)).thenReturn(null);
		when(entity0.getEntities(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.getEntities(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.getString(attrUniqueStringName)).thenReturn("unique0"); // valid, because value is 'computed'
		when(entity0.getEntity(attrUniqueXrefName)).thenReturn(refEntity1);

		when(entity0.get(attrIdName)).thenReturn("id0");
		when(entity0.get(attrXrefName)).thenReturn(refEntity0);
		when(entity0.get(attrNillableXrefName)).thenReturn(null);
		when(entity0.get(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.get(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.get(attrUniqueStringName)).thenReturn("unique0"); // valid, because value is 'computed'
		when(entity0.get(attrUniqueXrefName)).thenReturn(refEntity1);

		// actual tests
		repositoryValidationDecorator.update(entity0);
		verify(decoratedRepo, times(1)).update(entity0);
		verify(entityAttributesValidator, times(1)).validate(entity0, entityType);
	}

	@Test
	public void updateUniqueXrefValueExistsInBackendValidationError()
	{
		Entity entityInBackend0 = mock(Entity.class);
		when(entityInBackend0.getEntityType()).thenReturn(entityType);

		when(entityInBackend0.getIdValue()).thenReturn("idbackend0");
		when(entityInBackend0.getEntity(attrXrefName)).thenReturn(refEntity0);
		when(entityInBackend0.getEntity(attrNillableXrefName)).thenReturn(null);
		when(entityInBackend0.getEntities(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entityInBackend0.getEntities(attrNillableMrefName)).thenReturn(emptyList());
		when(entityInBackend0.getString(attrUniqueStringName)).thenReturn("unique0");
		when(entityInBackend0.getEntity(attrUniqueXrefName)).thenReturn(refEntity0);

		when(entityInBackend0.get(attrIdName)).thenReturn("idbackend0");
		when(entityInBackend0.get(attrXrefName)).thenReturn(refEntity0);
		when(entityInBackend0.get(attrNillableXrefName)).thenReturn(null);
		when(entityInBackend0.get(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entityInBackend0.get(attrNillableMrefName)).thenReturn(emptyList());
		when(entityInBackend0.get(attrUniqueStringName)).thenReturn("unique0");
		when(entityInBackend0.get(attrUniqueXrefName)).thenReturn(refEntity0);

		when(decoratedRepo.findAll(new QueryImpl<>().fetch(new Fetch().field(attrIdName)))).thenReturn(
				Stream.of(entityInBackend0));

		// entities
		Entity entity0 = mock(Entity.class);
		when(entity0.getEntityType()).thenReturn(entityType);

		when(entity0.getIdValue()).thenReturn("id0");
		when(entity0.getEntity(attrXrefName)).thenReturn(refEntity0);
		when(entity0.getEntity(attrNillableXrefName)).thenReturn(null);
		when(entity0.getEntities(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.getEntities(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.getString(attrUniqueStringName)).thenReturn("unique1");
		when(entity0.getEntity(attrUniqueXrefName)).thenReturn(refEntity0); // duplicate

		when(entity0.get(attrIdName)).thenReturn("id0");
		when(entity0.get(attrXrefName)).thenReturn(refEntity0);
		when(entity0.get(attrNillableXrefName)).thenReturn(null);
		when(entity0.get(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.get(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.get(attrUniqueStringName)).thenReturn("unique1");
		when(entity0.get(attrUniqueXrefName)).thenReturn(refEntity0); // duplicate

		// actual tests
		try
		{
			repositoryValidationDecorator.update(entity0);
			throw new RuntimeException("Expected MolgenisValidationException instead of no exception");
		}
		catch (MolgenisValidationException e)
		{
			verify(entityAttributesValidator, times(1)).validate(entity0, entityType);
			assertEquals(e.getMessage(),
					"Duplicate value 'idref0' for unique attribute 'uniqueXrefAttr' from entity 'entity' (entity 1)");
		}
	}

	@Test
	public void updateReadOnlyStringAttrValidationError()
	{
		String attrReadonlyStringName = "readonlyStringAttr";

		Attribute readonlyStringAttr = when(mock(Attribute.class).getName()).thenReturn(attrReadonlyStringName)
																			.getMock();
		when(readonlyStringAttr.getDataType()).thenReturn(STRING);
		when(readonlyStringAttr.isReadOnly()).thenReturn(true);

		when(entityType.getAttribute(attrReadonlyStringName)).thenReturn(readonlyStringAttr);
		when(entityType.getAtomicAttributes()).thenReturn(
				Arrays.asList(idAttr, xrefAttr, nillableXrefAttr, mrefAttr, nillableMrefAttr, uniqueStringAttr,
						uniqueXrefAttr, readonlyStringAttr));

		// entities
		Entity entity0 = mock(Entity.class);
		when(entity0.getEntityType()).thenReturn(entityType);

		when(entity0.getIdValue()).thenReturn("id0");
		when(entity0.getEntity(attrXrefName)).thenReturn(refEntity0);
		when(entity0.getEntity(attrNillableXrefName)).thenReturn(null);
		when(entity0.getEntities(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.getEntities(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.getString(attrUniqueStringName)).thenReturn("unique1");
		when(entity0.getEntity(attrUniqueXrefName)).thenReturn(refEntity0);
		when(entity0.getString(attrReadonlyStringName)).thenReturn("str0");
		when(entity0.get(attrIdName)).thenReturn("id0");
		when(entity0.get(attrXrefName)).thenReturn(refEntity0);
		when(entity0.get(attrNillableXrefName)).thenReturn(null);
		when(entity0.get(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.get(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.get(attrUniqueStringName)).thenReturn("unique1");
		when(entity0.get(attrUniqueXrefName)).thenReturn(refEntity0);
		when(entity0.get(attrReadonlyStringName)).thenReturn("str0");

		when(decoratedRepo.findOneById("id0")).thenReturn(entity0);

		Entity updatedEntity0 = mock(Entity.class);
		when(updatedEntity0.getEntityType()).thenReturn(entityType);

		when(updatedEntity0.getIdValue()).thenReturn("id0");
		when(updatedEntity0.getEntity(attrXrefName)).thenReturn(refEntity0Clone);
		when(updatedEntity0.getEntity(attrNillableXrefName)).thenReturn(null);
		when(updatedEntity0.getEntities(attrMrefName)).thenReturn(Arrays.asList(refEntity0Clone));
		when(updatedEntity0.getEntities(attrNillableMrefName)).thenReturn(emptyList());
		when(updatedEntity0.getString(attrUniqueStringName)).thenReturn("unique1");
		when(updatedEntity0.getEntity(attrUniqueXrefName)).thenReturn(refEntity0Clone);
		when(updatedEntity0.getString(attrReadonlyStringName)).thenReturn("updatedstr0"); // read only attribute update
		when(updatedEntity0.get(attrIdName)).thenReturn("id0");
		when(updatedEntity0.get(attrXrefName)).thenReturn(refEntity0Clone);
		when(updatedEntity0.get(attrNillableXrefName)).thenReturn(null);
		when(updatedEntity0.get(attrMrefName)).thenReturn(Arrays.asList(refEntity0Clone));
		when(updatedEntity0.get(attrNillableMrefName)).thenReturn(emptyList());
		when(updatedEntity0.get(attrUniqueStringName)).thenReturn("unique1");
		when(updatedEntity0.get(attrUniqueXrefName)).thenReturn(refEntity0Clone);
		when(updatedEntity0.get(attrReadonlyStringName)).thenReturn("updatedstr0"); // read only attribute update

		// actual tests
		try
		{
			repositoryValidationDecorator.update(updatedEntity0);
			throw new RuntimeException("Expected MolgenisValidationException instead of no exception");
		}
		catch (MolgenisValidationException e)
		{
			verify(entityAttributesValidator, times(1)).validate(updatedEntity0, entityType);
			assertEquals(e.getMessage(),
					"The attribute 'readonlyStringAttr' of entity 'entity' can not be changed it is readonly. (entity 1)");
		}
	}

	@Test
	public void updateReadOnlyXrefAttr()
	{
		String attrReadonlyXrefName = "readonlyXrefAttr";

		Attribute readonlyXrefAttr = when(mock(Attribute.class).getName()).thenReturn(attrReadonlyXrefName).getMock();
		when(readonlyXrefAttr.getDataType()).thenReturn(XREF);
		when(readonlyXrefAttr.getRefEntity()).thenReturn(refEntityType);
		when(readonlyXrefAttr.isReadOnly()).thenReturn(true);

		when(entityType.getAttribute(attrReadonlyXrefName)).thenReturn(readonlyXrefAttr);
		when(entityType.getAtomicAttributes()).thenReturn(
				Arrays.asList(idAttr, xrefAttr, nillableXrefAttr, mrefAttr, nillableMrefAttr, uniqueStringAttr,
						uniqueXrefAttr, readonlyXrefAttr));

		// entities
		Entity entity0 = mock(Entity.class);
		when(entity0.getEntityType()).thenReturn(entityType);

		when(entity0.getIdValue()).thenReturn("id0");
		when(entity0.getEntity(attrXrefName)).thenReturn(refEntity0);
		when(entity0.getEntity(attrNillableXrefName)).thenReturn(null);
		when(entity0.getEntities(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.getEntities(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.getString(attrUniqueStringName)).thenReturn("unique1");
		when(entity0.getEntity(attrUniqueXrefName)).thenReturn(refEntity0);
		when(entity0.getEntity(attrReadonlyXrefName)).thenReturn(refEntity0);
		when(entity0.get(attrIdName)).thenReturn("id0");
		when(entity0.get(attrXrefName)).thenReturn(refEntity0);
		when(entity0.get(attrNillableXrefName)).thenReturn(null);
		when(entity0.get(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.get(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.get(attrUniqueStringName)).thenReturn("unique1");
		when(entity0.get(attrUniqueXrefName)).thenReturn(refEntity0);
		when(entity0.get(attrReadonlyXrefName)).thenReturn(refEntity0);

		when(decoratedRepo.findOneById("id0")).thenReturn(entity0);

		Entity updatedEntity0 = mock(Entity.class);
		when(updatedEntity0.getEntityType()).thenReturn(entityType);

		when(updatedEntity0.getIdValue()).thenReturn("id0");
		when(updatedEntity0.getEntity(attrXrefName)).thenReturn(refEntity0Clone);
		when(updatedEntity0.getEntity(attrNillableXrefName)).thenReturn(null);
		when(updatedEntity0.getEntities(attrMrefName)).thenReturn(Arrays.asList(refEntity0Clone));
		when(updatedEntity0.getEntities(attrNillableMrefName)).thenReturn(emptyList());
		when(updatedEntity0.getString(attrUniqueStringName)).thenReturn("unique1");
		when(updatedEntity0.getEntity(attrUniqueXrefName)).thenReturn(refEntity0Clone);
		when(updatedEntity0.getEntity(attrReadonlyXrefName)).thenReturn(refEntity0Clone); // read only, no changes
		when(updatedEntity0.get(attrIdName)).thenReturn("id0");
		when(updatedEntity0.get(attrXrefName)).thenReturn(refEntity0Clone);
		when(updatedEntity0.get(attrNillableXrefName)).thenReturn(null);
		when(updatedEntity0.get(attrMrefName)).thenReturn(Arrays.asList(refEntity0Clone));
		when(updatedEntity0.get(attrNillableMrefName)).thenReturn(emptyList());
		when(updatedEntity0.get(attrUniqueStringName)).thenReturn("unique1");
		when(updatedEntity0.get(attrUniqueXrefName)).thenReturn(refEntity0Clone);
		when(updatedEntity0.get(attrReadonlyXrefName)).thenReturn(refEntity0Clone); // read only, no changes

		// actual tests
		repositoryValidationDecorator.update(updatedEntity0);
		verify(decoratedRepo, times(1)).update(updatedEntity0);
		verify(entityAttributesValidator, times(1)).validate(updatedEntity0, entityType);
	}

	@Test
	public void updateReadOnlyXrefAttrValidationError()
	{
		String attrReadonlyXrefName = "readonlyXrefAttr";

		Attribute readonlyXrefAttr = when(mock(Attribute.class).getName()).thenReturn(attrReadonlyXrefName).getMock();
		when(readonlyXrefAttr.getDataType()).thenReturn(XREF);
		when(readonlyXrefAttr.getRefEntity()).thenReturn(refEntityType);
		when(readonlyXrefAttr.isReadOnly()).thenReturn(true);

		when(entityType.getAttribute(attrReadonlyXrefName)).thenReturn(readonlyXrefAttr);
		when(entityType.getAtomicAttributes()).thenReturn(
				Arrays.asList(idAttr, xrefAttr, nillableXrefAttr, mrefAttr, nillableMrefAttr, uniqueStringAttr,
						uniqueXrefAttr, readonlyXrefAttr));

		// entities
		Entity entity0 = mock(Entity.class);
		when(entity0.getEntityType()).thenReturn(entityType);

		when(entity0.getIdValue()).thenReturn("id0");
		when(entity0.getEntity(attrXrefName)).thenReturn(refEntity0);
		when(entity0.getEntity(attrNillableXrefName)).thenReturn(null);
		when(entity0.getEntities(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.getEntities(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.getString(attrUniqueStringName)).thenReturn("unique1");
		when(entity0.getEntity(attrUniqueXrefName)).thenReturn(refEntity0);
		when(entity0.getEntity(attrReadonlyXrefName)).thenReturn(refEntity0);
		when(entity0.get(attrIdName)).thenReturn("id0");
		when(entity0.get(attrXrefName)).thenReturn(refEntity0);
		when(entity0.get(attrNillableXrefName)).thenReturn(null);
		when(entity0.get(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.get(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.get(attrUniqueStringName)).thenReturn("unique1");
		when(entity0.get(attrUniqueXrefName)).thenReturn(refEntity0);
		when(entity0.get(attrReadonlyXrefName)).thenReturn(refEntity0);

		when(decoratedRepo.findOneById("id0")).thenReturn(entity0);

		Entity updatedEntity0 = mock(Entity.class);
		when(updatedEntity0.getEntityType()).thenReturn(entityType);

		when(updatedEntity0.getIdValue()).thenReturn("id0");
		when(updatedEntity0.getEntity(attrXrefName)).thenReturn(refEntity0Clone);
		when(updatedEntity0.getEntity(attrNillableXrefName)).thenReturn(null);
		when(updatedEntity0.getEntities(attrMrefName)).thenReturn(Arrays.asList(refEntity0Clone));
		when(updatedEntity0.getEntities(attrNillableMrefName)).thenReturn(emptyList());
		when(updatedEntity0.getString(attrUniqueStringName)).thenReturn("unique1");
		when(updatedEntity0.getEntity(attrUniqueXrefName)).thenReturn(refEntity0Clone);
		when(updatedEntity0.getEntity(attrReadonlyXrefName)).thenReturn(refEntity1); // read only attribute update
		when(updatedEntity0.get(attrIdName)).thenReturn("id0");
		when(updatedEntity0.get(attrXrefName)).thenReturn(refEntity0Clone);
		when(updatedEntity0.get(attrNillableXrefName)).thenReturn(null);
		when(updatedEntity0.get(attrMrefName)).thenReturn(Arrays.asList(refEntity0Clone));
		when(updatedEntity0.get(attrNillableMrefName)).thenReturn(emptyList());
		when(updatedEntity0.get(attrUniqueStringName)).thenReturn("unique1");
		when(updatedEntity0.get(attrUniqueXrefName)).thenReturn(refEntity0Clone);
		when(updatedEntity0.get(attrReadonlyXrefName)).thenReturn(refEntity1); // read only attribute update

		// actual tests
		try
		{
			repositoryValidationDecorator.update(updatedEntity0);
			throw new RuntimeException("Expected MolgenisValidationException instead of no exception");
		}
		catch (MolgenisValidationException e)
		{
			verify(entityAttributesValidator, times(1)).validate(updatedEntity0, entityType);
			assertEquals(e.getMessage(),
					"The attribute 'readonlyXrefAttr' of entity 'entity' can not be changed it is readonly. (entity 1)");
		}
	}

	@Test
	public void updateReadOnlyMrefAttr()
	{
		String attrReadonlyMrefName = "readonlyMrefAttr";

		Attribute readonlyMrefAttr = when(mock(Attribute.class).getName()).thenReturn(attrReadonlyMrefName).getMock();
		when(readonlyMrefAttr.getDataType()).thenReturn(MREF);
		when(readonlyMrefAttr.getRefEntity()).thenReturn(refEntityType);
		when(readonlyMrefAttr.isReadOnly()).thenReturn(true);

		when(entityType.getAttribute(attrReadonlyMrefName)).thenReturn(readonlyMrefAttr);
		when(entityType.getAtomicAttributes()).thenReturn(
				Arrays.asList(idAttr, xrefAttr, nillableXrefAttr, mrefAttr, nillableMrefAttr, uniqueStringAttr,
						uniqueXrefAttr, readonlyMrefAttr));

		// entities
		Entity entity0 = mock(Entity.class);
		when(entity0.getEntityType()).thenReturn(entityType);

		when(entity0.getIdValue()).thenReturn("id0");
		when(entity0.getEntity(attrXrefName)).thenReturn(refEntity0);
		when(entity0.getEntity(attrNillableXrefName)).thenReturn(null);
		when(entity0.getEntities(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.getEntities(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.getString(attrUniqueStringName)).thenReturn("unique1");
		when(entity0.getEntity(attrUniqueXrefName)).thenReturn(refEntity0);
		when(entity0.getEntities(attrReadonlyMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.get(attrIdName)).thenReturn("id0");
		when(entity0.get(attrXrefName)).thenReturn(refEntity0);
		when(entity0.get(attrNillableXrefName)).thenReturn(null);
		when(entity0.get(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.get(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.get(attrUniqueStringName)).thenReturn("unique1");
		when(entity0.get(attrUniqueXrefName)).thenReturn(refEntity0);
		when(entity0.get(attrReadonlyMrefName)).thenReturn(Arrays.asList(refEntity0));

		when(decoratedRepo.findOneById("id0")).thenReturn(entity0);

		Entity updatedEntity0 = mock(Entity.class);
		when(updatedEntity0.getEntityType()).thenReturn(entityType);

		when(updatedEntity0.getIdValue()).thenReturn("id0");
		when(updatedEntity0.getEntity(attrXrefName)).thenReturn(refEntity0Clone);
		when(updatedEntity0.getEntity(attrNillableXrefName)).thenReturn(null);
		when(updatedEntity0.getEntities(attrMrefName)).thenReturn(Arrays.asList(refEntity0Clone));
		when(updatedEntity0.getEntities(attrNillableMrefName)).thenReturn(emptyList());
		when(updatedEntity0.getString(attrUniqueStringName)).thenReturn("unique1");
		when(updatedEntity0.getEntity(attrUniqueXrefName)).thenReturn(refEntity0Clone);
		when(updatedEntity0.getEntities(attrReadonlyMrefName)).thenReturn(Arrays.asList(refEntity0Clone)); // read only,
		// no
		// changes
		when(updatedEntity0.get(attrIdName)).thenReturn("id0");
		when(updatedEntity0.get(attrXrefName)).thenReturn(refEntity0Clone);
		when(updatedEntity0.get(attrNillableXrefName)).thenReturn(null);
		when(updatedEntity0.get(attrMrefName)).thenReturn(Arrays.asList(refEntity0Clone));
		when(updatedEntity0.get(attrNillableMrefName)).thenReturn(emptyList());
		when(updatedEntity0.get(attrUniqueStringName)).thenReturn("unique1");
		when(updatedEntity0.get(attrUniqueXrefName)).thenReturn(refEntity0Clone);
		when(updatedEntity0.get(attrReadonlyMrefName)).thenReturn(Arrays.asList(refEntity0Clone)); // read only, no
		// changes
		// actual tests
		repositoryValidationDecorator.update(updatedEntity0);
		verify(decoratedRepo, times(1)).update(updatedEntity0);
		verify(entityAttributesValidator, times(1)).validate(updatedEntity0, entityType);
	}

	@Test
	public void updateReadOnlyMrefAttrNull()
	{
		String attrReadonlyMrefName = "readonlyMrefAttr";

		Attribute readonlyMrefAttr = when(mock(Attribute.class).getName()).thenReturn(attrReadonlyMrefName).getMock();
		when(readonlyMrefAttr.getDataType()).thenReturn(MREF);
		when(readonlyMrefAttr.getRefEntity()).thenReturn(refEntityType);
		when(readonlyMrefAttr.isReadOnly()).thenReturn(true);
		when(readonlyMrefAttr.isNillable()).thenReturn(true);

		when(entityType.getAttribute(attrReadonlyMrefName)).thenReturn(readonlyMrefAttr);
		when(entityType.getAtomicAttributes()).thenReturn(
				Arrays.asList(idAttr, xrefAttr, nillableXrefAttr, mrefAttr, nillableMrefAttr, uniqueStringAttr,
						uniqueXrefAttr, readonlyMrefAttr));

		// entities
		Entity entity0 = mock(Entity.class);
		when(entity0.getEntityType()).thenReturn(entityType);

		when(entity0.getIdValue()).thenReturn("id0");
		when(entity0.getEntity(attrXrefName)).thenReturn(refEntity0);
		when(entity0.getEntity(attrNillableXrefName)).thenReturn(null);
		when(entity0.getEntities(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.getEntities(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.getString(attrUniqueStringName)).thenReturn("unique1");
		when(entity0.getEntity(attrUniqueXrefName)).thenReturn(refEntity0);
		when(entity0.getEntities(attrReadonlyMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.get(attrIdName)).thenReturn("id0");
		when(entity0.get(attrXrefName)).thenReturn(refEntity0);
		when(entity0.get(attrNillableXrefName)).thenReturn(null);
		when(entity0.get(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.get(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.get(attrUniqueStringName)).thenReturn("unique1");
		when(entity0.get(attrUniqueXrefName)).thenReturn(refEntity0);
		when(entity0.get(attrReadonlyMrefName)).thenReturn(emptyList());
		when(entity0.getEntities(attrReadonlyMrefName)).thenReturn(emptyList());

		when(decoratedRepo.findOneById("id0")).thenReturn(entity0);

		Entity updatedEntity0 = mock(Entity.class);
		when(updatedEntity0.getEntityType()).thenReturn(entityType);

		when(updatedEntity0.getIdValue()).thenReturn("id0");
		when(updatedEntity0.getEntity(attrXrefName)).thenReturn(refEntity0Clone);
		when(updatedEntity0.getEntity(attrNillableXrefName)).thenReturn(null);
		when(updatedEntity0.getEntities(attrMrefName)).thenReturn(Arrays.asList(refEntity0Clone));
		when(updatedEntity0.getEntities(attrNillableMrefName)).thenReturn(emptyList());
		when(updatedEntity0.getString(attrUniqueStringName)).thenReturn("unique1");
		when(updatedEntity0.getEntity(attrUniqueXrefName)).thenReturn(refEntity0Clone);
		when(updatedEntity0.get(attrReadonlyMrefName)).thenReturn(null);
		when(updatedEntity0.getEntities(attrReadonlyMrefName)).thenReturn(emptyList()); // read only,
		// no
		// changes
		when(updatedEntity0.get(attrIdName)).thenReturn("id0");
		when(updatedEntity0.get(attrXrefName)).thenReturn(refEntity0Clone);
		when(updatedEntity0.get(attrNillableXrefName)).thenReturn(null);
		when(updatedEntity0.get(attrMrefName)).thenReturn(Arrays.asList(refEntity0Clone));
		when(updatedEntity0.get(attrNillableMrefName)).thenReturn(emptyList());
		when(updatedEntity0.get(attrUniqueStringName)).thenReturn("unique1");
		when(updatedEntity0.get(attrUniqueXrefName)).thenReturn(refEntity0Clone);
		// changes
		// actual tests
		repositoryValidationDecorator.update(updatedEntity0);
		verify(decoratedRepo, times(1)).update(updatedEntity0);
		verify(entityAttributesValidator, times(1)).validate(updatedEntity0, entityType);
	}

	@Test
	public void updateReadOnlyMrefAttrValidationError()
	{
		String attrReadonlyMrefName = "readonlyMrefAttr";

		Attribute readonlyMrefAttr = when(mock(Attribute.class).getName()).thenReturn(attrReadonlyMrefName).getMock();
		when(readonlyMrefAttr.getDataType()).thenReturn(MREF);
		when(readonlyMrefAttr.getRefEntity()).thenReturn(refEntityType);
		when(readonlyMrefAttr.isReadOnly()).thenReturn(true);

		when(entityType.getAttribute(attrReadonlyMrefName)).thenReturn(readonlyMrefAttr);
		when(entityType.getAtomicAttributes()).thenReturn(
				Arrays.asList(idAttr, xrefAttr, nillableXrefAttr, mrefAttr, nillableMrefAttr, uniqueStringAttr,
						uniqueXrefAttr, readonlyMrefAttr));

		// entities
		Entity entity0 = mock(Entity.class);
		when(entity0.getEntityType()).thenReturn(entityType);

		when(entity0.getIdValue()).thenReturn("id0");
		when(entity0.getEntity(attrXrefName)).thenReturn(refEntity0);
		when(entity0.getEntity(attrNillableXrefName)).thenReturn(null);
		when(entity0.getEntities(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.getEntities(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.getString(attrUniqueStringName)).thenReturn("unique1");
		when(entity0.getEntity(attrUniqueXrefName)).thenReturn(refEntity0);
		when(entity0.getEntities(attrReadonlyMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.get(attrIdName)).thenReturn("id0");
		when(entity0.get(attrXrefName)).thenReturn(refEntity0);
		when(entity0.get(attrNillableXrefName)).thenReturn(null);
		when(entity0.get(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.get(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.get(attrUniqueStringName)).thenReturn("unique1");
		when(entity0.get(attrUniqueXrefName)).thenReturn(refEntity0);
		when(entity0.get(attrReadonlyMrefName)).thenReturn(Arrays.asList(refEntity0));

		when(decoratedRepo.findOneById("id0")).thenReturn(entity0);

		Entity updatedEntity0 = mock(Entity.class);
		when(updatedEntity0.getEntityType()).thenReturn(entityType);

		when(updatedEntity0.getIdValue()).thenReturn("id0");
		when(updatedEntity0.getEntity(attrXrefName)).thenReturn(refEntity0Clone);
		when(updatedEntity0.getEntity(attrNillableXrefName)).thenReturn(null);
		when(updatedEntity0.getEntities(attrMrefName)).thenReturn(Arrays.asList(refEntity0Clone));
		when(updatedEntity0.getEntities(attrNillableMrefName)).thenReturn(emptyList());
		when(updatedEntity0.getString(attrUniqueStringName)).thenReturn("unique1");
		when(updatedEntity0.getEntity(attrUniqueXrefName)).thenReturn(refEntity0Clone);
		when(updatedEntity0.getEntities(attrReadonlyMrefName)).thenReturn(Arrays.asList(refEntity1)); // read only
		// attribute
		// update
		when(updatedEntity0.get(attrIdName)).thenReturn("id0");
		when(updatedEntity0.get(attrXrefName)).thenReturn(refEntity0Clone);
		when(updatedEntity0.get(attrNillableXrefName)).thenReturn(null);
		when(updatedEntity0.get(attrMrefName)).thenReturn(Arrays.asList(refEntity0Clone));
		when(updatedEntity0.get(attrNillableMrefName)).thenReturn(emptyList());
		when(updatedEntity0.get(attrUniqueStringName)).thenReturn("unique1");
		when(updatedEntity0.get(attrUniqueXrefName)).thenReturn(refEntity0Clone);
		when(updatedEntity0.get(attrReadonlyMrefName)).thenReturn(Arrays.asList(refEntity1)); // read only attribute
		// update

		// actual tests
		try
		{
			repositoryValidationDecorator.update(updatedEntity0);
			throw new RuntimeException("Expected MolgenisValidationException instead of no exception");
		}
		catch (MolgenisValidationException e)
		{
			verify(entityAttributesValidator, times(1)).validate(updatedEntity0, entityType);
			assertEquals(e.getMessage(),
					"The attribute 'readonlyMrefAttr' of entity 'entity' can not be changed it is readonly. (entity 1)");
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void updateStream()
	{
		// entities
		Entity entity0 = mock(Entity.class);
		when(entity0.getEntityType()).thenReturn(entityType);

		when(entity0.getIdValue()).thenReturn("id0");
		when(entity0.getEntity(attrXrefName)).thenReturn(refEntity0);
		when(entity0.getEntity(attrNillableXrefName)).thenReturn(null);
		when(entity0.getEntities(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.getEntities(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.getString(attrUniqueStringName)).thenReturn("unique0");
		when(entity0.getEntity(attrUniqueXrefName)).thenReturn(refEntity0);

		when(entity0.get(attrIdName)).thenReturn("id0");
		when(entity0.get(attrXrefName)).thenReturn(refEntity0);
		when(entity0.get(attrNillableXrefName)).thenReturn(null);
		when(entity0.get(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.get(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.get(attrUniqueStringName)).thenReturn("unique0");
		when(entity0.get(attrUniqueXrefName)).thenReturn(refEntity0);

		// actual tests
		List<Entity> entities = Arrays.asList(entity0);
		repositoryValidationDecorator.update(entities.stream());

		ArgumentCaptor<Stream<Entity>> captor = ArgumentCaptor.forClass(Stream.class);
		verify(decoratedRepo, times(1)).update(captor.capture());
		Stream<Entity> stream = captor.getValue();
		stream.collect(Collectors.toList()); // process stream to enable validation

		verify(entityAttributesValidator, times(1)).validate(entity0, entityType);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void updateStreamEntityAttributesValidationError()
	{
		// entities
		Entity entity0 = mock(Entity.class);
		when(entity0.getEntityType()).thenReturn(entityType);

		when(entity0.getIdValue()).thenReturn("id0");
		when(entity0.getEntity(attrXrefName)).thenReturn(refEntity0);
		when(entity0.getEntity(attrNillableXrefName)).thenReturn(null);
		when(entity0.getEntities(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.getEntities(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.getString(attrUniqueStringName)).thenReturn("unique0");
		when(entity0.getEntity(attrUniqueXrefName)).thenReturn(refEntity0);

		when(entity0.get(attrIdName)).thenReturn("id0");
		when(entity0.get(attrXrefName)).thenReturn(refEntity0);
		when(entity0.get(attrNillableXrefName)).thenReturn(null);
		when(entity0.get(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.get(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.get(attrUniqueStringName)).thenReturn("unique0");
		when(entity0.get(attrUniqueXrefName)).thenReturn(refEntity0);

		Entity entity1 = mock(Entity.class);
		when(entity1.getIdValue()).thenReturn("id1");
		when(entity1.getEntityType()).thenReturn(entityType);
		when(entity1.getEntity(attrXrefName)).thenReturn(refEntity0);
		when(entity1.getEntity(attrNillableXrefName)).thenReturn(null);
		when(entity1.getEntities(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity1.getEntities(attrNillableMrefName)).thenReturn(emptyList());
		when(entity1.getString(attrUniqueStringName)).thenReturn("unique1");
		when(entity1.getEntity(attrUniqueXrefName)).thenReturn(refEntity1);

		when(entity1.get(attrIdName)).thenReturn("id1");
		when(entity1.get(attrXrefName)).thenReturn(refEntity0);
		when(entity1.get(attrNillableXrefName)).thenReturn(null);
		when(entity1.get(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity1.get(attrNillableMrefName)).thenReturn(emptyList());
		when(entity1.get(attrUniqueStringName)).thenReturn("unique1");
		when(entity1.get(attrUniqueXrefName)).thenReturn(refEntity1);

		Set<ConstraintViolation> violations = singleton(new ConstraintViolation("violation", 2L));
		when(entityAttributesValidator.validate(entity1, entityType)).thenReturn(violations);

		// actual tests
		List<Entity> entities = Arrays.asList(entity0, entity1);
		repositoryValidationDecorator.update(entities.stream());

		ArgumentCaptor<Stream<Entity>> captor = ArgumentCaptor.forClass(Stream.class);
		verify(decoratedRepo, times(1)).update(captor.capture());
		Stream<Entity> stream = captor.getValue();
		try
		{
			stream.collect(Collectors.toList()); // process stream to enable validation

			throw new RuntimeException("Expected MolgenisValidationException instead of no exception");
		}
		catch (MolgenisValidationException e)
		{
			verify(entityAttributesValidator, times(1)).validate(entity0, entityType);
			verify(entityAttributesValidator, times(1)).validate(entity1, entityType);
			assertEquals(e.getViolations(), violations);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void updateStreamRequiredValueValidationError()
	{
		// entities
		Entity entity0 = mock(Entity.class);
		when(entity0.getEntityType()).thenReturn(entityType);

		when(entity0.getIdValue()).thenReturn("id0");
		when(entity0.getEntity(attrXrefName)).thenReturn(null); // violation error
		when(entity0.getEntity(attrNillableXrefName)).thenReturn(null);
		when(entity0.getEntities(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.getEntities(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.getString(attrUniqueStringName)).thenReturn("unique0");
		when(entity0.getEntity(attrUniqueXrefName)).thenReturn(refEntity0);

		when(entity0.get(attrIdName)).thenReturn("id0");
		when(entity0.get(attrXrefName)).thenReturn(null); // violation error
		when(entity0.get(attrNillableXrefName)).thenReturn(null);
		when(entity0.get(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.get(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.get(attrUniqueStringName)).thenReturn("unique0");
		when(entity0.get(attrUniqueXrefName)).thenReturn(refEntity0);

		// actual tests
		List<Entity> entities = Arrays.asList(entity0);
		repositoryValidationDecorator.update(entities.stream());

		ArgumentCaptor<Stream<Entity>> captor = ArgumentCaptor.forClass(Stream.class);
		verify(decoratedRepo, times(1)).update(captor.capture());
		Stream<Entity> stream = captor.getValue();
		try
		{
			stream.collect(Collectors.toList()); // process stream to enable validation

			throw new RuntimeException("Expected MolgenisValidationException instead of no exception");
		}
		catch (MolgenisValidationException e)
		{
			verify(entityAttributesValidator, times(1)).validate(entity0, entityType);
			assertEquals(e.getMessage(), "The attribute 'xrefAttr' of entity 'entity' can not be null. (entity 1)");
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void updateStreamRequiredValueWithExpression()
	{
		when(xrefAttr.getExpression()).thenReturn("expr");

		// entities
		Entity entity0 = mock(Entity.class);
		when(entity0.getEntityType()).thenReturn(entityType);

		when(entity0.getIdValue()).thenReturn("id0");
		when(entity0.getEntity(attrXrefName)).thenReturn(null); // valid, because the value is 'computed'
		when(entity0.getEntity(attrNillableXrefName)).thenReturn(null);
		when(entity0.getEntities(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.getEntities(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.getString(attrUniqueStringName)).thenReturn("unique0");
		when(entity0.getEntity(attrUniqueXrefName)).thenReturn(refEntity0);

		when(entity0.get(attrIdName)).thenReturn("id0");
		when(entity0.get(attrXrefName)).thenReturn(null); // valid, because the value is 'computed'
		when(entity0.get(attrNillableXrefName)).thenReturn(null);
		when(entity0.get(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.get(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.get(attrUniqueStringName)).thenReturn("unique0");
		when(entity0.get(attrUniqueXrefName)).thenReturn(refEntity0);

		// actual tests
		List<Entity> entities = Arrays.asList(entity0);
		repositoryValidationDecorator.update(entities.stream());

		ArgumentCaptor<Stream<Entity>> captor = ArgumentCaptor.forClass(Stream.class);
		verify(decoratedRepo, times(1)).update(captor.capture());
		Stream<Entity> stream = captor.getValue();

		stream.collect(Collectors.toList()); // process stream to enable validation
		verify(entityAttributesValidator, times(1)).validate(entity0, entityType);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void updateStreamRequiredMrefValueValidationError()
	{
		// entities
		Entity entity0 = mock(Entity.class);
		when(entity0.getEntityType()).thenReturn(entityType);

		when(entity0.getIdValue()).thenReturn("id0");
		when(entity0.getEntity(attrXrefName)).thenReturn(refEntity0);
		when(entity0.getEntity(attrNillableXrefName)).thenReturn(null);
		when(entity0.getEntities(attrMrefName)).thenReturn(emptyList());
		when(entity0.getEntities(attrNillableMrefName)).thenReturn(emptyList()); // violation error
		when(entity0.getString(attrUniqueStringName)).thenReturn("unique0");
		when(entity0.getEntity(attrUniqueXrefName)).thenReturn(refEntity0);

		when(entity0.get(attrIdName)).thenReturn("id0");
		when(entity0.get(attrXrefName)).thenReturn(refEntity0);
		when(entity0.get(attrNillableXrefName)).thenReturn(null);
		when(entity0.get(attrMrefName)).thenReturn(emptyList());
		when(entity0.get(attrNillableMrefName)).thenReturn(emptyList()); // violation error
		when(entity0.get(attrUniqueStringName)).thenReturn("unique0");
		when(entity0.get(attrUniqueXrefName)).thenReturn(refEntity0);

		// actual tests
		List<Entity> entities = Arrays.asList(entity0);
		repositoryValidationDecorator.update(entities.stream());

		ArgumentCaptor<Stream<Entity>> captor = ArgumentCaptor.forClass(Stream.class);
		verify(decoratedRepo, times(1)).update(captor.capture());
		Stream<Entity> stream = captor.getValue();
		try
		{
			stream.collect(Collectors.toList()); // process stream to enable validation

			throw new RuntimeException("Expected MolgenisValidationException instead of no exception");
		}
		catch (MolgenisValidationException e)
		{
			verify(entityAttributesValidator, times(1)).validate(entity0, entityType);
			assertEquals(e.getMessage(), "The attribute 'mrefAttr' of entity 'entity' can not be null. (entity 1)");
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void updateStreamReferenceXrefSelfReferenceToPreviouslyAddedEntity()
	{
		when(xrefAttr.getRefEntity()).thenReturn(entityType);

		// entities
		Entity entity0 = mock(Entity.class);
		when(entity0.getEntityType()).thenReturn(entityType);

		when(entity0.getIdValue()).thenReturn("id0");
		when(entity0.getEntity(attrXrefName)).thenReturn(entity0); // self reference
		when(entity0.getEntity(attrNillableXrefName)).thenReturn(null);
		when(entity0.getEntities(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.getEntities(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.getString(attrUniqueStringName)).thenReturn("unique0");
		when(entity0.getEntity(attrUniqueXrefName)).thenReturn(refEntity0);

		when(entity0.get(attrIdName)).thenReturn("id0");
		when(entity0.get(attrXrefName)).thenReturn(entity0); // self reference
		when(entity0.get(attrNillableXrefName)).thenReturn(null);
		when(entity0.get(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.get(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.get(attrUniqueStringName)).thenReturn("unique0");
		when(entity0.get(attrUniqueXrefName)).thenReturn(refEntity0);

		// actual tests
		List<Entity> entities = Arrays.asList(entity0);
		when(dataService.findAll(entityTypeId,
				new QueryImpl<>().fetch(new Fetch().field(attrIdName)))).thenReturn(Stream.of(entity0));
		repositoryValidationDecorator.update(entities.stream());

		ArgumentCaptor<Stream<Entity>> captor = ArgumentCaptor.forClass(Stream.class);
		verify(decoratedRepo, times(1)).update(captor.capture());
		Stream<Entity> stream = captor.getValue();

		stream.collect(Collectors.toList()); // process stream to enable validation
		verify(entityAttributesValidator, times(1)).validate(entity0, entityType);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void updateStreamReferenceXrefSelfReferenceToSelf()
	{
		when(xrefAttr.getRefEntity()).thenReturn(entityType);

		// entities
		Entity entity0 = mock(Entity.class);
		when(entity0.getEntityType()).thenReturn(entityType);

		when(entity0.getIdValue()).thenReturn("id0");
		when(entity0.getEntity(attrXrefName)).thenReturn(entity0); // self reference
		when(entity0.getEntity(attrNillableXrefName)).thenReturn(null);
		when(entity0.getEntities(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.getEntities(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.getString(attrUniqueStringName)).thenReturn("unique0");
		when(entity0.getEntity(attrUniqueXrefName)).thenReturn(refEntity0);

		when(entity0.get(attrIdName)).thenReturn("id0");
		when(entity0.get(attrXrefName)).thenReturn(entity0); // self reference
		when(entity0.get(attrNillableXrefName)).thenReturn(null);
		when(entity0.get(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.get(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.get(attrUniqueStringName)).thenReturn("unique0");
		when(entity0.get(attrUniqueXrefName)).thenReturn(refEntity0);

		Entity entity1 = mock(Entity.class);
		when(entity1.getEntityType()).thenReturn(entityType);

		when(entity1.getIdValue()).thenReturn("id1");
		when(entity1.getEntity(attrXrefName)).thenReturn(entity0); // reference to previously added entity
		when(entity1.getEntity(attrNillableXrefName)).thenReturn(null);
		when(entity1.getEntities(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity1.getEntities(attrNillableMrefName)).thenReturn(emptyList());
		when(entity1.getString(attrUniqueStringName)).thenReturn("unique1");
		when(entity1.getEntity(attrUniqueXrefName)).thenReturn(refEntity1);

		when(entity1.get(attrIdName)).thenReturn("id1");
		when(entity1.get(attrXrefName)).thenReturn(entity0); // reference to previously added entity
		when(entity1.get(attrNillableXrefName)).thenReturn(null);
		when(entity1.get(attrMrefName)).thenReturn(Arrays.asList(refEntity1));
		when(entity1.get(attrNillableMrefName)).thenReturn(emptyList());
		when(entity1.get(attrUniqueStringName)).thenReturn("unique1");
		when(entity1.get(attrUniqueXrefName)).thenReturn(refEntity1);

		// actual tests
		List<Entity> entities = Arrays.asList(entity0, entity1);
		when(dataService.findAll(entityTypeId,
				new QueryImpl<>().fetch(new Fetch().field(attrIdName)))).thenReturn(Stream.of(entity0, entity1));
		repositoryValidationDecorator.update(entities.stream());

		ArgumentCaptor<Stream<Entity>> captor = ArgumentCaptor.forClass(Stream.class);
		verify(decoratedRepo, times(1)).update(captor.capture());
		Stream<Entity> stream = captor.getValue();

		stream.collect(Collectors.toList()); // process stream to enable validation
		verify(entityAttributesValidator, times(1)).validate(entity0, entityType);
		verify(entityAttributesValidator, times(1)).validate(entity1, entityType);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void updateStreamReferenceXrefDoesNotExistsValidationError()
	{
		String refEntityDoesNotExistId = "id1";
		Entity refEntityDoesNotExist = mock(Entity.class);
		when(refEntityDoesNotExist.getEntityType()).thenReturn(refEntityType);
		when(refEntityDoesNotExist.getIdValue()).thenReturn(refEntityDoesNotExistId);
		when(refEntityDoesNotExist.get(refAttrIdName)).thenReturn(refEntityDoesNotExistId);
		when(refEntityDoesNotExist.getString(refAttrIdName)).thenReturn(refEntityDoesNotExistId);

		// entities
		Entity entity0 = mock(Entity.class);
		when(entity0.getEntityType()).thenReturn(entityType);

		when(entity0.getIdValue()).thenReturn("id0");
		when(entity0.getEntity(attrXrefName)).thenReturn(refEntity0);
		when(entity0.getEntity(attrNillableXrefName)).thenReturn(null);
		when(entity0.getEntities(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.getEntities(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.getString(attrUniqueStringName)).thenReturn("unique0");
		when(entity0.getEntity(attrUniqueXrefName)).thenReturn(refEntity0);

		when(entity0.get(attrIdName)).thenReturn("id0");
		when(entity0.get(attrXrefName)).thenReturn(refEntity0);
		when(entity0.get(attrNillableXrefName)).thenReturn(null);
		when(entity0.get(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.get(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.get(attrUniqueStringName)).thenReturn("unique0");
		when(entity0.get(attrUniqueXrefName)).thenReturn(refEntity0);

		Entity entity1 = mock(Entity.class);
		when(entity1.getEntityType()).thenReturn(entityType);

		when(entity1.getIdValue()).thenReturn("id1");
		when(entity1.getEntity(attrXrefName)).thenReturn(refEntityDoesNotExist);
		when(entity1.getEntity(attrNillableXrefName)).thenReturn(null);
		when(entity1.getEntities(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity1.getEntities(attrNillableMrefName)).thenReturn(emptyList());
		when(entity1.getString(attrUniqueStringName)).thenReturn("unique1");
		when(entity1.getEntity(attrUniqueXrefName)).thenReturn(refEntity1);

		when(entity1.get(attrIdName)).thenReturn("id1");
		when(entity1.get(attrXrefName)).thenReturn(refEntityDoesNotExist);
		when(entity1.get(attrNillableXrefName)).thenReturn(null);
		when(entity1.get(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity1.get(attrNillableMrefName)).thenReturn(emptyList());
		when(entity1.get(attrUniqueStringName)).thenReturn("unique1");
		when(entity1.get(attrUniqueXrefName)).thenReturn(refEntity1);

		// actual tests
		List<Entity> entities = Arrays.asList(entity0, entity1);
		repositoryValidationDecorator.update(entities.stream());

		ArgumentCaptor<Stream<Entity>> captor = ArgumentCaptor.forClass(Stream.class);
		verify(decoratedRepo, times(1)).update(captor.capture());
		Stream<Entity> stream = captor.getValue();
		try
		{
			stream.collect(Collectors.toList()); // process stream to enable validation

			throw new RuntimeException("Expected MolgenisValidationException instead of no exception");
		}
		catch (MolgenisValidationException e)
		{
			verify(entityAttributesValidator, times(1)).validate(entity0, entityType);
			verify(entityAttributesValidator, times(1)).validate(entity1, entityType);
			assertEquals(e.getMessage(),
					"Unknown xref value 'id1' for attribute 'xrefAttr' of entity 'entity'. (entity 2)");
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void updateStreamReferenceAttrWithExpression()
	{
		when(xrefAttr.getExpression()).thenReturn("expr");

		String refEntityDoesNotExistId = "id1";
		Entity refEntityDoesNotExist = mock(Entity.class);
		when(refEntityDoesNotExist.getEntityType()).thenReturn(refEntityType);
		when(refEntityDoesNotExist.getIdValue()).thenReturn(refEntityDoesNotExistId);
		when(refEntityDoesNotExist.get(refAttrIdName)).thenReturn(refEntityDoesNotExistId);
		when(refEntityDoesNotExist.getString(refAttrIdName)).thenReturn(refEntityDoesNotExistId);

		// entities
		Entity entity0 = mock(Entity.class);
		when(entity0.getEntityType()).thenReturn(entityType);

		when(entity0.getIdValue()).thenReturn("id0");
		when(entity0.getEntity(attrXrefName)).thenReturn(refEntity0);
		when(entity0.getEntity(attrNillableXrefName)).thenReturn(null);
		when(entity0.getEntities(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.getEntities(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.getString(attrUniqueStringName)).thenReturn("unique0");
		when(entity0.getEntity(attrUniqueXrefName)).thenReturn(refEntity0);

		when(entity0.get(attrIdName)).thenReturn("id0");
		when(entity0.get(attrXrefName)).thenReturn(refEntity0);
		when(entity0.get(attrNillableXrefName)).thenReturn(null);
		when(entity0.get(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.get(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.get(attrUniqueStringName)).thenReturn("unique0");
		when(entity0.get(attrUniqueXrefName)).thenReturn(refEntity0);

		Entity entity1 = mock(Entity.class);
		when(entity1.getEntityType()).thenReturn(entityType);

		when(entity1.getIdValue()).thenReturn("id1");
		when(entity1.getEntity(attrXrefName)).thenReturn(refEntityDoesNotExist); // valid, because the value is computed
		when(entity1.getEntity(attrNillableXrefName)).thenReturn(null);
		when(entity1.getEntities(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity1.getEntities(attrNillableMrefName)).thenReturn(emptyList());
		when(entity1.getString(attrUniqueStringName)).thenReturn("unique1");
		when(entity1.getEntity(attrUniqueXrefName)).thenReturn(refEntity1);

		when(entity1.get(attrIdName)).thenReturn("id1");
		when(entity1.get(attrXrefName)).thenReturn(refEntityDoesNotExist);
		when(entity1.get(attrNillableXrefName)).thenReturn(null);
		when(entity1.get(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity1.get(attrNillableMrefName)).thenReturn(emptyList());
		when(entity1.get(attrUniqueStringName)).thenReturn("unique1");
		when(entity1.get(attrUniqueXrefName)).thenReturn(refEntity1);

		// actual tests
		List<Entity> entities = Arrays.asList(entity0, entity1);
		repositoryValidationDecorator.update(entities.stream());

		ArgumentCaptor<Stream<Entity>> captor = ArgumentCaptor.forClass(Stream.class);
		verify(decoratedRepo, times(1)).update(captor.capture());
		Stream<Entity> stream = captor.getValue();

		stream.collect(Collectors.toList()); // process stream to enable validation
		verify(entityAttributesValidator, times(1)).validate(entity0, entityType);
		verify(entityAttributesValidator, times(1)).validate(entity1, entityType);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void updateStreamReferenceMrefDoesNotExistsValidationError()
	{
		String refEntityDoesNotExistId = "id1";
		Entity refEntityDoesNotExist = mock(Entity.class);
		when(refEntityDoesNotExist.getEntityType()).thenReturn(refEntityType);
		when(refEntityDoesNotExist.getIdValue()).thenReturn(refEntityDoesNotExistId);
		when(refEntityDoesNotExist.get(refAttrIdName)).thenReturn(refEntityDoesNotExistId);
		when(refEntityDoesNotExist.getString(refAttrIdName)).thenReturn(refEntityDoesNotExistId);

		// entities
		Entity entity0 = mock(Entity.class);
		when(entity0.getEntityType()).thenReturn(entityType);

		when(entity0.getIdValue()).thenReturn("id0");
		when(entity0.getEntity(attrXrefName)).thenReturn(refEntity0);
		when(entity0.getEntity(attrNillableXrefName)).thenReturn(null);
		when(entity0.getEntities(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.getEntities(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.getString(attrUniqueStringName)).thenReturn("unique0");
		when(entity0.getEntity(attrUniqueXrefName)).thenReturn(refEntity0);

		when(entity0.get(attrIdName)).thenReturn("id0");
		when(entity0.get(attrXrefName)).thenReturn(refEntity0);
		when(entity0.get(attrNillableXrefName)).thenReturn(null);
		when(entity0.get(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.get(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.get(attrUniqueStringName)).thenReturn("unique0");
		when(entity0.get(attrUniqueXrefName)).thenReturn(refEntity0);

		Entity entity1 = mock(Entity.class);
		when(entity1.getEntityType()).thenReturn(entityType);

		when(entity1.getIdValue()).thenReturn("id1");
		when(entity1.getEntity(attrXrefName)).thenReturn(refEntity0);
		when(entity1.getEntity(attrNillableXrefName)).thenReturn(null);
		when(entity1.getEntities(attrMrefName)).thenReturn(Arrays.asList(refEntity0, refEntityDoesNotExist));
		when(entity1.getEntities(attrNillableMrefName)).thenReturn(emptyList());
		when(entity1.getString(attrUniqueStringName)).thenReturn("unique1");
		when(entity1.getEntity(attrUniqueXrefName)).thenReturn(refEntity1);

		when(entity1.get(attrIdName)).thenReturn("id1");
		when(entity1.get(attrXrefName)).thenReturn(refEntityDoesNotExist);
		when(entity1.get(attrNillableXrefName)).thenReturn(null);
		when(entity1.get(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity1.get(attrNillableMrefName)).thenReturn(emptyList());
		when(entity1.get(attrUniqueStringName)).thenReturn("unique1");
		when(entity1.get(attrUniqueXrefName)).thenReturn(refEntity1);

		// actual tests
		List<Entity> entities = Arrays.asList(entity0, entity1);
		repositoryValidationDecorator.update(entities.stream());

		ArgumentCaptor<Stream<Entity>> captor = ArgumentCaptor.forClass(Stream.class);
		verify(decoratedRepo, times(1)).update(captor.capture());
		Stream<Entity> stream = captor.getValue();
		try
		{
			stream.collect(Collectors.toList()); // process stream to enable validation

			throw new RuntimeException("Expected MolgenisValidationException instead of no exception");
		}
		catch (MolgenisValidationException e)
		{
			verify(entityAttributesValidator, times(1)).validate(entity0, entityType);
			verify(entityAttributesValidator, times(1)).validate(entity1, entityType);
			assertEquals(e.getMessage(),
					"Unknown xref value 'id1' for attribute 'mrefAttr' of entity 'entity'. (entity 2)");
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void updateStreamUniqueStringValueExistsInBackendValidationError()
	{
		Entity entityInBackend0 = mock(Entity.class);
		when(entityInBackend0.getEntityType()).thenReturn(entityType);

		when(entityInBackend0.getIdValue()).thenReturn("idbackend0");
		when(entityInBackend0.getEntity(attrXrefName)).thenReturn(refEntity0);
		when(entityInBackend0.getEntity(attrNillableXrefName)).thenReturn(null);
		when(entityInBackend0.getEntities(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entityInBackend0.getEntities(attrNillableMrefName)).thenReturn(emptyList());
		when(entityInBackend0.getString(attrUniqueStringName)).thenReturn("unique0");
		when(entityInBackend0.getEntity(attrUniqueXrefName)).thenReturn(refEntity0);

		when(entityInBackend0.get(attrIdName)).thenReturn("idbackend0");
		when(entityInBackend0.get(attrXrefName)).thenReturn(refEntity0);
		when(entityInBackend0.get(attrNillableXrefName)).thenReturn(null);
		when(entityInBackend0.get(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entityInBackend0.get(attrNillableMrefName)).thenReturn(emptyList());
		when(entityInBackend0.get(attrUniqueStringName)).thenReturn("unique0");
		when(entityInBackend0.get(attrUniqueXrefName)).thenReturn(refEntity0);

		when(decoratedRepo.findAll(new QueryImpl<>().fetch(new Fetch().field(attrIdName)))).thenReturn(
				Stream.of(entityInBackend0));

		// entities
		Entity entity0 = mock(Entity.class);
		when(entity0.getEntityType()).thenReturn(entityType);

		when(entity0.getIdValue()).thenReturn("id0");
		when(entity0.getEntity(attrXrefName)).thenReturn(refEntity0);
		when(entity0.getEntity(attrNillableXrefName)).thenReturn(null);
		when(entity0.getEntities(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.getEntities(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.getString(attrUniqueStringName)).thenReturn("unique0"); // duplicate
		when(entity0.getEntity(attrUniqueXrefName)).thenReturn(refEntity1);

		when(entity0.get(attrIdName)).thenReturn("id0");
		when(entity0.get(attrXrefName)).thenReturn(refEntity0);
		when(entity0.get(attrNillableXrefName)).thenReturn(null);
		when(entity0.get(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.get(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.get(attrUniqueStringName)).thenReturn("unique0"); // duplicate
		when(entity0.get(attrUniqueXrefName)).thenReturn(refEntity1);

		// actual tests
		List<Entity> entities = Arrays.asList(entity0);
		repositoryValidationDecorator.update(entities.stream());

		ArgumentCaptor<Stream<Entity>> captor = ArgumentCaptor.forClass(Stream.class);
		verify(decoratedRepo, times(1)).update(captor.capture());
		Stream<Entity> stream = captor.getValue();
		try
		{
			stream.collect(Collectors.toList()); // process stream to enable validation

			throw new RuntimeException("Expected MolgenisValidationException instead of no exception");
		}
		catch (MolgenisValidationException e)
		{
			verify(entityAttributesValidator, times(1)).validate(entity0, entityType);
			assertEquals(e.getMessage(),
					"Duplicate value 'unique0' for unique attribute 'uniqueStringAttr' from entity 'entity' (entity 1)");
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void updateStreamUniqueValueWithExpression()
	{
		when(uniqueStringAttr.getExpression()).thenReturn("expr");

		Entity entityInBackend0 = mock(Entity.class);
		when(entityInBackend0.getEntityType()).thenReturn(entityType);

		when(entityInBackend0.getIdValue()).thenReturn("idbackend0");
		when(entityInBackend0.getEntity(attrXrefName)).thenReturn(refEntity0);
		when(entityInBackend0.getEntity(attrNillableXrefName)).thenReturn(null);
		when(entityInBackend0.getEntities(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entityInBackend0.getEntities(attrNillableMrefName)).thenReturn(emptyList());
		when(entityInBackend0.getString(attrUniqueStringName)).thenReturn("unique0");
		when(entityInBackend0.getEntity(attrUniqueXrefName)).thenReturn(refEntity0);

		when(entityInBackend0.get(attrIdName)).thenReturn("idbackend0");
		when(entityInBackend0.get(attrXrefName)).thenReturn(refEntity0);
		when(entityInBackend0.get(attrNillableXrefName)).thenReturn(null);
		when(entityInBackend0.get(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entityInBackend0.get(attrNillableMrefName)).thenReturn(emptyList());
		when(entityInBackend0.get(attrUniqueStringName)).thenReturn("unique0");
		when(entityInBackend0.get(attrUniqueXrefName)).thenReturn(refEntity0);

		when(decoratedRepo.findAll(new QueryImpl<>().fetch(new Fetch().field(attrIdName)))).thenReturn(
				Stream.of(entityInBackend0));

		// entities
		Entity entity0 = mock(Entity.class);
		when(entity0.getEntityType()).thenReturn(entityType);

		when(entity0.getIdValue()).thenReturn("id0");
		when(entity0.getEntity(attrXrefName)).thenReturn(refEntity0);
		when(entity0.getEntity(attrNillableXrefName)).thenReturn(null);
		when(entity0.getEntities(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.getEntities(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.getString(attrUniqueStringName)).thenReturn("unique0"); // valid, because value is 'computed'
		when(entity0.getEntity(attrUniqueXrefName)).thenReturn(refEntity1);

		when(entity0.get(attrIdName)).thenReturn("id0");
		when(entity0.get(attrXrefName)).thenReturn(refEntity0);
		when(entity0.get(attrNillableXrefName)).thenReturn(null);
		when(entity0.get(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.get(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.get(attrUniqueStringName)).thenReturn("unique0"); // valid, because value is 'computed'
		when(entity0.get(attrUniqueXrefName)).thenReturn(refEntity1);

		// actual tests
		List<Entity> entities = Arrays.asList(entity0);
		repositoryValidationDecorator.update(entities.stream());

		ArgumentCaptor<Stream<Entity>> captor = ArgumentCaptor.forClass(Stream.class);
		verify(decoratedRepo, times(1)).update(captor.capture());
		Stream<Entity> stream = captor.getValue();

		stream.collect(Collectors.toList()); // process stream to enable validation
		verify(entityAttributesValidator, times(1)).validate(entity0, entityType);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void updateStreamUniqueStringValueExistsInSourceValidationError()
	{
		// entities
		Entity entity0 = mock(Entity.class);
		when(entity0.getEntityType()).thenReturn(entityType);

		when(entity0.getIdValue()).thenReturn("id0");
		when(entity0.getEntity(attrXrefName)).thenReturn(refEntity0);
		when(entity0.getEntity(attrNillableXrefName)).thenReturn(null);
		when(entity0.getEntities(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.getEntities(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.getString(attrUniqueStringName)).thenReturn("unique0");
		when(entity0.getEntity(attrUniqueXrefName)).thenReturn(refEntity0);

		when(entity0.get(attrIdName)).thenReturn("id0");
		when(entity0.get(attrXrefName)).thenReturn(refEntity0);
		when(entity0.get(attrNillableXrefName)).thenReturn(null);
		when(entity0.get(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.get(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.get(attrUniqueStringName)).thenReturn("unique0");
		when(entity0.get(attrUniqueXrefName)).thenReturn(refEntity0);

		Entity entity1 = mock(Entity.class);
		when(entity1.getEntityType()).thenReturn(entityType);

		when(entity1.getIdValue()).thenReturn("id1");
		when(entity1.getEntity(attrXrefName)).thenReturn(refEntity0);
		when(entity1.getEntity(attrNillableXrefName)).thenReturn(null);
		when(entity1.getEntities(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity1.getEntities(attrNillableMrefName)).thenReturn(emptyList());
		when(entity1.getString(attrUniqueStringName)).thenReturn("unique0"); // duplicate
		when(entity1.getEntity(attrUniqueXrefName)).thenReturn(refEntity1);

		when(entity1.get(attrIdName)).thenReturn("id1");
		when(entity1.get(attrXrefName)).thenReturn(refEntity0);
		when(entity1.get(attrNillableXrefName)).thenReturn(null);
		when(entity1.get(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity1.get(attrNillableMrefName)).thenReturn(emptyList());
		when(entity1.get(attrUniqueStringName)).thenReturn("unique0"); // duplicate
		when(entity1.get(attrUniqueXrefName)).thenReturn(refEntity1);

		// actual tests
		List<Entity> entities = Arrays.asList(entity0, entity1);
		repositoryValidationDecorator.update(entities.stream());

		ArgumentCaptor<Stream<Entity>> captor = ArgumentCaptor.forClass(Stream.class);
		verify(decoratedRepo, times(1)).update(captor.capture());
		Stream<Entity> stream = captor.getValue();
		try
		{
			stream.collect(Collectors.toList()); // process stream to enable validation

			throw new RuntimeException("Expected MolgenisValidationException instead of no exception");
		}
		catch (MolgenisValidationException e)
		{
			verify(entityAttributesValidator, times(1)).validate(entity0, entityType);
			verify(entityAttributesValidator, times(1)).validate(entity1, entityType);
			assertEquals(e.getMessage(),
					"Duplicate value 'unique0' for unique attribute 'uniqueStringAttr' from entity 'entity' (entity 2)");
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void updateStreamUniqueXrefValueExistsInBackendValidationError()
	{
		Entity entityInBackend0 = mock(Entity.class);
		when(entityInBackend0.getEntityType()).thenReturn(entityType);

		when(entityInBackend0.getIdValue()).thenReturn("idbackend0");
		when(entityInBackend0.getEntity(attrXrefName)).thenReturn(refEntity0);
		when(entityInBackend0.getEntity(attrNillableXrefName)).thenReturn(null);
		when(entityInBackend0.getEntities(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entityInBackend0.getEntities(attrNillableMrefName)).thenReturn(emptyList());
		when(entityInBackend0.getString(attrUniqueStringName)).thenReturn("unique0");
		when(entityInBackend0.getEntity(attrUniqueXrefName)).thenReturn(refEntity0);

		when(entityInBackend0.get(attrIdName)).thenReturn("idbackend0");
		when(entityInBackend0.get(attrXrefName)).thenReturn(refEntity0);
		when(entityInBackend0.get(attrNillableXrefName)).thenReturn(null);
		when(entityInBackend0.get(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entityInBackend0.get(attrNillableMrefName)).thenReturn(emptyList());
		when(entityInBackend0.get(attrUniqueStringName)).thenReturn("unique0");
		when(entityInBackend0.get(attrUniqueXrefName)).thenReturn(refEntity0);

		when(decoratedRepo.findAll(new QueryImpl<>().fetch(new Fetch().field(attrIdName)))).thenReturn(
				Stream.of(entityInBackend0));

		// entities
		Entity entity0 = mock(Entity.class);
		when(entity0.getEntityType()).thenReturn(entityType);

		when(entity0.getIdValue()).thenReturn("id0");
		when(entity0.getEntity(attrXrefName)).thenReturn(refEntity0);
		when(entity0.getEntity(attrNillableXrefName)).thenReturn(null);
		when(entity0.getEntities(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.getEntities(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.getString(attrUniqueStringName)).thenReturn("unique1");
		when(entity0.getEntity(attrUniqueXrefName)).thenReturn(refEntity0); // duplicate

		when(entity0.get(attrIdName)).thenReturn("id0");
		when(entity0.get(attrXrefName)).thenReturn(refEntity0);
		when(entity0.get(attrNillableXrefName)).thenReturn(null);
		when(entity0.get(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.get(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.get(attrUniqueStringName)).thenReturn("unique1");
		when(entity0.get(attrUniqueXrefName)).thenReturn(refEntity0); // duplicate

		// actual tests
		List<Entity> entities = Arrays.asList(entity0);
		repositoryValidationDecorator.update(entities.stream());

		ArgumentCaptor<Stream<Entity>> captor = ArgumentCaptor.forClass(Stream.class);
		verify(decoratedRepo, times(1)).update(captor.capture());
		Stream<Entity> stream = captor.getValue();
		try
		{
			stream.collect(Collectors.toList()); // process stream to enable validation

			throw new RuntimeException("Expected MolgenisValidationException instead of no exception");
		}
		catch (MolgenisValidationException e)
		{
			verify(entityAttributesValidator, times(1)).validate(entity0, entityType);
			assertEquals(e.getMessage(),
					"Duplicate value 'idref0' for unique attribute 'uniqueXrefAttr' from entity 'entity' (entity 1)");
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void updateStreamReadOnlyStringAttrValidationError()
	{
		String attrReadonlyStringName = "readonlyStringAttr";

		Attribute readonlyStringAttr = when(mock(Attribute.class).getName()).thenReturn(attrReadonlyStringName)
																			.getMock();
		when(readonlyStringAttr.getDataType()).thenReturn(STRING);
		when(readonlyStringAttr.isReadOnly()).thenReturn(true);

		when(entityType.getAttribute(attrReadonlyStringName)).thenReturn(readonlyStringAttr);
		when(entityType.getAtomicAttributes()).thenReturn(
				Arrays.asList(idAttr, xrefAttr, nillableXrefAttr, mrefAttr, nillableMrefAttr, uniqueStringAttr,
						uniqueXrefAttr, readonlyStringAttr));

		// entities
		Entity entity0 = mock(Entity.class);
		when(entity0.getEntityType()).thenReturn(entityType);

		when(entity0.getIdValue()).thenReturn("id0");
		when(entity0.getEntity(attrXrefName)).thenReturn(refEntity0);
		when(entity0.getEntity(attrNillableXrefName)).thenReturn(null);
		when(entity0.getEntities(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.getEntities(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.getString(attrUniqueStringName)).thenReturn("unique1");
		when(entity0.getEntity(attrUniqueXrefName)).thenReturn(refEntity0);
		when(entity0.getString(attrReadonlyStringName)).thenReturn("str0");
		when(entity0.get(attrIdName)).thenReturn("id0");
		when(entity0.get(attrXrefName)).thenReturn(refEntity0);
		when(entity0.get(attrNillableXrefName)).thenReturn(null);
		when(entity0.get(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.get(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.get(attrUniqueStringName)).thenReturn("unique1");
		when(entity0.get(attrUniqueXrefName)).thenReturn(refEntity0);
		when(entity0.get(attrReadonlyStringName)).thenReturn("str0");

		when(decoratedRepo.findOneById("id0")).thenReturn(entity0);

		Entity updatedEntity0 = mock(Entity.class);
		when(updatedEntity0.getEntityType()).thenReturn(entityType);

		when(updatedEntity0.getIdValue()).thenReturn("id0");
		when(updatedEntity0.getEntity(attrXrefName)).thenReturn(refEntity0Clone);
		when(updatedEntity0.getEntity(attrNillableXrefName)).thenReturn(null);
		when(updatedEntity0.getEntities(attrMrefName)).thenReturn(Arrays.asList(refEntity0Clone));
		when(updatedEntity0.getEntities(attrNillableMrefName)).thenReturn(emptyList());
		when(updatedEntity0.getString(attrUniqueStringName)).thenReturn("unique1");
		when(updatedEntity0.getEntity(attrUniqueXrefName)).thenReturn(refEntity0Clone);
		when(updatedEntity0.getString(attrReadonlyStringName)).thenReturn("updatedstr0"); // read only attribute update
		when(updatedEntity0.get(attrIdName)).thenReturn("id0");
		when(updatedEntity0.get(attrXrefName)).thenReturn(refEntity0Clone);
		when(updatedEntity0.get(attrNillableXrefName)).thenReturn(null);
		when(updatedEntity0.get(attrMrefName)).thenReturn(Arrays.asList(refEntity0Clone));
		when(updatedEntity0.get(attrNillableMrefName)).thenReturn(emptyList());
		when(updatedEntity0.get(attrUniqueStringName)).thenReturn("unique1");
		when(updatedEntity0.get(attrUniqueXrefName)).thenReturn(refEntity0Clone);
		when(updatedEntity0.get(attrReadonlyStringName)).thenReturn("updatedstr0"); // read only attribute update

		// actual tests
		List<Entity> updatedEntities = Arrays.asList(updatedEntity0);
		repositoryValidationDecorator.update(updatedEntities.stream());

		ArgumentCaptor<Stream<Entity>> captor = ArgumentCaptor.forClass(Stream.class);
		verify(decoratedRepo, times(1)).update(captor.capture());
		Stream<Entity> stream = captor.getValue();
		try
		{
			stream.collect(Collectors.toList()); // process stream to enable validation

			throw new RuntimeException("Expected MolgenisValidationException instead of no exception");
		}
		catch (MolgenisValidationException e)
		{
			verify(entityAttributesValidator, times(1)).validate(updatedEntity0, entityType);
			assertEquals(e.getMessage(),
					"The attribute 'readonlyStringAttr' of entity 'entity' can not be changed it is readonly. (entity 1)");
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void updateStreamReadOnlyXrefAttr()
	{
		String attrReadonlyXrefName = "readonlyXrefAttr";

		Attribute readonlyXrefAttr = when(mock(Attribute.class).getName()).thenReturn(attrReadonlyXrefName).getMock();
		when(readonlyXrefAttr.getDataType()).thenReturn(XREF);
		when(readonlyXrefAttr.getRefEntity()).thenReturn(refEntityType);
		when(readonlyXrefAttr.isReadOnly()).thenReturn(true);

		when(entityType.getAttribute(attrReadonlyXrefName)).thenReturn(readonlyXrefAttr);
		when(entityType.getAtomicAttributes()).thenReturn(
				Arrays.asList(idAttr, xrefAttr, nillableXrefAttr, mrefAttr, nillableMrefAttr, uniqueStringAttr,
						uniqueXrefAttr, readonlyXrefAttr));

		// entities
		Entity entity0 = mock(Entity.class);
		when(entity0.getEntityType()).thenReturn(entityType);

		when(entity0.getIdValue()).thenReturn("id0");
		when(entity0.getEntity(attrXrefName)).thenReturn(refEntity0);
		when(entity0.getEntity(attrNillableXrefName)).thenReturn(null);
		when(entity0.getEntities(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.getEntities(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.getString(attrUniqueStringName)).thenReturn("unique1");
		when(entity0.getEntity(attrUniqueXrefName)).thenReturn(refEntity0);
		when(entity0.getEntity(attrReadonlyXrefName)).thenReturn(refEntity0);
		when(entity0.get(attrIdName)).thenReturn("id0");
		when(entity0.get(attrXrefName)).thenReturn(refEntity0);
		when(entity0.get(attrNillableXrefName)).thenReturn(null);
		when(entity0.get(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.get(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.get(attrUniqueStringName)).thenReturn("unique1");
		when(entity0.get(attrUniqueXrefName)).thenReturn(refEntity0);
		when(entity0.get(attrReadonlyXrefName)).thenReturn(refEntity0);

		when(decoratedRepo.findOneById("id0")).thenReturn(entity0);

		Entity updatedEntity0 = mock(Entity.class);
		when(updatedEntity0.getEntityType()).thenReturn(entityType);

		when(updatedEntity0.getIdValue()).thenReturn("id0");
		when(updatedEntity0.getEntity(attrXrefName)).thenReturn(refEntity0Clone);
		when(updatedEntity0.getEntity(attrNillableXrefName)).thenReturn(null);
		when(updatedEntity0.getEntities(attrMrefName)).thenReturn(Arrays.asList(refEntity0Clone));
		when(updatedEntity0.getEntities(attrNillableMrefName)).thenReturn(emptyList());
		when(updatedEntity0.getString(attrUniqueStringName)).thenReturn("unique1");
		when(updatedEntity0.getEntity(attrUniqueXrefName)).thenReturn(refEntity0Clone);
		when(updatedEntity0.getEntity(attrReadonlyXrefName)).thenReturn(refEntity0Clone); // read only, no changes
		when(updatedEntity0.get(attrIdName)).thenReturn("id0");
		when(updatedEntity0.get(attrXrefName)).thenReturn(refEntity0Clone);
		when(updatedEntity0.get(attrNillableXrefName)).thenReturn(null);
		when(updatedEntity0.get(attrMrefName)).thenReturn(Arrays.asList(refEntity0Clone));
		when(updatedEntity0.get(attrNillableMrefName)).thenReturn(emptyList());
		when(updatedEntity0.get(attrUniqueStringName)).thenReturn("unique1");
		when(updatedEntity0.get(attrUniqueXrefName)).thenReturn(refEntity0Clone);
		when(updatedEntity0.get(attrReadonlyXrefName)).thenReturn(refEntity0Clone); // read only, no changes

		// actual tests
		List<Entity> entities = Arrays.asList(updatedEntity0);
		repositoryValidationDecorator.update(entities.stream());

		ArgumentCaptor<Stream<Entity>> captor = ArgumentCaptor.forClass(Stream.class);
		verify(decoratedRepo, times(1)).update(captor.capture());
		Stream<Entity> stream = captor.getValue();

		stream.collect(Collectors.toList()); // process stream to enable validation
		verify(entityAttributesValidator, times(1)).validate(updatedEntity0, entityType);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void updateStreamReadOnlyXrefAttrValidationError()
	{
		String attrReadonlyXrefName = "readonlyXrefAttr";

		Attribute readonlyXrefAttr = when(mock(Attribute.class).getName()).thenReturn(attrReadonlyXrefName).getMock();
		when(readonlyXrefAttr.getDataType()).thenReturn(XREF);
		when(readonlyXrefAttr.getRefEntity()).thenReturn(refEntityType);
		when(readonlyXrefAttr.isReadOnly()).thenReturn(true);

		when(entityType.getAttribute(attrReadonlyXrefName)).thenReturn(readonlyXrefAttr);
		when(entityType.getAtomicAttributes()).thenReturn(
				Arrays.asList(idAttr, xrefAttr, nillableXrefAttr, mrefAttr, nillableMrefAttr, uniqueStringAttr,
						uniqueXrefAttr, readonlyXrefAttr));

		// entities
		Entity entity0 = mock(Entity.class);
		when(entity0.getEntityType()).thenReturn(entityType);

		when(entity0.getIdValue()).thenReturn("id0");
		when(entity0.getEntity(attrXrefName)).thenReturn(refEntity0);
		when(entity0.getEntity(attrNillableXrefName)).thenReturn(null);
		when(entity0.getEntities(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.getEntities(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.getString(attrUniqueStringName)).thenReturn("unique1");
		when(entity0.getEntity(attrUniqueXrefName)).thenReturn(refEntity0);
		when(entity0.getEntity(attrReadonlyXrefName)).thenReturn(refEntity0);
		when(entity0.get(attrIdName)).thenReturn("id0");
		when(entity0.get(attrXrefName)).thenReturn(refEntity0);
		when(entity0.get(attrNillableXrefName)).thenReturn(null);
		when(entity0.get(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.get(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.get(attrUniqueStringName)).thenReturn("unique1");
		when(entity0.get(attrUniqueXrefName)).thenReturn(refEntity0);
		when(entity0.get(attrReadonlyXrefName)).thenReturn(refEntity0);

		when(decoratedRepo.findOneById("id0")).thenReturn(entity0);

		Entity updatedEntity0 = mock(Entity.class);
		when(updatedEntity0.getEntityType()).thenReturn(entityType);

		when(updatedEntity0.getIdValue()).thenReturn("id0");
		when(updatedEntity0.getEntity(attrXrefName)).thenReturn(refEntity0Clone);
		when(updatedEntity0.getEntity(attrNillableXrefName)).thenReturn(null);
		when(updatedEntity0.getEntities(attrMrefName)).thenReturn(Arrays.asList(refEntity0Clone));
		when(updatedEntity0.getEntities(attrNillableMrefName)).thenReturn(emptyList());
		when(updatedEntity0.getString(attrUniqueStringName)).thenReturn("unique1");
		when(updatedEntity0.getEntity(attrUniqueXrefName)).thenReturn(refEntity0Clone);
		when(updatedEntity0.getEntity(attrReadonlyXrefName)).thenReturn(refEntity1); // read only attribute update
		when(updatedEntity0.get(attrIdName)).thenReturn("id0");
		when(updatedEntity0.get(attrXrefName)).thenReturn(refEntity0Clone);
		when(updatedEntity0.get(attrNillableXrefName)).thenReturn(null);
		when(updatedEntity0.get(attrMrefName)).thenReturn(Arrays.asList(refEntity0Clone));
		when(updatedEntity0.get(attrNillableMrefName)).thenReturn(emptyList());
		when(updatedEntity0.get(attrUniqueStringName)).thenReturn("unique1");
		when(updatedEntity0.get(attrUniqueXrefName)).thenReturn(refEntity0Clone);
		when(updatedEntity0.get(attrReadonlyXrefName)).thenReturn(refEntity1); // read only attribute update

		// actual tests
		List<Entity> updatedEntities = Arrays.asList(updatedEntity0);
		repositoryValidationDecorator.update(updatedEntities.stream());

		ArgumentCaptor<Stream<Entity>> captor = ArgumentCaptor.forClass(Stream.class);
		verify(decoratedRepo, times(1)).update(captor.capture());
		Stream<Entity> stream = captor.getValue();
		try
		{
			stream.collect(Collectors.toList()); // process stream to enable validation

			throw new RuntimeException("Expected MolgenisValidationException instead of no exception");
		}
		catch (MolgenisValidationException e)
		{
			verify(entityAttributesValidator, times(1)).validate(updatedEntity0, entityType);
			assertEquals(e.getMessage(),
					"The attribute 'readonlyXrefAttr' of entity 'entity' can not be changed it is readonly. (entity 1)");
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void updateStreamReadOnlyMrefAttr()
	{
		String attrReadonlyMrefName = "readonlyMrefAttr";

		Attribute readonlyMrefAttr = when(mock(Attribute.class).getName()).thenReturn(attrReadonlyMrefName).getMock();
		when(readonlyMrefAttr.getDataType()).thenReturn(MREF);
		when(readonlyMrefAttr.getRefEntity()).thenReturn(refEntityType);
		when(readonlyMrefAttr.isReadOnly()).thenReturn(true);

		when(entityType.getAttribute(attrReadonlyMrefName)).thenReturn(readonlyMrefAttr);
		when(entityType.getAtomicAttributes()).thenReturn(
				Arrays.asList(idAttr, xrefAttr, nillableXrefAttr, mrefAttr, nillableMrefAttr, uniqueStringAttr,
						uniqueXrefAttr, readonlyMrefAttr));

		// entities
		Entity entity0 = mock(Entity.class);
		when(entity0.getEntityType()).thenReturn(entityType);

		when(entity0.getIdValue()).thenReturn("id0");
		when(entity0.getEntity(attrXrefName)).thenReturn(refEntity0);
		when(entity0.getEntity(attrNillableXrefName)).thenReturn(null);
		when(entity0.getEntities(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.getEntities(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.getString(attrUniqueStringName)).thenReturn("unique1");
		when(entity0.getEntity(attrUniqueXrefName)).thenReturn(refEntity0);
		when(entity0.getEntities(attrReadonlyMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.get(attrIdName)).thenReturn("id0");
		when(entity0.get(attrXrefName)).thenReturn(refEntity0);
		when(entity0.get(attrNillableXrefName)).thenReturn(null);
		when(entity0.get(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.get(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.get(attrUniqueStringName)).thenReturn("unique1");
		when(entity0.get(attrUniqueXrefName)).thenReturn(refEntity0);
		when(entity0.get(attrReadonlyMrefName)).thenReturn(Arrays.asList(refEntity0));

		when(decoratedRepo.findOneById("id0")).thenReturn(entity0);

		Entity updatedEntity0 = mock(Entity.class);
		when(updatedEntity0.getEntityType()).thenReturn(entityType);

		when(updatedEntity0.getIdValue()).thenReturn("id0");
		when(updatedEntity0.getEntity(attrXrefName)).thenReturn(refEntity0Clone);
		when(updatedEntity0.getEntity(attrNillableXrefName)).thenReturn(null);
		when(updatedEntity0.getEntities(attrMrefName)).thenReturn(Arrays.asList(refEntity0Clone));
		when(updatedEntity0.getEntities(attrNillableMrefName)).thenReturn(emptyList());
		when(updatedEntity0.getString(attrUniqueStringName)).thenReturn("unique1");
		when(updatedEntity0.getEntity(attrUniqueXrefName)).thenReturn(refEntity0Clone);
		when(updatedEntity0.getEntities(attrReadonlyMrefName)).thenReturn(Arrays.asList(refEntity0Clone)); // read only,
		// no
		// changes
		when(updatedEntity0.get(attrIdName)).thenReturn("id0");
		when(updatedEntity0.get(attrXrefName)).thenReturn(refEntity0Clone);
		when(updatedEntity0.get(attrNillableXrefName)).thenReturn(null);
		when(updatedEntity0.get(attrMrefName)).thenReturn(Arrays.asList(refEntity0Clone));
		when(updatedEntity0.get(attrNillableMrefName)).thenReturn(emptyList());
		when(updatedEntity0.get(attrUniqueStringName)).thenReturn("unique1");
		when(updatedEntity0.get(attrUniqueXrefName)).thenReturn(refEntity0Clone);
		when(updatedEntity0.get(attrReadonlyMrefName)).thenReturn(Arrays.asList(refEntity0Clone)); // read only, no
		// changes
		// actual tests
		List<Entity> entities = Arrays.asList(updatedEntity0);
		repositoryValidationDecorator.update(entities.stream());

		ArgumentCaptor<Stream<Entity>> captor = ArgumentCaptor.forClass(Stream.class);
		verify(decoratedRepo, times(1)).update(captor.capture());
		Stream<Entity> stream = captor.getValue();

		stream.collect(Collectors.toList()); // process stream to enable validation
		verify(entityAttributesValidator, times(1)).validate(updatedEntity0, entityType);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void updateStreamReadOnlyMrefAttrValidationError()
	{
		String attrReadonlyMrefName = "readonlyMrefAttr";

		Attribute readonlyMrefAttr = when(mock(Attribute.class).getName()).thenReturn(attrReadonlyMrefName).getMock();
		when(readonlyMrefAttr.getDataType()).thenReturn(MREF);
		when(readonlyMrefAttr.getRefEntity()).thenReturn(refEntityType);
		when(readonlyMrefAttr.isReadOnly()).thenReturn(true);

		when(entityType.getAttribute(attrReadonlyMrefName)).thenReturn(readonlyMrefAttr);
		when(entityType.getAtomicAttributes()).thenReturn(
				Arrays.asList(idAttr, xrefAttr, nillableXrefAttr, mrefAttr, nillableMrefAttr, uniqueStringAttr,
						uniqueXrefAttr, readonlyMrefAttr));

		// entities
		Entity entity0 = mock(Entity.class);
		when(entity0.getEntityType()).thenReturn(entityType);

		when(entity0.getIdValue()).thenReturn("id0");
		when(entity0.getEntity(attrXrefName)).thenReturn(refEntity0);
		when(entity0.getEntity(attrNillableXrefName)).thenReturn(null);
		when(entity0.getEntities(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.getEntities(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.getString(attrUniqueStringName)).thenReturn("unique1");
		when(entity0.getEntity(attrUniqueXrefName)).thenReturn(refEntity0);
		when(entity0.getEntities(attrReadonlyMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.get(attrIdName)).thenReturn("id0");
		when(entity0.get(attrXrefName)).thenReturn(refEntity0);
		when(entity0.get(attrNillableXrefName)).thenReturn(null);
		when(entity0.get(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.get(attrNillableMrefName)).thenReturn(emptyList());
		when(entity0.get(attrUniqueStringName)).thenReturn("unique1");
		when(entity0.get(attrUniqueXrefName)).thenReturn(refEntity0);
		when(entity0.get(attrReadonlyMrefName)).thenReturn(Arrays.asList(refEntity0));

		when(decoratedRepo.findOneById("id0")).thenReturn(entity0);

		Entity updatedEntity0 = mock(Entity.class);
		when(updatedEntity0.getEntityType()).thenReturn(entityType);

		when(updatedEntity0.getIdValue()).thenReturn("id0");
		when(updatedEntity0.getEntity(attrXrefName)).thenReturn(refEntity0Clone);
		when(updatedEntity0.getEntity(attrNillableXrefName)).thenReturn(null);
		when(updatedEntity0.getEntities(attrMrefName)).thenReturn(Arrays.asList(refEntity0Clone));
		when(updatedEntity0.getEntities(attrNillableMrefName)).thenReturn(emptyList());
		when(updatedEntity0.getString(attrUniqueStringName)).thenReturn("unique1");
		when(updatedEntity0.getEntity(attrUniqueXrefName)).thenReturn(refEntity0Clone);
		when(updatedEntity0.getEntities(attrReadonlyMrefName)).thenReturn(Arrays.asList(refEntity1)); // read only
		// attribute
		// update
		when(updatedEntity0.get(attrIdName)).thenReturn("id0");
		when(updatedEntity0.get(attrXrefName)).thenReturn(refEntity0Clone);
		when(updatedEntity0.get(attrNillableXrefName)).thenReturn(null);
		when(updatedEntity0.get(attrMrefName)).thenReturn(Arrays.asList(refEntity0Clone));
		when(updatedEntity0.get(attrNillableMrefName)).thenReturn(emptyList());
		when(updatedEntity0.get(attrUniqueStringName)).thenReturn("unique1");
		when(updatedEntity0.get(attrUniqueXrefName)).thenReturn(refEntity0Clone);
		when(updatedEntity0.get(attrReadonlyMrefName)).thenReturn(Arrays.asList(refEntity1)); // read only attribute
		// update

		// actual tests
		List<Entity> updatedEntities = Arrays.asList(updatedEntity0);
		repositoryValidationDecorator.update(updatedEntities.stream());

		ArgumentCaptor<Stream<Entity>> captor = ArgumentCaptor.forClass(Stream.class);
		verify(decoratedRepo, times(1)).update(captor.capture());
		Stream<Entity> stream = captor.getValue();
		try
		{
			stream.collect(Collectors.toList()); // process stream to enable validation

			throw new RuntimeException("Expected MolgenisValidationException instead of no exception");
		}
		catch (MolgenisValidationException e)
		{
			verify(entityAttributesValidator, times(1)).validate(updatedEntity0, entityType);
			assertEquals(e.getMessage(),
					"The attribute 'readonlyMrefAttr' of entity 'entity' can not be changed it is readonly. (entity 1)");
		}
	}

	@Test
	public void findAllStream()
	{
		Object id0 = "id0";
		Object id1 = "id1";
		Entity entity0 = mock(Entity.class);
		Entity entity1 = mock(Entity.class);
		Stream<Object> entityIds = Stream.of(id0, id1);
		when(decoratedRepo.findAll(entityIds)).thenReturn(Stream.of(entity0, entity1));
		Stream<Entity> expectedEntities = repositoryValidationDecorator.findAll(entityIds);
		assertEquals(expectedEntities.collect(Collectors.toList()), Arrays.asList(entity0, entity1));
	}

	@Test
	public void findAllStreamFetch()
	{
		Fetch fetch = new Fetch();
		Object id0 = "id0";
		Object id1 = "id1";
		Entity entity0 = mock(Entity.class);
		Entity entity1 = mock(Entity.class);
		Stream<Object> entityIds = Stream.of(id0, id1);
		when(decoratedRepo.findAll(entityIds, fetch)).thenReturn(Stream.of(entity0, entity1));
		Stream<Entity> expectedEntities = repositoryValidationDecorator.findAll(entityIds, fetch);
		assertEquals(expectedEntities.collect(Collectors.toList()), Arrays.asList(entity0, entity1));
	}

	@Test
	public void findOneObjectFetch()
	{
		DataService dataService = mock(DataService.class);
		EntityType entityType = mock(EntityType.class);
		@SuppressWarnings("unchecked")
		Repository<Entity> decoratedRepository = mock(Repository.class);
		when(decoratedRepository.getEntityType()).thenReturn(entityType);
		EntityAttributesValidator entityAttributesValidator = mock(EntityAttributesValidator.class);

		@SuppressWarnings("resource")
		RepositoryValidationDecorator myRepositoryValidationDecorator = new RepositoryValidationDecorator(dataService,
				decoratedRepository, entityAttributesValidator, expressionValidator);

		Object id = 0;
		Fetch fetch = new Fetch();
		Entity entity = mock(Entity.class);
		when(decoratedRepository.findOneById(id, fetch)).thenReturn(entity);
		assertEquals(entity, myRepositoryValidationDecorator.findOneById(id, fetch));
		verify(decoratedRepository, times(1)).findOneById(id, fetch);
	}

	@Test
	public void findAll()
	{
		Entity entity0 = mock(Entity.class);
		@SuppressWarnings("unchecked")
		Query<Entity> query = mock(Query.class);
		when(decoratedRepo.findAll(query)).thenReturn(Stream.of(entity0));
		Stream<Entity> entities = repositoryValidationDecorator.findAll(query);
		assertEquals(entities.collect(Collectors.toList()), Arrays.asList(entity0));
	}

	@Test
	public void forEachBatchedFetch()
	{
		Fetch fetch = new Fetch();
		@SuppressWarnings("unchecked")
		Consumer<List<Entity>> consumer = mock(Consumer.class);
		repositoryValidationDecorator.forEachBatched(fetch, consumer, 234);
		verify(decoratedRepo, times(1)).forEachBatched(fetch, consumer, 234);
	}
}

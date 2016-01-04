package org.molgenis.data.validation;

import static java.util.Collections.emptyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.MolgenisFieldTypes.MREF;
import static org.molgenis.MolgenisFieldTypes.STRING;
import static org.molgenis.MolgenisFieldTypes.XREF;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.mockito.ArgumentCaptor;
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Fetch;
import org.molgenis.data.Repository;
import org.molgenis.data.support.DataServiceImpl;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.NonDecoratingRepositoryDecoratorFactory;
import org.molgenis.data.support.QueryImpl;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;

public class RepositoryValidationDecoratorTest
{
	private RepositoryValidationDecorator repositoryValidationDecorator;
	private Repository decoratedRepository;

	@BeforeMethod
	public void beforeMethod()
	{
		decoratedRepository = mock(Repository.class);
		repositoryValidationDecorator = new RepositoryValidationDecorator(
				new DataServiceImpl(new NonDecoratingRepositoryDecoratorFactory()), decoratedRepository,
				new EntityAttributesValidator());
	}

	// entity attributes validation
	// unique validation
	// reference validation
	// nillable validation
	// read-only validation
	@SuppressWarnings(
	{ "rawtypes", "unchecked", "resource" })
	@Test
	public void addStream()
	{
		// ref entity meta
		String refEntityName = "refEntity";

		String refAttrIdName = "refId";

		AttributeMetaData refIdAttr = when(mock(AttributeMetaData.class).getName()).thenReturn(refAttrIdName).getMock();
		when(refIdAttr.getDataType()).thenReturn(STRING);

		EntityMetaData refEntityMeta = mock(EntityMetaData.class);
		when(refEntityMeta.getName()).thenReturn(refEntityName);
		when(refEntityMeta.getIdAttribute()).thenReturn(refIdAttr);
		when(refEntityMeta.getAtomicAttributes()).thenReturn(Arrays.asList(refIdAttr));

		// ref entities
		String refEntity0Id = "id0";
		Entity refEntity0 = mock(Entity.class);
		when(refEntity0.getEntityMetaData()).thenReturn(refEntityMeta);
		when(refEntity0.getIdValue()).thenReturn(refEntity0Id);
		when(refEntity0.get(refAttrIdName)).thenReturn(refEntity0Id);
		when(refEntity0.getString(refAttrIdName)).thenReturn(refEntity0Id);

		List<Entity> refEntities = Arrays.asList(refEntity0);

		// entity meta
		String entityName = "entity";

		String attrIdName = "id";
		String attrXrefName = "xrefAttr";
		String attrNillableXrefName = "nillableXrefAttr";
		String attrMrefName = "mrefAttr";
		String attrNillableMrefName = "nillableMrefAttr";

		AttributeMetaData idAttr = when(mock(AttributeMetaData.class).getName()).thenReturn(attrIdName).getMock();
		when(idAttr.getDataType()).thenReturn(STRING);

		AttributeMetaData xrefAttr = when(mock(AttributeMetaData.class).getName()).thenReturn(attrXrefName).getMock();
		when(xrefAttr.getRefEntity()).thenReturn(refEntityMeta);
		when(xrefAttr.getDataType()).thenReturn(XREF);
		when(xrefAttr.isNillable()).thenReturn(false);

		AttributeMetaData nillableXrefAttr = when(mock(AttributeMetaData.class).getName())
				.thenReturn(attrNillableXrefName).getMock();
		when(nillableXrefAttr.getRefEntity()).thenReturn(refEntityMeta);
		when(nillableXrefAttr.getDataType()).thenReturn(XREF);
		when(nillableXrefAttr.isNillable()).thenReturn(true);

		AttributeMetaData mrefAttr = when(mock(AttributeMetaData.class).getName()).thenReturn(attrMrefName).getMock();
		when(mrefAttr.getRefEntity()).thenReturn(refEntityMeta);
		when(mrefAttr.getDataType()).thenReturn(MREF);
		when(mrefAttr.isNillable()).thenReturn(false);

		AttributeMetaData nillableMrefAttr = when(mock(AttributeMetaData.class).getName())
				.thenReturn(attrNillableMrefName).getMock();
		when(nillableMrefAttr.getRefEntity()).thenReturn(refEntityMeta);
		when(nillableMrefAttr.getDataType()).thenReturn(MREF);
		when(nillableMrefAttr.isNillable()).thenReturn(true);

		EntityMetaData entityMeta = mock(EntityMetaData.class);
		when(entityMeta.getName()).thenReturn(entityName);
		when(entityMeta.getIdAttribute()).thenReturn(idAttr);
		when(entityMeta.getAttribute(attrIdName)).thenReturn(idAttr);
		when(entityMeta.getAttribute(attrXrefName)).thenReturn(xrefAttr);
		when(entityMeta.getAttribute(attrNillableXrefName)).thenReturn(nillableXrefAttr);
		when(entityMeta.getAttribute(attrMrefName)).thenReturn(mrefAttr);
		when(entityMeta.getAttribute(attrNillableMrefName)).thenReturn(nillableMrefAttr);
		when(entityMeta.getAtomicAttributes())
				.thenReturn(Arrays.asList(idAttr, xrefAttr, nillableXrefAttr, mrefAttr, nillableMrefAttr));

		// entities
		Entity entity0 = mock(Entity.class);
		when(entity0.getEntityMetaData()).thenReturn(entityMeta);

		when(entity0.getEntity(attrXrefName)).thenReturn(refEntity0);
		when(entity0.getEntity(attrNillableXrefName)).thenReturn(null);
		when(entity0.getEntities(attrMrefName)).thenReturn(Arrays.asList(refEntity0));
		when(entity0.getEntities(attrNillableMrefName)).thenReturn(emptyList());

		// actual tests
		Repository decoratedRepo = mock(Repository.class);
		when(decoratedRepo.getEntityMetaData()).thenReturn(entityMeta);

		Repository refRepo = mock(Repository.class);
		when(refRepo.getEntityMetaData()).thenReturn(refEntityMeta);

		DataService dataService = mock(DataService.class);
		when(dataService.getRepository(entityName)).thenReturn(decoratedRepo);
		when(dataService.getRepository(refEntityName)).thenReturn(refRepo);
		when(dataService.findAll(refEntityName, new QueryImpl().fetch(new Fetch().field(refAttrIdName))))
				.thenReturn(refEntities);

		EntityAttributesValidator entityAttributesValidator = mock(EntityAttributesValidator.class);
		RepositoryValidationDecorator repositoryValidationDecorator = new RepositoryValidationDecorator(dataService,
				decoratedRepo, entityAttributesValidator);

		List<Entity> entities = Arrays.asList(entity0);
		repositoryValidationDecorator.add(entities.stream());

		ArgumentCaptor<Stream<Entity>> captor = ArgumentCaptor.forClass((Class) Stream.class);
		verify(decoratedRepo, times(1)).add(captor.capture());
		Stream<Entity> stream = captor.getValue();
		stream.collect(Collectors.toList()); // process stream to enable validation

		verify(entityAttributesValidator, times(1)).validate(entity0, entityMeta);
	}

	@Test
	public void checkNillable()
	{
		DefaultEntityMetaData emd = new DefaultEntityMetaData("test");
		emd.addAttribute("id").setIdAttribute(true).setDataType(MolgenisFieldTypes.INT).setNillable(false)
				.setAuto(true);
		emd.addAttribute("notnull").setNillable(false);
		when(decoratedRepository.getEntityMetaData()).thenReturn(emd);

		Entity e1 = new MapEntity("id");
		e1.set("notnull", "notnull");
		Set<ConstraintViolation> violations = repositoryValidationDecorator.checkNillable(Arrays.asList(e1));
		assertTrue(violations.isEmpty());

		Entity e2 = new MapEntity("id");
		e2.set("notnull", null);
		violations = repositoryValidationDecorator.checkNillable(Arrays.asList(e2));
		assertEquals(violations.size(), 1);

		// With defaultvalue
		emd = new DefaultEntityMetaData("test");
		emd.addAttribute("id").setIdAttribute(true).setDataType(MolgenisFieldTypes.INT).setNillable(false)
				.setAuto(true);
		emd.addAttribute("notnull").setNillable(false).setDefaultValue("");
		when(decoratedRepository.getEntityMetaData()).thenReturn(emd);
		violations = repositoryValidationDecorator.checkNillable(Arrays.asList(e2));
		assertEquals(violations.size(), 1);

	}

	@Test
	public void checkNillableMref()
	{
		DefaultEntityMetaData refEmd = new DefaultEntityMetaData("refEntity");
		String refIdAttrName = "refId";
		refEmd.addAttribute(refIdAttrName).setIdAttribute(true);

		Entity refEntity0 = new MapEntity(refEmd);
		refEntity0.set(refIdAttrName, "0");

		DefaultEntityMetaData emd = new DefaultEntityMetaData("entity");
		String requiredMrefAttrName = "requiredMref";
		emd.addAttribute(requiredMrefAttrName).setDataType(MolgenisFieldTypes.MREF).setNillable(false)
				.setRefEntity(refEmd);
		when(decoratedRepository.getEntityMetaData()).thenReturn(emd);

		Entity entity0 = new MapEntity();
		entity0.set(requiredMrefAttrName, null);

		Entity entity1 = new MapEntity();
		entity1.set(requiredMrefAttrName, Collections.emptyList());

		Entity entity2 = new MapEntity();
		entity2.set(requiredMrefAttrName, Arrays.asList(refEntity0));

		Set<ConstraintViolation> violations = repositoryValidationDecorator
				.checkNillable(Arrays.asList(entity0, entity1, entity2));
		assertEquals(violations.size(), 2);
	}

	@Test
	public void checkReadonly()
	{
		Entity e1 = new MapEntity("id");
		e1.set("id", Integer.valueOf(1));
		e1.set("name", "e1");
		e1.set("readonly", "readonly");

		DefaultEntityMetaData emd = new DefaultEntityMetaData("test");
		emd.addAttribute("id").setIdAttribute(true).setDataType(MolgenisFieldTypes.INT).setReadOnly(true);
		emd.addAttribute("readonly").setReadOnly(true);
		emd.setLabelAttribute("name");
		when(decoratedRepository.getEntityMetaData()).thenReturn(emd);

		when(repositoryValidationDecorator.findOne(Integer.valueOf(1))).thenReturn(e1);

		Entity e2 = new MapEntity("id");
		e2.set("id", Integer.valueOf(1));
		e2.set("readonly", "readonly");
		e2.set("name", "e2");
		Set<ConstraintViolation> violations = repositoryValidationDecorator.checkReadonlyByUpdate(Arrays.asList(e2));
		assertTrue(violations.isEmpty());

		Entity e3 = new MapEntity("id");
		e3.set("id", Integer.valueOf(1));
		e3.set("readonly", "readonlyNEW");
		e3.set("name", "e3");
		violations = repositoryValidationDecorator.checkReadonlyByUpdate(Arrays.asList(e3));
		assertEquals(violations.size(), 1);
	}

	@Test
	public void checkReadonlyByUpdate_Xref()
	{
		Entity refEntity = new MapEntity("id");
		refEntity.set("id", Integer.valueOf(10));

		Entity refEntityNew = new MapEntity("idNew");
		refEntity.set("id", Integer.valueOf(11));

		Entity e1 = new MapEntity("id");
		e1.set("id", Integer.valueOf(1));
		e1.set("name", "e1");
		e1.set("readonly-xref", refEntity);

		DefaultEntityMetaData emd = new DefaultEntityMetaData("test");
		emd.addAttribute("id").setIdAttribute(true).setDataType(MolgenisFieldTypes.INT).setReadOnly(true);
		emd.addAttribute("readonly-xref").setDataType(MolgenisFieldTypes.XREF).setReadOnly(true);
		emd.setLabelAttribute("name");
		when(decoratedRepository.getEntityMetaData()).thenReturn(emd);

		when(repositoryValidationDecorator.findOne(Integer.valueOf(1))).thenReturn(e1);

		Entity e2 = new MapEntity("id");
		e2.set("id", Integer.valueOf(1));
		e2.set("readonly-xref", refEntity);
		e2.set("name", "e2");
		Set<ConstraintViolation> violations = repositoryValidationDecorator.checkReadonlyByUpdate(Arrays.asList(e2));
		assertTrue(violations.isEmpty());

		Entity e3 = new MapEntity("id");
		e3.set("id", Integer.valueOf(1));
		e3.set("readonly-xref", refEntityNew);
		e3.set("name", "e3");
		violations = repositoryValidationDecorator.checkReadonlyByUpdate(Arrays.asList(e3));
		assertEquals(violations.size(), 1);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void checkUniques()
	{
		DefaultEntityMetaData emd = new DefaultEntityMetaData("test");
		emd.addAttribute("id").setIdAttribute(true).setDataType(MolgenisFieldTypes.INT).setNillable(false);
		emd.addAttribute("unique").setUnique(true);
		when(decoratedRepository.getEntityMetaData()).thenReturn(emd);
		when(decoratedRepository.count()).thenReturn(1l, 1l, 1l, 1l, 1l);

		Entity existing = new MapEntity("id");
		existing.set("id", 1);
		existing.set("unique", "qwerty");

		when(decoratedRepository.iterator()).thenReturn(Arrays.asList(existing).iterator(),
				Arrays.asList(existing).iterator(), Arrays.asList(existing).iterator(),
				Arrays.asList(existing).iterator(), Arrays.asList(existing).iterator());

		// Test add, no violations
		Entity new1 = new MapEntity("id");
		new1.set("id", 2);
		new1.set("unique", "mnbv");
		Set<ConstraintViolation> violations = repositoryValidationDecorator.checkUniques(Arrays.asList(new1),
				emd.getAttribute("unique"), false);
		assertTrue(violations.isEmpty());

		// Test add, already exists in repo
		Entity new2 = new MapEntity("id");
		new2.set("id", 2);
		new2.set("unique", "qwerty");
		violations = repositoryValidationDecorator.checkUniques(Arrays.asList(new2), emd.getAttribute("unique"), false);
		assertEquals(violations.size(), 1);

		// Test add double in new
		Entity new3 = new MapEntity("id");
		new3.set("id", 3);
		new3.set("unique", "qwerty1");

		Entity new4 = new MapEntity("id");
		new4.set("id", 4);
		new4.set("unique", "qwerty1");

		violations = repositoryValidationDecorator.checkUniques(Arrays.asList(new3, new4), emd.getAttribute("unique"),
				false);
		assertEquals(violations.size(), 1);

		// Test update itself
		Entity new5 = new MapEntity("id");
		new5.set("id", 1);
		new5.set("unique", "qwerty");
		violations = repositoryValidationDecorator.checkUniques(Arrays.asList(new5), emd.getAttribute("unique"), true);
		assertTrue(violations.isEmpty());

		// Test update already double in new
		violations = repositoryValidationDecorator.checkUniques(Arrays.asList(new3, new4), emd.getAttribute("unique"),
				true);
		assertEquals(violations.size(), 1);
	}

	@Test
	public void findAllIterableFetch()
	{
		DataService dataService = mock(DataService.class);
		EntityMetaData entityMeta = mock(EntityMetaData.class);
		Repository decoratedRepository = mock(Repository.class);
		when(decoratedRepository.getEntityMetaData()).thenReturn(entityMeta);
		EntityAttributesValidator entityAttributesValidator = mock(EntityAttributesValidator.class);

		@SuppressWarnings("resource")
		RepositoryValidationDecorator myRepositoryValidationDecorator = new RepositoryValidationDecorator(dataService,
				decoratedRepository, entityAttributesValidator);

		Iterable<Object> ids = Arrays.<Object> asList(Integer.valueOf(0), Integer.valueOf(1));
		Fetch fetch = new Fetch();
		Entity entity0 = mock(Entity.class);
		Entity entity1 = mock(Entity.class);
		Iterable<Entity> entities = Arrays.asList(entity0, entity1);
		when(decoratedRepository.findAll(ids, fetch)).thenReturn(entities);
		assertEquals(Arrays.asList(entity0, entity1),
				Lists.newArrayList(myRepositoryValidationDecorator.findAll(ids, fetch)));
		verify(decoratedRepository, times(1)).findAll(ids, fetch);
	}

	@Test
	public void findOneObjectFetch()
	{
		DataService dataService = mock(DataService.class);
		EntityMetaData entityMeta = mock(EntityMetaData.class);
		Repository decoratedRepository = mock(Repository.class);
		when(decoratedRepository.getEntityMetaData()).thenReturn(entityMeta);
		EntityAttributesValidator entityAttributesValidator = mock(EntityAttributesValidator.class);

		@SuppressWarnings("resource")
		RepositoryValidationDecorator myRepositoryValidationDecorator = new RepositoryValidationDecorator(dataService,
				decoratedRepository, entityAttributesValidator);

		Object id = Integer.valueOf(0);
		Fetch fetch = new Fetch();
		Entity entity = mock(Entity.class);
		when(decoratedRepository.findOne(id, fetch)).thenReturn(entity);
		assertEquals(entity, myRepositoryValidationDecorator.findOne(id, fetch));
		verify(decoratedRepository, times(1)).findOne(id, fetch);
	}
}

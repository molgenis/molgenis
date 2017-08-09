package org.molgenis.data.validation.meta;

import org.mockito.ArgumentCaptor;
import org.molgenis.data.Repository;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.validation.MolgenisValidationException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.stream.Stream;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;

public class PackageRepositoryValidationDecoratorTest
{
	private PackageRepositoryValidationDecorator packageRepositoryValidationDecorator;
	private Repository<Package> decoratedRepo;
	private PackageValidator packageValidator;

	@SuppressWarnings("unchecked")
	@BeforeMethod
	public void setUpBeforeMethod() throws Exception
	{
		decoratedRepo = mock(Repository.class);
		packageValidator = mock(PackageValidator.class);
		packageRepositoryValidationDecorator = new PackageRepositoryValidationDecorator(decoratedRepo,
				packageValidator);
	}

	@Test
	public void testDelegate() throws Exception
	{
		assertEquals(packageRepositoryValidationDecorator.delegate(), decoratedRepo);
	}

	@Test
	public void testAddValid() throws Exception
	{
		Package package_ = mock(Package.class);
		packageRepositoryValidationDecorator.add(package_);
		verify(decoratedRepo).add(package_);
	}

	@Test(expectedExceptions = MolgenisValidationException.class)
	public void testAddInvalid() throws Exception
	{
		Package package_ = mock(Package.class);
		doThrow(mock(MolgenisValidationException.class)).when(packageValidator).validate(package_);
		packageRepositoryValidationDecorator.add(package_);
	}

	@Test
	public void testAddStreamValid() throws Exception
	{
		Package package_ = mock(Package.class);
		packageRepositoryValidationDecorator.add(Stream.of(package_));
		@SuppressWarnings("unchecked")
		ArgumentCaptor<Stream<Package>> packageCaptor = ArgumentCaptor.forClass(Stream.class);
		verify(decoratedRepo).add(packageCaptor.capture());
		assertEquals(packageCaptor.getValue().collect(toList()), singletonList(package_));
		verify(packageValidator).validate(package_);
	}

	@SuppressWarnings("ResultOfMethodCallIgnored")
	@Test(expectedExceptions = MolgenisValidationException.class)
	public void testAddStreamInvalid() throws Exception
	{
		Package package_ = mock(Package.class);
		doThrow(mock(MolgenisValidationException.class)).when(packageValidator).validate(package_);
		packageRepositoryValidationDecorator.add(Stream.of(package_));
		@SuppressWarnings("unchecked")
		ArgumentCaptor<Stream<Package>> packageCaptor = ArgumentCaptor.forClass(Stream.class);
		verify(decoratedRepo).add(packageCaptor.capture());
		packageCaptor.getValue().count();
	}

	@Test
	public void testUpdateValid() throws Exception
	{
		Package package_ = mock(Package.class);
		packageRepositoryValidationDecorator.update(package_);
		verify(packageValidator).validate(package_);
		verify(decoratedRepo).update(package_);
	}

	@Test(expectedExceptions = MolgenisValidationException.class)
	public void testUpdateInvalid() throws Exception
	{
		Package package_ = mock(Package.class);
		doThrow(mock(MolgenisValidationException.class)).when(packageValidator).validate(package_);
		packageRepositoryValidationDecorator.update(package_);
	}

	@Test
	public void testUpdateStreamValid() throws Exception
	{
		Package package_ = mock(Package.class);
		packageRepositoryValidationDecorator.update(Stream.of(package_));
		@SuppressWarnings("unchecked")
		ArgumentCaptor<Stream<Package>> packageCaptor = ArgumentCaptor.forClass(Stream.class);
		verify(decoratedRepo).update(packageCaptor.capture());
		assertEquals(packageCaptor.getValue().collect(toList()), singletonList(package_));
		verify(packageValidator).validate(package_);
	}

	@SuppressWarnings("ResultOfMethodCallIgnored")
	@Test(expectedExceptions = MolgenisValidationException.class)
	public void testUpdateStreamInvalid() throws Exception
	{
		Package package_ = mock(Package.class);
		doThrow(mock(MolgenisValidationException.class)).when(packageValidator).validate(package_);
		packageRepositoryValidationDecorator.update(Stream.of(package_));
		@SuppressWarnings("unchecked")
		ArgumentCaptor<Stream<Package>> packageCaptor = ArgumentCaptor.forClass(Stream.class);
		verify(decoratedRepo).update(packageCaptor.capture());
		packageCaptor.getValue().count();
	}

	@Test
	public void testDeleteValid() throws Exception
	{
		Package package_ = mock(Package.class);
		packageRepositoryValidationDecorator.delete(package_);
		verify(packageValidator).validate(package_);
		verify(decoratedRepo).delete(package_);
	}

	@Test(expectedExceptions = MolgenisValidationException.class)
	public void testDeleteInvalid() throws Exception
	{
		Package package_ = mock(Package.class);
		doThrow(mock(MolgenisValidationException.class)).when(packageValidator).validate(package_);
		packageRepositoryValidationDecorator.delete(package_);
	}

	@Test
	public void testDeleteStreamValid() throws Exception
	{
		Package package_ = mock(Package.class);
		packageRepositoryValidationDecorator.delete(Stream.of(package_));
		@SuppressWarnings("unchecked")
		ArgumentCaptor<Stream<Package>> packageCaptor = ArgumentCaptor.forClass(Stream.class);
		verify(decoratedRepo).delete(packageCaptor.capture());
		assertEquals(packageCaptor.getValue().collect(toList()), singletonList(package_));
		verify(packageValidator).validate(package_);
	}

	@SuppressWarnings("ResultOfMethodCallIgnored")
	@Test(expectedExceptions = MolgenisValidationException.class)
	public void testDeleteStreamInvalid() throws Exception
	{
		Package package_ = mock(Package.class);
		doThrow(mock(MolgenisValidationException.class)).when(packageValidator).validate(package_);
		packageRepositoryValidationDecorator.delete(Stream.of(package_));
		@SuppressWarnings("unchecked")
		ArgumentCaptor<Stream<Package>> packageCaptor = ArgumentCaptor.forClass(Stream.class);
		verify(decoratedRepo).delete(packageCaptor.capture());
		packageCaptor.getValue().count();
	}

	@Test
	public void testDeleteByIdValid() throws Exception
	{
		Package package_ = mock(Package.class);
		Object id = mock(Object.class);
		when(decoratedRepo.findOneById(id)).thenReturn(package_);
		packageRepositoryValidationDecorator.deleteById(id);
		verify(packageValidator).validate(package_);
		verify(decoratedRepo).deleteById(id);
	}

	@Test(expectedExceptions = MolgenisValidationException.class)
	public void testDeleteByIdInvalid() throws Exception
	{
		Package package_ = mock(Package.class);
		Object id = mock(Object.class);
		when(decoratedRepo.findOneById(id)).thenReturn(package_);
		doThrow(mock(MolgenisValidationException.class)).when(packageValidator).validate(package_);
		packageRepositoryValidationDecorator.deleteById(id);
	}

	@Test
	public void testDeleteAllValid() throws Exception
	{
		Package package_ = mock(Package.class);
		when(decoratedRepo.iterator()).thenReturn(singletonList(package_).iterator());
		packageRepositoryValidationDecorator.deleteAll();
		verify(packageValidator).validate(package_);
		verify(decoratedRepo).deleteAll();
	}

	@Test(expectedExceptions = MolgenisValidationException.class)
	public void testDeleteAllInvalid() throws Exception
	{
		Package package_ = mock(Package.class);
		when(decoratedRepo.iterator()).thenReturn(singletonList(package_).iterator());
		doThrow(mock(MolgenisValidationException.class)).when(packageValidator).validate(package_);
		packageRepositoryValidationDecorator.deleteAll();
	}

	@SuppressWarnings("ResultOfMethodCallIgnored")
	@Test
	public void testDeleteAllStreamValid() throws Exception
	{
		Package package_ = mock(Package.class);
		Object id = mock(Object.class);
		when(decoratedRepo.findOneById(id)).thenReturn(package_);
		packageRepositoryValidationDecorator.deleteAll(Stream.of(id));
		@SuppressWarnings("unchecked")
		ArgumentCaptor<Stream<Object>> packageIdCaptor = ArgumentCaptor.forClass(Stream.class);
		verify(decoratedRepo).deleteAll(packageIdCaptor.capture());
		packageIdCaptor.getValue().count();
		verify(packageValidator).validate(package_);
	}

	@SuppressWarnings("ResultOfMethodCallIgnored")
	@Test(expectedExceptions = MolgenisValidationException.class)
	public void testDeleteAllStreamInvalid() throws Exception
	{
		Package package_ = mock(Package.class);
		Object id = mock(Object.class);
		when(decoratedRepo.findOneById(id)).thenReturn(package_);
		doThrow(mock(MolgenisValidationException.class)).when(packageValidator).validate(package_);
		packageRepositoryValidationDecorator.deleteAll(Stream.of(id));
		@SuppressWarnings("unchecked")
		ArgumentCaptor<Stream<Object>> packageIdCaptor = ArgumentCaptor.forClass(Stream.class);
		verify(decoratedRepo).deleteAll(packageIdCaptor.capture());
		packageIdCaptor.getValue().count();
	}
}
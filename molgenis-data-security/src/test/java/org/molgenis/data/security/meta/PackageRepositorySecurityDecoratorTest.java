package org.molgenis.data.security.meta;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.molgenis.data.Repository;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.security.PackageIdentity;
import org.molgenis.test.AbstractMockitoTestNGSpringContextTests;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;

@ContextConfiguration(classes = { PackageRepositorySecurityDecoratorTest.Config.class })
@TestExecutionListeners(listeners = WithSecurityContextTestExecutionListener.class)
public class PackageRepositorySecurityDecoratorTest extends AbstractMockitoTestNGSpringContextTests
{
	@Mock
	private Repository<Package> delegateRepository;
	@Mock
	private MutableAclService mutableAclService;

	private PackageRepositorySecurityDecorator repo;

	@BeforeMethod
	public void setUp()
	{
		repo = new PackageRepositorySecurityDecorator(delegateRepository, mutableAclService);
	}

	@Test
	public void testUpdate()
	{
		Package pack = mock(Package.class);
		Package parent = mock(Package.class);

		when(pack.getId()).thenReturn("1");
		when(parent.getId()).thenReturn("2");
		when(pack.getParent()).thenReturn(parent);

		MutableAcl acl = mock(MutableAcl.class);
		MutableAcl parentAcl = mock(MutableAcl.class);
		when(mutableAclService.readAclById(any())).thenAnswer(invocation ->
		{
			Object argument = invocation.getArguments()[0];
			if (argument.equals(new PackageIdentity("1")))
			{
				return acl;
			}
			else if (argument.equals(new PackageIdentity("2")))
			{
				return parentAcl;
			}
			return null;
		});
		repo.update(pack);

		verify(mutableAclService).updateAcl(acl);
		verify(delegateRepository).update(pack);
	}

	@Test
	public void testUpdate1()
	{
		Package package1 = mock(Package.class);
		Package package2 = mock(Package.class);
		when(package1.getId()).thenReturn("1");
		when(package2.getId()).thenReturn("2");
		Stream<Package> packages = Stream.of(package1, package2);
		repo.update(packages);

		//TODO: how to verify the deleteAcl method in the "filter" of the stream

		ArgumentCaptor<Stream<Package>> captor = ArgumentCaptor.forClass(Stream.class);
		verify(delegateRepository).update(captor.capture());
		assertEquals(captor.getValue().collect(toList()), asList(package1, package2));
	}

	@Test
	public void testDelete()
	{
		Package pack = mock(Package.class);
		when(pack.getId()).thenReturn("1");

		repo.delete(pack);

		verify(mutableAclService).deleteAcl(new PackageIdentity("1"), true);
		verify(delegateRepository).delete(pack);
	}

	@Test
	public void testDelete1()
	{
		Package package1 = mock(Package.class);
		Package package2 = mock(Package.class);
		when(package1.getId()).thenReturn("1");
		when(package2.getId()).thenReturn("2");

		Stream<Package> packages = Stream.of(package1, package2);

		repo.delete(packages);

		//TODO: how to verify the deleteAcl method in the "filter" of the stream

		ArgumentCaptor<Stream<Package>> captor = ArgumentCaptor.forClass(Stream.class);
		verify(delegateRepository).delete(captor.capture());
		assertEquals(captor.getValue().collect(toList()), asList(package1, package2));
	}

	@Test
	public void testDeleteById()
	{
		repo.deleteById("1");
		verify(mutableAclService).deleteAcl(new PackageIdentity("1"), true);
		verify(delegateRepository).deleteById("1");
	}

	@Test
	public void testDeleteAll()
	{
		Package package1 = mock(Package.class);
		Package package2 = mock(Package.class);
		when(package1.getId()).thenReturn("1");
		when(package2.getId()).thenReturn("2");
		when(delegateRepository.iterator()).thenReturn(Arrays.asList(package1, package2).iterator());
		repo.deleteAll();

		//TODO: how to verify the deleteAcl method in the "filter" of the stream

		verify(delegateRepository).deleteAll();
	}

	@Test
	public void testDeleteAll1()
	{
		Stream<Object> ids = Stream.of("1", "2");
		repo.deleteAll(ids);
		ArgumentCaptor<Stream<Object>> captor = ArgumentCaptor.forClass(Stream.class);
		verify(delegateRepository).deleteAll(captor.capture());
		assertEquals(captor.getValue().collect(toList()), asList("1", "2"));
	}

	@Test
	public void testAdd()
	{
		Package pack = mock(Package.class);
		Package parent = mock(Package.class);

		when(pack.getId()).thenReturn("1");
		when(parent.getId()).thenReturn("2");
		when(pack.getParent()).thenReturn(parent);

		MutableAcl acl = mock(MutableAcl.class);
		MutableAcl parentAcl = mock(MutableAcl.class);
		when(mutableAclService.createAcl(new PackageIdentity("1"))).thenReturn(acl);
		when(mutableAclService.readAclById(new PackageIdentity("2"))).thenReturn(parentAcl);
		repo.add(pack);

		verify(mutableAclService).createAcl(new PackageIdentity("1"));
		verify(mutableAclService).updateAcl(acl);
		verify(delegateRepository).add(pack);
	}

	@Test
	public void testAdd1()
	{
		Package package1 = mock(Package.class);
		Package package2 = mock(Package.class);
		when(package1.getId()).thenReturn("1");
		when(package2.getId()).thenReturn("2");
		Stream<Package> packages = Stream.of(package1, package2);
		repo.add(packages);
		ArgumentCaptor<Stream<Package>> captor = ArgumentCaptor.forClass(Stream.class);
		verify(delegateRepository).add(captor.capture());
		assertEquals(captor.getValue().collect(toList()), asList(package1, package2));
	}

	static class Config
	{

	}
}
package org.molgenis.metadata.manager.mapper;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.molgenis.data.DataService;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.model.PackageFactory;
import org.molgenis.data.meta.model.PackageMetadata;
import org.molgenis.metadata.manager.model.EditorPackageIdentifier;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

public class PackageMapperTest
{
	@Mock
	private PackageFactory packageFactory;

	@Mock
	private DataService dataService;

	private PackageMapper packageMapper;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		MockitoAnnotations.initMocks(this);
		PackageMetadata packageMetadata = mock(PackageMetadata.class);
		when(packageFactory.getEntityType()).thenReturn(packageMetadata);
		packageMapper = new PackageMapper(packageFactory, dataService);
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void testPackageMapper()
	{
		new PackageMapper(null, null);
	}

	@Test
	public void testToPackageReference()
	{
		String id = "id0";
		EditorPackageIdentifier packageIdentifier = EditorPackageIdentifier.create(id, "label");
		Package package_ = packageMapper.toPackageReference(packageIdentifier);
		assertEquals(package_.getIdValue(), id);
	}

	@Test
	public void testToPackageReferenceNull()
	{
		assertNull(packageMapper.toPackageReference(null));
	}

	@Test
	public void testToEditorPackage()
	{
		String id = "id0";
		String label = "label0";
		Package package_ = mock(Package.class);
		when(package_.getId()).thenReturn(id);
		when(package_.getLabel()).thenReturn(label);
		EditorPackageIdentifier editorPackage = packageMapper.toEditorPackage(package_);
		assertEquals(editorPackage, EditorPackageIdentifier.create(id, label));
	}

	@Test
	public void testToEditorPackageNull()
	{
		assertNull(packageMapper.toEditorPackage(null));
	}
}
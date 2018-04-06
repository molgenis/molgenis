package org.molgenis.apps.model;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.quality.Strictness;
import org.molgenis.data.Repository;
import org.molgenis.data.file.FileStore;
import org.molgenis.data.file.model.FileMeta;
import org.molgenis.data.validation.MolgenisValidationException;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.mockito.Mockito.*;
import static org.molgenis.util.ResourceUtils.getFile;
import static org.testng.Assert.assertEquals;

public class AppRepositoryDecoratorTest extends AbstractMockitoTest
{
	private static final String APP_NAME = "appName";

	@Mock
	private Repository<App> delegateRepository;

	@Mock
	private FileStore fileStore;

	private AppRepositoryDecorator appRepositoryDecorator;

	public AppRepositoryDecoratorTest()
	{
		super(Strictness.WARN);
	}

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		appRepositoryDecorator = new AppRepositoryDecorator(delegateRepository, fileStore);
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void testAppRepositoryDecorator()
	{
		new AppRepositoryDecorator(null, null);
	}

	@Test
	public void testAddInactiveNoResourceZip()
	{
		App app = getMockApp("id", false);
		appRepositoryDecorator.add(app);
		verify(delegateRepository).add(app);
	}

	@Test
	public void testAddActiveNoResourceZip()
	{
		App app = getMockApp("id", true);
		appRepositoryDecorator.add(app);
		verify(delegateRepository).add(app);
	}

	@Test(expectedExceptions = MolgenisValidationException.class, expectedExceptionsMessageRegExp = "'app-invalid.zip' is not a valid zip file.")
	public void testAddInvalidResourceZip()
	{
		App app = getMockApp("id", false, "app-invalid.zip");
		appRepositoryDecorator.add(app);
		verify(delegateRepository).add(app);
	}

	@Test
	public void testAddInactiveValidResourceZip()
	{
		App app = getMockApp("id", false, "app-valid.zip");
		appRepositoryDecorator.add(app);
		verify(delegateRepository).add(app);
		//noinspection ResultOfMethodCallIgnored
		verify(fileStore, never()).getStorageDir();
	}

	@Test
	public void testAddActiveValidResourceZip()
	{
		App app = getMockApp("id", true, "app-valid.zip");
		appRepositoryDecorator.add(app);
		verify(delegateRepository).add(app);
		//noinspection ResultOfMethodCallIgnored
		verify(fileStore).getStorageDir();
	}

	@Test
	public void testAddStream()
	{
		App app0 = getMockApp("id0", true, "app-valid.zip");
		App app1 = getMockApp("id1", false, "app-valid.zip");
		App app2 = getMockApp("id2", true, "app-valid.zip");
		App app3 = getMockApp("id3", true);
		App app4 = getMockApp("id4", false);
		appRepositoryDecorator.add(Stream.of(app0, app1, app2, app3, app4));

		@SuppressWarnings("unchecked")
		ArgumentCaptor<Stream<App>> captor = ArgumentCaptor.forClass(Stream.class);
		verify(delegateRepository).add(captor.capture());
		assertEquals(captor.getValue().collect(toList()), asList(app0, app1, app2, app3, app4));

		//noinspection ResultOfMethodCallIgnored
		verify(fileStore, times(2)).getStorageDir();
	}

	@Test
	public void testUpdateActivatedResourceToActivatedValidResource() throws IOException
	{
		App existingApp = getMockApp("id", true, "app-valid.zip");
		App app = getMockApp("id", true, "app-valid-update.zip");
		when(delegateRepository.findOneById("id")).thenReturn(existingApp);
		appRepositoryDecorator.update(app);
		verify(delegateRepository).update(app);
		verify(fileStore).deleteDirectory(anyString());
		//noinspection ResultOfMethodCallIgnored
		verify(fileStore).getStorageDir();
	}

	@Test
	public void testUpdateDeactivatedResourceToDeactivatedValidResource()
	{
		App existingApp = getMockApp("id", false, "app-valid.zip");
		App app = getMockApp("id", false, "app-valid.zip");
		when(delegateRepository.findOneById("id")).thenReturn(existingApp);
		appRepositoryDecorator.update(app);
		verify(delegateRepository).update(app);
		//noinspection ResultOfMethodCallIgnored
		verify(fileStore, never()).getStorageDir();
	}

	@Test(expectedExceptions = MolgenisValidationException.class, expectedExceptionsMessageRegExp = "'app-invalid.zip' is not a valid zip file.")
	public void testUpdateDeactivatedResourceToDeactivatedInvalidResource()
	{
		App existingApp = getMockApp("id", false, "app-valid.zip");
		App app = getMockApp("id", false, "app-invalid.zip");
		when(delegateRepository.findOneById("id")).thenReturn(existingApp);
		appRepositoryDecorator.update(app);
	}

	@Test(expectedExceptions = MolgenisValidationException.class, expectedExceptionsMessageRegExp = "'app-invalid.zip' is not a valid zip file.")
	public void testUpdateNoResourceToDeactivatedInvalidResource()
	{
		App existingApp = getMockApp("id", false);
		App app = getMockApp("id", false, "app-invalid.zip");
		when(delegateRepository.findOneById("id")).thenReturn(existingApp);
		appRepositoryDecorator.update(app);
	}

	@Test
	public void testUpdateStream() throws IOException
	{
		App existingApp = getMockApp("id", true, "app-valid.zip");
		App app = getMockApp("id", true, "app-valid-update.zip");
		when(delegateRepository.findOneById("id")).thenReturn(existingApp);
		appRepositoryDecorator.update(Stream.of(app));
		@SuppressWarnings("unchecked")
		ArgumentCaptor<Stream<App>> captor = ArgumentCaptor.forClass(Stream.class);
		verify(delegateRepository).update(captor.capture());
		assertEquals(captor.getValue().collect(toList()), singletonList(app));
		verify(fileStore).deleteDirectory(anyString());
		//noinspection ResultOfMethodCallIgnored
		verify(fileStore).getStorageDir();
	}

	@Test
	public void testDeleteActive() throws IOException
	{
		App app = getMockApp("id", true, "app-valid.zip");
		appRepositoryDecorator.delete(app);
		verify(fileStore).deleteDirectory(anyString());
	}

	@Test
	public void testDeleteInactive() throws IOException
	{
		App app = getMockApp("id", false, "app-valid.zip");
		appRepositoryDecorator.delete(app);
		verify(fileStore, never()).deleteDirectory(anyString());
	}

	@Test
	public void testDeleteStream() throws IOException
	{
		App app0 = getMockApp("id0", true, "app-valid.zip");
		App app1 = getMockApp("id1", false, "app-valid.zip");
		App app2 = getMockApp("id2", true, "app-valid-update.zip");
		appRepositoryDecorator.delete(Stream.of(app0, app1, app2));

		@SuppressWarnings("unchecked")
		ArgumentCaptor<Stream<App>> captor = ArgumentCaptor.forClass(Stream.class);
		verify(delegateRepository).delete(captor.capture());
		assertEquals(captor.getValue().collect(toList()), asList(app0, app1, app2));

		verify(fileStore, times(2)).deleteDirectory(anyString());
	}

	@Test
	public void testDeleteById() throws IOException
	{
		App app = getMockApp("id", true, "app-valid.zip");
		when(delegateRepository.findOneById("id")).thenReturn(app);
		appRepositoryDecorator.deleteById("id");
		verify(fileStore).deleteDirectory(anyString());
	}

	private App getMockApp(String id, boolean isActive)
	{
		return getMockApp(id, isActive, null);
	}

	private App getMockApp(String id, boolean isActive, String resourceFilename)
	{
		App app = mock(App.class);
		when(app.getId()).thenReturn(id);
		when(app.getName()).thenReturn(APP_NAME);
		when(app.isActive()).thenReturn(isActive);
		when(app.getUseFreemarkerTemplate()).thenReturn(true);
		if (resourceFilename != null)
		{
			FileMeta fileMeta = mock(FileMeta.class);
			when(fileMeta.getId()).thenReturn(resourceFilename);
			when(fileMeta.getFilename()).thenReturn(resourceFilename);
			when(app.getSourceFiles()).thenReturn(fileMeta);
			when(fileStore.getFile(resourceFilename)).thenReturn(getFile(getClass(), '/' + resourceFilename));
			when(fileStore.getStorageDir()).thenReturn("target/generated-test-resources");
		}
		return app;
	}
}
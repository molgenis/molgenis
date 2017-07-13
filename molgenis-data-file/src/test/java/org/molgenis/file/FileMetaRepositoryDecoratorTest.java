package org.molgenis.file;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.file.model.FileMeta;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;

public class FileMetaRepositoryDecoratorTest extends AbstractMockitoTest
{
	@Mock
	private Repository<FileMeta> fileMetaRepository;

	@Mock
	private FileStore fileStore;

	private FileMetaRepositoryDecorator fileMetaRepositoryDecorator;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		EntityType entityType = when(mock(EntityType.class).getLabel()).thenReturn("file metadata").getMock();
		when(fileMetaRepository.getEntityType()).thenReturn(entityType);
		when(fileStore.delete(anyString())).thenReturn(true);
		fileMetaRepositoryDecorator = new FileMetaRepositoryDecorator(fileMetaRepository, fileStore);
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void testAppRepositoryDecorator()
	{
		new FileMetaRepositoryDecorator(null, null);
	}

	@Test
	public void testDelegate()
	{
		assertEquals(fileMetaRepositoryDecorator.delegate(), fileMetaRepository);
	}

	@Test
	public void testDelete() throws Exception
	{
		FileMeta fileMeta = getMockFileMeta("id");
		fileMetaRepositoryDecorator.delete(fileMeta);
		verify(fileMetaRepository).delete(fileMeta);
		verify(fileStore).delete("id");
	}

	@Test
	public void testDeleteStream() throws Exception
	{
		FileMeta fileMeta0 = getMockFileMeta("id0");
		FileMeta fileMeta1 = getMockFileMeta("id1");
		fileMetaRepositoryDecorator.delete(Stream.of(fileMeta0, fileMeta1));
		@SuppressWarnings("unchecked")
		ArgumentCaptor<Stream<FileMeta>> captor = ArgumentCaptor.forClass(Stream.class);
		verify(fileMetaRepository).delete(captor.capture());
		assertEquals(captor.getValue().collect(toList()), asList(fileMeta0, fileMeta1));
		verify(fileStore).delete("id0");
		verify(fileStore).delete("id1");
	}

	@Test
	public void testDeleteById() throws Exception
	{
		FileMeta fileMeta = getMockFileMeta("id");
		when(fileMetaRepository.findOneById("id")).thenReturn(fileMeta);
		fileMetaRepositoryDecorator.deleteById("id");
		verify(fileMetaRepository).deleteById("id");
		verify(fileStore).delete("id");
	}

	@Test(expectedExceptions = UnknownEntityException.class, expectedExceptionsMessageRegExp = "Unknown \\[file metadata] with id \\[id]")
	public void testDeleteByIdUnknownId() throws Exception
	{
		FileMeta fileMeta = getMockFileMeta("id");
		when(fileMetaRepository.findOneById("id")).thenReturn(null);
		fileMetaRepositoryDecorator.deleteById("id");
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testDeleteAll() throws Exception
	{
		FileMeta fileMeta0 = getMockFileMeta("id0");
		FileMeta fileMeta1 = getMockFileMeta("id1");
		when(fileMetaRepository.findAll(any(Query.class))).thenReturn(Stream.of(fileMeta0, fileMeta1));
		fileMetaRepositoryDecorator.deleteAll();
		verify(fileMetaRepository).deleteAll();
		verify(fileStore).delete("id0");
		verify(fileStore).delete("id1");
	}

	@Test
	public void testDeleteAllStream() throws Exception
	{
		FileMeta fileMeta0 = getMockFileMeta("id0");
		FileMeta fileMeta1 = getMockFileMeta("id1");
		when(fileMetaRepository.findOneById("id0")).thenReturn(fileMeta0);
		when(fileMetaRepository.findOneById("id1")).thenReturn(fileMeta1);
		fileMetaRepositoryDecorator.deleteAll(Stream.of("id0", "id1"));
		@SuppressWarnings("unchecked")
		ArgumentCaptor<Stream<Object>> captor = ArgumentCaptor.forClass(Stream.class);
		verify(fileMetaRepository).deleteAll(captor.capture());
		assertEquals(captor.getValue().collect(toList()), asList("id0", "id1"));
		verify(fileStore).delete("id0");
		verify(fileStore).delete("id1");
	}

	private FileMeta getMockFileMeta(String id)
	{
		FileMeta fileMeta = mock(FileMeta.class);
		when(fileMeta.getId()).thenReturn(id);
		return fileMeta;
	}
}
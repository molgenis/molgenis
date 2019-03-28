package org.molgenis.data.file;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.stream.Stream;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.file.model.FileMeta;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class FileMetaRepositoryDecoratorTest extends AbstractMockitoTest {
  @Mock private Repository<FileMeta> delegateRepository;

  @Mock private FileStore fileStore;
  @Mock private BlobStore blobStore;

  private FileMetaRepositoryDecorator fileMetaRepositoryDecorator;

  @BeforeMethod
  public void setUpBeforeMethod() {
    fileMetaRepositoryDecorator =
        new FileMetaRepositoryDecorator(delegateRepository, fileStore, blobStore);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void testAppRepositoryDecorator() {
    new FileMetaRepositoryDecorator(null, null, null);
  }

  @Test
  public void testDelete() {
    FileMeta fileMeta = getMockFileMeta("id");
    fileMetaRepositoryDecorator.delete(fileMeta);
    verify(delegateRepository).delete(fileMeta);
    verify(fileStore).delete("id");
  }

  @Test
  public void testDeleteStream() {
    FileMeta fileMeta0 = getMockFileMeta("id0");
    FileMeta fileMeta1 = getMockFileMeta("id1");
    fileMetaRepositoryDecorator.delete(Stream.of(fileMeta0, fileMeta1));
    @SuppressWarnings("unchecked")
    ArgumentCaptor<Stream<FileMeta>> captor = ArgumentCaptor.forClass(Stream.class);
    verify(delegateRepository).delete(captor.capture());
    assertEquals(captor.getValue().collect(toList()), asList(fileMeta0, fileMeta1));
    verify(fileStore).delete("id0");
    verify(fileStore).delete("id1");
  }

  @Test
  public void testDeleteById() {
    FileMeta fileMeta = getMockFileMeta("id");
    when(delegateRepository.findOneById("id")).thenReturn(fileMeta);
    fileMetaRepositoryDecorator.deleteById("id");
    verify(delegateRepository).deleteById("id");
    verify(fileStore).delete("id");
  }

  @Test(
      expectedExceptions = UnknownEntityException.class,
      expectedExceptionsMessageRegExp = "type:sys_file_FileMeta id:id attribute:idAttribute")
  public void testDeleteByIdUnknownId() {
    Attribute idAttribute =
        when(mock(Attribute.class).getName()).thenReturn("idAttribute").getMock();
    EntityType entityType =
        when(mock(EntityType.class).getId()).thenReturn("sys_file_FileMeta").getMock();
    when(entityType.getIdAttribute()).thenReturn(idAttribute);
    when(delegateRepository.getEntityType()).thenReturn(entityType);
    when(delegateRepository.findOneById("id")).thenReturn(null);
    fileMetaRepositoryDecorator.deleteById("id");
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testDeleteAll() {
    FileMeta fileMeta0 = getMockFileMeta("id0");
    FileMeta fileMeta1 = getMockFileMeta("id1");
    when(delegateRepository.findAll(any(Query.class))).thenReturn(Stream.of(fileMeta0, fileMeta1));
    fileMetaRepositoryDecorator.deleteAll();
    verify(delegateRepository).deleteAll();
    verify(fileStore).delete("id0");
    verify(fileStore).delete("id1");
  }

  @Test
  public void testDeleteAllStream() {
    FileMeta fileMeta0 = getMockFileMeta("id0");
    FileMeta fileMeta1 = getMockFileMeta("id1");
    doReturn(fileMeta0).when(delegateRepository).findOneById("id0");
    doReturn(fileMeta1).when(delegateRepository).findOneById("id1");
    fileMetaRepositoryDecorator.deleteAll(Stream.of("id0", "id1"));
    @SuppressWarnings("unchecked")
    ArgumentCaptor<Stream<Object>> captor = ArgumentCaptor.forClass(Stream.class);
    verify(delegateRepository).deleteAll(captor.capture());
    assertEquals(captor.getValue().collect(toList()), asList("id0", "id1"));
    verify(fileStore).delete("id0");
    verify(fileStore).delete("id1");
  }

  @Test
  public void testDeleteBlobStore() {
    FileMeta fileMeta = getMockFileMeta("id");
    when(fileMeta.getUrl()).thenReturn("/api/files/v1/id?alt=media");
    fileMetaRepositoryDecorator.delete(fileMeta);
    verify(delegateRepository).delete(fileMeta);
    verify(blobStore).delete("id");
  }

  private FileMeta getMockFileMeta(String id) {
    FileMeta fileMeta = mock(FileMeta.class);
    when(fileMeta.getId()).thenReturn(id);
    when(fileMeta.getUrl()).thenReturn("/files/" + id);
    return fileMeta;
  }
}

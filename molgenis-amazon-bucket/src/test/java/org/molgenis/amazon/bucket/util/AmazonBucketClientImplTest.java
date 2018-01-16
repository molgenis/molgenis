package org.molgenis.amazon.bucket.util;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import org.apache.http.client.methods.HttpRequestBase;
import org.molgenis.amazon.bucket.client.AmazonBucketClient;
import org.molgenis.amazon.bucket.client.AmazonBucketClientImpl;
import org.molgenis.data.file.FileStore;
import org.molgenis.util.ResourceUtils;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;

import static org.mockito.Mockito.*;

public class AmazonBucketClientImplTest
{
	private AmazonS3Client client;
	private FileStore fileStore;
	private S3Object s3Object;
	private HttpRequestBase httpRequestBase;
	private AmazonBucketClient amazonBucketClient;

	@BeforeTest
	public void setUp()
	{
		client = mock(AmazonS3Client.class);
		fileStore = mock(FileStore.class);
		s3Object = mock(S3Object.class);
		httpRequestBase = mock(HttpRequestBase.class);
		amazonBucketClient = new AmazonBucketClientImpl();
	}

	@Test
	public void downloadFileExactTest() throws IOException
	{
		when(client.getObject(any())).thenReturn(s3Object);
		when(s3Object.getObjectContent()).thenReturn(
				new S3ObjectInputStream(new FileInputStream(ResourceUtils.getFile(getClass(), "/test_data_only.xlsx")),
						httpRequestBase));

		amazonBucketClient.downloadFile(client, fileStore, "ID", "bucket", "key", null, false, null);
		verify(fileStore).store(any(), eq("bucket_ID" + File.separatorChar + "key.xlsx"));
	}

	@Test
	public void downloadFileExpression() throws IOException
	{
		ObjectListing objectListing = mock(ObjectListing.class);

		S3ObjectSummary s3ObjectSummary1 = mock(S3ObjectSummary.class);
		S3ObjectSummary s3ObjectSummary2 = mock(S3ObjectSummary.class);
		S3ObjectSummary s3ObjectSummary3 = mock(S3ObjectSummary.class);
		S3ObjectSummary s3ObjectSummary4 = mock(S3ObjectSummary.class);

		when(client.getObject(any())).thenReturn(s3Object);
		when(s3Object.getObjectContent()).thenReturn(
				new S3ObjectInputStream(new FileInputStream(ResourceUtils.getFile(getClass(), "/test_data_only.xlsx")),
						httpRequestBase));
		when(client.listObjects("bucket")).thenReturn(objectListing);

		Calendar c1 = Calendar.getInstance();
		c1.set(2017, 2, 7);
		Calendar c2 = Calendar.getInstance();
		c2.set(2016, 2, 7);
		Calendar c3 = Calendar.getInstance();
		c3.set(2015, 2, 7);
		Calendar c4 = Calendar.getInstance();
		c4.set(2017, 3, 7);

		when(s3ObjectSummary1.getKey()).thenReturn("kie");
		when(s3ObjectSummary1.getLastModified()).thenReturn(c1.getTime());
		when(s3ObjectSummary2.getKey()).thenReturn("keye");
		when(s3ObjectSummary2.getLastModified()).thenReturn(c2.getTime());
		when(s3ObjectSummary3.getKey()).thenReturn("keyw");
		when(s3ObjectSummary3.getLastModified()).thenReturn(c3.getTime());
		when(s3ObjectSummary4.getKey()).thenReturn("keyq");
		when(s3ObjectSummary4.getLastModified()).thenReturn(c4.getTime());

		when(objectListing.getObjectSummaries()).thenReturn(
				Arrays.asList(s3ObjectSummary1, s3ObjectSummary2, s3ObjectSummary3, s3ObjectSummary4));

		amazonBucketClient.downloadFile(client, fileStore, "ID", "bucket", "key(.*)", null, true, null);
		verify(fileStore).store(any(), eq("bucket_ID" + File.separatorChar + "keyq.xlsx"));
	}
}

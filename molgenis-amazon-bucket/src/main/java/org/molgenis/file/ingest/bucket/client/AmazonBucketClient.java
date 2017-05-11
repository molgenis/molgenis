package org.molgenis.file.ingest.bucket.client;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.s3.AmazonS3;
import org.molgenis.file.FileStore;

import java.io.File;
import java.io.IOException;

/**
 * Created by Bart on 5/10/2017.
 */
public interface AmazonBucketClient
{
	AmazonS3 getClient(String profile);

	File downloadFile(AmazonS3 s3Client, FileStore fileStore, String jobIdentifier, String bucketName, String keyName,
			boolean isExpression) throws IOException, AmazonClientException;

	void renameSheet(String targetEntityTypeName, File file);
}

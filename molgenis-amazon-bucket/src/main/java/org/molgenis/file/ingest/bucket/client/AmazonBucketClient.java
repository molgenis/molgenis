package org.molgenis.file.ingest.bucket.client;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.s3.AmazonS3;
import org.molgenis.file.FileStore;

import java.io.File;
import java.io.IOException;

public interface AmazonBucketClient
{
	AmazonS3 getClient(String accessKey, String secretKey, String region);

	File downloadFile(AmazonS3 s3Client, FileStore fileStore, String jobIdentifier, String bucketName, String keyName,
			boolean isExpression) throws IOException, AmazonClientException;
}

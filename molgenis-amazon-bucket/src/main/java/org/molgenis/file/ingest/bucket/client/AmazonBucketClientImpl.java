package org.molgenis.file.ingest.bucket.client;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.file.FileStore;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.Date;
import java.util.TreeMap;

@Component
public class AmazonBucketClientImpl implements AmazonBucketClient
{
	@Override
	public AmazonS3 getClient(String profile)
	{
		return AmazonS3ClientBuilder.standard().withCredentials(new ProfileCredentialsProvider(profile)).build();
	}

	@Override
	public File downloadFile(AmazonS3 s3Client, FileStore fileStore, String jobIdentifier, String bucketName,
			String keyName, boolean isExpression) throws IOException, AmazonClientException
	{
		String key;
		String identifier = "bucket_" + jobIdentifier;
		if (isExpression)
		{
			key = this.getMostRecentMatchingKey(s3Client, bucketName, keyName);
		}
		else
		{
			key = keyName;
		}
		S3Object s3Object = s3Client.getObject(new GetObjectRequest(bucketName, key));
		InputStream in = s3Object.getObjectContent();
		File folder = new File(fileStore.getStorageDir(), identifier);
		folder.mkdir();

		key = key.replaceAll("[\\/:*?\"<>|]", "_");
		String filename = identifier + '/' + key + ".xlsx";
		return fileStore.store(in, filename);
	}

	@Override
	public void renameSheet(String targetEntityTypeName, File file)
	{
		try
		{
			Workbook workbook = WorkbookFactory.create(new FileInputStream(file));
			if (workbook.getNumberOfSheets() == 1)
			{
				workbook.setSheetName(0, targetEntityTypeName);
				workbook.write(new FileOutputStream(file));
			}
			else
			{
				throw new MolgenisDataException(
						"Amazon Bucket imports to a specified entityType are only possible with one sheet");
			}
		}
		catch (Exception e)
		{
			throw new MolgenisDataException(e);
		}
	}

	private String getMostRecentMatchingKey(AmazonS3 s3Client, String bucketName, String regex)
	{
		ObjectListing objectListing = s3Client.listObjects(bucketName);
		TreeMap<Date, String> keys = new TreeMap<>();
		for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries())
		{
			if (objectSummary.getKey().matches(regex))
			{
				keys.put(objectSummary.getLastModified(), objectSummary.getKey());
			}
		}
		if (keys.size() == 0) throw new MolgenisDataException("No key matching regular expression: " + regex);
		return keys.lastEntry().getValue();
	}
}
package org.molgenis.file.ingest.amazon;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.cloudfront.model.InvalidArgumentException;
import com.amazonaws.services.s3.AmazonS3;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

import java.io.File;

import static java.util.Arrays.asList;
import static org.molgenis.file.ingest.amazon.BucketUtils.*;

public class AmazonBucketClient
{
	private static final String FILE = "file";
	private static final String OVERWRITE = "overwrite";
	private static final String BUCKET = "bucket";
	private static final String KEY = "key";
	private static final String PROFILE = "profile";
	private static final String MODE = "mode";

	//	private static String key = "lab1/testfile17feb.txt";
	//	private static String bucketName = "com.cartagenia.consortium.vkgl";
	//	private static String keyName = "lab1/testfile17feb.txt";
	//	private static String uploadFileName = "testfile17feb.txt";

	/*
	*
	* Install the amazon web services command line interface Instruction here: https://aws.amazon.com/cli/
	*
	* Configure the awscli: aws configure --profile [PROFILENAME]
	* Fill out AWS Access Key ID en AWS Secret Access Key. (you should have received these keys with your account)
	* Default region name en Default output format can be left empty, default values are used in this case.
	*
	**/

	public static void main(final String[] args)
	{
		try
		{
			OptionParser parser = createOptionParser();
			OptionSet options = parser.parse(args);
			run(options);
		}
		catch (AmazonServiceException ase)
		{
			handleAmazonServiceException(ase);
		}
		catch (AmazonClientException ace)
		{
			handleAmazonClientException(ace);
		}
		catch (Exception e)
		{
			System.out.println("args = [" + args + "] " + e.getMessage());
		}
	}

	private static void run(OptionSet options) throws Exception
	{

		String mode = (String) options.valueOf(MODE);
		File file = (File) options.valueOf(FILE);
		String bucket = (String) options.valueOf(BUCKET);
		String profile = (String) options.valueOf(PROFILE);
		String key = (String) options.valueOf(KEY);
		boolean overwrite = options.has(OVERWRITE);

		AmazonS3 s3client = getClient(profile);

		switch (mode)
		{
			case "upload":
				uploadFile(s3client, file, bucket, key);
				break;
			case "download":
				downloadFile(s3client, file, bucket, key, overwrite);
				break;
			default:
				throw new InvalidArgumentException("Invalid mode, valid options: 'download','upload'");
		}

	}

	private static OptionParser createOptionParser()
	{
		OptionParser parser = new OptionParser();
		parser.acceptsAll(asList("m", MODE), "Mode of use for the tool, can be either 'upload' or download'")
				.withRequiredArg().ofType(String.class).required();
		parser.acceptsAll(asList("f", FILE), "Name of the file to upload from/download to.").withRequiredArg()
				.ofType(File.class).required();
		parser.acceptsAll(asList("o", OVERWRITE), "Overwrite the exisiting file if it exists. (download only)");
		parser.acceptsAll(asList("b", BUCKET), "Name of the amazon bucket").withRequiredArg().ofType(String.class)
				.required();
		parser.acceptsAll(asList("k", KEY), "Key for the file in the bucket").withRequiredArg().ofType(String.class)
				.required();
		parser.acceptsAll(asList("p", PROFILE), "profile name to use for logging in on the bucket").withRequiredArg()
				.ofType(String.class).required();

		return parser;
	}

	private static void handleAmazonClientException(AmazonClientException ace)
	{
		System.out.println("Caught an AmazonClientException, which means" + " the client encountered "
				+ "an internal error while trying to " + "communicate with S3, "
				+ "such as not being able to access the network.");
		System.out.println("Error Message: " + ace.getMessage());
	}

	private static void handleAmazonServiceException(AmazonServiceException ase)
	{
		System.out.println("Caught an AmazonServiceException, which" + " means your request made it "
				+ "to Amazon S3, but was rejected with an error response" + " for some reason.");
		System.out.println("Error Message:    " + ase.getMessage());
		System.out.println("HTTP Status Code: " + ase.getStatusCode());
		System.out.println("AWS Error Code:   " + ase.getErrorCode());
		System.out.println("Error Type:       " + ase.getErrorType());
		System.out.println("Request ID:       " + ase.getRequestId());
	}
}
package org.molgenis.amazon.bucket.meta;

import org.molgenis.data.file.model.FileMetaMetaData;
import org.molgenis.data.meta.SystemEntityType;
import org.molgenis.jobs.model.JobExecutionMetaData;
import org.molgenis.jobs.model.JobPackage;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.AttributeType.BOOL;
import static org.molgenis.data.meta.AttributeType.XREF;
import static org.molgenis.jobs.model.JobPackage.PACKAGE_JOB;

@Component
public class AmazonBucketJobExecutionMetaData extends SystemEntityType
{
	private static final String SIMPLE_NAME = "AmazonBucketJobExecution";

	public static final String FILE = "file";
	public static final String BUCKET = "bucket";
	public static final String KEY = "key";
	public static final String EXPRESSION = "expression";
	public static final String ACCESS_KEY = "accessKey";
	public static final String SECRET_KEY = "secretKey";
	public static final String TARGET_ENTITY_ID = "targetEntityId";
	public static final String REGION = "region";
	public static final String EXTENSION = "extension";

	private final FileMetaMetaData fileMetaMetaData;
	private final JobExecutionMetaData jobExecutionMetaData;
	private final JobPackage jobPackage;

	public static final String AMAZON_BUCKET_JOB_TYPE = "AmazonBucketJob";

	AmazonBucketJobExecutionMetaData(FileMetaMetaData fileMetaMetaData, JobExecutionMetaData jobExecutionMetaData,
			JobPackage jobPackage)
	{
		super(SIMPLE_NAME, PACKAGE_JOB);
		this.fileMetaMetaData = requireNonNull(fileMetaMetaData);
		this.jobExecutionMetaData = requireNonNull(jobExecutionMetaData);
		this.jobPackage = requireNonNull(jobPackage);
	}

	@Override
	public void init()
	{
		setLabel("Amazon Bucket file ingest job execution");
		setExtends(jobExecutionMetaData);
		setPackage(jobPackage);
		addAttribute(BUCKET).setLabel("Bucket name")
							.setDescription("The name of the amazon bucket.")
							.setNillable(false);
		addAttribute(KEY).setLabel("Key").setDescription("Expression to match the file key").setNillable(false);
		addAttribute(EXPRESSION).setDataType(BOOL)
								.setLabel("Is key expression")
								.setDescription("Is the key an expression or an exact match")
								.setNillable(false)
								.setDefaultValue(Boolean.FALSE.toString());
		addAttribute(ACCESS_KEY).setLabel("the access key to be used to login to the amazon bucket")
								.setNillable(false)
								.setVisible(false);
		addAttribute(SECRET_KEY).setLabel("the secret key to be used to login to the amazon bucket")
								.setNillable(false)
								.setVisible(false);
		addAttribute(REGION).setLabel("Region").setDescription("The region of the amazon bucket.").setNillable(false);
		addAttribute(TARGET_ENTITY_ID).setLabel("Target EntityType ID").setNillable(true);
		addAttribute(FILE).setLabel("File")
						  .setDescription("The imported file.")
						  .setDataType(XREF)
						  .setRefEntity(fileMetaMetaData)
						  .setNillable(true);
		addAttribute(EXTENSION).setLabel("Extension")
							   .setDescription("Optional extension of the file, is not part of the key in the bucket")
							   .setNillable(true);
	}
}

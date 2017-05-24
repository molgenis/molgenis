package org.molgenis.data.matrix.meta;

import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.SystemEntityType;
import org.molgenis.file.model.FileMetaMetaData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.*;
import static org.molgenis.data.system.model.RootSystemPackage.PACKAGE_SYSTEM;

@Component
public class MatrixMetadata extends SystemEntityType
{
	public static final String FILE_LOCATION = "matrixFileLocation";
	public static final String ID = "id";
	public static final String SEPERATOR = "seperator";
	public static final String COLUMNMAPPINGFILE = "columnMappingFile";
	public static final String ROWMAPPINGFILE = "rowMappingFile";
	public static final String SIMPLE_NAME = "Matrix";
	public static final String PACKAGE = PACKAGE_SYSTEM;
	public static final String COMMA = "COMMA";
	public static final String TAB = "TAB";
	public static final String SEMICOLON = "SEMICOLON";
	public static final String PIPE = "PIPE";
	public final List<String> separators = Arrays.asList(COMMA, TAB, SEMICOLON, PIPE);

	private final FileMetaMetaData fileMetaMetaData;

	@Autowired
	public MatrixMetadata(FileMetaMetaData fileMetaMetaData)
	{
		super(SIMPLE_NAME, PACKAGE);

		this.fileMetaMetaData = requireNonNull(fileMetaMetaData);
	}

	@Override
	public void init()
	{
		setLabel("Matrix metadata");
		setDescription("metadata with information about the matrix file");
		addAttribute(ID, ROLE_ID);
		addAttribute(FILE_LOCATION, ROLE_LABEL, ROLE_LOOKUP).setLabel("Location of the matrix file").setUnique(true)
				.setNillable(false);
		addAttribute(SEPERATOR).setLabel("The seperator used in the matrix file").setNillable(false)
				.setDataType(AttributeType.ENUM).setEnumOptions(separators).setDefaultValue(COMMA);
		addAttribute(COLUMNMAPPINGFILE).setDescription(
				"Optional mapping file to map search parameters to columnheaders (format 'matrixValue TAB mappedValue', file should contain a header line)")
				.setNillable(true).setDataType(AttributeType.FILE).setRefEntity(fileMetaMetaData);
		addAttribute(ROWMAPPINGFILE).setDescription(
				"Optional mapping file to map search parameters to rowheaders (format 'matrixValue TAB mappedValue', file should contain a header line)")
				.setNillable(true).setDataType(AttributeType.FILE).setRefEntity(fileMetaMetaData);
	}

	public static char getSeparatorValue(String separatorName)
	{
		char value = ',';
		switch (separatorName)
		{
			case TAB:
				value = '\t';
				break;
			case COMMA:
				value = ',';
				break;
			case SEMICOLON:
				value = ';';
				break;
			case PIPE:
				value = '|';
				break;
		}
		return value;
	}
}

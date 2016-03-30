package org.molgenis.data.vcf.utils;

import com.google.common.io.BaseEncoding;
import org.apache.commons.lang3.StringUtils;
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.vcf.VcfRepository;
import org.molgenis.data.vcf.datastructures.Sample;
import org.molgenis.data.vcf.datastructures.Trio;
import org.molgenis.vcf.meta.VcfMetaInfo;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import static org.molgenis.data.vcf.VcfRepository.ALT;
import static org.molgenis.data.vcf.VcfRepository.CHROM;
import static org.molgenis.data.vcf.VcfRepository.POS;
import static org.molgenis.data.vcf.VcfRepository.REF;

public class VcfUtils {
    /**
     * Creates a internal molgenis id from a vcf entity
     *
     * @param vcfEntity
     * @return the id
     */
    public static String createId(Entity vcfEntity)
    {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append(StringUtils.strip(vcfEntity.get(CHROM).toString()));
        strBuilder.append("_");
        strBuilder.append(StringUtils.strip(vcfEntity.get(POS).toString()));
        strBuilder.append("_");
        strBuilder.append(StringUtils.strip(vcfEntity.get(REF).toString()));
        strBuilder.append("_");
        strBuilder.append(StringUtils.strip(vcfEntity.get(ALT).toString()));
        String idStr = strBuilder.toString();

        // use MD5 hash to prevent ids that are too long
        MessageDigest messageDigest;
        try
        {
            messageDigest = MessageDigest.getInstance("MD5");
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new RuntimeException(e);
        }
        byte[] md5Hash = messageDigest.digest(idStr.getBytes(Charset.forName("UTF-8")));

        // convert MD5 hash to string ids that can be safely used in URLs
        String id = BaseEncoding.base64Url().omitPadding().encode(md5Hash);

        return id;
    }

    static String getIdFromInfoField(String line)
    {
        int idStartIndex = line.indexOf("ID=") + 3;
        int idEndIndex = line.indexOf(",");
        return line.substring(idStartIndex, idEndIndex);
    }

    public static List<AttributeMetaData> getAtomicAttributesFromList(Iterable<AttributeMetaData> outputAttrs)
    {
        List<AttributeMetaData> result = new ArrayList<>();
        for (AttributeMetaData attributeMetaData : outputAttrs)
        {
            if (attributeMetaData.getDataType().getEnumType().equals(MolgenisFieldTypes.FieldTypeEnum.COMPOUND))
            {
                result.addAll(getAtomicAttributesFromList(attributeMetaData.getAttributeParts()));
            }
            else
            {
                result.add(attributeMetaData);
            }
        }
        return result;
    }

    public static Map<String, AttributeMetaData> getAttributesMapFromList(Iterable<AttributeMetaData> outputAttrs)
    {
        Map<String, AttributeMetaData> attributeMap = new LinkedHashMap<>();
        List<AttributeMetaData> attributes = getAtomicAttributesFromList(outputAttrs);
        for (AttributeMetaData attr : attributes)
        {
            attributeMap.put(attr.getName(), attr);
        }
        return attributeMap;
    }

    static String toVcfDataType(MolgenisFieldTypes.FieldTypeEnum dataType)
    {
        switch (dataType)
        {
            case BOOL:
                return VcfMetaInfo.Type.FLAG.toString();
            case LONG:
            case DECIMAL:
                return VcfMetaInfo.Type.FLOAT.toString();
            case INT:
                return VcfMetaInfo.Type.INTEGER.toString();
            case EMAIL:
            case ENUM:
            case HTML:
            case HYPERLINK:
            case STRING:
            case TEXT:
            case DATE:
            case DATE_TIME:
            case CATEGORICAL:
            case XREF:
            case CATEGORICAL_MREF:
            case MREF:
                return VcfMetaInfo.Type.STRING.toString();
            case COMPOUND:
            case FILE:
                throw new RuntimeException("invalid vcf data type " + dataType);
            default:
                throw new RuntimeException("unsupported vcf data type " + dataType);
        }
    }

    static Map<String, AttributeMetaData> attributesToMap(List<AttributeMetaData> attributeMetaDataList)
    {
        Map<String, AttributeMetaData> attributeMap = new HashMap<>();
        for (AttributeMetaData attributeMetaData : attributeMetaDataList)
        {
            attributeMap.put(attributeMetaData.getName(), attributeMetaData);
        }
        return attributeMap;

    }

    /**
     *
     * Get pedigree data from VCF Now only support child, father, mother No fancy data structure either Output:
     * result.put(childID, Arrays.asList(new String[]{motherID, fatherID}));
     *
     * @param inputVcfFile
     * @return
     * @throws FileNotFoundException
     */
    public static HashMap<String, Trio> getPedigree(File inputVcfFile) throws FileNotFoundException
    {
        HashMap<String, Trio> result = new HashMap<String, Trio>();

        Scanner inputVcfFileScanner = new Scanner(inputVcfFile, "UTF-8");
        String line = inputVcfFileScanner.nextLine();

        // if first line does not start with ##, we don't trust this file as VCF
        if (line.startsWith(VcfRepository.PREFIX))
        {
            while (inputVcfFileScanner.hasNextLine())
            {
                // detect pedigree line
                // expecting:
                // ##PEDIGREE=<Child=100400,Mother=100402,Father=100401>
                if (line.startsWith("##PEDIGREE"))
                {
                    System.out.println("Pedigree data line: " + line);
                    String childID = null;
                    String motherID = null;
                    String fatherID = null;

                    String lineStripped = line.replace("##PEDIGREE=<", "").replace(">", "");
                    String[] lineSplit = lineStripped.split(",", -1);
                    for (String element : lineSplit)
                    {
                        if (element.startsWith("Child"))
                        {
                            childID = element.replace("Child=", "");
                        }
                        else if (element.startsWith("Mother"))
                        {
                            motherID = element.replace("Mother=", "");
                        }
                        else if (element.startsWith("Father"))
                        {
                            fatherID = element.replace("Father=", "");
                        }
                        else
                        {
                            inputVcfFileScanner.close();
                            throw new MolgenisDataException(
                                    "Expected Child, Mother or Father, but found: " + element + " in line " + line);
                        }
                    }

                    if (childID != null && motherID != null && fatherID != null)
                    {
                        // good
                        result.put(childID, new Trio(new Sample(childID), new Sample(motherID), new Sample(fatherID)));
                    }
                    else
                    {
                        inputVcfFileScanner.close();
                        throw new MolgenisDataException("Missing Child, Mother or Father ID in line " + line);
                    }
                }

                line = inputVcfFileScanner.nextLine();
                if (!line.startsWith(VcfRepository.PREFIX))
                {
                    break;
                }
            }
        }

        inputVcfFileScanner.close();
        return result;
    }
}

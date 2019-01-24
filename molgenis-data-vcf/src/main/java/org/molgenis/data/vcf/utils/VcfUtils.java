package org.molgenis.data.vcf.utils;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.molgenis.data.meta.AttributeType.COMPOUND;
import static org.molgenis.data.vcf.model.VcfAttributes.ALT;
import static org.molgenis.data.vcf.model.VcfAttributes.CHROM;
import static org.molgenis.data.vcf.model.VcfAttributes.FILTER;
import static org.molgenis.data.vcf.model.VcfAttributes.ID;
import static org.molgenis.data.vcf.model.VcfAttributes.POS;
import static org.molgenis.data.vcf.model.VcfAttributes.QUAL;
import static org.molgenis.data.vcf.model.VcfAttributes.REF;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.util.UnexpectedEnumException;
import org.molgenis.vcf.meta.VcfMetaInfo;
import org.springframework.stereotype.Component;

@Component
public class VcfUtils {
  /**
   * Creates a internal molgenis id from a vcf entity
   *
   * @return the id
   */
  public static String createId(Entity vcfEntity) {
    String idStr =
        StringUtils.strip(vcfEntity.get(CHROM).toString())
            + "_"
            + StringUtils.strip(vcfEntity.get(POS).toString())
            + "_"
            + StringUtils.strip(vcfEntity.get(REF).toString())
            + "_"
            + StringUtils.strip(vcfEntity.get(ALT).toString())
            + "_"
            + StringUtils.strip(vcfEntity.get(ID).toString())
            + "_"
            + StringUtils.strip(vcfEntity.get(QUAL) != null ? vcfEntity.get(QUAL).toString() : "")
            + "_"
            + StringUtils.strip(
                vcfEntity.get(FILTER) != null ? vcfEntity.get(FILTER).toString() : "");

    // use MD5 hash to prevent ids that are too long
    MessageDigest messageDigest;
    try {
      messageDigest = MessageDigest.getInstance("MD5");
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
    byte[] md5Hash = messageDigest.digest(idStr.getBytes(UTF_8));

    // convert MD5 hash to string ids that can be safely used in URLs
    return Base64.getUrlEncoder().withoutPadding().encodeToString(md5Hash);
  }

  public static String getIdFromInfoField(String line) {
    int idStartIndex = line.indexOf("ID=") + 3;
    int idEndIndex = line.indexOf(',');
    return line.substring(idStartIndex, idEndIndex);
  }

  public static List<Attribute> getAtomicAttributesFromList(Iterable<Attribute> outputAttrs) {
    List<Attribute> result = new ArrayList<>();
    for (Attribute attribute : outputAttrs) {
      if (attribute.getDataType() == COMPOUND) {
        result.addAll(getAtomicAttributesFromList(attribute.getChildren()));
      } else {
        result.add(attribute);
      }
    }
    return result;
  }

  public static Map<String, Attribute> getAttributesMapFromList(Iterable<Attribute> outputAttrs) {
    Map<String, Attribute> attributeMap = new LinkedHashMap<>();
    List<Attribute> attributes = getAtomicAttributesFromList(outputAttrs);
    for (Attribute attribute : attributes) {
      attributeMap.put(attribute.getName(), attribute);
    }
    return attributeMap;
  }

  protected static String toVcfDataType(AttributeType dataType) {
    switch (dataType) {
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
      case ONE_TO_MANY:
        return VcfMetaInfo.Type.STRING.toString();
      case COMPOUND:
      case FILE:
        throw new RuntimeException("invalid vcf data type " + dataType);
      default:
        throw new UnexpectedEnumException(dataType);
    }
  }
}

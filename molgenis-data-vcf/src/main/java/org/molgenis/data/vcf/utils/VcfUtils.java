package org.molgenis.data.vcf.utils;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.molgenis.data.vcf.model.VcfAttributes.ALT;
import static org.molgenis.data.vcf.model.VcfAttributes.CHROM;
import static org.molgenis.data.vcf.model.VcfAttributes.FILTER;
import static org.molgenis.data.vcf.model.VcfAttributes.ID;
import static org.molgenis.data.vcf.model.VcfAttributes.POS;
import static org.molgenis.data.vcf.model.VcfAttributes.QUAL;
import static org.molgenis.data.vcf.model.VcfAttributes.REF;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.util.UnexpectedEnumException;
import org.molgenis.vcf.meta.VcfMetaInfo;
import org.springframework.stereotype.Component;

public class VcfUtils {

  private VcfUtils() {}

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
}

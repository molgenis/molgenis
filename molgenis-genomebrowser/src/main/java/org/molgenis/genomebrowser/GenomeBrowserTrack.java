package org.molgenis.genomebrowser;

import com.google.auto.value.AutoValue;
import com.google.common.base.Strings;
import java.util.stream.Collectors;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.rest.v2.RestControllerV2;
import org.molgenis.genomebrowser.meta.GenomeBrowserAttributes;
import org.molgenis.genomebrowser.meta.GenomeBrowserSettings;
import org.springframework.web.util.UriComponentsBuilder;

@AutoValue
@SuppressWarnings(
    "squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class GenomeBrowserTrack {

  @SuppressWarnings("squid:S00107") // Methods should not have too many parameters
  public static GenomeBrowserTrack create(
      String id,
      String label,
      String labelAttr,
      EntityType entity,
      GenomeBrowserSettings.TrackType trackType,
      Iterable<GenomeBrowserTrack> molgenisReferenceTracks,
      GenomeBrowserSettings.MolgenisReferenceMode molgenisReferenceMode,
      GenomeBrowserAttributes genomeBrowserAttrs,
      String actions,
      String attrs,
      String scoreAttr,
      String exonKey,
      String featureInfoPlugin) {
    return new AutoValue_GenomeBrowserTrack(
        id,
        label,
        labelAttr,
        entity,
        trackType,
        molgenisReferenceTracks,
        molgenisReferenceMode,
        genomeBrowserAttrs,
        actions,
        attrs,
        scoreAttr,
        exonKey,
        featureInfoPlugin);
  }

  public static GenomeBrowserTrack create(GenomeBrowserSettings settings) {
    return new AutoValue_GenomeBrowserTrack(
        settings.getIdentifier(),
        settings.getLabel(),
        settings.getLabelAttr().getName(),
        settings.getEntity(),
        settings.getTrackType(),
        settings.getMolgenisReferenceTracks().collect(Collectors.toList()),
        settings.getMolgenisReferenceMode(),
        settings.getGenomeBrowserAttrs(),
        settings.getActions(),
        settings.getAttrs(),
        settings.getScoreAttr(),
        settings.getExonKey(),
        settings.getFeatureInfoPlugin());
  }

  public abstract String getId();

  public abstract String getLabel();

  public abstract String getLabelAttr();

  public abstract EntityType getEntity();

  public abstract GenomeBrowserSettings.TrackType getTrackType();

  public abstract Iterable<GenomeBrowserTrack> getMolgenisReferenceTracks();

  public abstract GenomeBrowserSettings.MolgenisReferenceMode getMolgenisReferenceMode();

  public abstract GenomeBrowserAttributes getGenomeBrowserAttrs();

  @Nullable
  @CheckForNull
  public abstract String getActions();

  @Nullable
  @CheckForNull
  public abstract String getAttrs();

  @Nullable
  @CheckForNull
  public abstract String getScoreAttr();

  @Nullable
  @CheckForNull
  public abstract String getExonKey();

  @Nullable
  @CheckForNull
  public abstract String getFeatureInfoPlugin();

  public String toTrackString() {
    StringBuilder config = new StringBuilder("{");
    config.append(getConfigStringValue("name", getLabel()));
    config.append(",").append(getConfigStringValue("entity", getEntity().getId()));
    config.append(",").append(getConfigStringValue("tier_type", "molgenis"));
    config
        .append(",")
        .append(
            getConfigStringValue(
                "uri",
                UriComponentsBuilder.fromPath(RestControllerV2.BASE_URI)
                    .pathSegment(getEntity().getId())
                    .toUriString()));
    config
        .append(",")
        .append(
            String.format(
                "\"%s\":%s",
                "genome_attrs", getGenomeBrowserAttrsJSON(getGenomeBrowserAttrs()).toString()));
    if (getLabelAttr() != null)
      config.append(",").append(getConfigStringValue("label_attr", getLabelAttr()));
    String attrs = getAttrs();
    if (attrs != null)
      config.append(",").append(getConfigObjectValue("attrs", getAttrsJSON(attrs).toString()));
    if (getActions() != null)
      config.append(",").append(getConfigObjectValue("actions", JSONObject.quote(getActions())));
    if (getTrackType() != null)
      config.append(",").append(getConfigStringValue("track_type", getTrackType().name()));
    if (getScoreAttr() != null)
      config.append(",").append(getConfigStringValue("score_attr", getScoreAttr()));
    if (getExonKey() != null)
      config.append(",").append(getConfigStringValue("exon_key", getExonKey()));
    if (!Strings.isNullOrEmpty(getFeatureInfoPlugin()))
      config
          .append(",")
          .append(
              getConfigObjectValue(
                  "featureInfoPlugin",
                  String.format("function(f, info) {%s}", getFeatureInfoPlugin())));
    config.append("}");
    return config.toString();
  }

  private String getConfigObjectValue(String key, String value) {
    return String.format("\"%s\":%s", key, value);
  }

  private String getConfigStringValue(String key, String value) {
    return String.format("\"%s\":\"%s\"", key, value);
  }

  private JSONObject getGenomeBrowserAttrsJSON(GenomeBrowserAttributes genomeBrowserAttrs) {
    JSONObject genomeAttrsJSON = new JSONObject();
    genomeAttrsJSON.put("chr", genomeBrowserAttrs.getChrom());
    genomeAttrsJSON.put("pos", genomeBrowserAttrs.getPos());
    if (genomeBrowserAttrs.getRef() != null) {
      genomeAttrsJSON.put("ref", genomeBrowserAttrs.getRef());
    }
    if (genomeBrowserAttrs.getAlt() != null) {
      genomeAttrsJSON.put("alt", genomeBrowserAttrs.getAlt());
    }
    if (genomeBrowserAttrs.getStop() != null) {
      genomeAttrsJSON.put("stop", genomeBrowserAttrs.getStop());
    }
    return genomeAttrsJSON;
  }

  private JSONArray getAttrsJSON(String attrsString) {
    JSONArray attrsArray = new JSONArray();
    String[] attrs = attrsString.split(",");
    for (String attr : attrs) {
      attrsArray.put(attr);
    }
    return attrsArray;
  }
}

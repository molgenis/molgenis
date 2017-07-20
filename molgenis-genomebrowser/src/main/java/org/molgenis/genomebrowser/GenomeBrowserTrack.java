package org.molgenis.genomebrowser;

import com.google.auto.value.AutoValue;

import javax.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONObject;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.genomebrowser.meta.GenomeBrowserAttributes;
import org.molgenis.genomebrowser.meta.GenomeBrowserSettings;

import java.util.stream.Collectors;

@AutoValue
public abstract class GenomeBrowserTrack
{
	public static GenomeBrowserTrack create(String id, String labelAttr, EntityType entity,
			GenomeBrowserSettings.TrackType trackType, Iterable<GenomeBrowserTrack> molgenisReferenceTracks,
			GenomeBrowserSettings.MolgenisReferenceMode molgenisReferenceMode,
			GenomeBrowserAttributes genomeBrowserAttrs, String actions, String attrs, String scoreAttr, String exonKey)
	{
		return new AutoValue_GenomeBrowserTrack(id, labelAttr, entity, trackType, molgenisReferenceTracks,
				molgenisReferenceMode, genomeBrowserAttrs, actions, attrs, scoreAttr, exonKey);
	}

	public static GenomeBrowserTrack create(GenomeBrowserSettings settings)
	{
		return new AutoValue_GenomeBrowserTrack(settings.getIdentifier(), settings.getLabelAttr().getName(),
				settings.getEntity(), settings.getTrackType(),
				settings.getMolgenisReferenceTracks().collect(Collectors.toList()), settings.getMolgenisReferenceMode(),
				settings.getGenomeBrowserAttrs(), settings.getActions(), settings.getAttrs(), settings.getScoreAttr(),
				settings.getExonKey());
	}

	public abstract String getId();

	public abstract String getLabelAttr();

	public abstract EntityType getEntity();

	public abstract GenomeBrowserSettings.TrackType getTrackType();

	@Nullable
	public abstract Iterable<GenomeBrowserTrack> getMolgenisReferenceTracks();

	public abstract GenomeBrowserSettings.MolgenisReferenceMode getMolgenisReferenceMode();

	public abstract GenomeBrowserAttributes getGenomeBrowserAttrs();

	@Nullable
	public abstract String getActions();

	@Nullable
	public abstract String getAttrs();

	@Nullable
	public abstract String getScoreAttr();

	@Nullable
	public abstract String getExonKey();

	public JSONObject toTrackJson()
	{
		JSONObject json = new JSONObject();
		json.put("name", getEntity().getLabel());
		json.put("uri", "http://localhost:8080/api/v2/" + getEntity().getId() + "?" + getId());
		json.put("tier_type", "molgenis");
		json.put("genome_attrs", getGenomeBrowserAttrsJSON(getGenomeBrowserAttrs()));
		if (getLabelAttr() != null) json.put("label_attr", getLabelAttr());
		if (getAttrs() != null) json.put("attrs", getAttrsJSON(getAttrs()));
		if (getActions() != null) json.put("actions", getActions());
		if (getTrackType() != null) json.put("track_type", getTrackType());
		if (getScoreAttr() != null) json.put("score_attr", getScoreAttr());
		if (getExonKey() != null) json.put("exon_key", getExonKey());
		return json;
	}

	private JSONObject getGenomeBrowserAttrsJSON(GenomeBrowserAttributes genomeBrowserAttrs)
	{
		JSONObject genomeAttrsJSON = new JSONObject();
		genomeAttrsJSON.put("chr", genomeBrowserAttrs.getChrom());
		genomeAttrsJSON.put("pos", genomeBrowserAttrs.getPos());
		if (genomeBrowserAttrs.getRef() != null)
		{
			genomeAttrsJSON.put("ref", genomeBrowserAttrs.getRef());
		}
		if (genomeBrowserAttrs.getAlt() != null)
		{
			genomeAttrsJSON.put("alt", genomeBrowserAttrs.getAlt());
		}
		if (genomeBrowserAttrs.getStop() != null)
		{
			genomeAttrsJSON.put("stop", genomeBrowserAttrs.getStop());
		}
		return genomeAttrsJSON;
	}

	private JSONArray getAttrsJSON(String attrsString)
	{
		JSONArray attrsArray = new JSONArray();
		String[] attrs = attrsString.split(",");
		for (String attr : attrs)
		{
			attrsArray.put(attr);
		}
		return attrsArray;
	}
}

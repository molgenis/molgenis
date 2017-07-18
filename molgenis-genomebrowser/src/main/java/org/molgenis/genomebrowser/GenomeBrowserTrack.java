package org.molgenis.genomebrowser;

import org.molgenis.data.meta.model.EntityType;
import org.molgenis.genomebrowser.meta.GenomeBrowserAttributes;
import org.molgenis.genomebrowser.meta.GenomeBrowserSettings;

import java.util.stream.Collectors;

public final class GenomeBrowserTrack
{
	public final String id;
	public final String labelAttr;
	public final EntityType entity;
	public final GenomeBrowserSettings.TrackType trackType;
	public final Iterable<GenomeBrowserTrack> molgenisReferenceTracks;
	public final GenomeBrowserSettings.MolgenisReferenceMode molgenisReferenceMode;
	public final GenomeBrowserAttributes genomeBrowserAttrs;
	public final String actions;
	public final String attrs;
	public final String scoreAttr;
	public final String exonKey;

	public GenomeBrowserTrack(String id, String labelAttr, EntityType entity, GenomeBrowserSettings.TrackType trackType,
			Iterable<GenomeBrowserTrack> molgenisReferenceTracks,
			GenomeBrowserSettings.MolgenisReferenceMode molgenisReferenceMode,
			GenomeBrowserAttributes genomeBrowserAttrs, String actions, String attrs, String scoreAttr, String exonKey)
	{
		this.id = id;
		this.labelAttr = labelAttr;
		this.entity = entity;
		this.trackType = trackType;
		this.molgenisReferenceTracks = molgenisReferenceTracks;
		this.molgenisReferenceMode = molgenisReferenceMode;
		this.genomeBrowserAttrs = genomeBrowserAttrs;
		this.actions = actions;
		this.attrs = attrs;
		this.scoreAttr = scoreAttr;
		this.exonKey = exonKey;
	}

	public GenomeBrowserTrack(GenomeBrowserSettings settings)
	{
		this.id = settings.getIdentifier();
		this.labelAttr = settings.getLabelAttr().getName();
		this.entity = settings.getEntity();
		this.trackType = settings.getTrackType();
		this.molgenisReferenceTracks = settings.getMolgenisReferenceTracks().collect(Collectors.toList());
		this.molgenisReferenceMode = settings.getMolgenisReferenceMode();
		this.genomeBrowserAttrs = settings.getGenomeBrowserAttrs();
		this.actions = settings.getActions();
		this.attrs = settings.getAttrs();
		this.scoreAttr = settings.getScoreAttr();
		this.exonKey = settings.getExonKey();
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		GenomeBrowserTrack that = (GenomeBrowserTrack) o;

		if (!id.equals(that.id)) return false;
		if (!labelAttr.equals(that.labelAttr)) return false;
		if (!entity.equals(that.entity)) return false;
		if (trackType != that.trackType) return false;
		if (molgenisReferenceTracks != null ? !molgenisReferenceTracks.equals(that.molgenisReferenceTracks) :
				that.molgenisReferenceTracks != null) return false;
		if (molgenisReferenceMode != that.molgenisReferenceMode) return false;
		if (!genomeBrowserAttrs.equals(that.genomeBrowserAttrs)) return false;
		if (actions != null ? !actions.equals(that.actions) : that.actions != null) return false;
		if (attrs != null ? !attrs.equals(that.attrs) : that.attrs != null) return false;
		if (scoreAttr != null ? !scoreAttr.equals(that.scoreAttr) : that.scoreAttr != null) return false;
		return exonKey != null ? exonKey.equals(that.exonKey) : that.exonKey == null;
	}

	@Override
	public int hashCode()
	{
		int result = id.hashCode();
		result = 31 * result + labelAttr.hashCode();
		result = 31 * result + entity.hashCode();
		result = 31 * result + trackType.hashCode();
		result = 31 * result + (molgenisReferenceTracks != null ? molgenisReferenceTracks.hashCode() : 0);
		result = 31 * result + molgenisReferenceMode.hashCode();
		result = 31 * result + genomeBrowserAttrs.hashCode();
		result = 31 * result + (actions != null ? actions.hashCode() : 0);
		result = 31 * result + (attrs != null ? attrs.hashCode() : 0);
		result = 31 * result + (scoreAttr != null ? scoreAttr.hashCode() : 0);
		result = 31 * result + (exonKey != null ? exonKey.hashCode() : 0);
		return result;
	}

	@Override
	public String toString()
	{
		return "GenomeBrowserTrack{" + "id='" + id + '\'' + ", labelAttr='" + labelAttr + '\'' + ", entity=" + entity
				+ ", trackType=" + trackType + ", molgenisReferenceTracks=" + molgenisReferenceTracks
				+ ", molgenisReferenceMode=" + molgenisReferenceMode + ", genomeBrowserAttrs=" + genomeBrowserAttrs
				+ ", actions='" + actions + '\'' + ", attrs='" + attrs + '\'' + ", scoreAttr='" + scoreAttr + '\''
				+ ", exonKey='" + exonKey + '\'' + '}';
	}

	public String getId()
	{
		return id;
	}

	public String getLabelAttr()
	{
		return labelAttr;
	}

	public EntityType getEntity()
	{
		return entity;
	}

	public GenomeBrowserSettings.TrackType getTrackType()
	{
		return trackType;
	}

	public Iterable<GenomeBrowserTrack> getMolgenisReferenceTracks()
	{
		return molgenisReferenceTracks;
	}

	public GenomeBrowserSettings.MolgenisReferenceMode getMolgenisReferenceMode()
	{
		return molgenisReferenceMode;
	}

	public GenomeBrowserAttributes getGenomeBrowserAttrs()
	{
		return genomeBrowserAttrs;
	}

	public String getActions()
	{
		return actions;
	}

	public String getAttrs()
	{
		return attrs;
	}

	public String getScoreAttr()
	{
		return scoreAttr;
	}

	public String getExonKey()
	{
		return exonKey;
	}
}

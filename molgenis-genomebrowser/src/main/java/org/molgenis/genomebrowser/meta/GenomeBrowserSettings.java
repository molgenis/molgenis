package org.molgenis.genomebrowser.meta;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.StaticEntity;
import org.molgenis.genomebrowser.GenomeBrowserTrack;

import javax.annotation.Nullable;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class GenomeBrowserSettings extends StaticEntity
{
	public GenomeBrowserSettings(Entity entity)
	{
		super(entity);
	}

	public GenomeBrowserSettings(EntityType entityType)
	{
		super(entityType);
	}

	public GenomeBrowserSettings(String identifier, EntityType entityType)
	{
		super(identifier, entityType);
	}

	public String getIdentifier()
	{
		return getString(GenomeBrowserSettingsMetadata.IDENTIFIER);
	}

	public void setIdentifier(String identifier)
	{
		set(GenomeBrowserSettingsMetadata.IDENTIFIER, identifier);
	}

	public String getLabel()
	{
		return getString(GenomeBrowserSettingsMetadata.LABEL);
	}

	public void setLabel(String label)
	{
		set(GenomeBrowserSettingsMetadata.LABEL, label);
	}

	public Attribute getLabelAttr()
	{
		return getEntity(GenomeBrowserSettingsMetadata.LABEL_ATTR, Attribute.class);
	}

	public void setLabelAttr(Attribute labelAttr)
	{
		set(GenomeBrowserSettingsMetadata.LABEL_ATTR, labelAttr);
	}

	public EntityType getEntity()
	{
		return getEntity(GenomeBrowserSettingsMetadata.ENTITY, EntityType.class);
	}

	public void setEntity(EntityType entity)
	{
		set(GenomeBrowserSettingsMetadata.ENTITY, entity);
	}

	public TrackType getTrackType()
	{
		String string = getString(GenomeBrowserSettingsMetadata.TRACK_TYPE);
		return string != null ? TrackType.valueOf(string.toUpperCase()) : null;
	}

	public void setTrackType(TrackType trackType)
	{
		set(GenomeBrowserSettingsMetadata.TRACK_TYPE, trackType.toString().toUpperCase());
	}

	public Stream<GenomeBrowserTrack> getMolgenisReferenceTracks()
	{
		return StreamSupport.stream(getEntities(GenomeBrowserSettingsMetadata.MOLGENIS_REFERENCE_TRACKS,
				GenomeBrowserSettings.class).spliterator(), false).map(settings -> GenomeBrowserTrack.create(settings));
	}

	public void setMolgenisReferenceTracks(GenomeBrowserSettings molgenisReferenceTracks)
	{
		set(GenomeBrowserSettingsMetadata.MOLGENIS_REFERENCE_TRACKS, molgenisReferenceTracks);
	}

	public MolgenisReferenceMode getMolgenisReferenceMode()
	{
		String string = getString(GenomeBrowserSettingsMetadata.MOLGENIS_REFERENCES_MODE);
		return string != null ? MolgenisReferenceMode.valueOf(
				getString(GenomeBrowserSettingsMetadata.MOLGENIS_REFERENCES_MODE).toUpperCase()) : null;
	}

	public void setMolgenisReferenceMode(MolgenisReferenceMode mode)
	{
		set(GenomeBrowserSettingsMetadata.MOLGENIS_REFERENCES_MODE, mode.toString().toUpperCase());
	}

	public GenomeBrowserAttributes getGenomeBrowserAttrs()
	{
		return getEntity(GenomeBrowserSettingsMetadata.GENOME_BROWSER_ATTRS, GenomeBrowserAttributes.class);
	}

	public void setGenomeBrowserAttrs(GenomeBrowserAttributes genomeBrowserAttrs)
	{
		set(GenomeBrowserSettingsMetadata.GENOME_BROWSER_ATTRS, genomeBrowserAttrs);
	}

	@Nullable
	public String getActions()
	{
		return getString(GenomeBrowserSettingsMetadata.ACTIONS);
	}

	public void setActions(String actions)
	{
		set(GenomeBrowserSettingsMetadata.ACTIONS, actions);
	}

	@Nullable
	public String getAttrs()
	{
		return getString(GenomeBrowserSettingsMetadata.ATTRS);
	}

	public void setAttrs(String attrs)
	{
		set(GenomeBrowserSettingsMetadata.ATTRS, attrs);
	}

	@Nullable
	public String getScoreAttr()
	{
		return getString(GenomeBrowserSettingsMetadata.SCORE_ATTR);
	}

	public void setScoreAttr(String scoreAttr)
	{
		set(GenomeBrowserSettingsMetadata.SCORE_ATTR, scoreAttr);
	}

	@Nullable
	public String getExonKey()
	{
		return getString(GenomeBrowserSettingsMetadata.EXON_KEY);
	}

	public void setExonKey(String exonKe)
	{
		set(GenomeBrowserSettingsMetadata.EXON_KEY, exonKe);
	}

	public enum TrackType
	{
		VARIANT, NUMERIC, EXON
	}

	public enum MolgenisReferenceMode
	{
		ALL, NONE, CONFIGURED
	}
}

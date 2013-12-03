package org.molgenis.omx.protocolviewer;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import javax.mail.MessagingException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Part;

import org.molgenis.catalog.CatalogMeta;
import org.molgenis.catalog.UnknownCatalogException;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.omx.study.StudyDataRequest;
import org.molgenis.study.StudyDefinition;
import org.molgenis.study.UnknownStudyDefinitionException;

public interface ProtocolViewerService
{
    /**
     * Gets all active catalog meta data
     *
     * @return meta data of active catalogs
     */
    Iterable<CatalogMeta> getCatalogs();

    /**
     * Gets the current study definition draft for the current logged in user
     *
     * @throws UnknownCatalogException
     * @return the current study definition draft or null if no study definition draft exists
     */
    StudyDefinition getStudyDefinitionDraftForCurrentUser(String catalogId) throws UnknownCatalogException;

    /**
     * Creates a study definition draft for the current logged in user
     *
     * @param catalogId id of the catalog used for this study definition
     * @throws UnknownCatalogException
     * @return empty draft study definition
     */
    StudyDefinition createStudyDefinitionDraftForCurrentUser(String catalogId) throws UnknownCatalogException;

	/**
	 * Returns order for the current user
	 * 
	 * @return
	 */
	Iterable<StudyDefinition> getStudyDefinitionsForCurrentUser();

	/**
	 * Returns the given order for the current user
	 * 
	 * @param id
     * @throws UnknownStudyDefinitionException
	 * @return
	 */
    StudyDefinition getStudyDefinitionForCurrentUser(Integer id) throws UnknownStudyDefinitionException;

    /**
     * Place an order for the current user
     *
     * @param studyName
     * @param requestForm
     * @throws MessagingException
     * @throws IOException
     * @throws UnknownCatalogException
     */
    void submitStudyDefinitionDraftForCurrentUser(String studyName, Part requestForm, String catalogId) throws MessagingException, IOException, UnknownCatalogException, UnknownStudyDefinitionException;

    /**
     * Update the study definition draft with the given catalog items
     *
     * @param catalogItemIds
     * @throws UnknownCatalogException
     */
    void updateStudyDefinitionDraftForCurrentUser(List<Integer> catalogItemIds, String catalogId) throws UnknownCatalogException;

    /**
     * Write the study definition draft for the current user to the given stream
     *
     * @param outputStream
     * @throws IOException
     * @throws UnknownCatalogException
     */
    void createStudyDefinitionDraftXlsForCurrentUser(OutputStream outputStream, String catalogId) throws IOException, UnknownCatalogException;
}
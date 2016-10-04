// WikiPathways Java library,
// Copyright 2014 WikiPathways
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
package org.molgenis.wikipathways.client;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface WikiPathwaysPortType extends Remote
{
	boolean updatePathway(String pwId, String description, String gpml, int revision, WSAuth auth)
			throws RemoteException;

	WSSearchResult[] findPathwaysByXref(String[] ids, String[] codes) throws RemoteException;

	byte[] getColoredPathway(String pwId, String revision, String[] graphId, String[] color, String fileType)
			throws RemoteException;

	boolean saveOntologyTag(String pwId, String term, String termId, WSAuth auth) throws RemoteException;

	WSPathwayInfo[] getPathwaysByParentOntologyTerm(String term) throws RemoteException;

	WSPathwayInfo getPathwayInfo(String pwId) throws RemoteException;

	WSPathwayInfo[] listPathways(String organism) throws RemoteException;

	WSSearchResult[] findPathwaysByLiterature(String query) throws RemoteException;

	boolean removeCurationTag(String pwId, String tagName, WSAuth auth) throws RemoteException;

	String[] listOrganisms() throws RemoteException;

	WSCurationTag[] getCurationTagsByName(String tagName) throws RemoteException;

	boolean saveCurationTag(String pwId, String tagName, String tagText, int revision, WSAuth auth)
			throws RemoteException;

	WSPathwayHistory getPathwayHistory(String pwId, String timestamp) throws RemoteException;

	String[] getXrefList(String pwId, String code) throws RemoteException;

	WSSearchResult[] findPathwaysByText(String query, String species) throws RemoteException;

	WSPathwayInfo createPathway(String gpml, WSAuth auth) throws RemoteException;

	byte[] getPathwayAs(String fileType, String pwId, int revision) throws RemoteException;

	WSCurationTagHistory[] getCurationTagHistory(String pwId, String timestamp) throws RemoteException;

	WSCurationTag[] getCurationTags(String pwId) throws RemoteException;

	WSSearchResult[] findInteractions(String query) throws RemoteException;

	String login(String name, String pass) throws RemoteException;

	WSPathwayInfo[] getRecentChanges(String timestamp) throws RemoteException;

	WSPathway getPathway(String pwId, int revision) throws RemoteException;

	WSOntologyTerm[] getOntologyTermsByPathway(String pwId) throws RemoteException;

	WSPathwayInfo[] getPathwaysByOntologyTerm(String term) throws RemoteException;

	String getUserByOrcid(String orcid) throws RemoteException;

	boolean removeOntologyTag(String pwId, String termId, WSAuth auth) throws RemoteException;
}
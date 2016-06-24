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

public interface WikiPathwaysPortType extends Remote {
    public boolean updatePathway(String pwId, String description, String gpml, int revision, WSAuth auth) throws RemoteException;
    public WSSearchResult[] findPathwaysByXref(String[] ids, String[] codes) throws RemoteException;
    public byte[] getColoredPathway(String pwId, String revision, String[] graphId, String[] color, String fileType) throws RemoteException;
    public boolean saveOntologyTag(String pwId, String term, String termId, WSAuth auth) throws RemoteException;
    public WSPathwayInfo[] getPathwaysByParentOntologyTerm(String term) throws RemoteException;
    public WSPathwayInfo getPathwayInfo(String pwId) throws RemoteException;
    public WSPathwayInfo[] listPathways(String organism) throws RemoteException;
    public WSSearchResult[] findPathwaysByLiterature(String query) throws RemoteException;
    public boolean removeCurationTag(String pwId, String tagName, WSAuth auth) throws RemoteException;
    public String[] listOrganisms() throws RemoteException;
    public WSCurationTag[] getCurationTagsByName(String tagName) throws RemoteException;
    public boolean saveCurationTag(String pwId, String tagName, String tagText, int revision, WSAuth auth) throws RemoteException;
    public WSPathwayHistory getPathwayHistory(String pwId, String timestamp) throws RemoteException;
    public String[] getXrefList(String pwId, String code) throws RemoteException;
    public WSSearchResult[] findPathwaysByText(String query, String species) throws RemoteException;
    public WSPathwayInfo createPathway(String gpml, WSAuth auth) throws RemoteException;
    public byte[] getPathwayAs(String fileType, String pwId, int revision) throws RemoteException;
    public WSCurationTagHistory[] getCurationTagHistory(String pwId, String timestamp) throws RemoteException;
    public WSCurationTag[] getCurationTags(String pwId) throws RemoteException;
    public WSSearchResult[] findInteractions(String query) throws RemoteException;
    public String login(String name, String pass) throws RemoteException;
    public WSPathwayInfo[] getRecentChanges(String timestamp) throws RemoteException;
    public WSPathway getPathway(String pwId, int revision) throws RemoteException;
    public WSOntologyTerm[] getOntologyTermsByPathway(String pwId) throws RemoteException;
    public WSPathwayInfo[] getPathwaysByOntologyTerm(String term) throws RemoteException;
    public String getUserByOrcid(String orcid) throws RemoteException;
    public boolean removeOntologyTag(String pwId, String termId, WSAuth auth) throws RemoteException;
}
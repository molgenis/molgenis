package org.molgenis.jena;

import com.hp.hpl.jena.query.*;

public class testBioModels
{
    public static void main( String[] args ) {
        String biomodelsQuery = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" +
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                "PREFIX dc: <http://purl.org/dc/elements/1.1/>\n" +
                "PREFIX dcterms: <http://purl.org/dc/terms/>\n" +
                "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" +
                "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>\n" +
                "PREFIX sbmlrdf: <http://identifiers.org/biomodels.vocabulary#>\n" +
                "\n" +
                "SELECT ?speciesid ?name WHERE {\n" +
                " <http://identifiers.org/biomodels.db/BIOMD0000000001> sbmlrdf:species ?speciesid . \n" +
                " ?speciesid sbmlrdf:name ?name}";

        Query query = QueryFactory.create(biomodelsQuery); //s2 = the query above
        QueryExecution qExe = QueryExecutionFactory.sparqlService( "http://www.ebi.ac.uk/rdf/services/biomodels/sparql", query );
        ResultSet results = qExe.execSelect();
        ResultSetFormatter.out(System.out, results, query) ;
    }
}
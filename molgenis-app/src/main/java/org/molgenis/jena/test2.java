package org.molgenis.jena;

import com.hp.hpl.jena.query.*;

public class test2
{
    public static void main( String[] args ) {
        String s2 = "PREFIX  g:    <http://www.w3.org/2003/01/geo/wgs84_pos#>\n" +
                "PREFIX  rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "PREFIX  onto: <http://dbpedia.org/ontology/>\n" +
                "\n" +
                "SELECT  ?subject ?stadium ?lat ?long\n" +
                "WHERE\n" +
                "  { ?subject g:lat ?lat .\n" +
                "    ?subject g:long ?long .\n" +
                "    ?subject <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> onto:Stadium .\n" +
                "    ?subject rdfs:label ?stadium\n" +
                //"    FILTER ( ( ( ( ( ?lat >= 52.4714 ) && ( ?lat <= 57.4914 ) ) && ( ?long >= -1.89258 ) ) && ( ?long <= 10.10542 ) ) && ( lang(?stadium) = \"en\" ) )\n" +
                "  }\n" +
                "LIMIT   1000";

        Query query = QueryFactory.create(s2);
        QueryExecution qExe = QueryExecutionFactory.sparqlService( "http://dbpedia.org/sparql", query );
        ResultSet results = qExe.execSelect();
        ResultSetFormatter.out(System.out, results, query) ;
    }
}
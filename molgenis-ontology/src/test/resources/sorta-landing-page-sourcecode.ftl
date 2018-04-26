<br>
<div class="row">
    <div class="col-md-offset-2 col-md-8">
        <p style="font-size: 24pt;font-family: arial, helvetica, sans-serif;">Welcome to SORTA</p>
    </div>
</div>
<div class="row">
    <div class="col-md-offset-2 col-md-8">
        <p class="pull-right" style="font-size:16pt;font-family: arial, helvetica, sans-serif;"><strong>S</strong>ystem
            for&nbsp;<strong>O</strong>ntology-based&nbsp;<strong>R</strong>e-coding and&nbsp;<strong>T</strong>echnical&nbsp;<strong>A</strong>nnotation
        </p>
    </div>
</div>
<br>
<div class="row">
    <div class="col-md-offset-2 col-md-8" style="font-size: 14pt; font-family: arial, helvetica, sans-serif;">
        <p style="font-size: 14pt; font-family: arial, helvetica, sans-serif;"><strong>Background</strong></p>
    </div>
</div>
<div class="row">
    <div class="col-md-offset-2 col-md-8"
         style="font-size:14pt; font-family: arial, helvetica, sans-serif; text-align: justify;">
        <p style="font-size: 14pt; font-family: arial, helvetica, sans-serif;">SORTA, a matching tool built in <a
                href="https://github.com/molgenis/molgenis" target="_blank">MOLGENIS</a>,
            is able to semi-automatically match data values with standard codes such as ontologies or local
            terminologies. For each data value, SORTA provides a list of the most relevant standard codes based on the
            lexical similarity in percentage, users can then pick the correct matches from the suggested list.</p>
    </div>
</div>
<div class="row">
    <div class="col-md-offset-2 col-md-8">
        <hr></hr>
        <p style="font-size: 14pt; font-family: arial, helvetica, sans-serif;"><strong>Demo</strong></p>
        <p style="font-size: 14pt; font-family: arial, helvetica, sans-serif; text-align: justify;">
            Click <a href="https://molgenis19.target.rug.nl/menu/main/sorta_anonymous/">here for a demo.</a> <br>The
            demo version <strong>does not</strong> have full functionality, data <strong>will not be saved</strong> in
            the database
            and <strong>will be lost</strong> after the session expires.
            To get access to SORTA, please contact the <a href="mailto:ChaoPang229@gmail.com">administrator</a> for
            login credentials. Try out the examples below, you can directly get match results by clicking one of the two
            example links.
        </p>
        <br>
        <form action="https://molgenis19.target.rug.nl/menu/main/sorta_anonymous/match" method="POST">
            <input name="selectOntologies" type="hidden" value="http://purl.obolibrary.org/obo/hp.owl"/>
            <input name="inputTerms" type="hidden" value="Name
			hearing impairment
			protruding eyeball
			hyperextensibility at elbow joint"/>
            <p style="font-size: 14pt; font-family: arial, helvetica, sans-serif;">
                <button class="btn btn-lg btn-link" type="submit" style="padding-left:0px;">Example 1:</button>
                <span style="font-size:14pt;margin-left:-15px;">matching with Human Phenotype Ontology</span>
            </p>
            <div class="highlight">
                <p style="font-size: 10pt; font-family: arial, helvetica, sans-serif;">To reproduce the matching
                    results, 1. copy the example below 2. click the demo link above 3. paste the example into the text
                    area 4. select the human phenotype ontology</p>
                <pre>Name<br>Hearing impairment<br>protruding eyeball<br>hyperextensibility at elbow joint</pre>
            </div>
        </form>
        <br>
        <form action="https://molgenis19.target.rug.nl/menu/main/sorta_anonymous/match" method="POST">
            <input name="selectOntologies" type="hidden" value="http://www.orpha.net/ontology/orphanet.owl"/>
            <input name="inputTerms" type="hidden" value="Name;Synonym;OMIM
			3-oxoacyl-CoA thiolase deficiency;peroxisomal thiolase deficiency;604054
			2-ketoglutarate dehydrogenase deficiency;2-ketoglutaric aciduria;203740
			acid sphingomyelinase deficiency;sfingomyelinase deficiency;607608"/>
            <p style="font-size: 14pt; font-family: arial, helvetica, sans-serif;">
                <button class="btn  btn-lg btn-link" type="submit" style="padding-left:0px;">Example 2:</button>
                <span style="font-size:14pt;margin-left:-15px;">matching with Orphanet</span>
            </p>
            <div class="highlight">
                <p style="font-size: 10pt; font-family: arial, helvetica, sans-serif;">To reproduce the matching
                    results, 1. copy the example below 2. click the demo link above 3. paste the example into the text
                    area 4. select the orphanet ontology</p>
                <pre>Name;Synonym;OMIM<br>3-oxoacyl-CoA thiolase deficiency;peroxisomal thiolase deficiency;604054<br>2-ketoglutarate dehydrogenase deficiency;2-ketoglutaric aciduria;203740<br>acid sphingomyelinase deficiency;sfingomyelinase deficiency;607608</pre>
            </div>
        </form>
    </div>
</div>
<br>
<div class="row">
    <div class="col-md-offset-2 col-md-8">
        <hr></hr>
        <p style="font-size: 14pt; font-family: arial, helvetica, sans-serif;"><strong>Technical design</strong></p>
        <p style="font-size: 14pt; font-family: arial, helvetica, sans-serif; text-align: justify;">
            SORTA is built based on Lucene in combination with the N-gram string matching algorithm to achieve high
            performance and accuracy.
            Lucene matching scores are too abstract for users to understand and they are not comparable between each
            other.
            Therefore we use the N-gram algorithm to re-calculate the similarity scores (in percentages) between data
            values and the concepts retrieved by Lucene.
            The new similarity scores are more clear and comparable, enabling us to explore the uniform cut-off value.
        </p>
        <ul style="font-size: 14pt; font-family: arial, helvetica, sans-serif;">
            <li>Step 1 - Index the standard concepts in Lucene to establish a knowledge base.</li>
            <li>Step 2 - Lucene retrieves the most relevant concepts for data values from the knowledge base.</li>
            <li>Step 3 - The N-gram algorithm is applied to re-calculate the similarity scores between data values and
                concepts retrieved by Lucene.
            </li>
            <li>Step 4 - Users can pick the correct matches from the list of concepts sorted based on N-gram similarity
                scores.
            </li>
        </ul>
    </div>
</div>
<br>
<div class="row">
    <div class="col-md-offset-2 col-md-8">
        <img src="https://molgenis26.target.rug.nl/downloads/sorta/figures/figure.1.png" alt="Not available"
             class="img-responsive" style="border:1px solid grey"/>
    </div>
</div>
<br>
<div class="row">
    <div class="col-md-offset-2 col-md-8">
        <p style="font-size: 14pt; font-family: arial, helvetica, sans-serif;"><strong>Ontology model</strong></p>
        <p style="font-size: 14pt; font-family: arial, helvetica, sans-serif; text-align: justify;">Standard codes
            (ontologies) can be imported using the <a href="https://github.com/molgenis/molgenis/wiki/EMX-upload-format"
                                                      target="_blank">EMX format</a>.&nbsp;</p>
    </div>
</div>
<br>
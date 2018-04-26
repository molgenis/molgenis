<div class="modal-header">
    <h1>Monogenic disease candidate report for ${datasetRepository.getName()}</h1>
</div>
<div class="modal-body" style="background-color: #FFFFFF; ">


    <script type="text/javascript">
        $('.togglediv_grey').click(function () {
            $(this).each(function () {
                var classes = ['togglediv_border togglediv_blue', 'togglediv_border togglediv_red', 'togglediv_border togglediv_grey'];
                this.className = classes[($.inArray(this.className, classes) + 1) % classes.length];
            });
        });
        $('.togglediv_lightgreen').click(function () {
            $(this).each(function () {
                var classes = ['togglediv_border togglediv_blue', 'togglediv_border togglediv_red', 'togglediv_border togglediv_lightgreen'];
                this.className = classes[($.inArray(this.className, classes) + 1) % classes.length];
            });
        });
        $('.togglediv_green').click(function () {
            $(this).each(function () {
                var classes = ['togglediv_border togglediv_blue', 'togglediv_border togglediv_red', 'togglediv_border togglediv_green'];
                this.className = classes[($.inArray(this.className, classes) + 1) % classes.length];
            });
        });
        function changeContent(id, msg) {
            var el = document.getElementById(id);
            if (id) {
                el.innerHTML = msg;
            }
        }
    </script>

<#assign severelateonset = ["AIP", "ALK", "APC", "AXIN2", "BAP1", "BMPR1A", "BRCA1", "CDH1", "CDK4", "CDKN2A", "CEBPA", "CHEK2", "CTHRC1", "CTNNA1", "DICER1", "EGFR", "FH", "FLCN", "GATA2", "KIT", "MAX", "MLH1", "MLH3", "MSH2", "MSH3", "MSH6", "MUTYH", "NF2", "PAX5", "PDGFRA", "PMS2", "PRKAR1A", "RAD51D", "STK11", "TMEM127", "TP53"]>

<#assign dom_high_candidate_genes = {}>
<#assign dom_mod_candidate_genes = {}>
<#assign rec_high_candidate_genes = {}>
<#assign rec_mod_candidate_genes = {}>
<#assign other_candidate_genes = {}>
<#assign all_candidates = {}><#-- all genes + counts per category -->

<#-- these need some post-processing -->
<#assign com_high_candidate_raw = {}>
<#assign com_mod_candidate_raw = {}>
<#assign excluded_mod_compound_raw = {}>
<#assign excluded_high_compound_raw = {}>

<#list datasetRepository as row>

    <#assign geneName = row.getString("INFO_ANN")?split("|")[3]>

<#-- INCLUDED_DOMINANT variants -->
    <#if row.getString("INFO_MONGENDISCAND") == "INCLUDED_DOMINANT_HIGHIMPACT">
        <#if dom_high_candidate_genes[geneName]??>
            <#assign dom_high_candidate_genes = dom_high_candidate_genes + {geneName: dom_high_candidate_genes[geneName] + [row] } />
        <#else>
            <#assign dom_high_candidate_genes = dom_high_candidate_genes + {geneName : [row]} />
            <#if all_candidates[geneName]??><#assign all_candidates = all_candidates + {geneName : all_candidates[geneName] + 1 }><#else><#assign all_candidates = all_candidates + {geneName : 1 }></#if>
        </#if>
    </#if>
    <#if row.getString("INFO_MONGENDISCAND") == "INCLUDED_DOMINANT">
        <#if dom_mod_candidate_genes[geneName]??>
            <#assign dom_mod_candidate_genes = dom_mod_candidate_genes + {geneName : dom_mod_candidate_genes[geneName] + [row] } />
        <#else>
            <#assign dom_mod_candidate_genes = dom_mod_candidate_genes + {geneName : [row]} />
            <#if all_candidates[geneName]??><#assign all_candidates = all_candidates + {geneName : all_candidates[geneName] + 1 }><#else><#assign all_candidates = all_candidates + {geneName : 1 }></#if>
        </#if>
    </#if>

<#-- INCLUDED_RECESSIVE variants -->
    <#if row.getString("INFO_MONGENDISCAND") == "INCLUDED_RECESSIVE_HIGHIMPACT">
        <#if rec_high_candidate_genes[geneName]??>
            <#assign rec_high_candidate_genes = rec_high_candidate_genes + {geneName: rec_high_candidate_genes[geneName] + [row] } />
        <#else>
            <#assign rec_high_candidate_genes = rec_high_candidate_genes + {geneName : [row]} />
            <#if all_candidates[geneName]??><#assign all_candidates = all_candidates + {geneName : all_candidates[geneName] + 1 }><#else><#assign all_candidates = all_candidates + {geneName : 1 }></#if>
        </#if>
    </#if>
    <#if row.getString("INFO_MONGENDISCAND") == "INCLUDED_RECESSIVE">
        <#if rec_mod_candidate_genes[geneName]??>
            <#assign rec_mod_candidate_genes = rec_mod_candidate_genes + {geneName: rec_mod_candidate_genes[geneName] + [row] } />
        <#else>
            <#assign rec_mod_candidate_genes = rec_mod_candidate_genes + {geneName : [row]} />
            <#if all_candidates[geneName]??><#assign all_candidates = all_candidates + {geneName : all_candidates[geneName] + 1 }><#else><#assign all_candidates = all_candidates + {geneName : 1 }></#if>
        </#if>
    </#if>

<#-- INCLUDED_RECESSIVE_COMPOUND variants -->
    <#if row.getString("INFO_MONGENDISCAND") == "INCLUDED_RECESSIVE_COMPOUND_HIGHIMPACT">
        <#if com_high_candidate_raw[geneName]??>
            <#assign com_high_candidate_raw = com_high_candidate_raw + {geneName: com_high_candidate_raw[geneName] + [row] } />
        <#else>
            <#assign com_high_candidate_raw = com_high_candidate_raw + {geneName : [row]} />
            <#if all_candidates[geneName]??><#assign all_candidates = all_candidates + {geneName : all_candidates[geneName] + 1 }><#else><#assign all_candidates = all_candidates + {geneName : 1 }></#if>
        </#if>
    </#if>
    <#if row.getString("INFO_MONGENDISCAND") == "INCLUDED_RECESSIVE_COMPOUND">
        <#if com_mod_candidate_raw[geneName]??>
            <#assign com_mod_candidate_raw = com_mod_candidate_raw + {geneName: com_mod_candidate_raw[geneName] + [row] } />
        <#else>
            <#assign com_mod_candidate_raw = com_mod_candidate_raw + {geneName : [row]} />
            <#if all_candidates[geneName]??><#assign all_candidates = all_candidates + {geneName : all_candidates[geneName] + 1 }><#else><#assign all_candidates = all_candidates + {geneName : 1 }></#if>
        </#if>
    </#if>

<#-- store EXCLUDED_FIRST_OF_COMPOUND / _HIGHIMPACT that will be retrieved once there is a INCLUDED_RECESSIVE_COMPOUND_HIGHIMPACT or INCLUDED_RECESSIVE_COMPOUND for this gene! -->
<#-- there should be only 1 such variant per compound candidate gene, or else error -->
    <#if row.getString("INFO_MONGENDISCAND") == "EXCLUDED_FIRST_OF_COMPOUND">
        <#if excluded_mod_compound_raw[geneName]??>
            ERROR: multiple EXCLUDED_FIRST_OF_COMPOUND variants for gene ${geneName} !!
        <#else>
            <#assign excluded_mod_compound_raw = excluded_mod_compound_raw + {geneName : [row] } />
        </#if>
    </#if>
    <#if row.getString("INFO_MONGENDISCAND") == "EXCLUDED_FIRST_OF_COMPOUND_HIGHIMPACT">
        <#if excluded_high_compound_raw[geneName]??>
            ERROR: multiple EXCLUDED_FIRST_OF_COMPOUND_HIGHIMPACT variants for gene ${geneName} !!
        <#else>
            <#assign excluded_high_compound_raw = excluded_high_compound_raw + {geneName : [row] } />
        </#if>
    </#if>

<#-- INCLUDED_OTHER variants -->
    <#if row.getString("INFO_MONGENDISCAND") == "INCLUDED_OTHER">
        <#if other_candidate_genes[geneName]??>
            <#assign other_candidate_genes = other_candidate_genes + {geneName: other_candidate_genes[geneName] + [row] } />
        <#else>
            <#assign other_candidate_genes = other_candidate_genes + {geneName : [row] } />
            <#if all_candidates[geneName]??><#assign all_candidates = all_candidates + {geneName : all_candidates[geneName] + 1 }><#else><#assign all_candidates = all_candidates + {geneName : 1 }></#if>
        </#if>
    </#if>

</#list>



<#-- POST PROCESSING OF COMPOUND RECESSIVE VARIANTS INTO HIGH AND MODERATE CANDIDATE GENES -->

<#assign com_high_candidate_genes = {}>
<#assign com_mod_candidate_genes = {}>

<#-- if already a candidate HIGH impact variant, always put under 'compound high', and copy over any MODERATE variants, and the EXCLUDED_FIRST_OF_COMPOUND variant (high or mod impact) -->
<#list com_high_candidate_raw?keys as gene>

<#-- always add candidate -->
    <#assign com_high_candidate_genes = com_high_candidate_genes + {gene : com_high_candidate_raw[gene] } />
<#--assign all_candidate_genes = all_candidate_genes + [geneName]-->

<#-- also add any MODERATE variants to the candidate-->
    <#if com_mod_candidate_raw[gene]??>
        <#assign com_high_candidate_genes = com_high_candidate_genes + {gene : com_high_candidate_genes[gene] + com_mod_candidate_raw[gene] } />
    </#if>

<#--include FIRST_OF_COMPOUND: either HIGH or MODERATE variant-->
    <#if excluded_high_compound_raw[gene]??>
        <#assign com_high_candidate_genes = com_high_candidate_genes + {gene : com_high_candidate_genes[gene] + excluded_high_compound_raw[gene] } />
    <#else>
        <#assign com_high_candidate_genes = com_high_candidate_genes + {gene : com_high_candidate_genes[gene] + excluded_mod_compound_raw[gene] } />
    </#if>
</#list>

<#-- if a candidate MODERATE impact variant, put in HIGH anyway when the EXCLUDED_FIRST_OF_COMPOUND has a HIGH impact.  -->
<#list com_mod_candidate_raw?keys as gene>

<#-- if FIRST_OF_COMPOUND for this gene was HIGH impact, add this variant to HIGH candidates plus the original MODERATE variants-->
    <#if excluded_high_compound_raw[gene]??>
        <#if com_high_candidate_genes[gene]??>
            <#assign com_high_candidate_genes = com_high_candidate_genes + {gene : com_high_candidate_genes[gene] + excluded_high_compound_raw[gene] } />
        <#else>
            <#assign com_high_candidate_genes = com_high_candidate_genes + {gene : excluded_high_compound_raw[gene] } />
        </#if>
        <#assign com_high_candidate_genes = com_high_candidate_genes + {gene : com_high_candidate_genes[gene] + com_mod_candidate_raw[gene] } />

    <#--FIRST_OF_COMPOUND was a MODERATE, and so is the rest! add them to MODERATE COMPOUND candidates -->
    <#else>
        <#if com_mod_candidate_genes[gene]??>
            <#assign com_mod_candidate_genes = com_mod_candidate_genes + {gene : com_mod_candidate_genes[gene] + excluded_mod_compound_raw[gene] } />
        <#else>
            <#assign com_mod_candidate_genes = com_mod_candidate_genes + {gene : excluded_mod_compound_raw[gene] } />
        </#if>
        <#assign com_mod_candidate_genes = com_mod_candidate_genes + {gene : com_mod_candidate_genes[gene] + com_mod_candidate_raw[gene]} />
    </#if>
</#list>


<#-- HTML -->

    <h4>Candidate genes</h4>
    <p>
    <div style="display:inline" class="togglediv_green">Green</div>
    genes have a strong Phenomizer symptom match (<i>p</i> < 0.05),
    <div style="display:inline" class="togglediv_lightgreen">light green</div>
    is a weak symptom match (<i>p</i> > 0.05), and
    <div style="display:inline" class="togglediv_grey">grey</div>
    genes do not a match.
    Hover over a gene to see details and the variants for this candidate below.
    Genes in <b>bold font</b> appear in multiple categories.
    Click on a gene to 'exclude' this candidate by flagging it with a
    <div style="display:inline" class="togglediv_red">red</div>
    color.
    </p>
    <table class="table table-bordered table-condensed">
        <tr class="active">
            <th style="background-color: #FFFFFF; border: 0px; border-color: #FFFFFF;"></th>
            <th><h4>Dominant</h4></th>
            <th><h4>Recessive</h4></th>
            <th><h4>Compound</h4></th>
        </tr>
        <tr>
            <td class="danger"><h4>High impact</h4></td>
            <td><@printGenes dom_high_candidate_genes /></td>
            <td><@printGenes rec_high_candidate_genes /></td>
            <td><@printGenes com_high_candidate_genes /></td>
        </tr>
        <tr>
            <td class="warning"><h4>Moderate impact</h4></td>
            <td><@printGenes dom_mod_candidate_genes /></td>
            <td><@printGenes rec_mod_candidate_genes /></td>
            <td><@printGenes com_mod_candidate_genes /></td>
        </tr>
        <tr>
            <td class="info"><h4>Other</h4></td>
            <td colspan="3"><@printGenes other_candidate_genes /></td>
        </tr>
    </table>

    <div id="infoDiv"><h4><i>No gene selected</i></h4></div>

</div>


<#macro printGenes genes>
    <#list genes?keys as geneName>

        <#if severelateonset?seq_contains(geneName)>
        <#-- skipping this gene -->
        <#else>

            <@compress single_line=true>
            <div class="togglediv_border togglediv_<#if genes[geneName][0].getDouble("INFO_PHENOMIZERPVAL")??><#if genes[geneName][0].getDouble("INFO_PHENOMIZERPVAL") lt 0.05>green<#else>lightgreen</#if><#else>grey</#if>"
                 style="display:inline" onclick="changeContent('infoDiv', '
                    <h4>Gene details</h4>
                    <table class=&quot;table table-bordered table-condensed&quot;>
                    <tr class=&quot;active&quot;>
                    <th>Name</th>
                    <th>Disorder</th>
                    <th>Inheritance</th>
                    <th>Generalized inh.</th>
                    <th>Onset</th>
                    <th>Phenomizer</th>
                    </tr>
                    <tr>
                    <td>${geneName}</td>
                    <td>${genes[geneName][0].getString("INFO_CGDCOND")}</td>
                    <td>${genes[geneName][0].getString("INFO_CGDINH")}</td>
                    <td>${genes[geneName][0].getString("INFO_CGDGIN")}</td>
                    <td>${genes[geneName][0].getString("INFO_CGDAGE")}</td>
                    <td><#if genes[geneName][0].getDouble("INFO_PHENOMIZERPVAL")??>${genes[geneName][0].getDouble("INFO_PHENOMIZERPVAL")}</#if></td>
                    </tr>
                    </table>
                    <h4>Variant details</h4>
                    <table class=&quot;table table-bordered table-condensed&quot;>
                    <tr class=&quot;active&quot;>
                    <th>Chr</th>
                    <th>Pos</th>
                    <th>Id</th>
                    <th>Ref</th>
                    <th>Alt</th>
                    <th>Effect</th>
                    <th>Impact</th>
                    <th>Transcript</th>
                    <th>AAchange</th>
                    <th>Genotype</th>
                    <th>Al.depth</th>
                    <th>GoNL</th>
                    <th>ExAC</th>
                    <th>1000G</th>
                    <th>ClinVar</th>
                    </tr>
                <#list genes[geneName] as row>
                        <tr>
                        <td>${row.getString("#CHROM")}</td>
                        <td>${row.getString("POS")}</td>
                        <td>${row.getString("ID")}</td>
                        <td>${row.getString("REF")}</td>
                        <td>${row.getString("ALT")}</td>
                        <td>${row.getString("INFO_ANN")?split("|")[1]}</td>
                        <td>${row.getString("INFO_ANN")?split("|")[2]}</td>
                        <td>${row.getString("INFO_ANN")?split("|")[6]}</td>
                        <td>${row.getString("INFO_ANN")?split("|")[10]}</td>
                        <td><#list row.getEntities("SAMPLES") as sample>${sample.getString("GT")} </#list></td>
                        <td><#list row.getEntities("SAMPLES") as sample>${sample.getString("AD")} </#list></td>
                        <td><#if row.getString("INFO_GONLMAF")??>${row.getString("INFO_GONLMAF")}</#if></td>
                        <td><#if row.getString("INFO_EXACMAF")??>${row.getString("INFO_EXACMAF")}</#if></td>
                        <td><#if row.getString("INFO_1KGMAF")??>${row.getString("INFO_1KGMAF")}</#if></td>
                        <td><#if row.getString("INFO_CLINVAR_CLNSIG")??>${row.getString("INFO_CLINVAR_CLNSIG")}</#if></td>
                        </tr>
                </#list>
                    </table>
                    <h4>Symptoms for ${genes[geneName][0].getString("INFO_CGDCOND")}</h4>
                    <p>
                <#if genes[geneName][0].getString("INFO_HPOTERMS")??>${genes[geneName][0].getString("INFO_HPOTERMS")?replace("/",", ")?lower_case}<#else>No symptoms available.</#if>
                    </p>
                    ')"><#if all_candidates[geneName] gt 1><b>${geneName}</b><#else>${geneName}</#if></div>
            </@compress>

        </#if>

    </#list>
</#macro>
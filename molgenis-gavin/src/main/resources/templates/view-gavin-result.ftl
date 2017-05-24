<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<#assign css=[]>
<#assign js=["gavin-result.js"]>
<@header css js/>

<div class="row" id="gavin-view">
    <div class="col-md-12">
        <div class="panel panel-primary" id="instant-import">
            <div class="panel-heading">
                <h4 class="panel-title">
                    GAVIN annotation job ${jobExecution.filename?html} submitted
                    on ${jobExecution.submissionDate.format('MMM d, yyyy HH:mm:ss')}
                </h4>
            </div>
            <div class="panel-body">
                <div id="instant-import-alert"></div>

            <#if jobExecution.status == 'RUNNING' || jobExecution.status == 'PENDING'>
            <#-- Job running, show progress -->
                <div id="gavin-job" data-execution-id="${jobExecution.identifier}"></div>
                <p>No results yet. Check back later at <a
                        href="${pageUrl}">${pageUrl}</a>.
                    Results will remain available for 24 hours.</p>
            <#else>
            <#-- Job finished -->
                <h4>Input</h4>
                <p>Input file contained:</p>
                <ul>
                    <#if jobExecution.comments?? && jobExecution.comments gt 0 >
                        <li>${jobExecution.comments} comment lines</li></#if>
                    <#if jobExecution.cadds?? && jobExecution.cadds gt 0 >
                        <li>${jobExecution.cadds} valid CADD lines</li></#if>
                    <#if jobExecution.vcfs?? && jobExecution.vcfs gt 0 >
                        <li>${jobExecution.vcfs} valid VCF lines</li></#if>
                    <#if (jobExecution.indels?? && jobExecution.indels gt 0)>
                        <li>${jobExecution.indels} lines contained insertions or deletions without a cadd score.
                            Please pre-score these variants using the <a href="http://cadd.gs.washington.edu/score">CADD
                                service</a>.
                        </li></#if>
                    <#if (jobExecution.errors?? && jobExecution.errors gt 0)>
                        <li>${jobExecution.errors} error lines (could not be parsed)</li></#if>
                    <#if jobExecution.skippeds?? && jobExecution.skippeds gt 0 >
                        <li>${jobExecution.skippeds} skipped lines. Too much input.</li></#if>
                </ul>
                <h4>Results</h4>
                <#if downloadFileExists>
                    <a class="btn btn-primary" href="/plugin/gavin-app/download/${jobExecution.identifier}">
                        <span class="glyphicon glyphicon-file" aria-hidden="true"></span>
                        Download GAVIN-annotated ${jobExecution.filename?html}</a>
                <#else>
                    <#if jobExecution.status == 'SUCCESS'>Download file no longer available.
                    <#else>There was a problem, no results available. Check execution log for details.
                    </#if>
                    <br/>
                </#if>
                <#if (jobExecution.errors?? && jobExecution.errors gt 0) || (jobExecution.indels?? && jobExecution.indels gt 0)>
                    <#if errorFileExists>
                        <a href="/plugin/gavin-app/error/${jobExecution.identifier}" class="btn btn-warning">
                            <span class="glyphicon glyphicon-file" aria-hidden="true"></span>Download failed lines
                        </a>
                    <#else>Error file no longer available.
                    </#if>
                    <br/>
                </#if>
                <p>This page will remain available on <a href="${pageUrl}">${pageUrl}</a>, but result files are removed
                    after 24 hours.</p>
                <#if jobExecution.log??>
                    <h4>Execution log</h4>
                    <pre class="pre-scrollable">${jobExecution.log?html}</pre>
                </#if>

                <a class="btn btn-info" type="button" href="..">All done! Return to Gavin Upload page</a>
            </#if>
            </div>
        </div>
    </div>
</div>

<@footer/>
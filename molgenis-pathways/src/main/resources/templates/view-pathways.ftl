<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css=[]>
<#assign js=["pathway.js", "svg-pan-zoom.min.js"]>

<@header css js/>

<div role="tabpanel">
    <ul class="nav nav-tabs" role="tablist">
        <li role="presentation" class="active"><a href="#searchTab" aria-controls="searchTab" role="tab"
                                                  data-toggle="tab">Search</a></li>
        <li role="presentation"><a href="#pathway-selectTab" aria-controls="pathway-selectTab" role="tab"
                                   data-toggle="tab">Pathway select</a></li>
    </ul>

    <div class="tab-content">
        <div role="tabpanel" class="tab-pane active" id="searchTab">
            <div class="row" id="search">
                <div class="col-md-3 col-md-offset-2">
                    <h5>Search for a pathway (by gene, description, disease etc.) </h5>
                    <form>
                        <input type="text" class="form-control" name="searchTerm" id="gene-search"
                               data-placeholder="enter a gene or description">
                        <option val=""></option>
                        <button type="btn" class="btn btn-default" id="submit-genename-btn">Search</button>
                    </form>
                    <br/>
                    <input id="pathway-select" value=""/>
                    <br/>
                    <small>This menu will be updated according to the search term that is used.
                        <br/>When no search term is used, all available pathways can be selected.
                    </small>
                    <br/><br/><br/>
                </div>
                <div class="row" id="pathwaySelect">
                    <div class="col-md-6 col-md-offset-1">
                        <div id="pathway-svg-image"></div>
                    </div>
                </div>
            </div>
        </div>
        <div role="tabpanel" class="tab-pane" id="pathway-selectTab">
            <div class="row">
                <div class="col-md-3 col-md-offset-2">
                    <br/>
                    Select a VCF file (Only VCF files with a effect (EFF) Field in the INFO attribute are shown)
                    <form>
                        <select class="form-control" id="dataset-select" data-placeholder="Select a VCF file">
                            <option val=""></option>
                        <#if entitiesMeta?has_content>
                            <#list entitiesMeta as entityMeta>
                                <option value="${entityMeta.fullyQualifiedName}"><#if entityMeta.label?has_content>${entityMeta.label}<#else>${entityMeta.fullyQualifiedName}</#if></option>
                            </#list>
                        </#if>
                        </select>
                        <br/>
                        <button type="btn" class="btn btn-default" id="submit-vcfFile-btn">Submit</button>
                    </form>
                </div>
            </div>

            <div class="row">
                <div class="col-md-6 col-md-offset-2" id="hiding-select2">
                    <br/>
                    <input id="pathway-select2" value=""/>
                </div>
            </div>
            <div class="row">
                <div class="col-md-6 col-md-offset-1">
                    <div id="colored-pathway-svg-image"></div>
                </div>
            </div>
        </div>
    </div>

<@footer />

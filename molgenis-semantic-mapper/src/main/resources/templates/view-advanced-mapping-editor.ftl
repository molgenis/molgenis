<div id="advanced-mapping-editor">
    <div class="row">
        <div class="col-md-12">
            <h4>Category mapping editor</h4>
            <p>
                Map ${sourceAttribute.label?html} values to ${targetAttribute.label?html} values.
                Select the correct category that you want to map the source attribute to from the target attribute
                dropdown.
            </p>

        <#assign showDefault = numberOfSourceAttributes gt 10>

        <#if showDefault>
            <div class="form-group">
                <div class="col-md-2">
                    <label>Default value </label>
                    <select id="default-value" class="form-control">
                        <#if targetAttribute.nillable>
                            <option <#if !categoryMapping.defaultValue?? >selected </#if> value="use-null-value"><em>None</em>
                            </option>
                        </#if>
                        <#list targetAttributeEntities as targetEntity>
                            <option value="${targetEntity.getString(targetAttributeIdAttribute)?html}"
                                <#if categoryMapping.defaultValue??>
                                    <#if categoryMapping.defaultValue?string == targetEntity.getString(targetAttributeIdAttribute)>selected </#if>
                                </#if>
                            >${targetEntity.get(targetAttributeLabelAttribute)?html}</option>
                        </#list>
                    </select>
                </div>
            </div>
        </#if>
        </div>
    </div>

    <div class="row">
        <div class="col-md-12">
        <#if showDefault><br></br></#if>
            <table id="advanced-mapping-table" class="table table-bordered scroll">
                <thead>
                <th>${source?html} attribute value</th>
                <th>Number of rows</th>
                <th>${target?html} attribute value</th>
                </thead>
                <tbody>
                <#assign count = 0 />
                <#list sourceAttributeEntities as sourceEntity>
                    <#assign id = sourceEntity.getIdValue()?string>
                <tr id="${id?html}">
                    <td>${sourceEntity.get(sourceAttributeLabelAttribute)?html}</td>
                    <td><#if aggregates??>${aggregates[count]!'0'}<#else>NA</#if></td>
                    <td>
                        <select class="form-control">
                            <#if showDefault>
                                <option value="use-default-option"
                                    <#if !categoryMapping.map?keys?seq_contains(id) > selected </#if>>use default
                                </option>
                            </#if>
                            <#if targetAttribute.nillable>
                            <#-- if the key exists but the value doesn't, the value is null -->
                                <option value="use-null-value"<#if categoryMapping.map?keys?seq_contains(id) && !categoryMapping.map[id]?? >
                                        selected </#if>><em>None</em></option>
                            </#if>
                            <#list targetAttributeEntities as targetEntity>
                                <option <#if categoryMapping.map[id]?? >
                                    <#if categoryMapping.map[id]=targetEntity.getIdValue()?string>selected </#if>
                                </#if>
                                    value="${targetEntity.getIdValue()?string}">${targetEntity.get(targetAttributeLabelAttribute)?html}</option>
                            </#list>
                        </select>
                    </td>
                </tr>
                    <#assign count = count + 1 />
                </#list>

                <#if sourceAttribute.nillable>
                <tr id="nullValue">
                    <td><em>None</em></td>
                    <td><#if aggregates??>${aggregates[count]!'0'}<#else>NA</#if></td>
                    <td>
                        <select class="form-control">
                            <#if showDefault>
                                <option<#if categoryMapping.nullValueUndefined> selected </#if>
                                                                                value="use-default-option">use default
                                </option>
                            </#if>
                            <#if targetAttribute.nillable>
                                <option<#if !categoryMapping.nullValueUndefined && !categoryMapping.nullValue??>
                                        selected </#if> value="use-null-value"><em>None<em></option>
                            </#if>
                            <#list targetAttributeEntities as targetEntity>
                                <option<#if categoryMapping.nullValue??>
                                    <#if categoryMapping.nullValue=targetEntity.getString(targetAttributeIdAttribute)>
                                            selected </#if>
                                </#if>
                                            value="${targetEntity.get(targetAttributeIdAttribute)?html}">${targetEntity.get(targetAttributeLabelAttribute)?html}</option>
                            </#list>
                        </select>
                    </td>
                </tr>
                </#if>
                </tbody>
            </table>

        <#--Hidden inputs for the javascript post-->
            <input type="hidden" name="sourceAttribute" value="${sourceAttribute.name?html}"/>
        </div>
    </div>
</div>
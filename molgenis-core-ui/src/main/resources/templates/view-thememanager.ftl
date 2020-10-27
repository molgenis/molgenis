<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css=[]>
<#assign js=["thememanager.js"]>
<#assign version = 2>

<@header css js version/>
<div class="row" id="thememanager">
    <div class="col-md-12">
        <legend>Theme Manager</legend>

        <form role="form">
            <div class="custom-control custom-radio">
                <input type="radio" class="custom-control-input" id="theme-selection-method-predefined" v-model="selectionMethod" name="radio-stacked" value="listed">
                <label class="custom-control-label" for="theme-selection-method-predefined">Select a public theme</label>
            </div>

            <div class="custom-control custom-radio mb-3">
                <input type="radio" class="custom-control-input" id="theme-selection-method-url" v-model="selectionMethod" name="radio-stacked" value="url">
                <label class="custom-control-label" for="theme-selection-method-url">Use a custom theme</label>
                <div class="invalid-feedback">More example invalid feedback text</div>
            </div>

            <div class="theme-select-listed" v-if="selectionMethod === 'listed'">
                <div class="form-group">
                    <label for="bootstrap-theme-select">Available Themes</label>
                    <select class="form-control"
                            id="bootstrap-theme-select"
                            v-model="selectedTheme"/>
                        <option v-for="theme in themes" :value="theme.id">{{theme.name || theme.id}}</option>
                    </select>
                </div>
            </div>
            <div class="theme-select-url" v-else>
                <div class="form-group">
                    <label for="theme-url">Theme url</label>
                    <input type="text" class="form-control" id="theme-url" autocomplete="off" v-model="themeUrl">
                    <small id="emailHelp" class="form-text text-muted">Url to a customized <a href="https://github.com/molgenis/molgenis-theme">molgenis-theme</a> Bootstrap 4 file.</small>
                </div>
                <div class="form-group">
                    <label for="theme-url-legacy">Theme url (legacy)</label>
                    <input type="text" class="form-control" id="theme-url-legacy" autocomplete="off" v-model="themeUrlLegacy">
                    <small id="emailHelp" class="form-text text-muted">Url to a customized <a href="https://github.com/molgenis/molgenis-theme">molgenis-theme</a> Bootstrap 3 file.</small>
                </div>
            </div>
        </form>
        <div class="btn-group mt-1" role="group">
            <button id="save-selected-bootstrap-theme"
                    type="button"
                    @click="save"
                    class="btn btn-primary">Save
            </button>
        </div>
    </div>
</div>
<script src="/js/thememanager.js"></script>
<@footer/>

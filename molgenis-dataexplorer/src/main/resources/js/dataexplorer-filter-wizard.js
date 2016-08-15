(function ($, molgenis) {
    "use strict";

    molgenis.dataexplorer = molgenis.dataexplorer || {};
    molgenis.dataexplorer.filter = molgenis.dataexplorer.filter || {};
    var self = molgenis.dataexplorer.filter.wizard = molgenis.dataexplorer.filter.wizard || {};

    var restApi = new molgenis.RestClient();

    self.openFilterWizardModal = function (entityMetaData, attributeFilters) {
        var modal = createFilterWizardModal(attributeFilters);
        createFilterWizardContent(entityMetaData, attributeFilters, modal);
        modal.modal('show');
    };

    function createFilterWizardModal(attributeFilters) {
        $('#filter-wizard-modal').remove();
        var modal = $('#filter-wizard-modal');
        var wizardTemplate = Handlebars.compile($("#filter-wizard-modal-template").html());
        modal = $(wizardTemplate({}));
        createFilterModalControls(modal, attributeFilters);
        return modal;
    }

    function createFilterModalControls(modal, attributeFilters) {
        $('.filter-wizard-apply-btn', modal).unbind('click');
        $('.filter-wizard-apply-btn', modal).click(function () {
            var filters = molgenis.dataexplorer.filter.createFilters($('form', modal));
            $(document).trigger('updateAttributeFilters', {
                'filters': filters
            });
        });

        modal.unbind('shown.bs.modal');
        modal.on('shown.bs.modal', function () {
            $('form input:visible:first', modal).focus();
        });

        modal.unbind('keypress');
        modal.keypress(function (e) {
            if (e.which == 13) {
                e.preventDefault();
                $('.filter-wizard-apply-btn', modal).click();
            }
        });
    }

    function createFilterWizardContent(entityMetaData, attributeFilters, modal) {
        var wizard = $('.filter-wizard', modal);
        if (wizard.data('bootstrapWizard')) {
            $.removeData(wizard.get(0));
        }

        var listItems = [];
        var paneItems = [];

        var compoundAttributes = molgenis.getCompoundAttributes(entityMetaData.attributes, restApi);
        compoundAttributes.unshift(entityMetaData);

        $.each(compoundAttributes, function (i, compoundAttribute) {
            var tabId = compoundAttribute.name + '-tab';
            var label = compoundAttribute.label || compoundAttribute.name;

            listItems.push('<li><a href="#' + tabId + '" data-toggle="tab">' + label + '</a></li>');

            var pane = $('<div class="tab-pane' + (i === 0 ? ' active"' : '"') + ' id="' + tabId + '">');
            var paneContainer = $('<div class="well filter-wizard-fixed-height"></div>');

            $.each(compoundAttribute.attributes, function (i, attribute) {
                if (attribute.fieldType !== 'COMPOUND' && attribute.visible) {
                    paneContainer.append(molgenis.dataexplorer.filter.createFilter(attribute, attributeFilters[attribute.href], true));
                }
            });

            pane.append(paneContainer);
            paneItems.push(pane);
        });

        if (compoundAttributes.length > 1) {
            $('.wizard-steps', wizard).show();
            $('.wizard-steps', wizard).html(listItems.join(''));
            $('.pager', wizard).show();
        } else {
            $('.wizard-steps', wizard).hide();
            $('.pager', wizard).hide();
        }
        $('.tab-content', wizard).html(paneItems);

        $('#filter-wizard-modal ul.pager.wizard').html('<li class="previous"><a href="#">Previous</a></li><li class="next"><a href="#">Next</a></li>');

        wizard.bootstrapWizard({
            tabClass: 'wizard-steps nav nav-pills',
            onTabShow: function (tab, navigation, index) {
                var $total = navigation.find('li').length;
                var $current = index + 1;

                // If it's the last tab then hide the last button and show the finish instead
                if ($total === 1) {
                    wizard.find('.pager .previous').hide();
                    wizard.find('.pager .next').hide();
                } else if ($current === 1) {
                    wizard.find('.pager .previous').hide();
                    wizard.find('.pager .next').show();
                } else if ($current > 1 && $current < $total) {
                    wizard.find('.pager .previous').show();
                    wizard.find('.pager .next').show();
                } else if ($current === $total && $current > 1) {
                    wizard.find('.pager .previous').show();
                    wizard.find('.pager .next').hide();
                } else {
                    wizard.find('.pager .previous').hide();
                    wizard.find('.pager .next').hide();
                }
            }
        });
    }
})($, window.top.molgenis = window.top.molgenis || {});	
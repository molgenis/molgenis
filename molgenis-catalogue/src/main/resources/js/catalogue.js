(function ($, molgenis) {
    "use strict";
    var restApi = new molgenis.RestClient();
    var selectedEntity;

    function createEntityMetaTree(entityMetaData) {
        $('#attribute-selection').tree({
            entityMetaData: entityMetaData,
            onAttributesSelect: function (selects) {
            },
            onAttributeClick: function (attribute) {
                createAttributeMetadataTable(attribute);
            }
        });
    }

    function createAttributeMetadataTable(attributeMetadata) {
        $('#attributes-table').attributeMetadataTable({
            attributeMetadata: attributeMetadata
        });
    }

    function createHeader(entityMetaData) {
        $('#entity-class-name').html(entityMetaData.label);

        if (entityMetaData.description) {
            var description = $('<span data-placement="bottom"></span>');
            description.html(abbreviate(entityMetaData.description, 180));
            description.attr('data-title', entityMetaData.description);
            $('#entity-class-description').html(description.tooltip());
        } else {
            $('#entity-class-description').html('');
        }
    }

    function getFirstAttribute(entityMetaData) {
        for (var name in entityMetaData.attributes)
            return entityMetaData.attributes[name];
    }

    function load(entityUri) {
        restApi.getAsync(entityUri + '/meta', {
            'expand': ['attributes']
        }, function (entityMetaData) {
            selectedEntity = entityMetaData;
            createHeader(entityMetaData);
            createEntityMetaTree(entityMetaData);

            $('#attributes-table').attributeMetadataTable({
                attributeMetadata: getFirstAttribute(entityMetaData)
            });
        });
    }

    $(function () {
        $('.entity-dropdown-item').click(function () {
            var entityUri = $(this).attr('id');
            load(entityUri);
        });

        if (selectedEntityName) {
            load('/api/v1/' + selectedEntityName);
        }
    });

}($, window.top.molgenis = window.top.molgenis || {}));
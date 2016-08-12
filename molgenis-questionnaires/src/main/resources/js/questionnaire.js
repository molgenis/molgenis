(function ($, molgenis) {
    "use strict";

    $(function () {
        var data = $('#form-holder').data();
        getForm(data.name, data.id, data.name + "/thanks");
    });

    function getForm(name, id, successUrl) {
        React.render(molgenis.ui.Questionnaire({
            entity: name,
            entityInstance: id,
            successUrl: successUrl,
            onContinueLaterClick: function () {
                document.location = '/menu/main/questionnaires';
            }
        }), $('#form-holder')[0]);
    }

}($, window.top.molgenis = window.top.molgenis || {}));
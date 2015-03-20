<div id="${entityName?html}-form-container"></div>
<script>
    React.render(molgenis.ui.Form({
        entity: '${entityName?html}',
        mode: 'create',
        formLayout: 'horizontal' 
    }), $('#${entityName?html}-form-container')[0]);
</script>
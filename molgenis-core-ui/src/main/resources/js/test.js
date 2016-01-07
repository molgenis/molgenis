require(['main'], function() {
    require(['react', 'molgenis', 'underscore', '/component/Button'], function(react, molgenis, _, Button) {
        console.log('testing require js....');
    	
    	new Button();
    });
});
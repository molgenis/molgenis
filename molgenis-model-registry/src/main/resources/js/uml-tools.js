/*
 * UTILS
 */

function objectToString(o){    
    var parse = function(_o){
        var a = [], t;        
        for(var p in _o){        
            if( _o.hasOwnProperty(p) ){            
                t = _o[p];                
                if(t && typeof t == "object"){                
                    a[a.length]= p + ":{ " + arguments.callee(t).join(", ") + "}";                    
                }
                else {                    
                    if(typeof t == "string"){                    
                        a[a.length] = [ p+ ": \"" + t.toString() + "\"" ];
                    }
                    else{
                        a[a.length] = [ p+ ": " + t.toString()];
                    }                    
                }
            }
        }        
        return a;        
    }    
    return "{" + parse(o).join(", ") + "}";    
}

/*
 *  UML STUFF (move these to a separate library)
 *  
 */

joint.shapes.myUML = {};
myUML = joint.shapes.myUML;

myUML.decorate = function (assoEnd) {
	var color = 'black';
	var path =  'M 10 0 L 0 5 L 10 10 z'
    if ( assoEnd.navigable) { 
    	color = 'red';
    } else {
    	color = 'green';
    	path =  'M 2.5 0 L 0 1.75 L 2.5 2.5 z'
    }			
	return { 
		fill : color,  d : path
	}	
}

myUML.multiplicity = function( end) { 
	var l = end.lowerBound;
	var u = end.upperBound;
	if ( l == null ) l="";
	if ( u == null ) u="";
	if( l == 0 && u == '*'){
		return '*';
	}
	if( l == 1 && u == 1){
		return '';
	}
	if ( l!="" && u!="") {
		return l+".."+u;
	}  else {
		return l+u
	}
}

myUML.createSemanticEdge  = function ( subject,  predicateName, object ) {
	var uml = joint.shapes.uml;
    var edge = new uml.Association({
        source : {
                id : subject.id
        },
        target : {
                id : object.id
        },
        smooth: true
    });
    if ( predicateName != 'owlEquivalent' && predicateName != 'owlDisjoint') {
    	//todo: make configurable.. no arrow for symmetric properties
    	edge.attr('.marker-target' ,{ fill: 'red', d: 'M 10 0 L 0 5 L 10 10 z'}); 
    }
    edge.attr('.connection', { 'stroke-dasharray': '3,3' })
    if ( predicateName != null) {
	 	edge.label(0, {
		    position: 0.5,
		    attrs: {
		        text: { fill: 'black', text: predicateName , 'font-size': 11, 'font-family': 'Times New Roman' }
		    }
			});
	}
    edge._ASSO  = { target: subject, source: object, predicate : predicateName};
    return edge;
}

myUML.createParentChildEdge  = function ( parent, child ) {
	var uml = joint.shapes.uml;
	var asso = undefined;
	
	switch (parent._ENTITY.type) {
	case "interface":
		asso = new uml.Implementation({
			source : {
				id : child.id
			},
			target : {
				id : parent.id
			},
			smooth : true
		})
		break;
	default:
		asso = new uml.Generalization({
			source : {
				id : child.id
			},
			target : {
				id : parent.id
			},
			smooth: true
		})
		break;
	}
	asso._ASSO  = { target: parent, source: child};
	return asso;
}


myUML.createEdge = function( name, sourceNode, targetNode, sourceEnd,targetEnd) {
	var uml = joint.shapes.uml;
	var edge = new uml.Association({
		source : {
			id : sourceNode.id
		},
		target : {
			id : targetNode.id
		},
		smooth: false
	})	

	edge.attr('.marker-source', myUML.decorate( targetEnd));
	edge.attr('.marker-target', myUML.decorate( sourceEnd));

	var NONAME = 'undefined'; //todo: from config
	
	var targetRole = targetEnd.name
	if ( targetRole == NONAME) targetRole = '';
	var sourceRole = sourceEnd.name
	if ( sourceRole == NONAME) sourceRole = '';
	
	edge.label(0, {
	    position: 0.2,
	    attrs: {
	        text: { fill: 'black', text: targetRole , 'font-size': 11, 'font-family': 'Times New Roman' }
	    }
		});
	edge.label(1, {
	    position: 0.8,
	    attrs: {
	        text: { fill: 'black', text: sourceRole ,'font-size': 11, 'font-family': 'Times New Roman' }
	    }
		});

	edge.label(2, {
	    position: 0.1,
	    attrs: {
	        text: { fill: 'black', text: myUML.multiplicity(targetEnd), 'font-size': 10, 'font-family': 'Times New Roman' }
	    }
		});
	edge.label(3, {
	    position: 0.9,
	    attrs: {
	        text: { fill: 'black', text: myUML.multiplicity(sourceEnd) ,'font-size': 10, 'font-family': 'Times New Roman' }
	    }
		});
	if ( name != NONAME) { 
		edge.label(4, {
		    position: 0.5,
		    attrs: {
		        text: { fill: 'black', text: name ,'font-size': 10, 'font-family': 'Times New Roman' }
		    }
			});
	}
	edge._ASSO  = { target: targetEnd, source: sourceEnd};
	return edge
}
/* See OWL representattion using class diagrams
 * e.g. owl in uml class notattion
 * http://www.omg.org/news/meetings/tc/dc-13/special-events/semantic-pdfs/T1-1-Kendall.pdf 
 */
myUML.createClassNode  = function ( entity, attributes_all ) { 
	var uml = myUML;
    var e = undefined;
    var attributes = attributes_all
    
    if ( attributes.length  > 10 ) {
    	console.log("toomany attributes. slicing the attributes array. ");
    	attributes = attributes.slice (1,10);
    	attributes.push("...");
    }
    var type = entity.type
    if ( entity.typeQualifier == "abstract" && type == "class") {
    	type = "abstractClass"
    }
	switch ( type ) {
	
	case "primitive":
		e = new uml.Datatype({
			id : entity.identifier,
			size : {
				width : 200,
				height : 30
			},
			name : "" + entity.name
		})
		break;
	case "interface":
		e = new uml.Interface({
			id : entity.identifier,
			size : {
				width : 200,
				height : (16 * attributes.length + 40)
			},
			name : "" + entity.name,
			attributes : attributes
		})
		break;
	case "abstractClass":
		e = new uml.Abstract({
			id : entity.identifier,
			size : {
				width : 200,
				height : (16 * attributes.length + 30)
			},
			name : "" + entity.name,
			attributes : attributes
		})
		break;
	case "artifact":
		e = new uml.Artifact({
			id : entity.identifier,
			size : {
				width : 200,
				height : (16 * attributes.length + 30)
			},
			name : "" + entity.name,
			attributes : attributes
		})
		break;
	case "enumeration":
		e = new uml.Enumeration({
			id : entity.identifier,
			size : {
				width : 200,
				height : (16 * attributes.length + 30)
			},
			name : "" + entity.name,
			attributes : attributes
		})
		break;
	case "owlClass":
		e = new uml.OwlClass({
			id : entity.identifier,
			size : {
				width : 200,
				height : (16 * attributes.length + 30)
			},
			name : "" + entity.name,
			attributes : attributes
		})
		break;
	case "owlUnionClass":
		e = new uml.OwlUnionClass({
			id : entity.identifier,
			size : {
				width : 200,
				height : (16 * attributes.length + 30)
			},
			name : "" + entity.name,
			attributes : attributes
		})
		break;
	case "owlIntersectionClass":
		e = new uml.OwlIntersectionClass({
			id : entity.identifier,
			size : {
				width : 200,
				height : (16 * attributes.length + 30)
			},
			name : "" + entity.name,
			attributes : attributes
		})
		break;
	case "owlComplementClass":
		e = new uml.OwlComplementClass({
			id : entity.identifier,
			size : {
				width : 200,
				height : (16 * attributes.length + 30)
			},
			name : "" + entity.name,
			attributes : attributes
		})
		break;
	case "owlRestrictionClass":
		e = new uml.OwlRestrictionClass({
			id : entity.identifier,
			size : {
				width : 200,
				height : (16 * attributes.length + 30)
			},
			name : "" + entity.name,
			attributes : attributes
		})
		break;
	case "owlObjectProperty":
		e = new uml.OwlObjectProperty({
			id : entity.identifier,
			size : {
				width : 200,
				height : (16 * attributes.length + 30)
			},
			name : "" + entity.name,
			attributes : attributes
		})
		break;
	case "owlDatatypeProperty":
		e = new uml.OwlDatatypeProperty({
			id : entity.identifier,
			size : {
				width : 200,
				height : (16 * attributes.length + 30)
			},
			name : "" + entity.name,
			attributes : attributes
		})
		break;
	case "owlAnnotationProperty":
		e = new uml.OwlAnnotationProperty({
			id : entity.identifier,
			size : {
				width : 200,
				height : (16 * attributes.length + 30)
			},
			name : "" + entity.name,
			attributes : attributes
		})
		break;
	default:
		e = new uml.Class({
			id : entity.identifier,
			size : {
				width : 200,
				height : (16 * attributes.length + 30)
			},
			name : "" + entity.name,
			attributes : attributes
		})
	}
	e.attr('text/font-size', 12);
	if ( entity.neighbourLevel === 0) {
		e.attr({
	        '.uml-class-name-rect': { 'opacity': 1.0,  'stroke': 'blue' }, /* 'stroke-dasharray': '6,2' */
	        '.uml-class-attrs-rect': { 'opacity': 1.0 , 'stroke': 'blue'},
	        '.uml-class-methods-rect': {  'opacity': 0.7 , 'stroke': 'blue'}
		});		
	} else {
		e.attr({
			'.uml-class-name-rect': { 'opacity': 0.9 },
			'.uml-class-attrs-rect': { 'opacity': 0.9 },
			'.uml-class-methods-rect': {  'opacity': 0.9 }
		});
	}
	e._ENTITY = entity
	return e;
}

myUML.createPackage = function (entity, attributes) { 

	var e = new myUML.Package({
		id : entity.identifier,
		size : {
			width : 200,
			height : (16 * attributes.length + 30) //todo
		},
		name : "" + entity.name,
		attributes: attributes
	})
	e._ENTITY = entity;
	return e;	
} 

myUML.PackagedElement = joint.shapes.basic.Generic.extend({
    // copied and modified from the JointJS-UML library
    markup: [
        '<g class="rotatable">',
          '<g class="scalable">',
            //'<rect class="uml-class-name-rect"/><rect class="uml-class-attrs-rect"/><rect class="uml-class-methods-rect"/>',
            '<rect class="uml-class-name-rect"/><rect class="uml-class-attrs-rect"/>',
          '</g>',
          //'<text class="uml-class-name-text"/><text class="uml-class-attrs-text"/><text class="uml-class-methods-text"/>',
          '<text class="uml-class-name-text"/><text class="uml-class-attrs-text"/>',
          '</g>'
    ].join(''),

    defaults: joint.util.deepSupplement({

        type: 'myUML.PackagedElement',

        attrs: {
            rect: { 'width': 200 },

            '.uml-class-name-rect': { 'stroke': 'black', 'stroke-width': 1, 'fill': '#B4D1B4' },
            '.uml-class-attrs-rect': { 'stroke': 'black', 'stroke-width': 1, 'fill': '#CFE6CF' },
            '.uml-class-methods-rect': { 'stroke': 'black', 'stroke-width': 1, 'fill': '#2980b9' },

            '.uml-class-name-text': {
                'ref': '.uml-class-name-rect', 'ref-y': .5, 'ref-x': .5, 'text-anchor': 'middle', 'y-alignment': 'middle', 'font-weight': 'bold',
                'fill': 'black', 'font-size': 12, 'font-family': 'Times New Roman'
            },
            '.uml-class-attrs-text': {
                'ref': '.uml-class-attrs-rect', 'ref-y': 5, 'ref-x': 5,
                'fill': 'black', 'font-size': 12, 'font-family': 'Times New Roman'
            },
            '.uml-class-methods-text': {
                'ref': '.uml-class-methods-rect', 'ref-y': 5, 'ref-x': 5,
                'fill': 'black', 'font-size': 12, 'font-family': 'Times New Roman'
            }
        },

        name: [],
        attributes: [],
        methods: []

    }, joint.shapes.basic.Generic.prototype.defaults),

    initialize: function() {

        _.bindAll(this, 'updateRectangles');

        this.on('change:name change:attributes change:methods', function() {
            this.updateRectangles();
            this.trigger('uml-update');
        });

        this.updateRectangles();

        joint.shapes.basic.Generic.prototype.initialize.apply(this, arguments);
    },

    getClassName: function() {
        return this.get('name');
    },

    updateRectangles: function() {

        var attrs = this.get('attrs');

        var rects = [
            { type: 'name', text: this.getClassName() },
            { type: 'attrs', text: this.get('attributes') },
            { type: 'methods', text: this.get('methods') }
        ];

        var offsetY = 0;

        _.each(rects, function(rect) {

            var lines = _.isArray(rect.text) ? rect.text : [rect.text];
            var rectHeight = lines.length * 20 + 20;

            attrs['.uml-class-' + rect.type + '-text'].text = lines.join('\n');
            attrs['.uml-class-' + rect.type + '-rect'].height = rectHeight;
            attrs['.uml-class-' + rect.type + '-rect'].transform = 'translate(0,'+ offsetY + ')';

            offsetY += rectHeight;
        });
    }

});


myUML.PackagedElementView = joint.dia.ElementView.extend({

           
    initialize: function() {

        joint.dia.ElementView.prototype.initialize.apply(this, arguments);

        this.model.on('uml-update', _.bind(function() {
            this.update();
            this.resize();
        }, this));
    }
});


myUML.Package = myUML.PackagedElement.extend({
	//do not change the type. This is used in service call to avoid querying package objects.
    defaults: joint.util.deepSupplement({
        type: 'myUML.Package',
        attrs: {
            '.uml-class-name-rect': { 'stroke': 'black', 'stroke-width': 1,fill : '#DBB6DB' },
            '.uml-class-attrs-rect': { 'stroke': 'black', 'stroke-width': 1,fill : '#E6CFE6' },
            '.uml-class-methods-rect': { 'stroke': 'black', 'stroke-width': 1,fill : '#E6CFE6' }
        }
    }, myUML.PackagedElement.prototype.defaults),

    getClassName: function() {
        return ['<<Package>>', this.get('name')];
    }	
});
myUML.PackageView = myUML.PackagedElementView;


//todo: type is missing??
myUML.Class = myUML.PackagedElement.extend({
});

myUML.ClassView = myUML.PackagedElementView;


myUML.Abstract = myUML.Class.extend({

    defaults: joint.util.deepSupplement({
        type: 'myUML.Abstract',
        attrs: {
            '.uml-class-name-rect': { 'stroke': 'black', 'stroke-width': 1,fill : '#e74c3c' },
            '.uml-class-attrs-rect': { 'stroke': 'black', 'stroke-width': 1,fill : '#c0392b' },
            '.uml-class-methods-rect': { 'stroke': 'black', 'stroke-width': 1,fill : '#c0392b' }
        }
    }, myUML.PackagedElement.prototype.defaults),

    getClassName: function() {
        return ['<<Abstract>>', this.get('name')];
    }

});
myUML.AbstractView = myUML.ClassView;

myUML.Interface = myUML.Class.extend({

    defaults: joint.util.deepSupplement({
        type: 'myUML.Interface',
        attrs: {
            '.uml-class-name-rect': { 'stroke': 'black', 'stroke-width': 1,fill : '#f1c40f' },
            '.uml-class-attrs-rect': { 'stroke': 'black', 'stroke-width': 1,fill : '#f39c12' },
            '.uml-class-methods-rect': { 'stroke': 'black', 'stroke-width': 1,fill : '#f39c12' }
        }
    }, myUML.PackagedElement.prototype.defaults),

    getClassName: function() {
        return ['<<Interface>>', this.get('name')];
    }

});
myUML.InterfaceView = myUML.ClassView;


myUML.Enumeration = myUML.Class.extend({

    defaults: joint.util.deepSupplement({
        type: 'myUML.Enumeration',
        attrs: {
            '.uml-class-name-rect': { 'stroke': 'black', 'stroke-width': 1,fill : '#F7819F' },
            '.uml-class-attrs-rect': { 'stroke': 'black', 'stroke-width': 1,fill : '#F5A9BC' },
            '.uml-class-methods-rect': { 'stroke': 'black', 'stroke-width': 1,fill : '#F8E0E6' }
        }
    }, myUML.PackagedElement.prototype.defaults),

    getClassName: function() {
    	//type should be given as stereotype .. document ?
        return [ this.get('name')];
    }

});
myUML.EnumerationView = myUML.ClassView;


joint.shapes.uml.Generalization = joint.dia.Link.extend({
    defaults: {
        type: 'myUML.Generalization',
        attrs: { '.marker-target': { d: 'M 20 0 L 0 10 L 20 20 z', fill: 'white' }}
    }
});


myUML.Artifact = myUML.Class.extend({

    defaults: joint.util.deepSupplement({
        type: 'myUML.Artifact',
        attrs: {
            '.uml-class-name-rect': { 'stroke': 'black', 'stroke-width': 1,fill : '#58FA82' },
            '.uml-class-attrs-rect': { 'stroke': 'black', 'stroke-width': 1,fill : '#A9F5A9' },
            '.uml-class-methods-rect': { 'stroke': 'black', 'stroke-width': 1,fill : '#58FAAC' }
        }
    }, myUML.PackagedElement.prototype.defaults),

    getClassName: function() {
    	//type should be given as stereotype .. document ?
        return [ this.get('name')];
    }

});
myUML.ArtifactView = myUML.ClassView;

joint.shapes.uml.Generalization = joint.dia.Link.extend({
    defaults: {
        type: 'myUML.Generalization',
        attrs: { '.marker-target': { d: 'M 20 0 L 0 10 L 20 20 z', fill: 'white' }}
    }
});


myUML.Datatype = myUML.Class.extend({

    markup: [
             '<g class="rotatable">',
               '<g class="scalable">',
                 //'<rect class="uml-class-name-rect"/><rect class="uml-class-attrs-rect"/><rect class="uml-class-methods-rect"/>',
                 '<rect class="uml-class-name-rect"/>',
               '</g>',
               //'<text class="uml-class-name-text"/><text class="uml-class-attrs-text"/><text class="uml-class-methods-text"/>',
               '<text class="uml-class-name-text"/>',
               '</g>'
         ].join(''),

    defaults: joint.util.deepSupplement({
        type: 'myUML.Datatype',
        attrs: {
        	rect: { 'height': 50 },
        	
            '.uml-class-name-rect': { 'stroke': 'black', 'stroke-width': 1,fill : '#e74c3c' }
//            '.uml-class-attrs-rect': { 'stroke': 'black', 'stroke-width': 1,fill : '#c0392b' },
//            '.uml-class-methods-rect': { 'stroke': 'black', 'stroke-width': 1,fill : '#c0392b' }
        }
    }, myUML.PackagedElement.prototype.defaults),

    getClassName: function() {
        return ['<<Datatype>>', this.get('name')];
    }

});

myUML.DatatypeView = myUML.ClassView;

/*
 * OWL STUFF
 * 
 */

myUML.OwlClass = myUML.Class.extend({

    defaults: joint.util.deepSupplement({
        type: 'myUML.OwlClass',
//        type: 'myUML.Clickable',
        attrs: {
            '.uml-class-name-rect': { 'stroke': 'black', 'stroke-width': 1,fill : '#CEEDEA' },
            '.uml-class-attrs-rect': { 'stroke': 'black', 'stroke-width': 1,fill : '#CEEDEA' },
            '.uml-class-methods-rect': { 'stroke': 'black', 'stroke-width': 1,fill : '#CEEDEA' }
        }
    }, myUML.PackagedElement.prototype.defaults),

    getClassName: function() {
        return ['<<OwlClass>>', this.get('name')];
    }

});
myUML.OwlClassView = myUML.ClassView;

myUML.ClickableView = joint.dia.ElementView.extend({
	  pointerdown: function () {
	    this._click = true;
	    joint.dia.ElementView.prototype.pointerdown.apply(this, arguments);
	  },
	  pointermove: function () {
	    this._click = false;
	    joint.dia.ElementView.prototype.pointermove.apply(this, arguments);
	  },
	  pointerup: function (evt, x, y) {
	    if (this._click) {
	      // triggers an event on the paper and the element itself
	      this.notify('cell:click', evt, x, y); 
	    } else {
	      joint.dia.ElementView.prototype.pointerup.apply(this, arguments);
	    }
	  }
	});

// also:
//var paper = new joint.dia.Paper({
//	  // el, width, height etc.
//	  elementView: ClickableView
//	});

myUML.OwlUnionClass = myUML.OwlClass.extend({

    defaults: joint.util.deepSupplement({
        type: 'myUML.OwlUnionClass',
        attrs: {
            '.uml-class-name-rect': { 'stroke': 'black', 'stroke-width': 1,fill : '#DBB2B6' },
            '.uml-class-attrs-rect': { 'stroke': 'black', 'stroke-width': 1,fill : '#DBB2B6' },
            '.uml-class-methods-rect': { 'stroke': 'black', 'stroke-width': 1,fill : '#DBB2B6' }
        }
    }, myUML.PackagedElement.prototype.defaults),

    getClassName: function() {
        return ['<<OwlUnionClass>>', this.get('name')];
    }

});
myUML.OwlUnionView = myUML.ClassView;

myUML.OwlIntersectionClass = myUML.OwlClass.extend({

    defaults: joint.util.deepSupplement({
        type: 'myUML.OwlIntersectionClass',
        attrs: {
            '.uml-class-name-rect': { 'stroke': 'black', 'stroke-width': 1,fill : '#E6A8AE' },
            '.uml-class-attrs-rect': { 'stroke': 'black', 'stroke-width': 1,fill : '#E6A8AE' },
            '.uml-class-methods-rect': { 'stroke': 'black', 'stroke-width': 1,fill : '#E6A8AE' }
        }
    }, myUML.PackagedElement.prototype.defaults),

    getClassName: function() {
        return ['<<OwlIntersectionClass>>', this.get('name')];
    }

});
myUML.OwlIntersectionClassView = myUML.ClassView;

myUML.OwlComplementClass = myUML.OwlClass.extend({

    defaults: joint.util.deepSupplement({
        type: 'myUML.OwlComplementClass',
        attrs: {
            '.uml-class-name-rect': { 'stroke': 'black', 'stroke-width': 1,fill : '#E87B86' },
            '.uml-class-attrs-rect': { 'stroke': 'black', 'stroke-width': 1,fill : '#E87B86' },
            '.uml-class-methods-rect': { 'stroke': 'black', 'stroke-width': 1,fill : '#E87B86' }
        }
    }, myUML.PackagedElement.prototype.defaults),

    getClassName: function() {
        return ['<<OwlComplementClass>>', this.get('name')];
    }

});
myUML.OwlComplementClassView = myUML.ClassView;

myUML.OwlRestrictionClass = myUML.OwlClass.extend({

    defaults: joint.util.deepSupplement({
        type: 'myUML.OwlRestrictionClass',
        attrs: {
            '.uml-class-name-rect': { 'stroke': 'black', 'stroke-width': 1,fill : '#F5B0B7' },
            '.uml-class-attrs-rect': { 'stroke': 'black', 'stroke-width': 1,fill : '#F5B0B7' },
            '.uml-class-methods-rect': { 'stroke': 'black', 'stroke-width': 1,fill : '#F5B0B7' }
        }
    }, myUML.PackagedElement.prototype.defaults),

    getClassName: function() {
        return ['<<OwlRestrictionClass>>', this.get('name')];
    }

});
myUML.OwlRestrictionClassView = myUML.ClassView;


myUML.OwlObjectProperty = myUML.OwlClass.extend({
    defaults: joint.util.deepSupplement({
        type: 'myUML.OwlObjectProperty',
        attrs: {
            '.uml-class-name-rect': { 'stroke': 'black', 'stroke-width': 1,fill : '#C1F5B0' },
            '.uml-class-attrs-rect': { 'stroke': 'black', 'stroke-width': 1,fill : '#C1F5B0' },
            '.uml-class-methods-rect': { 'stroke': 'black', 'stroke-width': 1,fill : '#C1F5B0' }
        }
    }, myUML.PackagedElement.prototype.defaults),

    getClassName: function() {
        return ['<<OwlObjectProperty>>', this.get('name')];
    }

});
myUML.OwlObjectPropertyView = myUML.ClassView;

myUML.OwlDatatypeProperty = myUML.OwlClass.extend({

    defaults: joint.util.deepSupplement({
        type: 'myUML.OwlDatatypeProperty',
        attrs: {
            '.uml-class-name-rect': { 'stroke': 'black', 'stroke-width': 1,fill : '#B1D6A5' },
            '.uml-class-attrs-rect': { 'stroke': 'black', 'stroke-width': 1,fill : '#B1D6A5' },
            '.uml-class-methods-rect': { 'stroke': 'black', 'stroke-width': 1,fill : '#B1D6A5' }
        }
    }, myUML.PackagedElement.prototype.defaults),

    getClassName: function() {
        return ['<<OwlDatatypeProperty>>', this.get('name')];
    }

});
myUML.OwlDatatypePropertyView = myUML.ClassView;

myUML.OwlAnnotationProperty = myUML.OwlClass.extend({

    defaults: joint.util.deepSupplement({
        type: 'myUML.OwlAnnotationProperty',
        attrs: {
            '.uml-class-name-rect': { 'stroke': 'black', 'stroke-width': 1,fill : '#9BC48D' },
            '.uml-class-attrs-rect': { 'stroke': 'black', 'stroke-width': 1,fill : '#9BC48D' },
            '.uml-class-methods-rect': { 'stroke': 'black', 'stroke-width': 1,fill : '#9BC48D' }
        }
    }, myUML.PackagedElement.prototype.defaults),

    getClassName: function() {
        return ['<<OwlAnnotationProperty>>', this.get('name')];
    }

});
myUML.OwlAnnotationPropertyView = myUML.ClassView;
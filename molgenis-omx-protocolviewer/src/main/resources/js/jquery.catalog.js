(function($, molgenis) {
    "use strict";

    var restApi = new molgenis.RestClient();
    var searchApi = new molgenis.SearchClient();

    $.fn.catalog = function(options) {
        // call pager method
        if (typeof options == 'string') {
            var args = Array.prototype.slice.call(arguments, 1);
            if(args.length === 0)
                return this.data('catalog')[options]();
            else if(args.length === 1)
                return this.data('catalog')[options](args[0]);
        }

        var container = this;

        // cleanup
        $('.catalog-tree', this).dynatree('destroy');
        container.empty();

        // create catalog
        var settings = $.extend({}, $.fn.catalog.defaults, options);

        // catalog plugin methods
        container.data('catalog', {
            getSelectedItems : function(){
                return $.map($('.catalog-tree', container).dynatree('getTree').getSelectedNodes(), function(node) {
                    if(!node.data.isFolder){
                        return {'item': node.data.key, 'folder': node.parent.data.key};
                    }
                    return null;
                });
            },
            selectItem : function(options) {
                console.log('select item', options);
                $('.catalog-tree', container).dynatree('getTree').getNodeByKey(options.item).select(options.select);
            }
        });

        var doSearch = false;
        var updatedNodes = null;
        var selectedAllNodes = null;
        var treePrevState = null;
        var searchQuery = null;

        var items = [];
        items.push('<div class="input-append">');
        items.push('<input class="catalog-search-text" type="text" title="Enter your search term">');
        items.push('<button class="catalog-search-btn btn" type="button"><i class="icon-large icon-search"></i></button>');
        items.push('<button class="catalog-search-clear-btn btn" type="button"><i class="icon-large icon-remove"></i></button>');
        items.push('</div>');
        items.push('<div class="catalog-tree"></div>');
        this.append(items.join(''));

        var treeContainer = $('.catalog-tree', this);

        function createChildren(protocolUri, featureOpts, protocolOpts) {
            var protocol = restApi.get(protocolUri, [ 'features', 'subprotocols' ]);
            var children = [];
            if (protocol.subprotocols) {
                if(settings.sort) {
                    protocol.subprotocols.items.sort(settings.sort);
                }
                // TODO deal with multiple entity pages
                $.each(protocol.subprotocols.items, function() {
                    children.push($.extend({
                        key : this.href,
                        title : this.name,
                        tooltip : molgenis.i18n.get(this.description),
                        isFolder : true,
                        isLazy : protocolOpts.expand != true,
                        children : protocolOpts.expand ? createChildren(this.href, featureOpts, protocolOpts) : null
                    }, protocolOpts));
                });
            }
            if (protocol.features) {
                if(settings.sort) {
                    protocol.features.items.sort(settings.sort);
                }

                // TODO deal with multiple entity pages
                $.each(protocol.features.items, function() {
                    children.push($.extend({
                        key : this.href,
                        title : this.name,
                        tooltip : molgenis.i18n.get(this.description)
                    }, featureOpts));
                });
            }
            return children;
        }

        function expandNodeRec(node) {
            if (node.childList == undefined) {
                node.toggleExpand();
            } else {
                $.each(node.childList, function() {
                    expandNodeRec(this);
                });
            }
        }

        // create catalog async
        restApi.getAsync('/api/v1/protocol/' + settings.protocolId, [ 'features', 'subprotocols' ], null, function(protocol) {
            if (treeContainer.children('ul').length > 0) {
                treeContainer.dynatree('destroy');
            }
            treeContainer.empty();
            if (typeof protocol === 'undefined') {
                treeContainer.append("<p>No features available</p>");
                return;
            }

            // render tree and open first branch
            treeContainer.dynatree({
                checkbox : true,
                selectMode : 3,
                minExpandLevel : 2,
                debugLevel : 0,
                children : [ {
                    key : protocol.href,
                    title : protocol.name,
                    icon : false,
                    isFolder : true,
                    isLazy : true,
                    //hideCheckbox: true, //FIXME hide first root + first level of checkboxes
                    children : createChildren(protocol.href, {
                        select : false
                    }, {})
                } ],
                onLazyRead : function(node) {
                    // workaround for dynatree lazy parent node select bug
                    var opts = node.isSelected() ? {
                        expand : true,
                        select : true
                    } : {};
                    var children = createChildren(node.data.key, opts, opts);
                    node.setLazyNodeStatus(DTNodeStatus_Ok);
                    node.addChild(children);
                },
                onClick : function(node, event) {
                    if (node.getEventTargetType(event) === "title" || node.getEventTargetType(event) === "icon") {
                        if(node.data.isFolder) {
                            settings.onFolderClick(node.data.key);
                        } else {
                            settings.onItemClick(node.data.key);
                        }
                    }
                },
                onSelect : function(select, node) {
                    if(node.data.isFolder) {
                        if (select){
                            expandNodeRec(node);
                        }
                        settings.onFolderSelect(node.data.key, select);
                    } else {
                        settings.onItemSelect(node.data.key, select);
                    }
                }
            });
            treeContainer.find(".dynatree-checkbox").first().hide();
        });

        function search(query) {
            if (query) {
                doSearch = true;
                searchAndUpdateTree(query, '/api/v1/protocol/' + settings.protocolId);
            }
        }

        /**
         *
         * @param query
         * @param protocolUri
         */
        function searchAndUpdateTree(query, protocolUri) {

            function preloadEntities(protocolIds, featureIds, callback) {

                var batchSize = 500;
                var nrProtocolRequests = Math.ceil(protocolIds.length / batchSize);
                var nrFeatureRequests = Math.ceil(featureIds.length / batchSize);
                var nrRequest = nrFeatureRequests + nrProtocolRequests;
                if(nrRequest > 0){
                    var workers = [], i;
                    for(i = 0 ; i < nrRequest ; i++) {
                        workers[i] = false;
                    }
                    for(i = 0 ; i < nrRequest ; i++) {
                        var entityType = i < nrProtocolRequests ?  "protocol" : "observablefeature";
                        var ids = i < nrProtocolRequests ?  protocolIds : featureIds;
                        var start = i < nrProtocolRequests ? i * batchSize : (i - nrProtocolRequests) * batchSize;
                        var q = {
                            q : [ {
                                "field" : "id",
                                "operator" : "IN",
                                "value" : ids
                            } ],
                            num : batchSize,
                            start : start
                        };
                        restApi.getAsync('/api/v1/' + entityType, null, q, $.proxy(function(){
                            workers[this.i] = true;
                            if($.inArray(false, workers) === -1){
                                this.callback();
                            }
                        }, {"i" : i, "callback" : callback}));
                    }
                }else{
                    callback();
                }
            }

            function selectedNodeIds(selectedNodes){
                var selectedIds = [];
                $.each(selectedNodes, function(index, element){
                    selectedIds.push(hrefToId(element.data.key));
                });
                return selectedIds;
            }

            function findAllParents(selectedFeatureIds, callback){
                var selectedFeaturesToLoad = [];
                if(selectedFeatureIds.length > 0){
                    var queryRules = [];
                    $.each(selectedFeatureIds, function(index, featureId){
                        if(queryRules.length !== 0){
                            queryRules.push({
                                operator : 'OR'
                            });
                        }
                        queryRules.push({
                            field : 'id',
                            operator : 'EQUALS',
                            value : featureId
                        });
                    });
                    queryRules.push({
                        operator : 'LIMIT',
                        value : 10000
                    });
                    var searchRequest = {
                        documentType : 'protocolTree-' + settings.protocolId,
                        queryRules : queryRules
                    };
                    searchApi.search(searchRequest, function(searchResponse){
                        $.each(searchResponse.searchHits, function(index, hit){
                            selectedFeaturesToLoad.push(hit);
                        });
                        callback(selectedFeaturesToLoad);
                    });
                }else{
                    callback(selectedFeaturesToLoad);
                }
            }

            function hrefToId (href){
                return href.substring(href.lastIndexOf('/') + 1);
            }

            searchQuery = $.trim(query);

            searchApi.search(createSearchRequest(), function(searchResponse) {
                var protocol = restApi.get(protocolUri);
                var rootNode =  treeContainer.dynatree("getTree").getNodeByKey(protocol.href);
                var selectedFeatureIds = selectedNodeIds(rootNode.tree.getSelectedNodes());
                //only save the state when search occurs for first time only
                if(treePrevState == null) treePrevState = rootNode.tree.toDict();
                if(updatedNodes == null) updatedNodes = {};
                if(selectedAllNodes == null){
                    selectedAllNodes = {};
                    $.each(rootNode.tree.getSelectedNodes(), function(index, node){
                        selectedAllNodes[node.data.key] = node;
                    });
                }

                findAllParents(selectedFeatureIds, function(hitsToHide){
                    var searchHits = searchResponse["searchHits"];
                    var protocols = {};
                    var features = {};
                    $.each(searchHits, function(){
                        var object = $(this)[0]["columnValueMap"];
                        var nodes = object["path"].split(".");
                        //collect all features and their ancestors using REST API first.
                        for(var i = 0; i < nodes.length; i++){
                            if(nodes[i].indexOf("F") === 0) features[nodes[i].substring(1)] = null;
                            else protocols[nodes[i]] = null;
                        }
                    });

                    preloadEntities(Object.keys(protocols), Object.keys(features), function(){
                        var cachedNode = {};
                        var topNodes = [];
                        $.each(searchHits, function(){
                            var object = $(this)[0]["columnValueMap"];
                            var nodes = object["path"].split(".");
                            var entityId = object["id"];
                            //split the path to get all ancestors;
                            for(var i = 0; i < nodes.length; i++) {
                                var isFeature = nodes[i].indexOf("F") === 0;
                                if(isFeature) nodes[i] = nodes[i].substring(1);
                                if(!cachedNode[nodes[i]]){
                                    var entityInfo = null;
                                    var options = null;
                                    //this is the last node and check if this is a feature
                                    if(isFeature){
                                        entityInfo = restApi.get('/api/v1/observablefeature/' + nodes[i]);
                                        options = {
                                            isFolder : false
                                        };
                                    }else{
                                        entityInfo = restApi.get('/api/v1/protocol/' + nodes[i]);
                                        options = {
                                            isFolder : true,
                                            isLazy : true,
                                            expand : true,
                                            children : []
                                        };
                                        //check if the last node is protocol if so recursively adding all subNodes
                                        if(i === nodes.length - 1) {
                                            options.children = createChildren('/api/v1/protocol/' + nodes[i], null, {expand : false});
                                            $.each(options.children, function(index, child){
                                                var nodeId = child.key.substring(child.key.lastIndexOf('/') + 1);
                                                if(!cachedNode[nodeId]){
                                                    if(child.isFolder) child.children = [];
                                                    cachedNode[nodeId] = child;
                                                }
                                            });
                                        }
                                    }
                                    options = $.extend({
                                        key : entityInfo.href,
                                        title : entityInfo.name,
                                        tooltip : molgenis.i18n.get(entityInfo.description)
                                    }, options);

                                    if($.inArray(entityInfo.href, selectedFeatureIds) !== -1){
                                        options["select"] = true;
                                    }
                                    //locate the node in dynatree and otherwise create the node and insert it
                                    if(i != 0){
                                        var parentNode = cachedNode[nodes[i-1]];
                                        parentNode.expand = true;
                                        parentNode["children"].push(options);
                                        cachedNode[nodes[i-1]] = parentNode;
                                    }
                                    else
                                        topNodes.push(options);
                                    cachedNode[nodes[i]] = options;
                                }else{
                                    if (nodes[i] === entityId.toString() && i != 0) {
                                        var parentNode = cachedNode[nodes[i-1]];
                                        parentNode.expand = true;
                                        var childNode = cachedNode[nodes[i]];
                                        if($.inArray(childNode, parentNode.children) === -1){
                                            parentNode.children.push(cachedNode[nodes[i]]);
                                            cachedNode[nodes[i-1]] = parentNode;
                                        }
                                    }
                                }
                            }
                        });

                        $.each(hitsToHide, function(index, hit){
                            var object = hit.columnValueMap;
                            var nodes = object["path"].split(".");
                            //split the path to get all ancestors;
                            for(var i = 0; i < nodes.length; i++) {
                                var isFeature = nodes[i].indexOf("F") === 0;
                                if(isFeature) nodes[i] = nodes[i].substring(1);
                                if(cachedNode[nodes[i]] && i !== 0){
                                    if(isFeature) cachedNode[nodes[i]].select = true;
                                    else {
                                        var currentNode = cachedNode[nodes[i]];
                                        if(currentNode.children.length === 0){
                                            currentNode.children = createChildren('/api/v1/protocol/' + nodes[i], null, {expand : false});
                                            cachedNode[nodes[i]] = currentNode;

                                            $.each(currentNode.children, function(index, child){
                                                var nodeId = child.key.substring(child.key.lastIndexOf('/') + 1);
                                                if(!cachedNode[nodeId]){
                                                    if(child.isFolder) child.children = [];
                                                    cachedNode[nodeId] = child;
                                                }
                                            });
                                        }
                                    }
                                }
                            }
                        });

                        sortNodes(topNodes);
                        rootNode.removeChildren();
                        if(topNodes.length !== 0)
                            rootNode.addChild(topNodes[0].children);

                        if(treeContainer.next().length > 0) treeContainer.next().remove();
                        if(topNodes.length === 0) {
                            treeContainer.hide();
                            treeContainer.after('<div id="match-message">No data items were matched!</div>');
                        } else treeContainer.show();
                    });
                });
            });
        }

        function sortNodes(nodes){
            if(nodes && settings.sort){
                nodes.sort(function(a,b){
                    return settings.sort(a.title, b.title);
                });
                $.each(nodes, function(index, node){
                    if(node.children) sortNodes(node.children);
                });
            }
        }

        function createSearchRequest(){
            var terms = searchQuery.split(" ");
            var queryRules = [];
            $.each(terms, function(index, element){
                //FIXME can searchApi do the tokenization?
                queryRules.push({
                    operator : 'SEARCH',
                    value : element
                });
                if(index < terms.length - 1)
                    queryRules.push({
                        operator : 'AND'
                    });
            });

            //todo: how to unlimit the search result
            queryRules.push({
                operator : 'LIMIT',
                value : 1000000
            });
            var searchRequest = {
                documentType : "protocolTree-" + settings.protocolId,
                queryRules : queryRules
            };
            return searchRequest;
        }

        //merge the newly selected nodes back into the previous state of tree
        function recursivelyExpand (selectedFeatures, expandedNodes, nodeData, cachedNodes) {
            var entityInfo = restApi.get(nodeData.key, ["features", "subprotocols"]);
            var options = null;
            if(entityInfo.features.items.length && entityInfo.features.items.length != 0){
                $.each(entityInfo.features.items, function(index, feature){
                    options = {
                        key : feature.href,
                        title : feature.name,
                        tooltip : molgenis.i18n(feature.description),
                        isFolder : false,
                        expand : true
                    };
                    if($.inArray(feature.href, selectedFeatures) != -1){
                        options.select = true;
                    }
                    nodeData.children.push(options);
                    if(cachedNodes != null){
                        var fragments = options.key.split("/");
                        var id = fragments[fragments.length - 1];
                        if(!cachedNodes[id]) cachedNodes[id] = options;
                    }
                });
            }
            if(entityInfo.subprotocols.items.length && entityInfo.subprotocols.items.length != 0){
                $.each(entityInfo.subprotocols.items, function(index, protocol){
                    options = {
                        key : protocol.href,
                        title : protocol.name,
                        tooltip : molgenis.i18n.get(protocol.description),
                        isFolder : true,
                        isLazy : true,
                        children : []
                    };
                    if(expandedNodes === null || $.inArray(protocol.href, expandedNodes) != -1){
                        options.expand = true;
                        nodeData.children.push(recursivelyExpand(selectedFeatures, expandedNodes, options, cachedNodes));
                    }else{
                        nodeData.children.push(options);
                    }
                    if(cachedNodes != null){
                        var fragments = options.key.split("/");
                        var id = fragments[fragments.length - 1];
                        if(!cachedNodes[id]) cachedNodes[id] = options;
                    }
                });
            }
            return nodeData;
        }

        function clearSearch() {
            var rootNode = treeContainer.dynatree("getTree").getRoot();
            if(treePrevState == null) {
                if(rootNode.tree.getSelectedNodes().length == 0){
                    rootNode.tree.reload();
                }
                return;
            }
            var prevRenderMode = rootNode.tree.enableUpdate(false); // disable rendering

            rootNode.removeChildren();
            rootNode.addChild(treePrevState.children);

            var selectedFeatureNodes = [];
            var expandedNodes = [];
            //check if newly selected nodes exist in previous tree
            if(updatedNodes.select != null){
                $.each(updatedNodes.select, function(index, node){
                    while(!rootNode.tree.getNodeByKey(node.data.key)){
                        if(node.data.isFolder)
                            expandedNodes.push(node.data.key);
                        else
                            selectedFeatureNodes.push(node.data.key);
                        node = node.parent;
                    }
                    var currentNode = rootNode.tree.getNodeByKey(node.data.key);
                    if(node.data.isFolder){
                        currentNode.data.children = [];
                        var nodeData = recursivelyExpand(selectedFeatureNodes, expandedNodes, currentNode.data);
                        sortNodes(nodeData.children);
                        currentNode.removeChildren();
                        currentNode.addChild(nodeData.children);
                        currentNode.toggleExpand();
                    }else{
                        currentNode.select(true);
                    }
                });
            }
            if(updatedNodes.unselect != null){
                $.each(updatedNodes.unselect, function(index, node){
                    var currentNode = rootNode.tree.getNodeByKey(node.data.key);
                    if(currentNode != null) currentNode.select(false);
                });
            }

            rootNode.tree.enableUpdate(prevRenderMode); // restore previous rendering state

            //reset variables
            doSearch = false;
            updatedNodes = null;
            treePrevState = null;
            selectedAllNodes = null;
            $('.catalog-search-text', container).val("");
            treeContainer.show();
            if(treeContainer.next().length > 0) treeContainer.next().remove();
            //updateFeatureSelection(rootNode.tree); //TODO can we remove this safely?
        }

        // register event handlers
        $('.catalog-search-text', container).keyup(function(e){
            e.preventDefault();
            if(e.keyCode == 13 || e.which === '13') { // enter
                $('.catalog-search-btn', container).click();
            }
        });

        $('.catalog-search-btn', container).click(function(e) {
            e.preventDefault();
            search($('.catalog-search-text', container).val());
        });

        $('.catalog-search-clear-btn', container).click(function(e) {
            e.preventDefault();
            clearSearch();
        });

        return this;
    };

    // default pager settings
    $.fn.catalog.defaults = {
        'protocolId' : null,
        'selectedItems': null,
        'sort' : null,
        'onFolderClick' : null,
        'onItemClick' : null,
        'onFolderSelect' : null,
        'onItemSelect' : null
    };
}($, window.top.molgenis = window.top.molgenis || {}));
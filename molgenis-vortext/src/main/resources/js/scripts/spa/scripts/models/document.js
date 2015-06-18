/* -*- mode: js2; tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2; js2-basic-offset: 2 -*- */
define(function (require) {
  'use strict';

  var Q = require("Q");
  var _ = require("underscore");
  var Backbone = require("backbone");
  var PDFJS = require("PDFJS");

  var TextSearcher = new (require("../vendor/dom-anchor-bitap/text_searcher"))();

  var RenderingStates = window.RenderingStates = {
    INITIAL: 0,
    RUNNING: 1,
    HAS_PAGE: 2,
    HAS_CONTENT: 3,
    FINISHED: 4
  };

  var Page = Backbone.Model.extend({
    defaults: {
      raw: null,
      content: null,
      state: RenderingStates.INITIAL,
      annotations: {}
    }
  });

  var Pages = Backbone.Collection.extend({
    model: Page,
    _buildAggregate: function() {
      this._aggregate = { totalLength: 0, nodes: [], pages: [], text: "" };
    },
    _appendAggregate: function(pageIndex, pageContent) {
      var totalLength = this._aggregate.totalLength;
      var offset = 0;
      var items = pageContent.items;
      for (var j = 0; j < items.length; j++) {
        var item = items[j];
        var str = item.str;
        var nextOffset = offset + str.length;
        var node = { pageIndex: pageIndex,
                     nodeIndex: j,
                     interval: { lower: totalLength + offset,
                                 upper: totalLength + nextOffset }};
        this._aggregate.text += (str + " ");
        offset = nextOffset + 1;
        this._aggregate.nodes.push(node);
      }
      this._aggregate.pages.push({ offset: totalLength, length: offset });
      this._aggregate.totalLength += offset;
    },
    _requestPage: function(model, pagePromise) {
      return pagePromise
        .then(function(raw) {
          model.set({
            raw: raw,
            state: RenderingStates.HAS_PAGE
          });
          return raw.getTextContent();
        })
        .then(function(content) {
          model.set({
            content: content,
            state: RenderingStates.HAS_CONTENT
          });
          return content;
        });
    },
    annotate: function(annotation, color, uuid) {
      var self = this;
      var aggregate = this._aggregate;
      if (!aggregate) {
        return [];
      }
      var text = aggregate.text;

      var findMatch = _.memoize(function(text, annotation) {
        var match;

        match = TextSearcher.searchExact(text, annotation.content);
        if(_.size(match) === 0) {
          if(annotation.position && annotation.position <= text.length) { // Let's try fuzzy search
            if(annotation.prefix && annotation.suffix) {
              match = TextSearcher.searchFuzzyWithContext(text,
                                                          annotation.prefix,
                                                          annotation.suffix,
                                                          annotation.content,
                                                          annotation.position,
                                                          annotation.position + annotation.content.length);
            } else {
              match = TextSearcher.searchFuzzy(text, annotation.content, annotation.position);
            }
          }
        }
        return match;
      }, function(text, annotation) { // hash
        return text.substr(text.length - 32) + annotation;
      });

      var match = findMatch(text, annotation);

      if(!_.isEmpty(match.matches)) {
        var lower = match.matches[0].start;
        var upper = match.matches[0].end;
        var mapping = [];
        var nodes = aggregate.nodes;
        var pages = aggregate.pages;
        var nrNodes = nodes.length;
        for(var i = 0; i < nrNodes; ++i) {
          var node = _.clone(nodes[i]);
          if(node.interval.lower < upper && lower < node.interval.upper) {
            var pageOffset = pages[node.pageIndex].offset;
            var interval = { lower: node.interval.lower - pageOffset,
                             upper: node.interval.upper - pageOffset};
            mapping.push(_.extend(node, { range: _.clone(interval),
                                          interval: _.clone(interval)}));
          }
        }
        if(!_.isEmpty(mapping)) {
          mapping[0].range.lower = lower - pages[mapping[0].pageIndex].offset;
          mapping[mapping.length - 1].range.upper = upper - pages[mapping[mapping.length - 1].pageIndex].offset;
        }

        return mapping.map(function(m) {
          m.color = color;
          m.uuid = annotation.uuid;
          return m;
        });
      }
      return [];
    },
    populate: function(pdf) {
      var self  = this;

      this._buildAggregate();

      var pageQueue = _.range(0, pdf.numPages);
      var pages = _.map(pageQueue, function(pageNr) {
        return new Page();
      });
      this.reset(pages, {silent: true}); // set a bunch of empty pages

      var process = function(arr) {
        if(arr.length === 0) {
          self.trigger("ready");
          return;
        }
        var pageIndex = _.first(arr);
        var page = pages[pageIndex];
        page.set({state: RenderingStates.RUNNING});
        var p = self._requestPage(page, pdf.getPage(pageIndex + 1));
        p.then(function(content) {
          self._appendAggregate(pageIndex, content);
          process(_.rest(arr));
        });
      };
      process(pageQueue);
    }
  });

  var Document = Backbone.Model.extend({
    defaults: {
      fingerprint: null,
      binary: null,
      raw: null
    },
    initialize: function() {
      var self = this;
      var pages = new Pages();
      this.set("pages", pages);
      pages.on("all", function(e, obj) {
        self.trigger("pages:" + e, obj);
      });
    },
    annotate: function(marginalia) {
      var self = this; // *sigh*
      if(!marginalia) {
        self.get("pages").map(function(page, pageIndex) {
          page.set({annotations: []});
        });
        return;
      }

      var getAnnotationsPerPage = function(marginalia) {
        var mappings = [];
        marginalia.forEach(function(marginalis) {
          var color = marginalis.get("color");
          var annotations = marginalis.get("annotations").toJSON();
          var m = _.flatten(annotations.map(function(annotation) {
            return self.get("pages").annotate(annotation, color);
          }));
          mappings.push.apply(mappings, m);
        });

        var result = {};
        mappings.forEach(function(mapping) {
          result[mapping.pageIndex] = result[mapping.pageIndex] || {};
          result[mapping.pageIndex][mapping.nodeIndex] = _.union(result[mapping.pageIndex][mapping.nodeIndex] || [], [mapping]);
        });
        return result;
      };

      var annotationsPerPage = getAnnotationsPerPage(marginalia);
      self.get("pages").map(function(page, pageIndex) {
        page.set({annotations: annotationsPerPage[pageIndex] || []});
      });
    },
    loadFromUrl: function(url) {
      var self = this;
      PDFJS.getDocument(url).then(function(pdf) {
        self.set({binary: null, raw: pdf, fingerprint: pdf.pdfInfo.fingerprint});
        self.get("pages").populate(pdf);
      });
    },
    loadFromData: function(data) {
      var self = this;
      this.set({binary: data});
      PDFJS.getDocument(data).then(function(pdf) {
        self.set({raw: pdf, fingerprint: pdf.pdfInfo.fingerprint});
        self.get("pages").populate(pdf);
      });
    }
  });

  return Document;
});

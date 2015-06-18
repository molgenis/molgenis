/* -*- tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2; js-indent-level: 2 -*- */
define(function (require) {
  'use strict';

  var React = require("react");
  var FileUtil = require("spa/helpers/fileUtil");

  var TopBar = React.createClass({
    loadExample: function() {
      var self = this;
      // From http://stackoverflow.com/questions/18447468/jquery-ajax-downloading-incomplete-binary-file
      var request = new XMLHttpRequest();
      request.open("GET", "/files/example.pdf", true);
      request.responseType = "arraybuffer";
      request.onload = function (e) {
        var arrayBuffer = request.response; // Note: not request.responseText
        if (arrayBuffer) {
          var byteArray = new Uint8Array(arrayBuffer);
          self.props.callback(byteArray);
        }
      };
      request.send(null);
    },
    triggerFileUpload: function() {
      this.refs.file.getDOMNode().click();
    },
    loadFile: function() {
      var file = this.refs.file.getDOMNode().files[0];
      if (file.type.match(this.props.mimeType)) {
        FileUtil.readFileAsBinary(file).then(this.props.callback);
      }
      return false;
    },
    render: function() {
      return (
          <div>
            <ul className="title-area">
              <li className="name">
                <h1><a href="/"><img src="/img/logo.png" />vortext</a></h1>
              </li>
            </ul>

            <section className="top-bar-section">
              <ul className="right">
                <input accept={this.props.accept} style={{display:"none"}} name="file" type="file" ref="file" onChange={this.loadFile} />

                <li><a onClick={this.loadExample}>Example</a></li>
                <li className="active" onClick={this.triggerFileUpload}><a>Upload</a></li>
              </ul>
            </section>
          </div>
      );
    }
  });

  return TopBar;
});

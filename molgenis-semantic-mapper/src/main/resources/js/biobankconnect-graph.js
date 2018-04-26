(function ($) {

    $.fn.bcgraph = function (array, options) {
        var mean = jStat.mean(array);
        var stdev = jStat.stdev(array);
        var cdfValues = array.map(function (element) {
            return jStat.normal.pdf(element, mean, stdev);
        });
        var data = {"points": []};
        var graphId = 'graph-id-' + new Date().getMilliseconds();
        this.attr('id', graphId);

        var settings = $.extend({}, $.fn.bcgraph.defaults, options);
        array.forEach(function (d, i) {
            data.points.push({"x": array[i], "y": cdfValues[i]});
        });
        vg.parse.spec($.fn.bcgraph.defaults, function (chart) {
            var view = chart({el: '#' + graphId, data: data}).update();
        });
        return this;
    };

    $.fn.bcgraph.defaults = {
        "width": 400,
        "height": 200,
        "padding": {"top": 10, "left": 30, "bottom": 30, "right": 10},
        "data": [{"name": "points"}],
        "scales": [
            {
                "name": "x",
                "nice": true,
                "range": "width",
                "domain": {"data": "points", "field": "data.x"}
            },
            {
                "name": "y",
                "nice": true,
                "range": "height",
                "domain": {"data": "points", "field": "data.y"}
            }
        ],
        "axes": [
            {
                "type": "x", "scale": "x"
            },
            {
                "type": "y", "scale": "y"
            }
        ],
        "marks": [
            {
                "type": "symbol",
                "from": {"data": "points"},
                "properties": {
                    "enter": {
                        "x": {"scale": "x", "field": "data.x"},
                        "y": {"scale": "y", "field": "data.y"},
                        "stroke": {"value": "steelblue"},
                        "fillOpacity": {"value": 0.5}
                    },
                    "update": {
                        "fill": {"value": "transparent"},
                        "size": {"value": 100}
                    },
                    "hover": {
                        "fill": {"value": "pink"},
                        "size": {"value": 300}
                    }
                }
            }
        ]
    };
}($));
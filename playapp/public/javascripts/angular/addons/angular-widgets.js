/**
 * The MIT License
 *
 * Copyright (c) 2011 Łukasz Twarogowski, Axiom Computing, axiomcomputing.pl
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

// ui:selectable directive
//    makes a set of elements selectable, based of jQuery UI selectable
angular.directive('ui:selectable', function(expr, el) {
    var propExpr = widgetUtils.parseExpr(expr);
    return function(el) {
        var currentScope = this;
        if (propExpr) {
            // init values on load (per each single element)
            /*
            var values = widgetUtils.getValue(currentScope, propExpr);
            if (angular.Array.indexOf(values, el.value) < 0) {
                angular.Array.add(values, el.value);
            }
            widgetUtils.setValue(currentScope, propExpr, values);
            */

            // binding called on select or unselect
            $(el).bind('_onSelectable', function(e, obj) {
                console.log("**** ui:selectable SET ::" + obj.value + " ->" + obj.operation);
                // Append to array instead of overwrite existing value
                var values = widgetUtils.getValue(currentScope, propExpr);
                if (typeof values == 'undefined') {
                    values = [];
                }
                if (obj.operation == 'selected') {
                    if (angular.Array.indexOf(values, obj.value) < 0) {
                        angular.Array.add(values, obj.value);
                    }
                } else {
                    // operation == 'unselected'
                    if (angular.Array.indexOf(values, obj.value) >= 0) {
                        angular.Array.remove(values, obj.value);
                    }
                }
                //console.log("            *** " + values);
                widgetUtils.setValue(currentScope, propExpr, values);
            });
            currentScope.$watch(propExpr.expression, function(value) {
                var d = widgetUtils.formatValue(value, propExpr, currentScope);
                var dataVal = $(el).data("value");
                // If clicked: value will be a single value
                if (dataVal == value) {
                    if (value) {
                        $(el).addClass('ui-selected');
                    } else {
                        $(el).removeClass('ui-selected');
                    }
                }
            }, null, true);
        }
    };
});


// ui:selectable-container
//    a directive for an element containing ui:selectable items
angular.directive('ui:selectable-container', function(expr, el) {
    var options = {
        filter: '*:first, *:first ~ *', //  dirty hack, to be optimized....
        selected: function(event, ui) {
            var value = $(ui.selected).data("value");
            console.log("**** ui:selectable-container SELECTED: " + value);
            $(ui.selected).trigger('_onSelectable', { "operation": "selected", "value": value});
        },
        unselected: function(event, ui) {
            var value = $(ui.unselected).data("value");
            console.log("**** ui:selectable-container UNSELECTED: " + value);
            $(ui.unselected).trigger('_onSelectable', { "operation": "unselected", "value": value});
        }
    };
    $(el).selectable(options);
    return function(el) {

    };
});



// ui:masked
//    a directive for a text element to obtain masked-field editing capabilities
angular.widget('ui:masked', function(el) {
    var compiler = this;
    var defaults = {allowInvalid: false};
    var valExpr = widgetUtils.parseAttrExpr(el, 'ui:value');
    var maskExpr = widgetUtils.parseAttrExpr(el, 'ui:mask');
    var isvalidExpr = widgetUtils.parseAttrExpr(el, 'ui:isvalid');
    var options = widgetUtils.getOptions(el, defaults);
    return function(el) {
        var currentScope = this;
        var d1 = $('<input type="text"/>');
        $(el).append(d1);
        $(d1).mask(maskExpr.expression);

        function handleEvent(){
            var um = d1.mask();
            var valid = d1.isMaskValid();
            if(!options.allowInvalid && !valid)
                um='';
            if(valid)
                d1.addClass('mask-valid').removeClass('mask-invalid');
            else
                d1.addClass('mask-invalid').removeClass('mask-valid');
            if(isvalidExpr)
                widgetUtils.setValue(currentScope, isvalidExpr, valid);
            widgetUtils.setValue(currentScope, valExpr, um);
        }

        d1.keypress(handleEvent).keydown(handleEvent);
        currentScope.$watch(valExpr.expression, function(val){
            var d = widgetUtils.formatValue(val, valExpr, currentScope);
            var old = d1.mask();
            if(old!=d)
                d1.val(d).trigger('blur.mask');
        }, null, true);
    };
});





// ui:button widget
// simple jQuery styled button
angular.widget('ui:button', function(el) {
    var compiler = this;
    var defaults = {};
    var clickExpr = widgetUtils.parseAttrExpr(el, 'ui:click');
    var iconsExpr = widgetUtils.parseAttrExpr(el, 'ui:icons');
    var disabledExpr = widgetUtils.parseAttrExpr(el, 'ui:disabled');
    var icons = null;
    var elTxt = $(el).text();
    var inHtml = $(el).html()
    var viewTxt = (inHtml && inHtml!='') ? angular.compile('<span>' + inHtml + '</span>') : null;
    if(iconsExpr){
        icons = {};
        var arr = iconsExpr.expression.split(',');
        if(arr.length > 0)
            icons.primary = arr[0];
        if(arr.length > 1)
            icons.secondary = arr[1];
    }
    var buttonOptions = {};
    if(icons)
        buttonOptions.icons = icons;
    if(!elTxt || elTxt == '')
        buttonOptions.text = false;
    return function(el) {
        var currentScope = this;
        var b = $('<button/>');
        if(viewTxt)
            g = viewTxt(currentScope);
        if(elTxt && elTxt != '')
            b.text(elTxt);
        else
            b.text('_');
        el.text('').append(b);
        $(b).button(buttonOptions);
        if(clickExpr)
            $(b).click(function(){ currentScope.$tryEval(clickExpr.expression, el); });
        if(disabledExpr)
            currentScope.$watch(disabledExpr.expression, function(val){
                var d = widgetUtils.formatValue(val, disabledExpr, currentScope);
                $(b).button('option','disabled', d);
            }, null, true);
    };
});



// ui:toggle widget
// simple jQuery styled 2-state button
angular.widget('ui:toggle', function(el) {
    var compiler = this;
    var defaults = {};
    var iconsExpr = widgetUtils.parseAttrExpr(el, 'ui:icons');
    var disabledExpr = widgetUtils.parseAttrExpr(el, 'ui:disabled');
    var valExpr = widgetUtils.parseAttrExpr(el, 'ui:value');
    var icons = null;
    var elTxt = $(el).text();
    var inHtml = $(el).html()
    var viewTxt = (inHtml && inHtml!='') ? angular.compile('<span>' + inHtml + '</span>') : null;
    if(iconsExpr){
        icons = {};
        var arr = iconsExpr.expression.split(',');
        if(arr.length > 0)
            icons.primary = arr[0];
        if(arr.length > 1)
            icons.secondary = arr[1];
    }
    var buttonOptions = {};
    if(icons)
        buttonOptions.icons = icons;
    if(!elTxt || elTxt == '')
        buttonOptions.text = false;
    return function(el) {
        var currentScope = this;
        var rnd = 'tog_'+(((1+Math.random())*0x10000000)|0).toString(16).substring(1)
        var b = $('<input type="checkbox" id="' + rnd + '"/>');
        var l = $('<label for="' + rnd + '"></label>');
        if(viewTxt)
            g = viewTxt(currentScope);
        if(elTxt && elTxt != '')
            l.text(elTxt);
        else
            l.text('_');
        el.text('').append(b).append(l);
        $(b).button(buttonOptions);
        if(valExpr){
            $(b).click(function(){
                var checked = $(b).attr('checked') == 'checked';
                widgetUtils.setValue(currentScope, valExpr, checked);
            });
            currentScope.$watch(valExpr.expression, function(val){
                var ch = widgetUtils.formatValue(val, valExpr, currentScope);
                $(b).attr('checked', ch).button('refresh');
            }, null, true);
        }

        if(disabledExpr)
            currentScope.$watch(disabledExpr.expression, function(val){
                var d = widgetUtils.formatValue(val, disabledExpr, currentScope);
                $(b).button('option','disabled', d);
            }, null, true);
    };
});



// ui:progess widget
// progress bar
angular.widget('ui:progress', function(el) {
    var compiler = this;
    var defaults = {minValue: 0, maxValue: 100, showText: true, minColor: '#cccccc', maxColor: '#1aad0c'};
    var options = widgetUtils.getOptions(el, defaults);
    var valueExpr = widgetUtils.parseAttrExpr(el, 'ui:value');
    return function(el) {
        var currentScope = this;
        var d1 = $('<div class="progress-body"/>');
        $(el).append(d1);
        var d2 = $('<div class="progress-bar"/>');
        $(d1).append(d2);
        if(options.showText){
            var d3 = $('<div class="progress-text"/>');
            $(d1).append(d3);
        }
        currentScope.$watch(valueExpr.expression, function(val) {
            var v = parseFloat(widgetUtils.formatValue(val, valueExpr, currentScope)) || 0, r0 = parseFloat(options.minValue), r1=parseFloat(options.maxValue);
            var perc = Math.max(Math.min(Math.round(Math.min((v - r0) / (r1 - r0), r1) * 100), 100), 0);
            $(d3).html(perc + '%');
            $(d2).css('width', perc+'%');
            if($.xcolor){
                var c = $.xcolor.gradientlevel(options.minColor, options.maxColor, perc, 100);
                (d2).css('background-color', c.getCSS());
            }
        }, null, true);
    };
});



// ui:emblem widget
// clickable emblem widget, sort of 'multiple state checkbox'
angular.widget('ui:emblem', function(el) {
    var compiler = this;
    var defaults = {emblems: ['', 'star', 'excl']};
    var options = widgetUtils.getOptions(el, defaults);
    var symbolExpr = widgetUtils.parseAttrExpr(el, 'ui:symbol');
    return function(el) {
        var currentScope = this;
        var d1 = $('<div class="emblem"/>').click(function(){
            var s0 = $(el).data('symbol');
            var i = 0;
            if(s0)
                i = _(options.emblems).indexOf(s0);
            i++;
            if(i>options.emblems.length-1)
                i=0;
            var emblem = options.emblems[i];
            $(d1).removeClass('emblem-' + s0).addClass('emblem-' + emblem);
            $(el).data('symbol', emblem);
            widgetUtils.setValue(currentScope, symbolExpr, emblem);
        });
        $(el).append(d1);
        currentScope.$watch(symbolExpr.expression, function(val) {
            var v = widgetUtils.formatValue(val, symbolExpr, currentScope);
            var s0 = $(el).data('symbol');
            $(el).data('symbol', v);
            $(d1).removeClass('emblem-' + s0).addClass('emblem-' + v);
        }, null, true);
    };
});



// ui:autocomplete widget
// jQuery UI autocomplete
angular.widget('@ui:autocomplete', function(expr, el, val) {

    var compiler = this;
    var defaults = {
        renderName: function(item){ return item.firstName + ' ' + item.lastName;},
        renderItem: function(term, item){
            var hl = this.highlight ? (this.highlightFunction || widgetUtils.highlight) : widgetUtils.noHighlight;
            return $('<a></a>').append(hl(term, options.renderName(item)));
        },
        clearOnSelect: false,
        delay: 50,
        highlight: true
    };
    var opt = widgetUtils.getOptions(el, {});
    var options = {};
    var presetName = $(el).attr('ui:preset');
    var itemExpr = widgetUtils.parseAttrExpr(el, 'ui:item');
    var linkFn = function($xhr, $log, presets, el) {
        var currentScope = this;
        var preset = null;
        if(presets && presetName)
            preset = presets.get(presetName) || {};
        var ac;

        $.extend(options, defaults, preset, opt);
        var events = {
            source: function(req, res){
                $xhr('GET', options.urls.list + req.term, function(code, response){
                    res(response);
                });
            },
            select: function(event, ui){
                var txt = '';
                if(!options.clearOnSelect)
                    txt = (options.renderText || options.renderName)(ui.item);
                $(el).val(txt).blur();
                if(options.onSelect)
                    options.onSelect(ui.item);
                if(itemExpr)
                    widgetUtils.setValue(currentScope, itemExpr, ui.item);
                return options.clearOnSelect;
            },
            focus: function(event, ui){
                var txt = (options.renderText || options.renderName)(ui.item);
                $(el).val(txt);
                return false;
            }
        };

        var renderFn = {
            _renderItem: function(ul, item){
                $('<li></li>').data('item.autocomplete', item).append(options.renderItem(this.term, item)).appendTo(ul);
            }
        };

        $.extend(options, events);
        ac = $(el).autocomplete(options).data('autocomplete');
        $.extend(ac, renderFn);

        if(itemExpr && itemExpr.expression)
            currentScope.$watch(itemExpr.expression, function(val){
                var txt;
                if(val)
                    txt = (options.renderText || options.renderName)(val);
                $(el).val(txt).blur();
            }, null, true);

    };
    linkFn.$inject = ['$xhr', '$log', 'autocompletePresets'];
    return linkFn;
});



// ui:datepicker widget
// jQuery UI datepicker
angular.widget('@ui:datepicker', function(expr, el, val) {
    if (!$.datepicker)
        return;
    var compiler = this;
    var defaults = {dateFormat: 'dd-mm-yy'};
    var options = widgetUtils.getOptions(el, defaults);
    var events = {};
    var dateExpr = widgetUtils.parseAttrExpr(el, 'ui:date');
    return function(el) {
        var currentScope = this;
        var tagName = $(el)[0].tagName.toLowerCase();
        if (tagName == 'input' || tagName == 'textarea')
            events.onClose = function(date, ui){
                var dt = $(el).datepicker('getDate'); // returns date object
                var dtStr = $.datepicker.formatDate(options.dateFormat, dt);
                widgetUtils.setValue(currentScope, dateExpr, dtStr);
            };
        else
            events.onSelect = function(date, ui){
                var dt = $(el).datepicker('getDate');
                var dtStr = $.datepicker.formatDate(options.dateFormat, dt);
                widgetUtils.setValue(currentScope, dateExpr, dtStr);
            };
        $.extend(options, events);
        $(el).datepicker(options);
        currentScope.$watch(dateExpr.expression, function(val){
            if (val && val instanceof Date) {
                // format Date to string
                $(el).datepicker('setDate', widgetUtils.formatValue(val, dateExpr, currentScope));
            } else {
                // assume string is given in proper representation
                $(el).datepicker('setDate', val);
            }
        }, null, true);
    };
});



// ui:map widget
// Google Maps API v. 3.5
angular.widget('ui:map', function(el) {
    if(!google || !google.maps)
        return;
    var compiler = this;
    var elem = el;
    var pinExpr = widgetUtils.parseAttrExpr(el, 'ui:pin');
    var viewExpr = widgetUtils.parseAttrExpr(el, 'ui:view');
    var defaults = {bindZoom : false, bindMapType: false, center: {lat:0, lng:0}, pinDraggable: true, map: {zoom: 4, mapTypeId: google.maps.MapTypeId.ROADMAP}};
    var options = widgetUtils.getOptions(el, defaults);
    defaults.map.center = new google.maps.LatLng(defaults.center.lat, defaults.center.lng);
    return function(el) {
        var currentScope = this;
        $(elem).append('<div/>')
        var div = ('div', elem).get(0);
        var map = new google.maps.Map(div,options.map);
        var marker = new google.maps.Marker({ position: map.center, map: map});
        marker.setDraggable(options.pinDraggable);

        google.maps.event.addListener(map, 'click', function(e) {
            marker.setPosition(e.latLng);
            marker.setVisible(true);
            var o = widgetUtils.getValue(currentScope, pinExpr) || {};
            $.extend(o, {lat:e.latLng.lat(), lng:e.latLng.lng()});
            widgetUtils.setValue(currentScope, pinExpr, o);
        });

        google.maps.event.addListener(marker, 'dragend', function(e) {
            var o = widgetUtils.getValue(currentScope, pinExpr) || {};
            $.extend(o, {lat: e.latLng.lat(), lng: e.latLng.lng()});
            widgetUtils.setValue(currentScope, pinExpr, o);
        });

        google.maps.event.addListener(map, 'dragend', function() {
            var c = map.getCenter();
            var o = widgetUtils.getValue(currentScope, viewExpr) || {};
            $.extend(o, {lat: c.lat(), lng: c.lng()});
            widgetUtils.setValue(currentScope, viewExpr, o);
        });

        if(defaults.bindZoom)
            google.maps.event.addListener(map, 'zoom_changed', function() {
                var c = map.getCenter();
                var z = map.getZoom();
                var o = widgetUtils.getValue(currentScope, viewExpr) || {};
                $.extend(o, {lat: c.lat(), lng: c.lng(), zoom: z});
                widgetUtils.setValue(currentScope, viewExpr, o);
            });

        if(defaults.bindMapType)
            google.maps.event.addListener(map, 'maptypeid_changed', function() {
                var t = map.getMapTypeId();
                var o = widgetUtils.getValue(currentScope, viewExpr) || {};
                $.extend(o, {mapType: t});
                widgetUtils.setValue(currentScope, viewExpr, o);
            });

        $(elem).data('map', map);
        $(elem).data('marker', marker);

        currentScope.$watch(pinExpr.expression + '.lat', function() {
            var map = $(elem).data('map');
            var marker = $(elem).data('marker');
            var newPos = widgetUtils.getValue(currentScope, pinExpr);
            if(!newPos || !newPos.lat || !newPos.lng){
                marker.setVisible(false);
                return;
            }
            marker.setPosition(new google.maps.LatLng(newPos.lat, newPos.lng));
            marker.setVisible(true);
        }, null, true);

        currentScope.$watch(pinExpr.expression + '.lng', function() {
            var map = $(elem).data('map');
            var marker = $(elem).data('marker');
            var newPos = widgetUtils.getValue(currentScope, pinExpr);
            if(!newPos || !newPos.lat || !newPos.lng){
                marker.setVisible(false);
                return;
            }
            marker.setPosition(new google.maps.LatLng(newPos.lat, newPos.lng));
            marker.setVisible(true);
        }, null, true);

        currentScope.$watch(viewExpr.expression + '.lng', function() {
            var map = $(elem).data('map');
            var newPos = widgetUtils.getValue(currentScope, viewExpr);
            if(newPos)
                map.setCenter(new google.maps.LatLng(newPos.lat, newPos.lng));
        }, null, true);

        currentScope.$watch(viewExpr.expression + '.lat', function() {
            var map = $(elem).data('map');
            var newPos = widgetUtils.getValue(currentScope, viewExpr);
            if(newPos)
                map.setCenter(new google.maps.LatLng(newPos.lat, newPos.lng));
        }, null, true);

        if(defaults.bindMapType)
            currentScope.$watch(viewExpr.expression + '.mapType', function(val) {
                var map = $(elem).data('map');
                if(val)
                    map.setMapTypeId(val);
            }, null, true);

        if(defaults.bindZoom)
            currentScope.$watch(viewExpr.expression + '.zoom', function(val) {
                var map = $(elem).data('map');
                if(val)
                    map.setZoom(val);
            }, null, true);

    };
});



// ui:enter directive
// calls a function when ENTER is pressed
angular.directive('ui:enter', function(expr, el) {
    return function(el) {
        var compiler = this;
        $(el).keyup(function(event){
            if(event.keyCode == 13){
                compiler.$tryEval(expr, el);
                $(el).val('');
                compiler.$parent.$eval();
                event.stopPropagation();
            }
        });
    };
});



// handy widgets functions
var widgetUtils = {
    highlight: function(term, text){
        if(!text)
            return null;
        var rx = new RegExp("("+$.ui.autocomplete.escapeRegex(term)+")", "ig" );
        return text.replace(rx, "<strong>$1</strong>");
    },
    noHighlight: function(term, text){
        return text;
    },
    getOptions : function (el, defaults, attrName){
        attrName = attrName || 'ui:options';
        var opts = $(el).attr(attrName);
        defaults = defaults || {};
        if(!opts)
            return defaults;
        var options = angular.fromJson('['+opts+']')[0];
        return $.extend(defaults, options);
    },
    parseExpr: function(val){
        if(!val || val=='')
            return null;
        var expr = {formatters:[]};
        var pts = val.split('|');
        expr.expression = pts[0];
        if(pts.length==1)
            return expr;
        for (var i = 0; i < pts.length; i++){
            var args = pts[i].split(':');
            var name = args.shift();
            var frmt = angular.formatter[name];
            if(frmt)
                expr.formatters.push({name: name, parse: frmt.parse, format: frmt.format, arguments: args});
        }
        return expr;
    },
    parseAttrExpr: function (el, attrName){
        if(!attrName)
            return null;
        var attr = $(el).attr(attrName);
        return this.parseExpr(attr);
    },
    setValue: function (scope, attrExpr, value){
        if(!attrExpr || !attrExpr.expression)
            return;
        var v = value;
        v = this.parseValue(v, attrExpr, scope);
        scope.$set(attrExpr.expression, v);
        scope.$parent.$eval();
    },
    getValue: function (scope, attrExpr){
        if(!attrExpr || !attrExpr.expression)
            return null;
        var val = scope.$get(attrExpr.expression);
        val = this.formatValue(val, attrExpr, scope);
        return val;
    },
    parseValue: function (value, attrExpr, scope){
        if(!attrExpr || !attrExpr.formatters || attrExpr.formatters.length==0)
            return value;
        var v = value;
        for (var i = 0; i < attrExpr.formatters.length; i++) {
            var fm = attrExpr.formatters[i];
            if(fm && fm.parse)
                v = fm.parse.apply(scope, [v].concat(fm.arguments));
        };
        return v;
    },
    formatValue: function (value, attrExpr, scope){
        if(!attrExpr || !attrExpr.formatters || attrExpr.formatters.length==0)
            return value;
        var v = value;
        for (var i = 0; i < attrExpr.formatters.length; i++) {
            var fm = attrExpr.formatters[i];
            if(fm && fm.format)
                v = fm.format.apply(scope, [v].concat(fm.arguments));
        };
        return v;
    }
};

// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
// Added on top of Łukasz Twarogowski stuff
// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

// --- Custom TinyMCE Service (works only with 10.5)
// TinyMCE angular integration by Dean Sofer: http://deansofer.com/posts/view/14/AngularJs-Tips-and-Tricks
angular.directive('ui:tinymceBETA', function(expression, config) {
    return function(element) {
        if (expression === 'fast') {
            element.tinymce({
                // Location of TinyMCE script
                script_url: 'http://resources.holycrap.ws/jscripts/tiny_mce/tiny_mce.js',

                // General options
                theme: "simple",

                // Update Textarea and Trigger change event
                handle_event_callback: function(e) {
                    if (this.isDirty()) {
                        this.save();
                        element.trigger('change');
                    }
                    return true;
                }
            });
        } else {
            $(element).tinymce({
                // Location of TinyMCE script
                script_url: 'http://resources.holycrap.ws/jscripts/tiny_mce/tiny_mce.js',

                // General options
                theme: "simple",

                // Update Textarea and Trigger change event
                onchange_callback: function(inst) {

                    if (inst.isDirty()) {
                        inst.save();
                        element.trigger('change');
                    }
                    return true; // Continue handling
                }
            });
        }
    };
});

angular.widget('@ui:tinymce', function(expr, el, val) {
    /* if (!$.tinymce) {
        console.log("tinymce plugin not available");
        return;
    }
    */
    // widget is initialized only once per element (eg. in array only once)
    console.log("START tinymce for element type: " + el[0].name);

    var compiler = this;
    var defaults = {};
    var options = widgetUtils.getOptions(el, defaults);
    var events = {};
    var contentExpr = widgetUtils.parseAttrExpr(el, 'name');

    // callback per each element instance available in the content node
    return function (el) {
        var currentScope = this;

        console.log("---> el : " + el[0].name);
        // get existing value, otherwise initialize
        var o = widgetUtils.getValue(currentScope, contentExpr) || '';
        console.log("---> o : " + o);

        /*
        events.onChange = function (theElem, ui) {
            console.log("ON CHANGE");
            var content = $(el).tinymce('getContent');
            widgetUtils.setValue(currentScope, contentExpr, content);
        };
        $.extend(options, events);
        */

        $(el).tinymce({
            mode : "specific_textareas",
            editor_selector : "mceRichText",
            elements : "ajaxfilemanager",
            theme : "advanced",
            plugins : "advimage,advlink,contextmenu,autosave,imggallery",
            theme_advanced_buttons1 : "bold,italic,underline,separator,"+
                                      "undo,redo,separator,"+
                                      "justifyleft,justifycenter,justifyright,justifyfull,bullist,numlist,separator,"+
                                      "link,unlink,image,imggallery,separator,"+
                                      "code,cleanup",
            theme_advanced_buttons2 : "",
            theme_advanced_buttons3 : "",
            theme_advanced_toolbar_location : "top",
            theme_advanced_toolbar_align : "left",
            extended_valid_elements : "hr[class|width|size|noshade]",
            file_browser_callback : "ajaxfilemanager",
            paste_use_dialog : false,
            theme_advanced_statusbar_location : 'bottom',
            theme_advanced_path : false,
            theme_advanced_resizing : true,
            theme_advanced_resize_horizontal : true,
            apply_source_formatting : true,
            force_br_newlines : true,
            force_p_newlines : false,
            relative_urls : false,

            oninit: function(inst) {
                var inHTML = o;
                console.log("ONINIT --> " + inHTML);
                inst.setContent(inHTML);
            },

            // Update Textarea and Trigger change event
            onchange_callback: function(inst) {
                console.log("CALLBACK --> " + inst.getContent());

                if (inst.isDirty()) {
                    console.log("CALLBACK DIRTY");
                    inst.save();
                    widgetUtils.setValue(currentScope, contentExpr, inst.getContent());
                    el.trigger('change');
                }
                return true; // Continue handling
            }
        });
    };
});

function ajaxfilemanager(field_name, url, type, win) {
    var view = 'detail';
    switch (type) {
        case "image":
            view = 'thumbnail';
            break;
        case "media":
            break;
        case "flash":
            break;
        case "file":
            break;
        default:
            return false;
    }
    tinyMCE.activeEditor.windowManager.open({
        url: "/blobs/forTiny.html?refer=ajaxfilemanager&view=" + view,
        width: 782,
        height: 440,
        inline : "yes",
        close_previous : "no"
    },{
        window : win,
        input : field_name
    });
}



// TODO: ~~~~~~~~~ NOT IN USE: currently
// ~~ jQuery WYSIWYG Plug-In Integration
angular.widget('@ui:wysiwyg', function(expr, el, val) {
    if (!$.wysiwyg) {
        console.log("wysiwyg plugin not available");
        return;
    }
    console.log("START wysiwyg");

    var compiler = this;
    var defaults = {};
    var options = widgetUtils.getOptions(el, defaults);
    var events = {};
    var contentExpr = widgetUtils.parseAttrExpr(el, 'ui:content');
    return function(el) {
        var currentScope = this;
        var tagName = $(el)[0].tagName.toLowerCase();
        /*
         if (tagName == 'textarea')
         events.onClose = function(theElem, ui) {
         console.log("ON CLOSE");
         var content = $(el).wysiwyg('getContent');
         widgetUtils.setValue(currentScope, contentExpr, content);
         };
         else
         events.onBlur = function(theElem, ui) {
         console.log("ON BLUR");
         var content = $(el).wysiwyg('getContent');
         widgetUtils.setValue(currentScope, contentExpr, content);
         };
         $.extend(options, events);
         */
        $(el).wysiwyg(options);
        /*
         currentScope.$watch(contentExpr.expression, function(val) {
         // assume string is given in proper representation
         console.log("WATCH SET CONTENT: " + val);
         $(el).wysiwyg('setContent', val);
         }, null, true);
         */
    };
});




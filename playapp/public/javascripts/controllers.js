/* App Controllers */

// ~~~~~~

function EditContentNodeCtrl($xhr) {
    var scope;
    scope = this;

    // define data we are working with
    this.contentType   = globalContentType;
    this.contentSchema = globalContentSchema;
    this.schemaRules   = globalSchemaRules;
    this.contentNode   = globalContentNode;      // will probably be empty
    this.contentNodeId = globalContentNodeId;    // -1 if not yet saved

    this.elementGroupsToRemove = [];


    this.submit = function() {
        // check whether content already exists or not
        if (this.contentNodeId < 0) {
            console.log("Going to create content ...");
            $xhr('POST', "/" + this.contentType, this.contentNode, function(code, response) {
                window.location.href =  "/?highlightId=" + response.id;
            }, function(code, response) {
                alert("PROBLEM: " + response);
            });
            return false;
        } else {
            // content already exists
            console.log("Updating content node " + this.contentNodeId + " ...");
            $xhr('PUT', "/" + this.contentType + "/" + this.contentNodeId, this.contentNode, function(code, response) {
                window.location.href =  "/?highlightId=" + response.id;
            }, function(code, response) {
                alert("PROBLEM: " + response);
            });
            return false;
        }
    };

    this.cancel = function() {
        console.log("Cancel form ...");
        window.history.back();
    };

    /**
     * Triggered when user presses the "Add" Button (sub elements in form).
     */
    scope.addChild = function(ctx) {
        if (ctx.child) {
            console.log("Add child (type: " + ctx.childtype + "): " + ctx.child);
            ctx.child.push({ _type: ctx.childtype });
        } else {
            // TODO: never getting here
            console.log("Init child (type: " + ctx.childtype + "): " + ctx.childname);
            ctx.parent[ctx.childname] = [{ _type: ctx.childtype }];
        }
        scope.elementGroupsToRemove = ctx.allChildtypes.split(',');
        var idx = scope.elementGroupsToRemove.indexOf(ctx.childtype);
        if (idx != -1) {
            scope.elementGroupsToRemove.splice(idx, 1);
        } else {
            // by default remove first element
            scope.elementGroupsToRemove.splice(0, 1);
        }
        // item hiding via directive 'autoremove' (defined in angular-widget.js)
    };

    scope.moveDown = function(arr) {
        var curPos = this.$index;
        var tmp = arr[curPos+1];
        arr[curPos+1] = arr[curPos];
        arr[curPos] = tmp;
    };

    scope.moveUp = function(arr) {
        var curPos = this.$index;
        var tmp = arr[curPos-1];
        arr[curPos-1] = arr[curPos];
        arr[curPos] = tmp;
    };

    scope.helper = new CalloutDialogHelper();

    // Called by referencing input element (see widget.js)
    scope.simple_select_value = function (callout_url, target_prop_names, src_prop_names) {
        var target_names = target_prop_names.split('#');
        var src_names = src_prop_names ? src_prop_names.split('#') : [];
        var fields = {};
        fields['update_fields']  = new Array();
        fields['values']         = new Array();
        fields['src_types']      = new Array();
        fields['src_properties'] = new Array();

        for (i in target_names) {
            var fq_target_name = target_names[i];
            if (fq_target_name) {
                // Special handling for array elements: insert position number
                var cur_pos = this.$index;
                if (typeof cur_pos != 'undefined') {
                    var dot_pos = fq_target_name.lastIndexOf('.');
                    fq_target_name = fq_target_name.substring(0, dot_pos) + '.' + cur_pos + fq_target_name.substring(dot_pos);
                }

                fields['update_fields'].push(fq_target_name);
                var cur_value = scope.$get(fq_target_name);
                fields['values'].push(cur_value);
                fields['src_types'].push('string');
                fields['src_properties'].push(src_names[i]);
            }
        }

        // Create Bootbox Modal with external selection form loaded as specified by callout URL
        return bootbox.dialog(scope.helper.selection_form(callout_url, fields), [
            {
                'label':'Cancel'
            },
            {
                'label':'Save',
                'class':'btn-primary success',
                'callback':function () {
                    return scope.save_values(calloutGetSelectedValues());
                }
            }
        ], {
            "animate":false
        });
    };


    // Called after "Save" Button in Callout-Dialog is pressed
    scope.save_values = function (doc_data) {
        console.log("doc_data: " + dump(doc_data));
        jQuery.each(doc_data, function (fieldname, val) {
            if (fieldname && fieldname != "null") {
                if (endsWith(fieldname, "_idref")) {
                    console.log("convert " + val + " to int");
                    if (val && val.indexOf(",") != -1) {
                        // dann ist das ein Array von IDs
                        var retval = Array();
                        values = val.split(",");
                        for (v in values) {
                            retval.push(parseInt(values[v]));
                        }
                        scope.$set(fieldname, retval);
                    } else {
                        scope.$set(fieldname, parseInt(val));
                    }

                } else {
                    scope.$set(fieldname, val);
                }

                console.log("Updated " + fieldname + " to: " + val);
            }

        });
        //scope.$set(fieldname, doc_data.value);
        //scope.$set('contentNode.title', doc_data.title);
        scope.$eval(); // force model update
    };

    function endsWith(str, suffix) {
        return str.indexOf(suffix, str.length - suffix.length) !== -1;
    }
}
EditContentNodeCtrl.$inject = ['$xhr'];


// ~~~ ~~~ ~~~ Utilities ~~~ ~~~ ~~~

CalloutDialogHelper = (function() {

    function CalloutDialogHelper() {}

    CalloutDialogHelper.prototype.selection_form = function(callout_url, fields) {
        // Make AJAX GET to callout_url and put inside HTML form
        setTimeout(function() {
            $.get(callout_url, fields,
                function(responseForm) {
                    $("#selection_form").html(responseForm);
                });
        }, 1); // small delay to ensure form ID is available
        return '<form id="selection_form" class="form-horizontal"></form>';
    };

    return CalloutDialogHelper;

})();


// TODO: move to a helper lib
function dump(arr, level) {
    var dumped_text = "";
    if (!level) level = 0;

    //The padding given at the beginning of the line.
    var level_padding = "";
    for (var j=0; j<level+1; j++) level_padding += "    ";

    if (typeof(arr) == 'object') { //Array/Hashes/Objects
        for (var item in arr) {
            var value = arr[item];

            if (typeof(value) == 'object') { //If it is an array,
                dumped_text += level_padding + "'" + item + "' ...\n";
                dumped_text += dump(value, level+1);
            } else {
                dumped_text += level_padding + "'" + item + "' => \"" + value + "\"\n";
            }
        }
    } else { //Stings/Chars/Numbers etc.
        dumped_text = "===>"+arr+"<===("+typeof(arr)+")";
    }
    return dumped_text;
}


/* App Controllers */

function EditContentNodeCtrl($xhr) {
    var scope;
    scope = this;

    // define data we are working with
    this.contentType   = globalContentType;
    this.contentSchema = globalContentSchema;
    this.schemaRules   = globalSchemaRules;
    this.contentNode   = globalContentNode;      // will probably be empty
    this.contentNodeId = globalContentNodeId;    // -1 if not yet saved


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
            console.log("Start saving ...");
            $xhr('PUT', "/" + this.contentType + "/" + this.contentNodeId, this.contentNode, function(code, response) {
                window.location.href =  "/?highlightId=" + response.id;
            }, function(code, response) {
                alert("PROBLEM: " + response);
            });
            return false;
        }
    };

    this.reset = function() {
        console.log("Reset form ...");
        this.form = angular.copy(this.contentNode);
    };

    scope.addChild = function(ctx) {
        if (ctx.child) {
            ctx.child.push({});
        } else {
            ctx.parent[ctx.childname] = [{}];
        }
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

    scope.select_value = function(callout_url, field_name) {
        var fq_name = field_name;
        var cur_pos = this.$index;
        if (typeof cur_pos != 'undefined') {
            var dotPos = fq_name.lastIndexOf('.');
            fq_name = fq_name.substring(0, dotPos) + '.' + cur_pos + fq_name.substring(dotPos);
        }
        var field_value = scope.$get(fq_name);
        // Create Bootbox Modal with external selection form loaded as specified by callout URL
        return bootbox.dialog(scope.helper.selection_form(callout_url, fq_name, field_value), [
            {
                'label': 'Cancel'
            },
            {
                'label': 'Save',
                'class': 'btn-primary success',
                'callback': function() {
                    return scope.save_value(fq_name, calloutGetSelectedValue());
                }
            }
        ], {
            "animate": false
        });
    };

    // Called after Save Button of Callout-Dialog is pressed
    scope.save_value = function(fieldname, doc_data) {
        scope.$set(fieldname, doc_data.value);
        scope.$eval(); // force model update
        console.log("Updated " + fieldname + " = " + doc_data.value);
    };

}
EditContentNodeCtrl.$inject = ['$xhr'];


// ~~~ ~~~ ~~~ Utilities ~~~ ~~~ ~~~

CalloutDialogHelper = (function() {

    function CalloutDialogHelper() {}

    CalloutDialogHelper.prototype.selection_form = function(callout_url, field_name, field_value) {
        // callout_url is specified
        setTimeout(function() {
            $.get(callout_url, {value: field_value},  // TODO: if value already exists, append field_value
                function(data) {
                    $("#selection_form").html(data);
                });
        }, 1); // small delay to ensure form ID is available
        return '<form id="selection_form" class="form-horizontal"></form>';
    };

    return CalloutDialogHelper;

})();
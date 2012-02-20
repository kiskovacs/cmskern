/* App Controllers */

function EditContentNodeCtrl($xhr) {
    var scope;
    scope = this;

    // define data we are working with
    this.contentType   = globalContentType;
    this.contentSchema = globalContentSchema;
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

    scope.select_value = function(callout_url, fieldname) {
        return bootbox.dialog(scope.helper.selection_form(callout_url, fieldname), [
            {
                'label': 'Cancel'
            },
            {
                'label': 'Save',
                'class': 'success',
                'callback': function() {
                    return scope.save_value(fieldname, scope.helper.form_data_to_object());
                }
            }
        ], {
            "animate": false
        });
    };

    scope.save_value = function(fieldname, doc_data) {
        scope.$set(fieldname, doc_data.value);
        scope.$eval(); // force model update
    };

}
EditContentNodeCtrl.$inject = ['$xhr'];


// ~~~ ~~~ ~~~ Utilities ~~~ ~~~ ~~~

CalloutDialogHelper = (function() {

    function CalloutDialogHelper() {}

    CalloutDialogHelper.prototype.selection_form = function(callout_url, field_name, selected_data) {
        var html;
        // TODO: if value already exists display it (selected_data)
        // TODO: allow flexible callout references
        setTimeout(function() {
            $.get(callout_url, function(data) {
                $('#selection_form').html(data);
            });
        }, 1);
        return '<div id="selection_form"></div>';
    };

    CalloutDialogHelper.prototype.form_data_to_object = function() {
        var selectedValue = $('#hugo5').val();
        alert("Selected: " + selectedValue);
        return {
            value: selectedValue
        };
    };

    return CalloutDialogHelper;

})();
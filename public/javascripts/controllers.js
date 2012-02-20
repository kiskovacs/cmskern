/* App Controllers */

function EditContentNodeCtrl($xhr) {
    var scope;
    scope = this;

    // define data we are working with
    this.contentType   = globalContentType;
    this.contentSchema = globalContentSchema;
    this.contentNode   = globalContentNode;      // will probably be empty
    this.contentNodeId = globalContentNodeId;  // -1 if not yet saved


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

    scope.helper = new CalloutDialogHelper();

    scope.select_value = function(fieldname) {
        return bootbox.dialog(scope.helper.selection_form(fieldname), [
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
        ]);
    };

    scope.save_value = function(fieldname, doc_data) {
        scope.$set(fieldname, doc_data.value);
        scope.$eval(); // force model update
        //scope.$eval(function(scope) { scope.contentNode = doc_data; });
        //  scope.$apply();
    };

    scope.addChild = function(ctx) {
        if (ctx.child) {
            ctx.child.push({});
        } else {
            ctx.parent[ctx.childname] = [{}];
        }
    };

    //scope.removeChild = function(childToRemove) {
    //    angular.Array.remove(this.contentNode.teasers, childToRemove);
    //};

    scope.moveDown = function(arr) {
        var curPos = this.$index;
        var tmp = arr[curPos+1];
        arr[curPos+1] = arr[curPos];
        arr[curPos] = tmp;
    }

    scope.moveUp = function(arr) {
        var curPos = this.$index;
        var tmp = arr[curPos-1];
        arr[curPos-1] = arr[curPos];
        arr[curPos] = tmp;
    }


}

EditContentNodeCtrl.$inject = ['$xhr'];


// ~~~

CalloutDialogHelper = (function() {

    function CalloutDialogHelper() {}

    CalloutDialogHelper.prototype.selection_form = function(selected_data) {
        var html;
        // TODO: if value already exists display it
        // TODO: allow flexible callout references
        html  = '<div id="selection_form">';
        html += "<h5>Select ...</h5>";
        html += '<form class="form-stacked">';
        html += '<label for="select-value">Value</label>';
        html += '<input type="text" id="select-value" name="value" value="" />';
        html += '</form>';
        return html += '</div>';
    };

    CalloutDialogHelper.prototype.form_data_to_object = function() {
        return {
            value: $('#selection_form #select-value').val()
        };
    };

    return CalloutDialogHelper;

})();
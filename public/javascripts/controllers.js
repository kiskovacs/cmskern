/* App Controllers */

function CreateContentNodeCtrl($xhr) {
    var scope;
    scope = this;

    // define data we are working with
    this.contentType = globalContentType;
    this.contentSchema = globalContentSchema;
    this.contentNode = globalContentNode;      // will probably be empty

    this.submit = function() {
        console.log("Start creating ...");
        $xhr('POST', "/" + this.contentType, this.contentNode, function(code, response) {
            window.location.href =  "/?highlightId=" + response.id;
        }, function(code, response) {
            alert("PROBLEM: " + response);
        });
        return false;
    };

    this.reset = function() {
        console.log("Reset form ...");
        this.form = angular.copy(this.contentNode);
    };

    scope.helper = new CalloutDialogHelper();
    scope.select_value = function(data) {
        return bootbox.dialog(scope.helper.selection_form(data), [
            {
                'label': 'Cancel'
            },
            {
                'label': 'Save',
                'class': 'success',
                'callback': function() {
                    return scope.save_value(scope.helper.form_data_to_object());
                }
            }
        ]);
    };
    scope.save_value = function(doc_data) {
        scope.doc = doc_data;
        // scope.$eval(function(scope) { scope.doc = doc_data; });
        scope.$apply();
    };

}

CreateContentNodeCtrl.$inject = ['$xhr'];


// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

function EditContentNodeCtrl($xhr) {

    // define data we are working with
    this.contentType = globalContentType;
    this.contentSchema = globalContentSchema;
    this.contentNodeId = globalContentNodeId;
    this.contentNode = globalContentNode;

    this.submit = function() {
        console.log("Start saving ...");
        $xhr('PUT', "/" + this.contentType + "/" + this.contentNodeId, this.contentNode, function(code, response) {
            window.location.href =  "/?highlightId=" + response.id;
        }, function(code, response) {
            alert("PROBLEM: " + response);
        });
        return false;
    };

    this.reset = function() {
        console.log("Reset form ...");
        this.form = angular.copy(this.contentNode);
    };
}

EditContentNodeCtrl.$inject = ['$xhr'];


// ~~~

CalloutDialogHelper = (function() {

    CalloutDialogHelper.url = null;

    function CalloutDialogHelper() {}

    CalloutDialogHelper.prototype.selection_form = function(selected_data) {
        var html;
        this.url = selected_data.url;
        html  = '<div id="selection_form">';
        html += "<img src=\"" + this.url + "\" alt=\"preview\" />";
        html += '<form class="form-stacked">';
        html += '<label for="asset-title">Title</label>';
        html += '<input type="text" id="asset-title" name="title" value="" />';
        html += '<label for="asset-description">Description</label>';
        html += '<textarea id="asset-description" name="description"></textarea>';
        html += '</form>';
        return html += '</div>';
    };

    CalloutDialogHelper.prototype.form_data_to_object = function() {
        return {
            url: this.url,
            title: $('#selection_form #asset-title').val(),
            description: $('#selection_form #asset-description').val()
        };
    };

    return CalloutDialogHelper;

})();
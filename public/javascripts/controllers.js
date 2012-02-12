/* App Controllers */

function CreateContentNodeCtrl($xhr) {

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

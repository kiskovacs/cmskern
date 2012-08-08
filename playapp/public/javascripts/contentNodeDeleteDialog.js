$(document).ready(function() {

    // Register on all delete buttons
    $("a.deleteContentNode").live("click", function(event) {
        var id          = $(this).data('id');
        var contentType = $(this).data('type');
        var title       = $(this).data('title');

        var heading         = 'Confirm Delete';
        var question        = 'Please confirm that you wish to delete "' + title + '" (ID: ' + id + ').';
        var cancelButtonTxt = 'Cancel';
        var okButtonTxt     = 'Confirm';

        var callback = function() {
            $.ajax({
                type: 'DELETE',
                url:  '/' + contentType + '/' + id,
                success: function(data, textStatus, jqXHR) {
                    console.log("Removed content node " + id);
                    location.reload();  // refresh content node list
                },
                error: function(jqXHR, textStatus, errorThrown) {
                    alert("Problem while deleting ' + contentType + ' with ID ' + id + ':\n " + errorThrown);
                }
            });
        };
        confirm(heading, question, cancelButtonTxt, okButtonTxt, callback);
    });

});

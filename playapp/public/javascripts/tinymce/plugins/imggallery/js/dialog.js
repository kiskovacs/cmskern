tinyMCEPopup.requireLangPack();

var ImgGalleryDialog = {
	init : function() {
		var f = document.forms[0];

		// Get the selected contents as text and place it in the input
		f.select_value.value = tinyMCEPopup.editor.selection.getContent({format : 'text'});
		// f.somearg.value = tinyMCEPopup.getWindowArg('some_custom_arg');
	},

	insert : function() {
		// Insert the contents from the input into the document
        var v = '<p>#{imageGallery id:\'' + document.forms[0].select_value.value + '\', title:\'' + document.forms[0].select_title.value + '\' /}</p>';
		tinyMCEPopup.editor.execCommand('mceInsertContent', false, v);
		tinyMCEPopup.close();
	}
};

tinyMCEPopup.onInit.add(ImgGalleryDialog.init, ImgGalleryDialog);

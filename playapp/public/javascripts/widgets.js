/* http://docs.angularjs.org/#!angular.widget */

/**
 * Widget for displaying a complete form as specified by the given schema.
 */
angular.widget('my:form', function(element) {

    this.descend(true);     // compiler will process children elements
    this.directives(true);  // compiler will process directives

    return function(element) {

        var scope = this,
            schema = scope.$eval(element.attr('schema')),
            data = element.attr('data'),
            fieldset = angular.element('<fieldset class="root"></fieldset>');

        // process every field as specified in the JSON schema definition
        //                               context object: {parentName, fqName, curDOMParent, childtype}
        angular.forEach(schema, function processField(field, fieldKey) {
            var qualifiedName = this.parentName + '.' + fieldKey,
                fullyQualifiedName = this.fqName + '.' + fieldKey,
                fieldElStr;
            console.log("----> field: " + fullyQualifiedName + ", relative: " + qualifiedName);

            // has hierarchical subforms?
            if (field.items && jQuery.isArray(field.items)) {
                var childElem = fieldKey + 'Elem';

                // ~~~~ FIXME: start (init array top-level)
                var contentChilds = scope.$eval(qualifiedName);
                if (!contentChilds) {
                    var propName = fullyQualifiedName.substr('contentNode'.length + 1);
                    //console.log("WARN:  No content childs for " + propName);
                    var propNameArr = propName.split('.');
                    if (propNameArr.length == 1) {
                        globalContentNode[propNameArr[0]] = [{}];
                    } else {
                        //console.log("*** WARN Unsupported nesting of arrays: " + propNameArr);
                    }
                } else {
                    //console.log("      ----> fieldset ng:repeat=" + childElem + " in " + qualifiedName);
                    // Nesting of ng:repeat must use relative variable reference names
                }
                // ~~~~ FIXME: end (init array top-level)

                // ~~~~~~ construct subform
                var subform = angular.element('<div class="subform"></div>');
                var subfieldset = angular.element('<fieldset ng:repeat="' + childElem + ' in ' + qualifiedName + '" jq:autoremove=""></fieldset>');

                var legendChild = angular.element('<legend>' + field.title +'</legend>'); // Position: {{$index}}

                // ~~ up button (only if not first element, see CSS selector)
                var moveUpButton = angular.element('<a class="move_up" href="#" ng:click="moveUp(' + qualifiedName + ')"><i class="icon-arrow-up" title="Move up"></i></a>');
                legendChild.append(moveUpButton);
                // ~~ down button (only if not last element, see CSS selector)
                var moveDownButton = angular.element('<a class="move_down" href="#" ng:click="moveDown(' + qualifiedName + ')"><i class="icon-arrow-down" title="Move down"></i></a>');
                legendChild.append(moveDownButton);

                // ~~ remove (per individual child group)
                var removeButton = angular.element('<a class="remove" href="#" ng:click="' + qualifiedName + '.$remove(' + childElem + ')"><i class="icon-minus" title="Remove ' + field.title + '"></i></a>');
                legendChild.append(removeButton);
                subfieldset.append(legendChild);
                jQuery.each(field.items, function (subIdx, subfield) {
                    // ~~ render fields of subform
                    var elGroup = angular.element('<div class="subelements ' + subfield.id + '"></div>');
                    angular.forEach(subfield.properties, processField,
                        {parentName: childElem, fqName: fullyQualifiedName, curDOMParent: elGroup, childtype: subfield.id});
                    subfieldset.append(elGroup);
                });
                subform.append(subfieldset);

                this.curDOMParent.append(subform);

                // ~~
                var localScope = this;
                var subfieldTypes = [];
                jQuery.each(field.items, function (subIdx, subfield) {
                   subfieldTypes.push(subfield.id);
                });
                jQuery.each(field.items, function (subIdx, subfield) {
                    // ~~ add sub-entity button (available no matter how many already exist)
                    var addButton = angular.element('<div class="btn_add"><a href="#" ' +
                        ' ng:click="addChild({parent:'+ localScope.parentName +',' +
                        ' child:' + qualifiedName + ', childname: \'' + fieldKey + '\',' +
                        ' childtype: \'' + subfield.id + '\', allChildtypes: \'' + subfieldTypes + '\'})">' +
                        '<i class="icon-plus" title="Add ' + subfield.title + '"></i>' + subfield.title + '</a></div>');
                    localScope.curDOMParent.append(addButton);
                });

                return;
            }

            // ~~~~~~~~~~~~~~~~~~~~~~~~~~~ Start Render Field Type

            var controlGroup = angular.element('<div class="control-group"></div>');

            // ~~ Label for input element
            controlGroup.append(angular.element('<label class="control-label" for="' + qualifiedName + '">' + field.title + '</label>'));
            var controlElem = angular.element('<div class="controls">');

            var typeLength = "medium";
            if (field.ui_width) {
                typeLength = field.ui_width;
            }
            var lengthCssClassName = 'input-' + typeLength;

            if (field.type == 'array' && field.items.type == 'string') {
                fieldElStr  = '<input type="text" class="valueArray ' + lengthCssClassName + '" ui:item="' + qualifiedName + '" ';
                fieldElStr += ' ui:valueArray >';
            }
            else if (field.enum) {
                fieldElStr  = '<ul ui:selectable-container class="selectBox">';
                console.log("    * enum: " + field.enum);
                fieldElStr += '    <li ui:selectable="' + qualifiedName + '" ng:repeat="i in [';
                for (idx in field.enum) {
                    fieldElStr += '\'' + field.enum[idx] + '\'';
                    if (idx < field.enum.length-1) {
                        fieldElStr += ',';
                    }
                }
                fieldElStr += ']" data-value="{{i}}">';
                fieldElStr += '        <div class="name">{{i}}</div>';
                fieldElStr += '    </li>';
                fieldElStr += '</ul>';
            }
            else if (field.ui_class == 'reference') {
                fieldElStr  = '<div class="reference input-append">';
                fieldElStr += '  <input class="' + lengthCssClassName + '" name="' + qualifiedName + '">';
                if (field.ui_update) {
                    var fieldnames = ""; // field.to_update.join("#");
                    for (i in field.ui_update) {
                        fieldnames += this.fqName+ '.' + field.ui_update[i] + "#";
                    }
                    console.log("fields to update: " + fieldnames);
                    fieldElStr += '  <span class="add-on" ng:click="simple_select_value(\'' + field.ui_callout + '\',\'' + fieldnames +'\'';
                } else {

                    fieldElStr += '  <span class="add-on" ng:click="simple_select_value(\'' + field.ui_callout + '\',\'' + fullyQualifiedName + '\'';
                }
                fieldElStr += ')"><i class="icon-edit"></i></span>';
                fieldElStr += '</div>';
            }
            else if (field.format == 'date') {
                fieldElStr  = '<div class="reference">';
                fieldElStr += '<input type="text" class="datepicker ' + lengthCssClassName + '"';
                // dateFormat according to http://docs.jquery.com/UI/Datepicker/formatDate
                fieldElStr += '  ui:datepicker ui:date="' + qualifiedName + '" ui:options="{dateFormat: \'yy-mm-dd\', showOn: \'both\',';
                fieldElStr += '                        buttonImage: \'/public/images/calendar.gif\', buttonImageOnly: true, firstDay: 1, gotoCurrent: true}">';
                fieldElStr += '</div>';
            }
            else if (field.ui_class == 'richtextarea') {
                fieldElStr = '<textarea ui:tinymce class="mceRichText ' + lengthCssClassName + '" name="' + qualifiedName + '" ';

                //angular.forEach(field, function(attribute) {
                //    fieldElStr += attribute + '="' + field[attribute] + '" ';
                //});

                fieldElStr += ' rows="8" cols="70"></textarea>';
            }
            else if (field.type == 'object') {
                fieldElStr = angular.element('<div class="subelements ' + fieldKey + '"></div>');
                console.log("**** START " + fieldKey);
                angular.forEach(field.properties, processField,
                    {parentName: fieldKey, fqName: fullyQualifiedName, curDOMParent: fieldElStr});
            }
            // ~~ "normal" text input field
            else {

                fieldElStr = '<input class="' + lengthCssClassName + '" name="' + qualifiedName + '" ';

                angular.forEach(field, function(value, attribute) {
                    if (attribute != 'tag') {
                        fieldElStr += attribute + '="' + value + '" ';
                    }
                });

                fieldElStr += '>';
            }

            /* type == 'autoComplete':
             // TODO: Under development (still hard-code to use tag search)
             fieldElStr  = '<input type="textbox" class="autoComplete ' + lengthClassName + '"';
             fieldElStr += '  ui:autocomplete ui:options="{urls: {list: \'/tag/search?q=\'}}" ui:item="' + qualifiedName + '" />';
             */
            controlElem.append(fieldElStr);
            // ~~~~~~~~~~~~~~~~~~~~~~~~~~~ End Render Field Type

            // ~~ optionally add help hint
            if (field.description) {
                controlElem.append('<p class="help-block">' + field.description + '</p>');
            }
            controlGroup.append(controlElem);
            this.curDOMParent.append(controlGroup);

        }, {parentName: data, fqName: data, curDOMParent: fieldset});

        angular.compile(fieldset)(scope);
        element.append(fieldset);
    };
});
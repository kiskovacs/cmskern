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
        //                               context object: {parentName, fqName, curDOMParent, invisible}
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
                var subfieldset = angular.element('<fieldset ng:repeat="' + childElem + ' in ' + qualifiedName + '"></fieldset>');

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
                subform.append(subfieldset);

                this.curDOMParent.append(subform);

                // ~~ add button (available no matter how many already exist)
                var localScope = this;
                jQuery.each(field.items, function (subIdx, subfield) {
                    // ~~ render fields of subform (TODO: erst später)
                    angular.forEach(subfield.properties, processField,
                        {parentName: childElem, fqName: fullyQualifiedName, curDOMParent: subfieldset, invisible: subIdx > 0});

                    var addButton = angular.element('<div class="btn_add"><a href="#" ' +
                        ' ng:click="addChild({parent:'+ localScope.parentName +',' +
                        ' child:' + qualifiedName + ', childname: \'' + fieldKey + '\', childtype: \'' + subfield.id + '\'})">' +
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
                fieldElStr += '    <li ui:selectable="' + qualifiedName + '" ng:repeat="i in schemaRules.' + field.rule_ref + '.allowedValues" data-value="{{i.value}}">';
                fieldElStr += '        <div class="name">{{i.title}}</div>';
                fieldElStr += '    </li>';
                fieldElStr += '</ul>';
            }
            else if (field.ui_class == 'reference') {
                fieldElStr  = '<div class="reference input-append">';
                fieldElStr += '  <input class="' + lengthCssClassName + '" name="' + qualifiedName + '">';
                fieldElStr += '  <span class="add-on" ng:click="select_value(\'' + field.ui_callout + '\',\'' + fullyQualifiedName + '\'';
                if (field.ui_update_also) {
                    var fullyQualifiedNameSeconday = this.fqName + '.' + field.ui_update_also;
                    fieldElStr += ',\'' + fullyQualifiedNameSeconday + '\'';
                }
                fieldElStr += ')"><i class="icon-edit"></i></span>';
                fieldElStr += '</div>';
            }
            else if (field.ui_class == 'simple_reference') {
                // Extends reference by allowing multiple fields to be updated
                fieldElStr  = '<div class="reference input-append">';
                fieldElStr += '  <input class="' + lengthCssClassName + '" name="' + qualifiedName + '">';
                var fieldnames = ""; // field.ui_to_update.join("#");
                for (i in field.ui_to_update) {
                    fieldnames += this.fqName+ '.' + field.ui_to_update[i] + "#";
                }
                fieldElStr += '  <span class="add-on" ng:click="simple_select_value(\'' + field.ui_callout + '\',\'' + fieldnames +'\'';
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
            // ~~ "normal" text input field
            else {

                fieldElStr = '<input class="' + lengthCssClassName + '" name="' + qualifiedName + '" ';

                angular.forEach(field, function(value, attribute) {
                    if (attribute != 'tag') {
                        fieldElStr += attribute + '="' + value + '" ';
                    }
                });

                //if (this.invisible) {
                //    fieldElStr += ' style="display:none"';
                //}
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
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
        angular.forEach(schema, function processField(field) {
            var qualifiedName = this.parentName + '.' + field.name,
                fullyQualifiedName = this.fqName + '.' + field.name,
                fieldElStr;
            console.log("----> field: " + fullyQualifiedName + ", relative: " + qualifiedName);

            // has hierarchical subforms?
            if (field.children) {
                var childElem = field.name + 'Elem';

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

                var legendChild = angular.element('<legend>' + field.label +'</legend>'); // Position: {{$index}}

                // ~~ up button (only if not first element, see CSS selector)
                var moveUpButton = angular.element('<a class="move_up" href="#" ng:click="moveUp(' + qualifiedName + ')"><i class="icon-arrow-up" title="Move up"></i></a>');
                legendChild.append(moveUpButton);
                // ~~ down button (only if not last element, see CSS selector)
                var moveDownButton = angular.element('<a class="move_down" href="#" ng:click="moveDown(' + qualifiedName + ')"><i class="icon-arrow-down" title="Move down"></i></a>');
                legendChild.append(moveDownButton);

                // ~~ remove (per individual child group)
                var removeButton = angular.element('<a class="remove" href="#" ng:click="' + qualifiedName + '.$remove(' + childElem + ')"><i class="icon-minus" title="Remove ' + field.label + '"></i></a>');
                legendChild.append(removeButton);
                subfieldset.append(legendChild);
                subform.append(subfieldset);

                // ~~
                this.curDOMParent.append(subform);
                angular.forEach(field.children, processField, {parentName: childElem, fqName: fullyQualifiedName, curDOMParent: subfieldset});

                // ~~ add button (should be available anytime)
                var addButton = angular.element('<div class="btn_add"><a href="#" ng:click="addChild({parent:'+ this.parentName +', child:' + qualifiedName + ', childname: \'' + field.name + '\'})"><i class="icon-plus" title="Add"></i>' + field.label + '</a></div>');
                this.curDOMParent.append(addButton);

                //console.log("~~ after add children   -> " + qualifiedName);
                return;
            }

            // ~~~~~~~~~~~~~~~~~~~~~~~~~~~ Render Field Types
            var typeLength = "medium";
            if (field.len) {
                typeLength = field.len;
            }
            var lengthClassName = 'input-' + typeLength;

            if (field.type == 'title') {
                field.type = 'text'; // for the inputing part, there is no difference (except that title is a required field)
            }

            switch (field.type || 'text') {
                case 'reference': {
                    fieldElStr  = '<div class="reference input-append">';
                    fieldElStr += '  <input class="' + lengthClassName + '" name="' + qualifiedName + '">';
                    fieldElStr += '  <span class="add-on" ng:click="select_value(\'' + field.callout + '\',\'' + fullyQualifiedName + '\'';
                    if (field.update_also) {
                        var fullyQualifiedNameSeconday = this.fqName + '.' + field.update_also;
                        fieldElStr += ',\'' + fullyQualifiedNameSeconday + '\'';
                    }
                    fieldElStr += ')"><i class="icon-edit"></i></span>';
                    fieldElStr += '</div>';
                    break;
                }
                 case 'simple_reference': {
                    fieldElStr  = '<div class="reference input-append">';
                    fieldElStr += '  <input class="' + lengthClassName + '" name="' + qualifiedName + '">';
                    var fieldnames = ""; // field.to_update.join("#");
                    for (i in field.to_update) { fieldnames += this.fqName+ '.' + field.to_update[i] + "#"; };

                    fieldElStr += '  <span class="add-on" ng:click="simple_select_value(\'' + field.callout + '\',\'' + fieldnames +'\'';
                    fieldElStr += ')"><i class="icon-edit"></i></span>';
                    fieldElStr += '</div>';
                    break;
                }
                case 'date': {
                    fieldElStr  = '<div class="reference">';
                    fieldElStr += '<input type="text" class="datepicker ' + lengthClassName + '"';
                    fieldElStr += '  ui:datepicker ui:date="' + qualifiedName + '" ui:options="{dateFormat: \'' + field.dateFormat + '\', showOn: \'both\',';
                    fieldElStr += '                        buttonImage: \'/public/images/calendar.gif\', buttonImageOnly: true, firstDay: 1, gotoCurrent: true}">';
                    fieldElStr += '</div>';
                    break;
                }
                case 'select': {
                    fieldElStr =  '<ul ui:selectable-container class="selectBox">';
                    fieldElStr += '    <li ui:selectable="' + qualifiedName + '" ng:repeat="i in schemaRules.' + field.rule_ref + '.allowedValues" data-value="{{i.value}}">';
                    fieldElStr += '        <div class="name">{{i.label}}</div>';
                    fieldElStr += '    </li>';
                    fieldElStr += '</ul>';
                    break;
                }
                case 'autoSuggest': {
                    fieldElStr += '<input type="textbox" class="autoSuggest ' + lengthClassName + '"';
                    fieldElStr += '  ui:autocomplete ui:options="{urls: {list: \'/tag/search?q=\'}}" ui:item="' + qualifiedName + '" />';
                    break;
                }
                case 'checkbox':; //fallthrough
                case 'password':; //fallthrough
                case 'text': {
                    fieldElStr = '<input class="' + lengthClassName + '" name="' + qualifiedName + '" ';

                    angular.forEach(field, function(value, attribute) {
                        if (attribute != 'tag') {
                            fieldElStr += attribute + '="' + value + '" ';
                        }
                    });

                    fieldElStr += '>';
                    break;
                }
                case 'textarea': {
                    fieldElStr = '<textarea ui:tinymce class="mceRichText ' + lengthClassName + '" name="' + qualifiedName + '" ';

                    //angular.forEach(field, function(attribute) {
                    //    fieldElStr += attribute + '="' + field[attribute] + '" ';
                    //});

                    fieldElStr += ' rows="8" cols="70"></textarea>';
                    break;
                }
            }

            var controlGroup = angular.element('<div class="control-group"></div>');
            controlGroup.append(angular.element('<label class="control-label" for="' + qualifiedName + '">' + field.label + '</label>'));
            var controlElem = angular.element('<div class="controls">');
            controlElem.append(fieldElStr);
            if (field.helptext) {
                controlElem.append('<p class="help-block">' + field.helptext + '</p>');
            }

            controlGroup.append(controlElem);
            //console.log("****** append to " + qualifiedName);
            this.curDOMParent.append(controlGroup);

        }, {parentName: data, fqName: data, curDOMParent: fieldset});

        angular.compile(fieldset)(scope);
        element.append(fieldset);
    };
});
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
            console.log("----> qualifiedName: " + qualifiedName);

            // has hierarchical subforms?
            if (field.children) {
                var childElem = field.name + 'Elem';

                // ~~~~ FIXME: start (init array top-level)
                var contentChilds = scope.$eval(qualifiedName);
                if (!contentChilds) {
                    var propName = fullyQualifiedName.substr('contentNode'.length + 1);
                    console.log("WARN:  No content childs for " + propName);
                    var propNameArr = propName.split('.');
                    if (propNameArr.length == 1) {
                        globalContentNode[propNameArr[0]] = [{}];
                    } else {
                        console.log("*** WARN Unsupported nesting of arrays: " + propNameArr);
                    }
                } else {
                    console.log("      ----> fieldset ng:repeat=" + childElem + " in " + qualifiedName);
                    // Nesting of ng:repeat must use relative variable reference names
                }
                // ~~~~ FIXME: end (init array top-level)

                // ~~~~~~ construct subform
                var subform = angular.element('<div class="subform"></div>');
                var subfieldset = angular.element('<fieldset ng:repeat="' + childElem + ' in ' + qualifiedName + '"></fieldset>');

                var legendChild = angular.element('<legend>' + field.label + '</legend>'); // Position: {{$index}}

                // ~~ up button (TODO: only if not first element)
                var moveUpButton = angular.element('<a class="move_up" href="#" ng:click="moveUp(' + qualifiedName + ')"><i class="icon-arrow-up" title="Move up"></i></a>');
                legendChild.append(moveUpButton);
                // ~~ down button (TODO: only if not last element)
                var moveDownButton = angular.element('<a class="move_down" href="#" ng:click="moveDown(' + qualifiedName + ')"><i class="icon-arrow-down" title="Move down"></i></a>');
                legendChild.append(moveDownButton);

                // ~~ remove (per individual child group)
                //var removeButton = angular.element('<a href="#" ng:click="removeChild(' + qualifiedName + ', ' + childElem + ')"><i class="icon-minus" title="Remove ' + field.label + '"></i></a>');
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

                console.log("~~ after add children   -> " + qualifiedName);
                return;
            }

            switch (field.type || 'text') {
                case 'reference': {
                    fieldElStr = '<input disabled name="' + qualifiedName + '" ';

                    angular.forEach(field, function(value, attribute) {
                        if (attribute != 'tag') {
                            fieldElStr += attribute + '="' + value + '" ';
                        }
                    });

                    fieldElStr += '><a class="btn" href="#" ng:click="select_value(\'' + qualifiedName + '\')">Select</a>';

                    break;
                }
                case 'checkbox':; //fallthrough
                case 'password':; //fallthrough
                case 'text': {
                    fieldElStr = '<input name="' + qualifiedName + '" ';

                    angular.forEach(field, function(value, attribute) {
                        if (attribute != 'tag') {
                            fieldElStr += attribute + '="' + value + '" ';
                        }
                    });

                    fieldElStr += '>';
                    break;
                }
                case 'textarea': {
                    fieldElStr = '<textarea name="' + qualifiedName + '" ';

                    angular.forEach(field, function(attribute) {
                        fieldElStr += attribute + '="' + field[attribute] + '" ';
                    });

                    fieldElStr += '></textarea>';
                    break;
                }
            }

            var controlGroup = angular.element('<div class="control-group"></div>');

            controlGroup.append(angular.element('<label class="control-label" for="' + qualifiedName + '">' + field.label + '</label>'));
            var controlElem = angular.element('<div class="controls">');
            controlElem.append(fieldElStr);
            controlGroup.append(controlElem);
            console.log("****** append to " + qualifiedName);
            this.curDOMParent.append(controlGroup);

        }, {parentName: data, fqName: data, curDOMParent: fieldset});

        angular.compile(fieldset)(scope);
        element.append(fieldset);
    };
});


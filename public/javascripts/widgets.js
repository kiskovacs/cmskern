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
            fieldset = angular.element('<fieldset></fieldset>');

        // process every field as specified in the JSON schema definition
        angular.forEach(schema, function processField(field) {
            var qualifiedName = this.parentName + '.' + field.name,
                fullyQualifiedName = this.fqName + '.' + field.name,
                fieldElStr;
            console.log("----> qualifiedName: " + qualifiedName);

            // has hierarchical subforms?
            if (field.children) {
                var contentChilds = scope.$eval(qualifiedName);
                var childElem = field.name + 'Elem';
                if (!contentChilds) {
                    var propName = fullyQualifiedName.substr('contentNode'.length + 1);
                    console.log("WARN:  No content childs for " + propName);
                    var propNameArr = propName.split('.');
                    if (propNameArr.length == 1) {
                        globalContentNode[propNameArr[0]] = [{}];
                    } else if (propNameArr.length == 2) {
                        var parent = scope.$parent;
                        parent[propNameArr[1]] = [{}];
                    } else {
                        console.log("*** ERROR Unsupported nesting of arrays: " + propNameArr);
                    }
                } else {
                    console.log("      ----> fieldset ng:repeat=" + childElem + " in " + qualifiedName);
                    // Nesting of ng:repeat must use relative variable reference names
                }
                // ~~~~~~ construct subform
                var subform = angular.element('<fieldset ng:repeat="' + childElem + ' in ' + qualifiedName + '"></fieldset>');
                var legendChild = angular.element('<legend>' + field.label + '</legend>');
                // ~~ remove (per individual child group)
                var removeButton = angular.element('<a href="#" ng:click="' + qualifiedName + '.$remove(' + childElem + ')"><i class="icon-minus" title="Remove ' + field.label + '"></i></a>');
                legendChild.append(removeButton);
                subform.append(legendChild);
                // ~~
                this.curDOMParent.append(subform);
                angular.forEach(field.children, processField, {parentName: childElem, fqName: fullyQualifiedName, curDOMParent: subform});

                // ~~ add button (should be available anytime)
                var addButton = angular.element('<a href="#" ng:click="' + qualifiedName + '.$add()"><i class="icon-plus" title="Add"></i>' + field.label + '</a>');
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

                    fieldElStr += '><a href="#" ng:click="select_value({\'key\':\'ref\'})">Select</a>';

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


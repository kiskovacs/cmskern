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
            var fullyQualifiedName = this.parentName + '.' + field.name,
                fieldElStr;
            console.log("----> field: " + field.name);

            // has hierarchical subforms?
            if (field.children) {
                console.log("      ----> (schema) children: " + fullyQualifiedName);
                var contentChilds = scope.$eval(fullyQualifiedName);
                console.log("      ----> (content) childs:  " + contentChilds[0].spitzmarke);
                var childElem = field.name + 'Elem';
                var subfieldset = angular.element('<fieldset ng:repeat="' + childElem + ' in ' + fullyQualifiedName + '"></fieldset>');  // TODO: auf dieser Ebene ng:repeat fuer kinder
                var legend = angular.element('<legend>' + field.label + '</legend>');
                // ~~ add
                var addButton = angular.element('<a href="#" ng:click="' + fullyQualifiedName + '.$add()"><i class="icon-plus" title="Add"></i></a>');
                legend.append(addButton);
                // ~~ remove
                var removeButton = angular.element('<a href="#" ng:click="' + fullyQualifiedName + '.$remove(' + childElem + ')"><i class="icon-minus" title="Remove"></i></a>');
                legend.append(removeButton);
                subfieldset.append(legend);
                // ~~
                angular.forEach(field.children, processField, {parentName: childElem, curDOMParent: subfieldset});
                fieldset.append(subfieldset);
                return;
            }

            switch (field.type || 'text') {
                case 'checkbox':; //fallthrough
                case 'password':; //fallthrough
                case 'reference': {
                    fieldElStr = '<input disabled name="' + fullyQualifiedName + '" ';

                    angular.forEach(field, function(value, attribute) {
                        if (attribute != 'tag') {
                            fieldElStr += attribute + '="' + value + '" ';
                        }
                    });

                    fieldElStr += '><a href="#" class="dialog">Select</a>';
                    break;
                }
                case 'text': {
                    fieldElStr = '<input name="' + fullyQualifiedName + '" ';

                    angular.forEach(field, function(value, attribute) {
                        if (attribute != 'tag') {
                            fieldElStr += attribute + '="' + value + '" ';
                        }
                    });

                    fieldElStr += '>';
                    break;
                }
                case 'textarea': {
                    fieldElStr = '<textarea name="' + fullyQualifiedName + '" ';

                    angular.forEach(field, function(attribute) {
                        fieldElStr += attribute + '="' + field[attribute] + '" ';
                    });

                    fieldElStr += '></textarea>';
                    break;
                }
            }

            var controlGroup = angular.element('<div class="control-group"></div>');

            controlGroup.append(angular.element('<label class="control-label" for="' + fullyQualifiedName + '">' + field.label + '</label>'));
            var controlElem = angular.element('<div class="controls">');
            controlElem.append(fieldElStr);
            controlGroup.append(controlElem);
            this.curDOMParent.append(controlGroup);

        }, {parentName: data, curDOMParent: fieldset});
        angular.compile(fieldset)(scope);
        element.append(fieldset);
    };
});


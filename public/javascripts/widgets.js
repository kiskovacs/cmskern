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
            model = element.attr('data'),
            fieldset = angular.element('<fieldset></fieldset>');

        angular.forEach(schema, function processField(field) {
            var name = this.model + '.' + field.name,
                fieldElStr;
            console.log("----> field: " + name);

            // has hierarchical subforms?
            if (field.children) {
                console.log("      ----> children: " + field.name);
                var subfieldset = angular.element('<fieldset></fieldset>');
                var legend = angular.element('<legend>' + field.name + '</legend>');
                subfieldset.append(legend);
                var addButton = angular.element('<a href="" ng:click="' + model + '.'+ field.name + '.$add()">add</a>');
                subfieldset.append(addButton);
                angular.forEach(field.children, processField, {model: name, curDOMParent: subfieldset});
                fieldset.append(subfieldset);
                return;
            }

            switch (field.type || 'text') {
                case 'checkbox':; //fallthrough
                case 'password':; //fallthrough
                case 'text': {
                    fieldElStr = '<input name="' + name + '" ';

                    angular.forEach(field, function(value, attribute) {
                        if (attribute != 'tag') {
                            fieldElStr += attribute + '="' + value + '" ';
                        }
                    });

                    fieldElStr += '>';
                    break;
                }
                case 'textarea': {
                    fieldElStr = '<textarea name="' + name + '" ';

                    angular.forEach(field, function(attribute) {
                        fieldElStr += attribute + '="' + field[attribute] + '" ';
                    });

                    fieldElStr += '></textarea>';
                    break;
                }
            }

            var controlGroup = angular.element('<div class="control-group"></div>');

            controlGroup.append(angular.element('<label class="control-label" for="' + name + '">' + field.label + '</label>'));
            var controlElem = angular.element('<div class="controls">');
            controlElem.append(fieldElStr);
            controlGroup.append(controlElem);
            this.curDOMParent.append(controlGroup);

        }, {model: model, curDOMParent: fieldset});
        angular.compile(fieldset)(scope);
        element.append(fieldset);
    };
});


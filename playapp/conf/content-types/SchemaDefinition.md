
Schema definition
=================

This document describes how you create your own schema definition for
displaying and storing documents in cmskern. The main goal in the design
of the form rendering engine was to be as close as compatible with
the JSON Schema specification (see references #1) with only some extensions allowing
cmskern to derive information on how specific fields should be rendered,
so that you can for example distinguish between a one-line text input field,
a standard text area and a text area using a WYSIWYG editor (cmskern
is currently using tinyMCE). This is achieved by an own namespace (ui),
which is explained in more detail in 'Extensions for UI rendering'.


Supported types
---------------

The following simple types are supported (see [1] paragraph 5.1 for more details):

* `string`  Value MUST be a string.
    Can be constrained by 'enum' (for example ["NG", "NG World", "NG Kids"])

* `number`  Value MUST be a number, floating point numbers are allowed.

* `integer`  Value MUST be an integer, no floating point numbers are allowed.  This is a subset of the number type.
           (currently there is no difference between integer and number in cmskern)

* `boolean`  Value MUST be a boolean.

* `object`  Value MUST be an object.

* `array`  Value MUST be an array.

More supported attributes:

* `default` (true or false)
* `required` (true or false)



Minimal definition
------------------

A minimal schema definition requires a field named 'title' (of type string) to be present to
allow cmskern's content model to display a title for a given object instance

    "title":{
        "title":"Titel (you are free to rename the title)",
        "type":"string",
        "required":true
    }


Extensions for UI rendering
---------------------------


cmskern supports hints on how to render the fields defined in the schema by
using an own  inside an definition under the element 'ui'. This element can
have the following attributes:

* width (mini, small, medium, large, xlarge, xxlarge)
  Specify field input width

* editor (textarea, richtext)
  allow to switch (for type: string) to a standard textarea

* query_type (tags)
* mode (compact, readonly, hidden)
* callout (see separate description in next paragraph)
* class (name of CSS class)

Examples:

    "text":{
        "title":"Teaser Text",
        "type":"string",
        "ui":{
            "editor":"textarea",
            "width":"xxlarge"
        }
    }

Callouts
--------

Examples:

    "node_idref":{
        "title":"Navigationsknoten",
        "type":"integer",
        "ui":{
            "callout":{
                "url":"/callouts/internal/node_reference",
                "src_properties":["id", "title"],
                "target_properties":["node_idref", "node_title"]
            },
            "width":"xlarge"
        }
    },
    "node_title":{
        "title":"Nav.knoten Titel",
        "type":"string",
        "ui":{
            "width":"xxlarge"
        }
    }


References
----------

1. [JSON Schema Definition](http://tools.ietf.org/html/draft-zyp-json-schema-03)

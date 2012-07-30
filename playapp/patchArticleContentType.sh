#!/bin/bash

DEF_CONTENT=$(cat ../article-schema.json)

DEF=`echo $DEF_CONTENT | tr '\n' ' '`

cat <<EOF > /tmp/patchTypeDef.js
    db.contentTypes.update({ 'slug' : 'article' }, { \$set : { 'jsonForm' : '$DEF' } });
EOF

mongo cmskern /tmp/patchTypeDef.js
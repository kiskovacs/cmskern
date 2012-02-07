

[niko, 29-Jan-2012]

Derzeit werden noch leere Strings in MongoDB abgelegt (json2 Serialisierung?)


[niko, 22-Jan-2012]

Since Play 1.2.4 contains a bug
https://play.lighthouseapp.com/projects/57987/tickets/1291-play-124-has-npe-when-creatingediting-morphia-entities-with-the-crud-module
which makes using the CRUD module for a Morphia based
Model impossible, you have to fix PlayPlugin.java (line 84) and recompile play
sources (by simply running ant and copying afterwards play-1.2-localbuild.jar to play-1.2.4.jar)

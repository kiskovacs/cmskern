#!/bin/bash
mongo cmskern < dbCleanContent.js && mongoimport -d cmskern -c content --file conf/initial-content-ng.json

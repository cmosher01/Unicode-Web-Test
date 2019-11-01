#!/bin/sh

mkdir -p src/main/resources/ucd
cd src/main/resources/ucd
curl -LO 'https://www.unicode.org/Public/UCD/latest/ucd/UnicodeData.txt'
cd -

mkdir -p src/main/resources/ucsur
cd src/main/resources/ucsur
curl -LO -H 'user-agent: Mozilla/5.0' 'http://www.kreativekorp.com/ucsur/UNIDATA/UnicodeData.txt'
cd -

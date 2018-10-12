#!/bin/sh

mkdir -p src/main/resources/ucd
cd src/main/resources/ucd
curl -LO 'https://www.unicode.org/Public/UCD/latest/ucd/UnicodeData.txt'
cd -

# currently doesn't work, download manually:
#mkdir -p src/main/resources/ucsur
#cd src/main/resources/ucsur
#curl -LO 'http://www.kreativekorp.com/ucsur/UNIDATA/UnicodeData.txt'
#cd -

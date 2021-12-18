# Unicode-Web-Test

Copyright © 2018-2021, by Christopher Alan Mosher, Shelton, Connecticut, USA, cmosher01@gmail.com

[![License](https://img.shields.io/github/license/cmosher01/Unicode-Web-Test.svg)](https://www.gnu.org/licenses/gpl.html)
[![Latest Release](https://img.shields.io/github/release-pre/cmosher01/Unicode-Web-Test.svg)](https://github.com/cmosher01/Unicode-Web-Test/releases/latest)
[![Build Status](https://travis-ci.com/cmosher01/Unicode-Web-Test.svg?branch=master)](https://travis-ci.com/cmosher01/Unicode-Web-Test)
[![Donate](https://img.shields.io/badge/Donate-PayPal-green.svg)](https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=CVSSQ2BWDCKQ2)

A web server that generates Unicode test pages.
See [PlainWebCss](https://github.com/cmosher01/PlainWebCss) to set up your own Universal Access web server.
Then modify [style.css](src/main/resources/nu/mine/mosher/unicode/style.css) to point it, for testing.

```
$ docker build -t unicode-web-test .
$ docker run -d -p 8080:8080 unicode-web-test
```
Then browse to http://localhost:8080/

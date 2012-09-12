EasyBuilder Readme
==================

EasyBuilder is a tool for using a hacked easy button to kick off builds (or anything else that can be done by issuing a HTTP GET request).

EasyBuilder is released under the [GPLv3](https://www.gnu.org/licenses/gpl.html) license. See the file "LICENSE" for more information.

EasyBuilder uses the [xbee-api](https://code.google.com/p/xbee-api/) library, which is also released under the GPLv3.

Usage
-----

1. You'll need to use Ant or NetBeans to build the project.
2. RXTX has binary that needed to be added to Java's library path. These can be found in lib/rxtx_bin. Modify the nbproject/project.properties file to point to the absolute path for that directory.

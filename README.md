GenericServer
=============

About
-----

This is a basic Android Socket Server, with a standalone version provided as well. By default it will answer http responses on port 4242. There are built in functions available using a RESTful API (documentation on its way). Also, a user directory may be specified, from which the server will send files when requested using the /user/FILENAME API call.

Requires
--------

The following java libraries are required. All libraries are Open Source and the version used in development is provided in the libs/ subdirectory.

* JSON Simple (http://code.google.com/p/json-simple/) which is available under the Apache License v2.0.
* Apache Commons IO (http://commons.apache.org/proper/commons-io/) which is available under the Apache License v2.0.
* Apache Commons CLI (http://commons.apache.org/proper/commons-cli/) which is available under the Apache License v2.0. Only required by standalone version.

License
-------

With the exception of third-party libraries in the libs/ directory (see Requires), everything may be used under the terms of the GNU General Public License version 3. For details see the LICENSE file or go to http://www.gnu.org/licenses/gpl.html

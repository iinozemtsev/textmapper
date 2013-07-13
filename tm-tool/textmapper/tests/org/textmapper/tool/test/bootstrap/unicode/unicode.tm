#  syntax: unicode test

#  Copyright 2002-2013 Evgeny Gryaznov
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.

lang = "java"
prefix = "UnicodeTest"
package = "org.textmapper.tool.test.bootstrap.unicode"
genCopyright = true

:: lexer

identifier(String): /[a-zA-Z_][a-zA-Z_0-9]*/   { $symbol = current(); }
icon(Integer):  /-?[0-9]+/                     { $symbol = Integer.parseInt(current()); }


schar = /[\w\p{Ll}]/
string(String): /"({schar})+"/			   { $symbol = current(); }
_skip:          /[\n\t\r ]+/       (space)
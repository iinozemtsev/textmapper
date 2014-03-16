#  syntax: regular expression grammar

#  Copyright 2002-2014 Evgeny Gryaznov
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

language jsregex(js);

module = "jsregex"
positions = "line,column,offset"
endpositions = "line,column,offset"
expandTabs = 2
genCopyright = true
genCleanup = true
breaks = true

:: lexer

# 0 - default
# 1 - after character/group - enables qualifiers
# 2 - in character set

[initial, afterChar, inSet]

char: /[^()\[\]\.|\\\/*?+-]/					{ this.quantifierReady(); }
escaped: /\\[^\r\n\t0-9uUxXwWsSdDpPabfnrtv]/ 	{ this.quantifierReady(); }
escaped: /\\[abfnrtv]/							{ this.quantifierReady(); }
escaped: /\\[0-7][0-7][0-7]/					{ this.quantifierReady(); }
hx = /[0-9A-Fa-f]/
escaped: /\\[xX]{hx}{hx}/						{ this.quantifierReady(); }
escaped: /\\[uU]{hx}{hx}{hx}{hx}/				{ this.quantifierReady(); }
charclass: /\\[wWsSdD]/							{ this.quantifierReady(); }
charclass: /\\p\{\w+\}/							{ this.quantifierReady(); }

'.':  /\./										{ this.quantifierReady(); }

[afterChar => initial]

'*':  /*/
'+':  /+/
'?':  /?/
quantifier:  /\{[0-9]+(,[0-9]*)?\}/

op_minus:		/\{\-\}/
op_union:		/\{\+\}/
op_intersect:	/\{&&\}/

[initial, inSet]

char: /[*+?]/									{ this.quantifierReady(); }

[initial, afterChar]

'(':  /\(/										{ this.state = 0; }
'|':  /\|/										{ this.state = 0; }
')':  /\)/										{ this.quantifierReady(); }

'(?':	/\(\?[is-]+:/							{ this.state = 0; }

'[':	/\[/  => inSet
'[^':	/\[^/ => inSet
char:  /-/										{ this.quantifierReady(); }

identifier = /[a-zA-Z_][a-zA-Z_\-0-9]*/

expand:			/\{{identifier}\}/	(class)		{ this.quantifierReady(); }
kw_eoi:			/\{eoi\}/						{ this.state = 0; }

[inSet]

']':  /\]/										{ this.state = 0; this.quantifierReady(); }
'-':  /-/
char:  /[\(\|\)]/

:: parser

%input pattern;

pattern ::=
	  partsopt
	| left=pattern '|' partsopt
;

part ::=
	  primitive_part
	| primitive_part '*'
	| primitive_part '+'
	| primitive_part '?'
	| primitive_part quantifier
;

primitive_part ::=
	  char
	| escaped
	| charclass
	| '.'
	| '(' pattern ')'
	| '[' charset ']'
	| '[^' charset ']'
	| expand
;

setsymbol ::=
	  char
	| escaped
	| charclass
;

%right char escaped;

charset ::=
	  sym='-'
	| setsymbol
	| charset setsymbol
	| charset sym='-'
			%prio char
	| charset '-' char
	| charset '-' escaped
;

parts ::=
	  part
	| list=parts part
;


%%

${template js_lexer.code-}
	quantifierReady: function() {
		if (this.chr == 0) {
			if (this.state == 1) this.state = 0;
			return;
		}
		if (this.state == 0) this.state = 1;
	},

${end}
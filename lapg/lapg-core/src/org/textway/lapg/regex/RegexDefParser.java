/**
 * Copyright 2002-2012 Evgeny Gryaznov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.textway.lapg.regex;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import org.textway.lapg.common.CharacterSetImpl;
import org.textway.lapg.regex.RegexDefLexer.ErrorReporter;
import org.textway.lapg.regex.RegexDefLexer.LapgSymbol;
import org.textway.lapg.regex.RegexDefLexer.Lexems;
import org.textway.lapg.regex.RegexDefTree.TextSource;

public class RegexDefParser {

	public static class ParseException extends Exception {
		private static final long serialVersionUID = 1L;

		public ParseException() {
		}
	}

	private final ErrorReporter reporter;

	public RegexDefParser(ErrorReporter reporter) {
		this.reporter = reporter;
	}


	private static final boolean DEBUG_SYNTAX = false;
	TextSource source;
	CharacterSetImpl.Builder setbuilder = new CharacterSetImpl.Builder();
	private static final int[] lapg_action = RegexDefLexer.unpack_int(37,
		"\ufffd\uffff\20\0\11\0\12\0\13\0\14\0\uffe7\uffff\uffff\uffff\uffff\uffff\uffff\uffff" +
		"\32\0\uffd1\uffff\uffb1\uffff\2\0\uffff\uffff\21\0\22\0\23\0\24\0\25\0\uffff\uffff" +
		"\uffff\uffff\uff99\uffff\5\0\6\0\7\0\10\0\33\0\15\0\16\0\uff81\uffff\26\0\17\0\3" +
		"\0\30\0\31\0\ufffe\uffff");

	private static final short[] lapg_lalr = RegexDefLexer.unpack_short(136,
		"\1\uffff\2\uffff\3\uffff\4\uffff\5\uffff\15\uffff\21\uffff\22\uffff\0\0\16\0\uffff" +
		"\ufffe\1\uffff\2\uffff\3\uffff\4\uffff\5\uffff\15\uffff\21\uffff\22\uffff\16\0\17" +
		"\0\uffff\ufffe\6\uffff\7\uffff\10\uffff\11\uffff\0\4\1\4\2\4\3\4\4\4\5\4\15\4\16" +
		"\4\17\4\21\4\22\4\uffff\ufffe\1\uffff\2\uffff\3\uffff\4\uffff\5\uffff\15\uffff\21" +
		"\uffff\22\uffff\0\1\16\1\17\1\uffff\ufffe\1\uffff\2\uffff\3\uffff\4\uffff\5\uffff" +
		"\15\uffff\21\uffff\22\uffff\0\0\16\0\17\0\uffff\ufffe\2\uffff\3\uffff\4\27\24\27" +
		"\25\27\uffff\ufffe");

	private static final short[] lapg_sym_goto = RegexDefLexer.unpack_short(30,
		"\0\1\5\16\27\37\43\44\45\46\47\47\47\47\53\55\56\56\62\66\66\70\74\76\102\106\112" +
		"\114\117\122");

	private static final short[] lapg_sym_from = RegexDefLexer.unpack_short(82,
		"\11\0\6\14\26\0\6\7\10\14\24\25\26\36\0\6\7\10\14\24\25\26\36\0\6\7\10\14\24\25\26" +
		"\0\6\14\26\13\13\13\13\0\6\14\26\11\16\16\0\6\14\26\0\6\14\26\24\25\7\10\24\25\0" +
		"\6\0\6\14\26\0\6\14\26\7\10\24\25\7\10\0\6\26\0\6\26");

	private static final short[] lapg_sym_to = RegexDefLexer.unpack_short(82,
		"\44\1\1\1\1\2\2\17\17\2\17\17\2\42\3\3\20\20\3\20\20\3\43\4\4\21\21\4\21\21\4\5\5" +
		"\5\5\27\30\31\32\6\6\6\6\26\26\34\7\7\7\7\10\10\10\10\35\40\22\22\36\36\11\16\12" +
		"\12\33\12\13\13\13\13\23\23\37\37\24\25\14\14\14\15\15\41");

	private static final short[] lapg_rlen = RegexDefLexer.unpack_short(28,
		"\0\1\1\3\1\2\2\2\2\1\1\1\1\3\3\3\1\1\1\1\1\1\2\2\3\3\1\2");

	private static final short[] lapg_rlex = RegexDefLexer.unpack_short(28,
		"\34\34\26\26\27\27\27\27\27\30\30\30\30\30\30\30\30\31\31\31\32\32\32\32\32\32\33" +
		"\33");

	protected static final String[] lapg_syms = new String[] {
		"eoi",
		"expand",
		"char",
		"escaped",
		"charclass",
		"'.'",
		"'*'",
		"'+'",
		"'?'",
		"quantifier",
		"op_minus",
		"op_union",
		"op_intersect",
		"'('",
		"'|'",
		"')'",
		"'(?'",
		"'['",
		"'[^'",
		"kw_eoi",
		"']'",
		"'-'",
		"pattern",
		"part",
		"primitive_part",
		"setsymbol",
		"charset",
		"parts",
		"partsopt",
	};

	public interface Tokens extends Lexems {
		// non-terminals
		public static final int pattern = 22;
		public static final int part = 23;
		public static final int primitive_part = 24;
		public static final int setsymbol = 25;
		public static final int charset = 26;
		public static final int parts = 27;
		public static final int partsopt = 28;
	}

	protected final int lapg_next(int state) {
		int p;
		if (lapg_action[state] < -2) {
			for (p = -lapg_action[state] - 3; lapg_lalr[p] >= 0; p += 2) {
				if (lapg_lalr[p] == lapg_n.lexem) {
					break;
				}
			}
			return lapg_lalr[p + 1];
		}
		return lapg_action[state];
	}

	protected final int lapg_state_sym(int state, int symbol) {
		int min = lapg_sym_goto[symbol], max = lapg_sym_goto[symbol + 1] - 1;
		int i, e;

		while (min <= max) {
			e = (min + max) >> 1;
			i = lapg_sym_from[e];
			if (i == state) {
				return lapg_sym_to[e];
			} else if (i < state) {
				min = e + 1;
			} else {
				max = e - 1;
			}
		}
		return -1;
	}

	protected int lapg_head;
	protected LapgSymbol[] lapg_m;
	protected LapgSymbol lapg_n;
	protected RegexDefLexer lapg_lexer;

	public RegexAstPart parse(RegexDefLexer lexer) throws IOException, ParseException {

		lapg_lexer = lexer;
		lapg_m = new LapgSymbol[1024];
		lapg_head = 0;

		lapg_m[0] = new LapgSymbol();
		lapg_m[0].state = 0;
		lapg_n = lapg_lexer.next();

		while (lapg_m[lapg_head].state != 36) {
			int lapg_i = lapg_next(lapg_m[lapg_head].state);

			if (lapg_i >= 0) {
				reduce(lapg_i);
			} else if (lapg_i == -1) {
				shift();
			}

			if (lapg_i == -2 || lapg_m[lapg_head].state == -1) {
				break;
			}
		}

		if (lapg_m[lapg_head].state != 36) {
			reporter.error(lapg_n.offset, lapg_n.endoffset, lapg_lexer.getTokenLine(),
						MessageFormat.format("syntax error before line {0}",
								lapg_lexer.getTokenLine()));
			throw new ParseException();
		}
		return (RegexAstPart)lapg_m[lapg_head - 1].sym;
	}

	protected void shift() throws IOException {
		lapg_m[++lapg_head] = lapg_n;
		lapg_m[lapg_head].state = lapg_state_sym(lapg_m[lapg_head - 1].state, lapg_n.lexem);
		if (DEBUG_SYNTAX) {
			System.out.println(MessageFormat.format("shift: {0} ({1})", lapg_syms[lapg_n.lexem], lapg_lexer.current()));
		}
		if (lapg_m[lapg_head].state != -1 && lapg_n.lexem != 0) {
			lapg_n = lapg_lexer.next();
		}
	}

	protected void reduce(int rule) {
		LapgSymbol lapg_gg = new LapgSymbol();
		lapg_gg.sym = (lapg_rlen[rule] != 0) ? lapg_m[lapg_head + 1 - lapg_rlen[rule]].sym : null;
		lapg_gg.lexem = lapg_rlex[rule];
		lapg_gg.state = 0;
		if (DEBUG_SYNTAX) {
			System.out.println("reduce to " + lapg_syms[lapg_rlex[rule]]);
		}
		LapgSymbol startsym = (lapg_rlen[rule] != 0) ? lapg_m[lapg_head + 1 - lapg_rlen[rule]] : lapg_n;
		lapg_gg.offset = startsym.offset;
		lapg_gg.endoffset = (lapg_rlen[rule] != 0) ? lapg_m[lapg_head].endoffset : lapg_n.offset;
		applyRule(lapg_gg, rule, lapg_rlen[rule]);
		for (int e = lapg_rlen[rule]; e > 0; e--) {
			lapg_m[lapg_head--] = null;
		}
		lapg_m[++lapg_head] = lapg_gg;
		lapg_m[lapg_head].state = lapg_state_sym(lapg_m[lapg_head - 1].state, lapg_gg.lexem);
	}

	@SuppressWarnings("unchecked")
	protected void applyRule(LapgSymbol lapg_gg, int rule, int ruleLength) {
		switch (rule) {
			case 2:  // pattern ::= partsopt
				 lapg_gg.sym = RegexUtil.emptyIfNull(((RegexAstPart)lapg_m[lapg_head].sym), source, lapg_m[lapg_head].offset); 
				break;
			case 3:  // pattern ::= pattern '|' partsopt
				 lapg_gg.sym = RegexUtil.createOr(((RegexAstPart)lapg_m[lapg_head - 2].sym), ((RegexAstPart)lapg_m[lapg_head].sym), source, lapg_m[lapg_head].offset); 
				break;
			case 5:  // part ::= primitive_part '*'
				 lapg_gg.sym = new RegexAstQuantifier(((RegexAstPart)lapg_m[lapg_head - 1].sym), 0, -1, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 6:  // part ::= primitive_part '+'
				 lapg_gg.sym = new RegexAstQuantifier(((RegexAstPart)lapg_m[lapg_head - 1].sym), 1, -1, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 7:  // part ::= primitive_part '?'
				 lapg_gg.sym = new RegexAstQuantifier(((RegexAstPart)lapg_m[lapg_head - 1].sym), 0, 1, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 8:  // part ::= primitive_part quantifier
				 lapg_gg.sym = RegexUtil.createQuantifier(((RegexAstPart)lapg_m[lapg_head - 1].sym), source, lapg_m[lapg_head].offset, lapg_gg.endoffset, reporter); 
				break;
			case 9:  // primitive_part ::= char
				 lapg_gg.sym = new RegexAstChar(((Character)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 10:  // primitive_part ::= escaped
				 lapg_gg.sym = new RegexAstChar(((Character)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 11:  // primitive_part ::= charclass
				 lapg_gg.sym = new RegexAstCharClass(((String)lapg_m[lapg_head].sym), RegexUtil.getClassSet(((String)lapg_m[lapg_head].sym), setbuilder), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 12:  // primitive_part ::= '.'
				 lapg_gg.sym = new RegexAstAny(source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 13:  // primitive_part ::= '(' pattern ')'
				 lapg_gg.sym = RegexUtil.wrap(((RegexAstPart)lapg_m[lapg_head - 1].sym)); 
				break;
			case 14:  // primitive_part ::= '[' charset ']'
				 lapg_gg.sym = RegexUtil.toSet(((List<RegexAstPart>)lapg_m[lapg_head - 1].sym), reporter, setbuilder, false); 
				break;
			case 15:  // primitive_part ::= '[^' charset ']'
				 lapg_gg.sym = RegexUtil.toSet(((List<RegexAstPart>)lapg_m[lapg_head - 1].sym), reporter, setbuilder, true); 
				break;
			case 16:  // primitive_part ::= expand
				 lapg_gg.sym = new RegexAstExpand(source, lapg_gg.offset, lapg_gg.endoffset); RegexUtil.checkExpand((RegexAstExpand) lapg_gg.sym, reporter); 
				break;
			case 17:  // setsymbol ::= char
				 lapg_gg.sym = new RegexAstChar(((Character)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 18:  // setsymbol ::= escaped
				 lapg_gg.sym = new RegexAstChar(((Character)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 19:  // setsymbol ::= charclass
				 lapg_gg.sym = new RegexAstCharClass(((String)lapg_m[lapg_head].sym), RegexUtil.getClassSet(((String)lapg_m[lapg_head].sym), setbuilder), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 20:  // charset ::= '-'
				 lapg_gg.sym = new ArrayList<RegexAstPart>(); ((List<RegexAstPart>)lapg_gg.sym).add(new RegexAstChar('-', source, lapg_m[lapg_head].offset, lapg_m[lapg_head].endoffset)); 
				break;
			case 21:  // charset ::= setsymbol
				 lapg_gg.sym = new ArrayList<RegexAstPart>(); RegexUtil.addSetSymbol(((List<RegexAstPart>)lapg_gg.sym), ((RegexAstPart)lapg_m[lapg_head].sym), reporter); 
				break;
			case 22:  // charset ::= charset setsymbol
				 RegexUtil.addSetSymbol(((List<RegexAstPart>)lapg_m[lapg_head - 1].sym), ((RegexAstPart)lapg_m[lapg_head].sym), reporter); 
				break;
			case 23:  // charset ::= charset '-' %prio char
				 ((List<RegexAstPart>)lapg_m[lapg_head - 1].sym).add(new RegexAstChar('-', source, lapg_m[lapg_head].offset, lapg_m[lapg_head].endoffset)); 
				break;
			case 24:  // charset ::= charset '-' char
				 RegexUtil.applyRange(((List<RegexAstPart>)lapg_m[lapg_head - 2].sym), new RegexAstChar(((Character)lapg_m[lapg_head].sym), source, lapg_m[lapg_head].offset, lapg_m[lapg_head].endoffset), reporter); 
				break;
			case 25:  // charset ::= charset '-' escaped
				 RegexUtil.applyRange(((List<RegexAstPart>)lapg_m[lapg_head - 2].sym), new RegexAstChar(((Character)lapg_m[lapg_head].sym), source, lapg_m[lapg_head].offset, lapg_m[lapg_head].endoffset), reporter); 
				break;
			case 27:  // parts ::= parts part
				 lapg_gg.sym = RegexUtil.createSequence(((RegexAstPart)lapg_m[lapg_head - 1].sym), ((RegexAstPart)lapg_m[lapg_head].sym)); 
				break;
		}
	}
}
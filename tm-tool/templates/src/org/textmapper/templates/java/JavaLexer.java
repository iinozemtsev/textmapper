package org.textmapper.templates.java;

import java.io.IOException;
import java.io.Reader;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

public class JavaLexer {

	public static class Span {
		public Object value;
		public int symbol;
		public int state;
		public int line;
		public int offset;
		public int endoffset;
	}

	public interface Tokens {
		int Unavailable_ = -1;
		int eoi = 0;
		int WhiteSpace = 1;
		int EndOfLineComment = 2;
		int TraditionalComment = 3;
		int Identifier = 4;
		int kw_abstract = 5;
		int kw_assert = 6;
		int kw_boolean = 7;
		int kw_break = 8;
		int kw_byte = 9;
		int kw_case = 10;
		int kw_catch = 11;
		int kw_char = 12;
		int kw_class = 13;
		int kw_const = 14;
		int kw_continue = 15;
		int kw_default = 16;
		int kw_do = 17;
		int kw_double = 18;
		int kw_else = 19;
		int kw_enum = 20;
		int kw_extends = 21;
		int kw_final = 22;
		int kw_finally = 23;
		int kw_float = 24;
		int kw_for = 25;
		int kw_goto = 26;
		int kw_if = 27;
		int kw_implements = 28;
		int kw_import = 29;
		int kw_instanceof = 30;
		int kw_int = 31;
		int kw_interface = 32;
		int kw_long = 33;
		int kw_native = 34;
		int kw_new = 35;
		int kw_package = 36;
		int kw_private = 37;
		int kw_protected = 38;
		int kw_public = 39;
		int kw_return = 40;
		int kw_short = 41;
		int kw_static = 42;
		int kw_strictfp = 43;
		int kw_super = 44;
		int kw_switch = 45;
		int kw_synchronized = 46;
		int kw_this = 47;
		int kw_throw = 48;
		int kw_throws = 49;
		int kw_transient = 50;
		int kw_try = 51;
		int kw_void = 52;
		int kw_volatile = 53;
		int kw_while = 54;
		int IntegerLiteral = 55;
		int FloatingPointLiteral = 56;
		int BooleanLiteral = 57;
		int CharacterLiteral = 58;
		int StringLiteral = 59;
		int NullLiteral = 60;
		int Lparen = 61;
		int Rparen = 62;
		int Lbrace = 63;
		int Rbrace = 64;
		int Lbrack = 65;
		int Rbrack = 66;
		int Semicolon = 67;
		int Comma = 68;
		int Dot = 69;
		int DotDotDot = 70;
		int Assign = 71;
		int Gt = 72;
		int Lt = 73;
		int Excl = 74;
		int Tilde = 75;
		int Quest = 76;
		int Colon = 77;
		int AssignAssign = 78;
		int LtAssign = 79;
		int GtAssign = 80;
		int ExclAssign = 81;
		int AndAnd = 82;
		int OrOr = 83;
		int PlusPlus = 84;
		int MinusMinus = 85;
		int Plus = 86;
		int Minus = 87;
		int Mult = 88;
		int Div = 89;
		int And = 90;
		int Or = 91;
		int Xor = 92;
		int Rem = 93;
		int LtLt = 94;
		int GtGt = 95;
		int GtGtGt = 96;
		int PlusAssign = 97;
		int MinusAssign = 98;
		int MultAssign = 99;
		int DivAssign = 100;
		int AndAssign = 101;
		int OrAssign = 102;
		int XorAssign = 103;
		int RemAssign = 104;
		int LtLtAssign = 105;
		int GtGtAssign = 106;
		int GtGtGtAssign = 107;
		int Atsign = 108;
	}

	public interface ErrorReporter {
		void error(String message, int line, int offset, int endoffset);
	}

	public static final int TOKEN_SIZE = 2048;

	private Reader stream;
	final private ErrorReporter reporter;

	private CharSequence input;
	private int tokenOffset;
	private int l;
	private int charOffset;
	private int chr;

	private int state;

	private int tokenLine;
	private int currLine;
	private int currOffset;

	public JavaLexer(CharSequence input, ErrorReporter reporter) throws IOException {
		this.reporter = reporter;
		reset(input);
	}

	public void reset(CharSequence input) throws IOException {
		this.state = 0;
		tokenLine = currLine = 1;
		currOffset = 0;
		this.input = input;
		tokenOffset = l = 0;
		charOffset = l;
		chr = l < input.length() ? input.charAt(l++) : -1;
		if (chr >= Character.MIN_HIGH_SURROGATE && chr <= Character.MAX_HIGH_SURROGATE && l < input.length() &&
				Character.isLowSurrogate(input.charAt(l))) {
			chr = Character.toCodePoint((char) chr, input.charAt(l++));
		}
	}

	protected void advance() {
		if (chr == -1) return;
		currOffset += l - charOffset;
		if (chr == '\n') {
			currLine++;
		}
		charOffset = l;
		chr = l < input.length() ? input.charAt(l++) : -1;
		if (chr >= Character.MIN_HIGH_SURROGATE && chr <= Character.MAX_HIGH_SURROGATE && l < input.length() &&
				Character.isLowSurrogate(input.charAt(l))) {
			chr = Character.toCodePoint((char) chr, input.charAt(l++));
		}
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public int getTokenLine() {
		return tokenLine;
	}

	public int getLine() {
		return currLine;
	}

	public void setLine(int currLine) {
		this.currLine = currLine;
	}

	public int getOffset() {
		return currOffset;
	}

	public void setOffset(int currOffset) {
		this.currOffset = currOffset;
	}

	public String tokenText() {
		return input.subSequence(tokenOffset, charOffset).toString();
	}

	public int tokenSize() {
		return charOffset - tokenOffset;
	}

	private static final char[] tmCharClass = unpack_vc_char(195102,
		"\11\1\1\50\1\6\1\1\1\50\1\5\14\1\1\4\5\1\1\50\1\31\1\15\1\1\1\44\1\42\1\35\1\14\1" +
		"\16\1\17\1\10\1\37\1\25\1\40\1\13\1\7\1\11\1\55\2\64\4\53\2\45\1\34\1\24\1\30\1\26" +
		"\1\27\1\33\1\43\1\46\1\54\1\46\1\57\1\56\1\57\5\44\1\51\3\44\1\60\7\44\1\52\2\44" +
		"\1\22\1\2\1\23\1\41\1\12\1\1\1\46\1\62\1\46\1\57\1\56\1\63\5\44\1\51\1\44\1\61\1" +
		"\44\1\60\1\44\1\61\1\44\1\61\1\3\2\44\1\52\2\44\1\20\1\36\1\21\1\32\53\1\1\44\12" +
		"\1\1\44\4\1\1\44\5\1\27\44\1\1\37\44\1\1\u01ca\44\4\1\14\44\16\1\5\44\7\1\1\44\1" +
		"\1\1\44\201\1\5\44\1\1\2\44\2\1\4\44\1\1\1\44\6\1\1\44\1\1\3\44\1\1\1\44\1\1\24\44" +
		"\1\1\123\44\1\1\213\44\10\1\246\44\1\1\46\44\2\1\1\44\7\1\47\44\110\1\33\44\5\1\3" +
		"\44\55\1\53\44\25\1\12\47\4\1\2\44\1\1\143\44\1\1\1\44\17\1\2\44\7\1\2\44\12\47\3" +
		"\44\2\1\1\44\20\1\1\44\1\1\36\44\35\1\131\44\13\1\1\44\16\1\12\47\41\44\11\1\2\44" +
		"\4\1\1\44\5\1\26\44\4\1\1\44\11\1\1\44\3\1\1\44\27\1\31\44\107\1\25\44\117\1\66\44" +
		"\3\1\1\44\22\1\1\44\7\1\12\44\4\1\12\47\1\1\20\44\4\1\10\44\2\1\2\44\2\1\26\44\1" +
		"\1\7\44\1\1\1\44\3\1\4\44\3\1\1\44\20\1\1\44\15\1\2\44\1\1\3\44\4\1\12\47\2\44\23" +
		"\1\6\44\4\1\2\44\2\1\26\44\1\1\7\44\1\1\2\44\1\1\2\44\1\1\2\44\37\1\4\44\1\1\1\44" +
		"\7\1\12\47\2\1\3\44\20\1\11\44\1\1\3\44\1\1\26\44\1\1\7\44\1\1\2\44\1\1\5\44\3\1" +
		"\1\44\22\1\1\44\17\1\2\44\4\1\12\47\11\1\1\44\13\1\10\44\2\1\2\44\2\1\26\44\1\1\7" +
		"\44\1\1\2\44\1\1\5\44\3\1\1\44\36\1\2\44\1\1\3\44\4\1\12\47\1\1\1\44\21\1\1\44\1" +
		"\1\6\44\3\1\3\44\1\1\4\44\3\1\2\44\1\1\1\44\1\1\2\44\3\1\2\44\3\1\3\44\3\1\14\44" +
		"\26\1\1\44\25\1\12\47\25\1\10\44\1\1\3\44\1\1\27\44\1\1\20\44\3\1\1\44\32\1\3\44" +
		"\5\1\2\44\4\1\12\47\25\1\10\44\1\1\3\44\1\1\27\44\1\1\12\44\1\1\5\44\3\1\1\44\40" +
		"\1\1\44\1\1\2\44\4\1\12\47\1\1\2\44\22\1\10\44\1\1\3\44\1\1\51\44\2\1\1\44\20\1\1" +
		"\44\20\1\3\44\4\1\12\47\12\1\6\44\5\1\22\44\3\1\30\44\1\1\11\44\1\1\1\44\2\1\7\44" +
		"\37\1\12\47\21\1\60\44\1\1\2\44\14\1\7\44\11\1\12\47\47\1\2\44\1\1\1\44\2\1\2\44" +
		"\1\1\1\44\2\1\1\44\6\1\4\44\1\1\7\44\1\1\3\44\1\1\1\44\1\1\1\44\2\1\2\44\1\1\4\44" +
		"\1\1\2\44\11\1\1\44\2\1\5\44\1\1\1\44\11\1\12\47\2\1\4\44\40\1\1\44\37\1\12\47\26" +
		"\1\10\44\1\1\44\44\33\1\5\44\163\1\53\44\24\1\1\44\12\47\6\1\6\44\4\1\4\44\3\1\1" +
		"\44\3\1\2\44\7\1\3\44\4\1\15\44\14\1\1\44\1\1\12\47\6\1\46\44\1\1\1\44\5\1\1\44\2" +
		"\1\53\44\1\1\u014d\44\1\1\4\44\2\1\7\44\1\1\1\44\1\1\4\44\2\1\51\44\1\1\4\44\2\1" +
		"\41\44\1\1\4\44\2\1\7\44\1\1\1\44\1\1\4\44\2\1\17\44\1\1\71\44\1\1\4\44\2\1\103\44" +
		"\45\1\20\44\20\1\126\44\2\1\6\44\3\1\u026c\44\2\1\21\44\1\1\32\44\5\1\113\44\3\1" +
		"\13\44\7\1\15\44\1\1\4\44\16\1\22\44\16\1\22\44\16\1\15\44\1\1\3\44\17\1\64\44\43" +
		"\1\1\44\4\1\1\44\3\1\12\47\46\1\12\47\6\1\130\44\10\1\51\44\1\1\1\44\5\1\106\44\12" +
		"\1\37\44\47\1\12\47\36\44\2\1\5\44\13\1\54\44\4\1\32\44\6\1\12\47\46\1\27\44\11\1" +
		"\65\44\53\1\12\47\6\1\12\47\15\1\1\44\135\1\57\44\21\1\7\44\4\1\12\47\51\1\36\44" +
		"\15\1\2\44\12\47\54\44\32\1\44\44\34\1\12\47\3\1\3\44\12\47\44\44\153\1\4\44\1\1" +
		"\4\44\3\1\2\44\11\1\300\44\100\1\u0116\44\2\1\6\44\2\1\46\44\2\1\6\44\2\1\10\44\1" +
		"\1\1\44\1\1\1\44\1\1\1\44\1\1\37\44\2\1\65\44\1\1\7\44\1\1\1\44\3\1\3\44\1\1\7\44" +
		"\3\1\4\44\2\1\6\44\4\1\15\44\5\1\3\44\1\1\7\44\164\1\1\44\15\1\1\44\20\1\15\44\145" +
		"\1\1\44\4\1\1\44\2\1\12\44\1\1\1\44\3\1\5\44\6\1\1\44\1\1\1\44\1\1\1\44\1\1\4\44" +
		"\1\1\13\44\2\1\4\44\5\1\5\44\4\1\1\44\21\1\51\44\u0a77\1\57\44\1\1\57\44\1\1\205" +
		"\44\6\1\4\44\3\1\2\44\14\1\46\44\1\1\1\44\5\1\1\44\2\1\70\44\7\1\1\44\20\1\27\44" +
		"\11\1\7\44\1\1\7\44\1\1\7\44\1\1\7\44\1\1\7\44\1\1\7\44\1\1\7\44\1\1\7\44\120\1\1" +
		"\44\u01d5\1\3\44\31\1\11\44\7\1\5\44\2\1\5\44\4\1\126\44\6\1\3\44\1\1\132\44\1\1" +
		"\4\44\5\1\51\44\3\1\136\44\21\1\33\44\65\1\20\44\u0200\1\u19b6\44\112\1\u51d6\44" +
		"\52\1\u048d\44\103\1\56\44\2\1\u010d\44\3\1\20\44\12\47\2\44\24\1\57\44\20\1\37\44" +
		"\2\1\120\44\47\1\11\44\2\1\147\44\2\1\43\44\2\1\10\44\77\1\13\44\1\1\3\44\1\1\4\44" +
		"\1\1\27\44\35\1\64\44\16\1\62\44\34\1\12\47\30\1\6\44\3\1\1\44\1\1\1\44\2\1\12\47" +
		"\34\44\12\1\27\44\31\1\35\44\7\1\57\44\34\1\1\44\12\47\6\1\5\44\1\1\12\44\12\47\5" +
		"\44\1\1\51\44\27\1\3\44\1\1\10\44\4\1\12\47\6\1\27\44\3\1\1\44\3\1\62\44\1\1\1\44" +
		"\3\1\2\44\2\1\5\44\2\1\1\44\1\1\1\44\30\1\3\44\2\1\13\44\7\1\3\44\14\1\6\44\2\1\6" +
		"\44\2\1\6\44\11\1\7\44\1\1\7\44\1\1\53\44\1\1\12\44\12\1\163\44\15\1\12\47\6\1\u2ba4" +
		"\44\14\1\27\44\4\1\61\44\u2104\1\u016e\44\2\1\152\44\46\1\7\44\14\1\5\44\5\1\1\44" +
		"\1\1\12\44\1\1\15\44\1\1\5\44\1\1\1\44\1\1\2\44\1\1\2\44\1\1\154\44\41\1\u016b\44" +
		"\22\1\100\44\2\1\66\44\50\1\14\44\164\1\5\44\1\1\207\44\23\1\12\47\7\1\32\44\6\1" +
		"\32\44\13\1\131\44\3\1\6\44\2\1\6\44\2\1\6\44\2\1\3\44\43\1\14\44\1\1\32\44\1\1\23" +
		"\44\1\1\2\44\1\1\17\44\2\1\16\44\42\1\173\44\105\1\65\44\u010b\1\35\44\3\1\61\44" +
		"\57\1\40\44\20\1\33\44\5\1\46\44\12\1\36\44\2\1\44\44\4\1\10\44\1\1\5\44\52\1\236" +
		"\44\2\1\12\47\126\1\50\44\10\1\64\44\234\1\u0137\44\11\1\26\44\12\1\10\44\230\1\6" +
		"\44\2\1\1\44\1\1\54\44\1\1\2\44\3\1\1\44\2\1\27\44\12\1\27\44\11\1\37\44\101\1\23" +
		"\44\1\1\2\44\12\1\26\44\12\1\32\44\106\1\70\44\6\1\2\44\100\1\1\44\17\1\4\44\1\1" +
		"\3\44\1\1\33\44\54\1\35\44\3\1\35\44\43\1\10\44\1\1\34\44\33\1\66\44\12\1\26\44\12" +
		"\1\23\44\15\1\22\44\156\1\111\44\67\1\63\44\15\1\63\44\u0310\1\65\44\56\1\12\47\23" +
		"\1\55\44\40\1\31\44\7\1\12\47\11\1\44\44\17\1\12\47\20\1\43\44\3\1\1\44\14\1\60\44" +
		"\16\1\4\44\13\1\12\47\1\44\1\1\1\44\43\1\22\44\1\1\31\44\124\1\7\44\1\1\1\44\1\1" +
		"\4\44\1\1\17\44\1\1\12\44\7\1\57\44\21\1\12\47\13\1\10\44\2\1\2\44\2\1\26\44\1\1" +
		"\7\44\1\1\2\44\1\1\5\44\3\1\1\44\22\1\1\44\14\1\5\44\u011e\1\60\44\24\1\2\44\1\1" +
		"\1\44\10\1\12\47\246\1\57\44\51\1\4\44\44\1\60\44\24\1\1\44\13\1\12\47\46\1\53\44" +
		"\25\1\12\47\66\1\32\44\26\1\12\47\u0166\1\100\44\12\47\25\1\1\44\u01c0\1\71\44\u0507" +
		"\1\u039a\44\146\1\157\44\21\1\304\44\u0abc\1\u042f\44\u0fd1\1\u0247\44\u21b9\1\u0239" +
		"\44\7\1\37\44\1\1\12\47\146\1\36\44\22\1\60\44\20\1\4\44\14\1\12\47\11\1\25\44\5" +
		"\1\23\44\u0370\1\105\44\13\1\1\44\102\1\15\44\u4060\1\2\44\u0bfe\1\153\44\5\1\15" +
		"\44\3\1\11\44\7\1\12\44\u1766\1\125\44\1\1\107\44\1\1\2\44\2\1\1\44\2\1\2\44\2\1" +
		"\4\44\1\1\14\44\1\1\1\44\1\1\7\44\1\1\101\44\1\1\4\44\2\1\10\44\1\1\7\44\1\1\34\44" +
		"\1\1\4\44\1\1\5\44\1\1\1\44\3\1\7\44\1\1\u0154\44\2\1\31\44\1\1\31\44\1\1\37\44\1" +
		"\1\31\44\1\1\37\44\1\1\31\44\1\1\37\44\1\1\31\44\1\1\37\44\1\1\31\44\1\1\10\44\2" +
		"\1\62\47\u1000\1\305\44\u053b\1\4\44\1\1\33\44\1\1\2\44\1\1\1\44\2\1\1\44\1\1\12" +
		"\44\1\1\4\44\1\1\1\44\1\1\1\44\6\1\1\44\4\1\1\44\1\1\1\44\1\1\1\44\1\1\3\44\1\1\2" +
		"\44\1\1\1\44\2\1\1\44\1\1\1\44\1\1\1\44\1\1\1\44\1\1\1\44\1\1\2\44\1\1\1\44\2\1\4" +
		"\44\1\1\7\44\1\1\4\44\1\1\4\44\1\1\1\44\1\1\12\44\1\1\21\44\5\1\3\44\1\1\5\44\1\1" +
		"\21\44\u1144\1\ua6d7\44\51\1\u1035\44\13\1\336\44\2\1\u1682\44\u295e\1\u021e\44");

	private static char[] unpack_vc_char(int size, String... st) {
		char[] res = new char[size];
		int t = 0;
		int count = 0;
		for (String s : st) {
			int slen = s.length();
			for (int i = 0; i < slen; ) {
				count = i > 0 || count == 0 ? s.charAt(i++) : count;
				if (i < slen) {
					char val = s.charAt(i++);
					while (count-- > 0) res[t++] = val;
				}
			}
		}
		assert res.length == t;
		return res;
	}

	private static final int[] tmRuleSymbol = unpack_int(116,
		"\4\0\0\0\1\0\2\0\3\0\5\0\6\0\7\0\10\0\11\0\12\0\13\0\14\0\15\0\16\0\17\0\20\0\21" +
		"\0\22\0\23\0\24\0\25\0\26\0\27\0\30\0\31\0\32\0\33\0\34\0\35\0\36\0\37\0\40\0\41" +
		"\0\42\0\43\0\44\0\45\0\46\0\47\0\50\0\51\0\52\0\53\0\54\0\55\0\56\0\57\0\60\0\61" +
		"\0\62\0\63\0\64\0\65\0\66\0\67\0\67\0\67\0\67\0\70\0\70\0\70\0\70\0\71\0\71\0\72" +
		"\0\73\0\74\0\75\0\76\0\77\0\100\0\101\0\102\0\103\0\104\0\105\0\106\0\107\0\110\0" +
		"\111\0\112\0\113\0\114\0\115\0\116\0\117\0\120\0\121\0\122\0\123\0\124\0\125\0\126" +
		"\0\127\0\130\0\131\0\132\0\133\0\134\0\135\0\136\0\137\0\140\0\141\0\142\0\143\0" +
		"\144\0\145\0\146\0\147\0\150\0\151\0\152\0\153\0\154\0");

	private static final int tmClassesCount = 53;

	private static final short[] tmGoto = unpack_vc_short(6996,
		"\1\ufffe\1\uffff\1\177\1\171\1\170\1\167\1\166\1\160\1\156\1\126\1\171\1\121\1\107" +
		"\1\76\1\75\1\74\1\73\1\72\1\71\1\70\1\67\1\66\1\64\1\56\1\52\1\50\1\47\1\46\1\45" +
		"\1\42\1\37\1\34\1\31\1\27\1\25\1\24\1\171\1\1\1\171\1\uffff\1\166\2\171\1\1\1\171" +
		"\1\1\6\171\1\1\11\uffc6\1\22\1\21\1\11\31\uffc6\1\22\3\uffc6\1\10\1\uffc6\1\22\1" +
		"\uffc6\1\22\1\3\1\2\3\uffc6\1\2\1\22\65\uffc0\11\uffff\1\5\25\uffff\2\4\4\uffff\1" +
		"\5\5\uffff\1\5\1\uffff\1\5\6\uffff\1\5\11\uffff\1\5\33\uffff\1\5\5\uffff\1\5\1\uffff" +
		"\1\5\6\uffff\1\5\11\uffc1\1\5\1\7\32\uffc1\1\5\5\uffc1\1\5\1\uffc1\1\5\1\uffc1\1" +
		"\6\3\uffc1\1\6\1\5\65\uffc1\11\uffff\1\5\1\7\32\uffff\1\5\5\uffff\1\5\1\uffff\1\5" +
		"\6\uffff\1\5\65\uffc6\11\uffc2\1\17\33\uffc2\1\17\5\uffc2\1\17\1\uffc2\1\17\1\13" +
		"\1\12\3\uffc2\1\12\1\17\65\uffc2\11\uffff\1\15\25\uffff\2\14\4\uffff\1\15\5\uffff" +
		"\1\15\1\uffff\1\15\6\uffff\1\15\11\uffff\1\15\33\uffff\1\15\5\uffff\1\15\1\uffff" +
		"\1\15\6\uffff\1\15\11\uffc2\1\15\1\16\32\uffc2\1\15\5\uffc2\1\15\1\uffc2\1\15\1\uffc2" +
		"\1\12\3\uffc2\1\12\1\15\11\uffff\1\15\1\16\32\uffff\1\15\5\uffff\1\15\1\uffff\1\15" +
		"\6\uffff\1\15\11\uffc2\1\17\1\20\32\uffc2\1\17\5\uffc2\1\17\1\uffc2\1\17\1\13\1\12" +
		"\3\uffc2\1\12\1\17\11\uffff\1\17\1\20\32\uffff\1\17\5\uffff\1\17\1\uffff\1\17\6\uffff" +
		"\1\17\11\uffff\1\22\1\21\32\uffff\1\22\5\uffff\1\22\1\uffff\1\22\6\uffff\1\22\11" +
		"\uffc6\1\22\1\23\1\11\31\uffc6\1\22\3\uffc6\1\10\1\uffc6\1\22\1\uffc6\1\22\1\3\1" +
		"\2\3\uffc6\1\2\1\22\11\uffff\1\22\1\23\32\uffff\1\22\5\uffff\1\22\1\uffff\1\22\6" +
		"\uffff\1\22\65\uff8a\26\uff99\1\26\36\uff99\65\uff8e\26\uff9a\1\30\36\uff9a\65\uff8f" +
		"\26\uff9f\1\33\11\uff9f\1\32\24\uff9f\65\uffa1\65\uff94\26\uffa0\1\36\10\uffa0\1" +
		"\35\25\uffa0\65\uffa2\65\uff95\26\uff9b\1\41\7\uff9b\1\40\26\uff9b\65\uffa3\65\uff90" +
		"\26\uff9c\1\44\6\uff9c\1\43\27\uff9c\65\uffa4\65\uff91\65\uffa9\65\uffaa\65\uffab" +
		"\26\uffac\1\51\36\uffac\65\uffa5\26\uffad\1\55\1\uffad\1\53\34\uffad\26\uff98\1\54" +
		"\36\uff98\65\uff8d\65\uffa7\26\uffae\1\63\1\57\35\uffae\26\uff97\1\62\1\60\35\uff97" +
		"\26\uff96\1\61\36\uff96\65\uff8b\65\uff8c\65\uffa6\26\uffaf\1\65\36\uffaf\65\uffa8" +
		"\65\uffb2\65\uffb3\65\uffb4\65\uffb5\65\uffb6\65\uffb7\65\uffb8\65\uffb9\1\uffff" +
		"\1\76\1\100\2\76\2\uffff\6\76\1\77\47\76\65\uffbb\2\uffff\1\76\1\103\5\uffff\1\102" +
		"\2\uffff\2\76\35\uffff\1\101\1\uffff\1\102\3\uffff\3\76\1\102\1\uffff\1\76\1\100" +
		"\2\76\2\uffff\6\76\1\77\47\76\1\uffff\1\76\1\100\2\76\2\uffff\2\76\1\101\3\76\1\77" +
		"\35\76\1\101\1\76\1\101\6\76\1\101\3\uffff\1\103\5\uffff\1\104\33\uffff\2\104\4\uffff" +
		"\5\104\2\uffff\3\104\11\uffff\1\105\33\uffff\2\105\4\uffff\5\105\2\uffff\3\105\11" +
		"\uffff\1\106\33\uffff\2\106\4\uffff\5\106\2\uffff\3\106\11\uffff\1\76\33\uffff\2" +
		"\76\4\uffff\5\76\2\uffff\3\76\1\uffff\1\120\1\110\2\120\2\uffff\5\120\1\uffff\50" +
		"\120\2\uffff\1\120\1\114\5\uffff\1\113\2\uffff\2\120\35\uffff\1\111\1\uffff\1\113" +
		"\3\uffff\3\120\1\113\11\uffff\1\120\2\uffff\1\112\36\uffff\1\120\1\uffff\1\120\6" +
		"\uffff\1\120\65\uffbc\11\uffff\1\111\2\uffff\1\112\36\uffff\1\111\1\uffff\1\111\6" +
		"\uffff\1\111\3\uffff\1\114\5\uffff\1\115\33\uffff\2\115\4\uffff\5\115\2\uffff\3\115" +
		"\11\uffff\1\116\33\uffff\2\116\4\uffff\5\116\2\uffff\3\116\11\uffff\1\117\33\uffff" +
		"\2\117\4\uffff\5\117\2\uffff\3\117\11\uffff\1\120\33\uffff\2\120\4\uffff\5\120\2" +
		"\uffff\3\120\14\uffff\1\112\50\uffff\11\uffb1\1\124\1\uffb1\1\122\31\uffb1\1\124" +
		"\5\uffb1\1\124\1\uffb1\1\124\6\uffb1\1\124\13\uffff\1\123\51\uffff\65\uffb0\11\uffc2" +
		"\1\124\1\125\32\uffc2\1\124\5\uffc2\1\124\1\uffc2\1\124\1\13\1\12\3\uffc2\1\12\1" +
		"\124\11\uffff\1\124\1\125\32\uffff\1\124\5\uffff\1\124\1\uffff\1\124\6\uffff\1\124" +
		"\11\uffc6\1\153\1\152\1\11\31\uffc6\1\150\3\uffc6\1\10\1\133\1\153\1\127\1\153\1" +
		"\3\1\2\2\uffc6\1\127\1\2\1\153\11\uffff\1\130\43\uffff\1\130\7\uffff\11\uffc3\1\130" +
		"\1\132\36\uffc3\1\131\3\uffc3\1\130\74\uffc3\11\uffff\1\130\1\132\42\uffff\1\130" +
		"\20\uffff\1\144\1\uffff\1\134\31\uffff\2\144\4\uffff\5\144\2\uffff\3\144\11\uffff" +
		"\1\135\33\uffff\2\135\4\uffff\5\135\2\uffff\3\135\11\uffff\1\135\1\143\32\uffff\2" +
		"\135\4\uffff\5\135\1\136\1\uffff\3\135\11\uffff\1\140\25\uffff\2\137\4\uffff\1\140" +
		"\5\uffff\1\140\1\uffff\1\140\6\uffff\1\140\11\uffff\1\140\33\uffff\1\140\5\uffff" +
		"\1\140\1\uffff\1\140\6\uffff\1\140\11\uffbf\1\140\1\142\32\uffbf\1\140\5\uffbf\1" +
		"\140\1\uffbf\1\140\1\uffbf\1\141\3\uffbf\1\141\1\140\65\uffbf\11\uffff\1\140\1\142" +
		"\32\uffff\1\140\5\uffff\1\140\1\uffff\1\140\6\uffff\1\140\11\uffff\1\135\1\143\32" +
		"\uffff\2\135\4\uffff\5\135\2\uffff\3\135\11\uffc5\1\144\1\147\1\146\31\uffc5\2\144" +
		"\2\uffc5\1\145\1\uffc5\5\144\1\136\1\uffc5\3\144\65\uffc5\11\uffff\1\135\33\uffff" +
		"\2\135\4\uffff\5\135\1\136\1\uffff\3\135\11\uffff\1\144\1\147\32\uffff\2\144\4\uffff" +
		"\5\144\2\uffff\3\144\11\uffff\1\150\1\151\1\11\31\uffff\1\150\5\uffff\1\150\1\uffff" +
		"\1\150\1\3\1\2\3\uffff\1\2\1\150\11\uffff\1\150\1\151\32\uffff\1\150\5\uffff\1\150" +
		"\1\uffff\1\150\6\uffff\1\150\11\uffff\1\153\1\152\32\uffff\1\150\5\uffff\1\153\1" +
		"\uffff\1\153\6\uffff\1\153\11\uffc4\1\153\1\155\1\11\31\uffc4\1\150\3\uffc4\1\154" +
		"\1\uffc4\1\153\1\uffc4\1\153\1\3\1\2\3\uffc4\1\2\1\153\65\uffc4\11\uffff\1\153\1" +
		"\155\32\uffff\1\150\5\uffff\1\153\1\uffff\1\153\6\uffff\1\153\26\uff9e\1\157\36\uff9e" +
		"\65\uff93\7\uff9d\1\165\1\162\15\uff9d\1\161\36\uff9d\65\uff92\1\uffff\7\162\1\163" +
		"\54\162\1\uffff\6\162\1\164\1\163\54\162\65\ufff9\1\ufffa\4\165\2\ufffa\56\165\73" +
		"\ufffb\1\166\56\ufffb\65\ufffc\2\ufffd\1\172\1\171\5\ufffd\2\171\31\ufffd\4\171\1" +
		"\ufffd\14\171\3\uffff\1\173\64\uffff\1\173\5\uffff\1\174\33\uffff\2\174\4\uffff\5" +
		"\174\2\uffff\3\174\11\uffff\1\175\33\uffff\2\175\4\uffff\5\175\2\uffff\3\175\11\uffff" +
		"\1\176\33\uffff\2\176\4\uffff\5\176\2\uffff\3\176\11\uffff\1\171\33\uffff\2\171\4" +
		"\uffff\5\171\2\uffff\3\171\3\uffff\1\200\64\uffff\1\200\5\uffff\1\201\33\uffff\2" +
		"\201\4\uffff\5\201\2\uffff\3\201\11\uffff\1\202\33\uffff\2\202\4\uffff\5\202\2\uffff" +
		"\3\202\11\uffff\1\203\33\uffff\2\203\4\uffff\5\203\2\uffff\3\203\11\uffff\1\171\33" +
		"\uffff\2\171\4\uffff\5\171\2\uffff\3\171");

	private static short[] unpack_vc_short(int size, String... st) {
		short[] res = new short[size];
		int t = 0;
		int count = 0;
		for (String s : st) {
			int slen = s.length();
			for (int i = 0; i < slen; ) {
				count = i > 0 || count == 0 ? s.charAt(i++) : count;
				if (i < slen) {
					short val = (short) s.charAt(i++);
					while (count-- > 0) res[t++] = val;
				}
			}
		}
		assert res.length == t;
		return res;
	}

	private static int mapCharacter(int chr) {
		if (chr >= 0 && chr < 195102) return tmCharClass[chr];
		return chr == -1 ? 0 : 1;
	}

	public Span next() throws IOException {
		Span token = new Span();
		int state;

		tokenloop:
		do {
			token.offset = currOffset;
			tokenLine = token.line = currLine;
			tokenOffset = charOffset;

			for (state = this.state; state >= 0; ) {
				state = tmGoto[state * tmClassesCount + mapCharacter(chr)];
				if (state == -1 && chr == -1) {
					token.endoffset = currOffset;
					token.symbol = 0;
					token.value = null;
					reporter.error("Unexpected end of input reached", token.line, token.offset, token.endoffset);
					token.offset = currOffset;
					break tokenloop;
				}
				if (state >= -1 && chr != -1) {
					currOffset += l - charOffset;
					if (chr == '\n') {
						currLine++;
					}
					charOffset = l;
					chr = l < input.length() ? input.charAt(l++) : -1;
					if (chr >= Character.MIN_HIGH_SURROGATE && chr <= Character.MAX_HIGH_SURROGATE && l < input.length() &&
							Character.isLowSurrogate(input.charAt(l))) {
						chr = Character.toCodePoint((char) chr, input.charAt(l++));
					}
				}
			}
			token.endoffset = currOffset;

			if (state == -1) {
				reporter.error(MessageFormat.format("invalid lexeme at line {0}: `{1}`, skipped", currLine, tokenText()), token.line, token.offset, token.endoffset);
				token.symbol = -1;
				continue;
			}

			if (state == -2) {
				token.symbol = Tokens.eoi;
				token.value = null;
				break tokenloop;
			}

			token.symbol = tmRuleSymbol[-state - 3];
			token.value = null;

		} while (token.symbol == -1 || !createToken(token, -state - 3));
		return token;
	}

	protected int charAt(int i) {
		if (i == 0) return chr;
		i += l - 1;
		int res = i < input.length() ? input.charAt(i++) : -1;
		if (res >= Character.MIN_HIGH_SURROGATE && res <= Character.MAX_HIGH_SURROGATE && i < input.length() &&
				Character.isLowSurrogate(input.charAt(i))) {
			res = Character.toCodePoint((char) res, input.charAt(i++));
		}
		return res;
	}

	protected boolean createToken(Span token, int ruleIndex) throws IOException {
		boolean spaceToken = false;
		switch (ruleIndex) {
			case 0:
				return createIdentifierToken(token, ruleIndex);
			case 2: // WhiteSpace: /[\r\n\t\f ]|\r\n/
				spaceToken = true;
				break;
			case 3: // EndOfLineComment: /\/\/[^\r\n]*/
				spaceToken = true;
				break;
			case 4: // TraditionalComment: /\/\*([^*]|\*+[^\/*])*\*+\//
				spaceToken = true;
				break;
		}
		return !(spaceToken);
	}

	private static Map<String,Integer> subTokensOfIdentifier = new HashMap<>();
	static {
		subTokensOfIdentifier.put("abstract", 5);
		subTokensOfIdentifier.put("assert", 6);
		subTokensOfIdentifier.put("boolean", 7);
		subTokensOfIdentifier.put("break", 8);
		subTokensOfIdentifier.put("byte", 9);
		subTokensOfIdentifier.put("case", 10);
		subTokensOfIdentifier.put("catch", 11);
		subTokensOfIdentifier.put("char", 12);
		subTokensOfIdentifier.put("class", 13);
		subTokensOfIdentifier.put("const", 14);
		subTokensOfIdentifier.put("continue", 15);
		subTokensOfIdentifier.put("default", 16);
		subTokensOfIdentifier.put("do", 17);
		subTokensOfIdentifier.put("double", 18);
		subTokensOfIdentifier.put("else", 19);
		subTokensOfIdentifier.put("enum", 20);
		subTokensOfIdentifier.put("extends", 21);
		subTokensOfIdentifier.put("final", 22);
		subTokensOfIdentifier.put("finally", 23);
		subTokensOfIdentifier.put("float", 24);
		subTokensOfIdentifier.put("for", 25);
		subTokensOfIdentifier.put("goto", 26);
		subTokensOfIdentifier.put("if", 27);
		subTokensOfIdentifier.put("implements", 28);
		subTokensOfIdentifier.put("import", 29);
		subTokensOfIdentifier.put("instanceof", 30);
		subTokensOfIdentifier.put("int", 31);
		subTokensOfIdentifier.put("interface", 32);
		subTokensOfIdentifier.put("long", 33);
		subTokensOfIdentifier.put("native", 34);
		subTokensOfIdentifier.put("new", 35);
		subTokensOfIdentifier.put("package", 36);
		subTokensOfIdentifier.put("private", 37);
		subTokensOfIdentifier.put("protected", 38);
		subTokensOfIdentifier.put("public", 39);
		subTokensOfIdentifier.put("return", 40);
		subTokensOfIdentifier.put("short", 41);
		subTokensOfIdentifier.put("static", 42);
		subTokensOfIdentifier.put("strictfp", 43);
		subTokensOfIdentifier.put("super", 44);
		subTokensOfIdentifier.put("switch", 45);
		subTokensOfIdentifier.put("synchronized", 46);
		subTokensOfIdentifier.put("this", 47);
		subTokensOfIdentifier.put("throw", 48);
		subTokensOfIdentifier.put("throws", 49);
		subTokensOfIdentifier.put("transient", 50);
		subTokensOfIdentifier.put("try", 51);
		subTokensOfIdentifier.put("void", 52);
		subTokensOfIdentifier.put("volatile", 53);
		subTokensOfIdentifier.put("while", 54);
		subTokensOfIdentifier.put("true", 63);
		subTokensOfIdentifier.put("false", 64);
		subTokensOfIdentifier.put("null", 67);
	}

	protected boolean createIdentifierToken(Span token, int ruleIndex) {
		Integer replacement = subTokensOfIdentifier.get(tokenText());
		if (replacement != null) {
			ruleIndex = replacement;
			token.symbol = tmRuleSymbol[ruleIndex];
		}
		return true;
	}

	/* package */ static int[] unpack_int(int size, String... st) {
		int[] res = new int[size];
		boolean second = false;
		char first = 0;
		int t = 0;
		for (String s : st) {
			int slen = s.length();
			for (int i = 0; i < slen; i++) {
				if (second) {
					res[t++] = (s.charAt(i) << 16) + first;
				} else {
					first = s.charAt(i);
				}
				second = !second;
			}
		}
		assert !second;
		assert res.length == t;
		return res;
	}

}

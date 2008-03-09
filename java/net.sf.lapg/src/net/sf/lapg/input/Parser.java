// Parser.java

package net.sf.lapg.input;

import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Parser {
	
	private Parser(String inputId, byte[] data, Map<String,String> defaultOptions) {
		this.inputId = inputId;
		this.buff = data;
		this.l = 0;
		
		addLexem(getSymbol(CSyntax.EOI, 1), null, null, null, null, 1);
		options.putAll(defaultOptions);
	}
	
	private static final boolean DEBUG_SYNTAX = false;
	private static final int BITS = 32;
	
	private Map<String,CSymbol> symCash = new HashMap<String,CSymbol>();
	private List<String> errors = new ArrayList<String>();
	
	private List<CSymbol> symbols = new ArrayList<CSymbol>();
	private List<CRule> rules = new ArrayList<CRule>();
	private List<CPrio> prios = new ArrayList<CPrio>();
	private Map<String,String> options = new HashMap<String, String>();
	private List<CLexem> lexems = new ArrayList<CLexem>();
	
	private String inputId;
	private byte[] buff;
	private int l;
	
	private int currentgroups = 1;
	private int deep = 0;
	
	private String rawData(int start, int end) {
		return new String(buff, start, end-start);
	}
	
	private CSymbol getSymbol(String name, int line) {
		CSymbol res = symCash.get(name);
		if( res == null ) {
			res = new CSymbol(name, inputId, line);
			symbols.add(res);
			symCash.put(name,res);
	
			if( name.endsWith(CSyntax.OPTSUFFIX) && name.length() > CSyntax.OPTSUFFIX.length() ) {
				try {
					CSymbol original = getSymbol(name.substring(0, name.length()-CSyntax.OPTSUFFIX.length()), line);
					res.setNonTerminal(null, null, 0);
					addRule(new CRule(Collections.singletonList(original), null, null, inputId, line), res);
					addRule(new CRule(null, null, null, inputId, line), res);
				} catch(ParseException ex) {
					/* should never happen */
				}
			}
		}
		return res;
	}
	
	private void addLexem(CSymbol sym, String type, String regexp, Integer lexprio, CAction command, int line) {
		try {
			sym.setTerminal(type, regexp != null, inputId, line);
			if( regexp != null ) {
				lexems.add(new CLexem(sym,regexp,command,lexprio!=null?lexprio.intValue():0,currentgroups,inputId,line));
			}
		} catch( ParseException ex ) {
			error(ex.getMessage());
		}
	}
	
	private void addNonterm(CSymbol sym, String type, int line ) {
		try {
			sym.setNonTerminal(type, inputId, line);
		} catch( ParseException ex ) {
			error(ex.getMessage());
		}
	}
	
	private void addRule( CRule rule, CSymbol left ) {
		rule.setLeft(left);
		rules.add(rule);
	}
	
	private void addPrio( String prio, List<CSymbol> list, int line ) {
		if( prio.equals("left") ) {
			prios.add(new CPrio(CPrio.LEFT, list,inputId,line));
		} else if( prio.equals("right") ) {
			prios.add(new CPrio(CPrio.RIGHT, list,inputId,line));
		} else if( prio.equals("nonassoc") ) {
			prios.add(new CPrio(CPrio.NONASSOC, list,inputId,line));
		} else {
			error("unknown priority identifier used: `"+prio+"` at " + line);
		}
	}
	
	private void addRuleSymbol(List<CSymbol> list, CAction cmdopt, CSymbol symbol) {
		if( cmdopt != null ) {
			try {
				CSymbol sym = new CSymbol("{}", inputId, 0);
				sym.setNonTerminal(null, inputId, cmdopt.getLine());
				symbols.add(sym);
				addRule(new CRule(null, cmdopt, null, inputId, cmdopt.getLine()), sym);
				list.add(sym);
			} catch( ParseException ex ) {
				error(ex.getMessage());
			}
		}
		list.add(symbol);
	}
	
	private void error( String s ) {
		errors.add(s);
	}
	
	private void propagateTypes() {
		for( CSymbol s : symbols) {
			String name = s.getName();
			if( name.endsWith(CSyntax.OPTSUFFIX) && name.length() > CSyntax.OPTSUFFIX.length() ) {
				CSymbol original = getSymbol(name.substring(0, name.length()-CSyntax.OPTSUFFIX.length()), -1);
				if( original != null && s.getType() == null && original.getType() != null ) {
					s.setType(original.getType());
				}
			}
		}
	}
	
	public static CSyntax process(String inputId, String contents, Map<String,String> defaultOptions) {
		try {
			Parser p = new Parser(inputId, contents.getBytes("utf-8"), defaultOptions);
			if( !p.parse() || !p.errors.isEmpty() ) {
				return new CSyntax(p.errors);
			}
			p.getSymbol(CSyntax.INPUT, 1);
			p.propagateTypes();
			
			String templates = p.l < p.buff.length ? new String(p.buff,p.l,p.buff.length-p.l,"utf-8") : null;
			return new CSyntax(p.symbols,p.rules,p.prios,p.options,p.lexems,templates);
		} catch( UnsupportedEncodingException ex ) {
			return null;
		}
	}
	
	static class ParseException extends Exception {
		private static final long serialVersionUID = 2811939050284758826L;
	
		public ParseException(String arg0) {
			super(arg0);
		}
	}

	public class lapg_place {
		public int line, offset;

		public lapg_place( int line, int offset ) {
			this.line = line;
			this.offset = offset;
		}
	};

	public class lapg_symbol {
		public Object sym;
		public int  lexem, state;
		public lapg_place pos;
		public lapg_place endpos;
	};

	private static final short[] lapg_char2no = new short[] {
		   0,   1,   1,   1,   1,   1,   1,   1,   1,   2,   3,   1,   1,   4,   1,   1,
		   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,
		   5,   1,   6,   7,   1,   8,   1,   9,  10,  11,   1,   1,   1,  12,  13,  14,
		  15,  16,  17,  18,  19,  20,  21,  22,  23,  24,  25,  26,  27,  28,   1,  29,
		   1,  30,  31,  32,  33,  34,  35,  36,  37,  38,  39,  40,  41,  42,  43,  44,
		  45,  46,  47,  48,  49,  50,  51,  52,  53,  54,  55,  56,  57,  58,   1,  59,
		   1,  60,  61,  62,  63,  64,  65,  66,  67,  68,  69,  70,  71,  72,  73,  74,
		  75,  76,  77,  78,  79,  80,  81,  82,  83,  84,  85,  86,  87,  88,   1,   1,
		   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,
		   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,
		   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,
		   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,
		   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,
		   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,
		   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,
		   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,
	};

	private static final short[][] lapg_lexem = new short[][] {
		{  -2,  -1,   2,   3,   2,   2,   4,   5,  -1,   6,   7,  -1,   8,   9,  10,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  12,  13,  14,  -1,  -1,  15,  15,  15,  15,  15,  15,  15,  15,  15,  15,  15,  15,  15,  15,  15,  15,  15,  15,  15,  15,  15,  15,  15,  15,  15,  15,  16,  -1,  17,  15,  15,  15,  15,  15,  15,  15,  15,  15,  15,  15,  15,  15,  15,  15,  15,  15,  15,  15,  15,  15,  15,  15,  15,  15,  15,  15,  18,  19,  -1, },
		{  -1,  20,  20,  20,  20,  20,  21,  20,  20,  22,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  23,  20,  24, },
		{  -9,  -9,   2,  -9,   2,   2,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9, },
		{  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  25,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9, },
		{  -1,   4,   4,  -1,   4,   4,  26,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,  -1,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4,   4, },
		{  -9,   5,   5,  -9,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5, },
		{  -1,  27,  27,  -1,  27,  27,  27,  27,  27,  -1,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27, },
		{  -1,  28,  28,  -1,  28,  28,  28,  28,  28,  28,  28,  -1,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28, },
		{  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1, },
		{ -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, },
		{  -1,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  29,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  30,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10, },
		{  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7, },
		{ -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14,  31, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, },
		{ -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, },
		{  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  32,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1, },
		{  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  15,  15,  15,  15,  15,  15,  15,  15,  15,  15,  -3,  -3,  -3,  -3,  -3,  15,  15,  15,  15,  15,  15,  15,  15,  15,  15,  15,  15,  15,  15,  15,  15,  15,  15,  15,  15,  15,  15,  15,  15,  15,  15,  -3,  -3,  -3,  15,  15,  15,  15,  15,  15,  15,  15,  15,  15,  15,  15,  15,  15,  15,  15,  15,  15,  15,  15,  15,  15,  15,  15,  15,  15,  15,  -3,  -3,  -3, },
		{ -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, },
		{ -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, },
		{ -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, },
		{ -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, },
		{  -9,  20,  20,  20,  20,  20,  -9,  20,  20,  -9,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  -9,  20,  -9, },
		{  -1,  21,  21,  -1,  21,  21,  33,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  34,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21, },
		{  -1,  22,  22,  -1,  22,  22,  22,  22,  22,  35,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  36,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22, },
		{ -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, },
		{ -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, },
		{  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  37,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8, },
		{  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5, },
		{  -1,  27,  27,  -1,  27,  27,  27,  27,  27,  38,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27,  27, },
		{  -1,  28,  28,  -1,  28,  28,  28,  28,  28,  28,  28,  39,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28,  28, },
		{  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4, },
		{  -1,  10,  10,  -1,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10,  10, },
		{  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  40,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1, },
		{ -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, },
		{  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9, },
		{  -1,  -1,  -1,  -1,  -1,  -1,  21,  -1,  -1,  21,  -1,  -1,  -1,  -1,  -1,  41,  41,  41,  41,  41,  41,  41,  41,  -1,  -1,  -1,  -1,  -1,  -1,  21,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  21,  -1,  -1,  21,  21,  -1,  -1,  -1,  21,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  21,  -1,  -1,  -1,  21,  -1,  21,  -1,  21,  -1,  42,  -1,  -1,  -1,  -1,  -1, },
		{  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9, },
		{  -1,  -1,  -1,  -1,  -1,  -1,  22,  -1,  -1,  22,  -1,  -1,  -1,  -1,  -1,  43,  43,  43,  43,  43,  43,  43,  43,  -1,  -1,  -1,  -1,  -1,  -1,  22,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  22,  -1,  -1,  22,  22,  -1,  -1,  -1,  22,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  22,  -1,  -1,  -1,  22,  -1,  22,  -1,  22,  -1,  44,  -1,  -1,  -1,  -1,  -1, },
		{  -2,  37,  37,  -2,  37,  37,  37,  37,  37,  37,  37,  37,  37,  37,  37,  37,  37,  37,  37,  37,  37,  37,  37,  37,  37,  37,  37,  37,  37,  37,  37,  37,  37,  37,  37,  37,  37,  37,  37,  37,  37,  37,  37,  37,  37,  37,  37,  37,  37,  37,  37,  37,  37,  37,  37,  37,  37,  37,  37,  37,  37,  37,  37,  37,  37,  37,  37,  37,  37,  37,  37,  37,  37,  37,  37,  37,  37,  37,  37,  37,  37,  37,  37,  37,  37,  37,  37,  37,  37, },
		{  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3, },
		{  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6, },
		{ -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, },
		{  -1,  21,  21,  -1,  21,  21,  33,  21,  21,  21,  21,  21,  21,  21,  21,  45,  45,  45,  45,  45,  45,  45,  45,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  34,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21, },
		{  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  46,  46,  46,  46,  46,  46,  46,  46,  46,  46,  -1,  -1,  -1,  -1,  -1,  46,  46,  46,  46,  46,  46,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  46,  46,  46,  46,  46,  46,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1, },
		{  -1,  22,  22,  -1,  22,  22,  22,  22,  22,  35,  22,  22,  22,  22,  22,  47,  47,  47,  47,  47,  47,  47,  47,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  36,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22, },
		{  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  48,  48,  48,  48,  48,  48,  48,  48,  48,  48,  -1,  -1,  -1,  -1,  -1,  48,  48,  48,  48,  48,  48,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  48,  48,  48,  48,  48,  48,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1, },
		{  -1,  21,  21,  -1,  21,  21,  33,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  34,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21, },
		{  -1,  21,  21,  -1,  21,  21,  33,  21,  21,  21,  21,  21,  21,  21,  21,  46,  46,  46,  46,  46,  46,  46,  46,  46,  46,  21,  21,  21,  21,  21,  46,  46,  46,  46,  46,  46,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  34,  21,  21,  46,  46,  46,  46,  46,  46,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21,  21, },
		{  -1,  22,  22,  -1,  22,  22,  22,  22,  22,  35,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  36,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22, },
		{  -1,  22,  22,  -1,  22,  22,  22,  22,  22,  35,  22,  22,  22,  22,  22,  48,  48,  48,  48,  48,  48,  48,  48,  48,  48,  22,  22,  22,  22,  22,  48,  48,  48,  48,  48,  48,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  36,  22,  22,  48,  48,  48,  48,  48,  48,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22,  22, },
	};

	private static final int[] lapg_action = new int[] {
		  -3,  -1,  -1, -11,   4,  -1,  41,  -1,  -1,   8, -19,   3,   5,   6,  18,  -1,
		  -1, -25,   7, -33,  21,  -1,  10,  -1,  19,   9,  -1, -41,  20,  -1, -47,  22,
		 -59,  27,  -1,  -1, -69, -81, -87,  15,  25, -97,-109,  23,  26,  24,-121,  -1,
		-127,  39,  -1,  33,  31,  28,-133,  13,-143,  -1,  37,  38,  34,  32,  30,  17,
		  40,  -1,  -2,
	};

	private static final short[] lapg_lalr = new short[] {
		  11,  -1,   1,   1,  13,   1,  -1,  -2,  11,  -1,   1,   0,  13,   0,  -1,  -2,
		   4,  -1,  12,  11,  -1,  -2,   1,  -1,   6,  -1,   0,   2,  -1,  -2,   4,  -1,
		   8,  11,  12,  11,  -1,  -2,   4,  -1,   8,  11,  -1,  -2,  16,  -1,   1,  16,
		   9,  16,  10,  16,  15,  16,  -1,  -2,   2,  -1,   1,  12,   6,  12,  13,  12,
		  -1,  -2,  16,  -1,   1,  16,   9,  16,  10,  16,  15,  16,  -1,  -2,  17,  -1,
		  18,  36,  -1,  -2,   1,  -1,  15,  -1,   9,  29,  10,  29,  -1,  -2,  16,  -1,
		   1,  16,   9,  16,  10,  16,  15,  16,  -1,  -2,   5,  -1,   1,  14,   6,  14,
		  13,  14,  16,  14,  -1,  -2,  17,  -1,  18,  36,  -1,  -2,  17,  -1,  18,  35,
		  -1,  -2,   1,  -1,  15,  -1,   9,  29,  10,  29,  -1,  -2,  16,  -1,   1,  16,
		   6,  16,  13,  16,  -1,  -2,
	};

	private static final short[] lapg_sym_goto = new short[] {
		   0,   1,  11,  12,  13,  16,  20,  22,  22,  24,  25,  27,  29,  31,  33,  34,
		  36,  40,  43,  45,  46,  47,  48,  49,  50,  52,  54,  55,  63,  66,  67,  71,
		  75,  77,  79,  80,  82,  84,  86,  88,  90,  92,  95,
	};

	private static final short[] lapg_sym_from = new short[] {
		  65,   1,   2,   8,  16,  17,  26,  34,  38,  50,  54,  32,   5,  10,  19,  27,
		   5,   7,  15,  42,   8,  17,  29,  35,  21,  21,  34,   0,   3,  23,  29,   2,
		   8,  15,  38,  54,  30,  36,  41,  56,  37,  46,  48,  47,  57,   0,   0,   0,
		   2,   8,   0,   3,   2,   8,   7,   2,   8,  17,  26,  34,  38,  50,  54,  10,
		  19,  27,  42,  30,  36,  41,  56,  30,  36,  41,  56,   8,  17,   8,  17,  26,
		  30,  36,  30,  36,  38,  54,  38,  54,  37,  46,  37,  46,  37,  46,  48,
	};

	private static final short[] lapg_sym_to = new short[] {
		  66,   5,   6,   6,  26,   6,   6,   6,   6,   6,   6,  42,  12,  22,  22,  22,
		  13,  14,  24,  55,  16,  16,  36,  36,  30,  31,  43,   1,   1,  32,  32,   7,
		   7,  25,  50,  50,  37,  37,  37,  37,  46,  46,  46,  58,  64,  65,   2,   3,
		   8,  17,   4,  11,   9,  18,  15,  10,  19,  27,  33,  44,  51,  60,  61,  23,
		  29,  35,  56,  38,  38,  54,  63,  39,  39,  39,  39,  20,  28,  21,  21,  34,
		  40,  45,  41,  41,  52,  62,  53,  53,  47,  57,  48,  48,  49,  49,  59,
	};

	private static final short[] lapg_rlen = new short[] {
		   1,   0,   3,   2,   1,   3,   3,   2,   1,   3,   1,   0,   3,   1,   0,   1,
		   0,   6,   1,   2,   2,   1,   2,   4,   4,   3,   2,   1,   1,   0,   3,   2,
		   3,   2,   2,   1,   0,   3,   2,   1,   3,   1,
	};

	private static final short[] lapg_rlex = new short[] {
		  20,  20,  19,  21,  21,  24,  24,  22,  22,  25,  28,  28,  25,  29,  29,  30,
		  30,  25,  26,  26,  23,  23,  32,  32,  33,  33,  34,  34,  37,  37,  35,  35,
		  36,  36,  38,  39,  39,  31,  40,  40,  41,  27,
	};

	private static final String[] lapg_syms = new String[] {
		"eoi",
		"identifier",
		"regexp",
		"scon",
		"type",
		"icon",
		"'%'",
		"_skip",
		"'::='",
		"'|'",
		"';'",
		"'.'",
		"':'",
		"'['",
		"']'",
		"'<<'",
		"'{'",
		"'i{'",
		"'}'",
		"input",
		"directivesopt",
		"directives",
		"lexical_definitions",
		"grammar_definitions",
		"directive",
		"lexical_definition",
		"iconlist_in_bits",
		"symbol",
		"typeopt",
		"iconopt",
		"commandopt",
		"command",
		"grammar_definition",
		"rules_definition",
		"symbol_list",
		"rule_def",
		"rule_symbols",
		"rule_priorityopt",
		"rule_priority",
		"command_tokensopt",
		"command_tokens",
		"command_token",
	};

	public enum Tokens {
		eoi,
		identifier,
		regexp,
		scon,
		type,
		icon,
		PERC,
		_skip,
		COLONCOLONEQ,
		OR,
		SEMICOLON,
		DOT,
		COLON,
		LBRACKET,
		RBRACKET,
		LESSLESS,
		LBRACE,
		iLBRACE,
		RBRACE,
		input,
		directivesopt,
		directives,
		lexical_definitions,
		grammar_definitions,
		directive,
		lexical_definition,
		iconlist_in_bits,
		symbol,
		typeopt,
		iconopt,
		commandopt,
		command,
		grammar_definition,
		rules_definition,
		symbol_list,
		rule_def,
		rule_symbols,
		rule_priorityopt,
		rule_priority,
		command_tokensopt,
		command_tokens,
		command_token,
	}

	private static int lapg_next( int state, int symbol ) {
		int p;
		if( lapg_action[state] < -2 ) {
			for( p = - lapg_action[state] - 3; lapg_lalr[p] >= 0; p += 2 )
				if( lapg_lalr[p] == symbol ) break;
			return lapg_lalr[p+1];
		}
		return lapg_action[state];
	}

	private static int lapg_state_sym( int state, int symbol ) {
		int min = lapg_sym_goto[symbol], max = lapg_sym_goto[symbol+1]-1;
		int i, e;

		while( min <= max ) {
			e = (min + max) >> 1;
			i = lapg_sym_from[e];
			if( i == state )
				return lapg_sym_to[e];
			else if( i < state )
				min = e + 1;
			else
				max = e - 1;
		}
		return -1;
	}

	public boolean parse() {

		byte[]        token = new byte[4096];
		int           lapg_head = 0, group = 0, lapg_i, lapg_size, chr;
		lapg_symbol[] lapg_m = new lapg_symbol[1024];
		lapg_symbol   lapg_n;
		int           lapg_current_line = 1, lapg_current_offset = 0;

		lapg_m[0] = new lapg_symbol();
		lapg_m[0].state = 0;
		chr = l < buff.length ? buff[l++] : 0;

		do {
			lapg_n = new lapg_symbol();
			lapg_n.pos = new lapg_place( lapg_current_line, lapg_current_offset );
			for( lapg_size = 0, lapg_i = group; lapg_i >= 0; ) {
				if( lapg_size < 4096-1 ) token[lapg_size++] = (byte)chr;
				lapg_i = lapg_lexem[lapg_i][lapg_char2no[(chr+256)%256]];
				if( lapg_i >= -1 && chr != 0 ) { 
					lapg_current_offset++;
					if( chr == '\n' ) lapg_current_line++;
					chr = l < buff.length ? buff[l++] : 0;
				}
			}
			lapg_n.endpos = new lapg_place( lapg_current_line, lapg_current_offset );

			if( lapg_i == -1 ) {
				if( chr == 0 ) {
					error( "Unexpected end of file reached");
					break;
				}
				error( MessageFormat.format( "invalid lexem at line {0}: `{1}`, skipped", lapg_n.pos.line, new String(token,0,lapg_size) ) );
				lapg_n.lexem = -1;
				continue;
			}

			lapg_size--;
			lapg_n.lexem = -lapg_i-2;
			lapg_n.sym = null;

			switch( lapg_n.lexem ) {
				case 1:
					 lapg_n.sym = new String(token,0,lapg_size); break; 
				case 2:
					 lapg_n.sym = new String(token,1,lapg_size-2); break; 
				case 3:
					 lapg_n.sym = new String(token,1,lapg_size-2); break; 
				case 4:
					 lapg_n.sym = new String(token,1,lapg_size-2); break; 
				case 5:
					 lapg_n.sym = Integer.parseInt(new String(token,0,lapg_size)); break; 
				case 7:
					 continue; 
				case 16:
					 deep = 1; group = 1; break; 
				case 17:
					 deep++; break; 
				case 18:
					 if( --deep == 0 ) group = 0; break; 
			}


			do {
				lapg_i = lapg_next( lapg_m[lapg_head].state, lapg_n.lexem );

				if( lapg_i >= 0 ) {
					lapg_symbol lapg_gg = new lapg_symbol();
					lapg_gg.sym = (lapg_rlen[lapg_i]!=0)?lapg_m[lapg_head+1-lapg_rlen[lapg_i]].sym:null;
					lapg_gg.lexem = lapg_rlex[lapg_i];
					lapg_gg.state = 0;
					if( DEBUG_SYNTAX )
						System.out.println( "reduce to " + lapg_syms[lapg_rlex[lapg_i]] );
					lapg_gg.pos = (lapg_rlen[lapg_i]!=0)?lapg_m[lapg_head+1-lapg_rlen[lapg_i]].pos:lapg_n.pos;
					lapg_gg.endpos = (lapg_rlen[lapg_i]!=0)?lapg_m[lapg_head].endpos:lapg_n.pos;
					switch( lapg_i ) {
						case 5:
							 options.put(((String)lapg_m[lapg_head-1].sym), ((String)lapg_m[lapg_head-0].sym)); 
							break;
						case 6:
							 options.put(((String)lapg_m[lapg_head-1].sym), ((Integer)lapg_m[lapg_head-0].sym).toString()); 
							break;
						case 9:
							 currentgroups = ((Integer)lapg_m[lapg_head-1].sym); 
							break;
						case 12:
							 addLexem(((CSymbol)lapg_m[lapg_head-2].sym), ((String)lapg_m[lapg_head-1].sym), null, null, null, lapg_m[lapg_head-2].pos.line); 
							break;
						case 17:
							 addLexem(((CSymbol)lapg_m[lapg_head-5].sym), ((String)lapg_m[lapg_head-4].sym), ((String)lapg_m[lapg_head-2].sym), ((Integer)lapg_m[lapg_head-1].sym), ((CAction)lapg_m[lapg_head-0].sym), lapg_m[lapg_head-5].pos.line); 
							break;
						case 18:
							 if( ((Integer)lapg_m[lapg_head-0].sym) < 0 || ((Integer)lapg_m[lapg_head-0].sym) >= BITS ) lapg_gg.sym = 0; else lapg_gg.sym = 1 << ((Integer)lapg_m[lapg_head-0].sym); 
							break;
						case 19:
							 lapg_gg.sym = ((Integer)lapg_gg.sym) | ((Integer)lapg_m[lapg_head-0].sym); 
							break;
						case 23:
							 addPrio(((String)lapg_m[lapg_head-2].sym), ((List<CSymbol>)lapg_m[lapg_head-1].sym), lapg_m[lapg_head-2].pos.line); 
							break;
						case 24:
							 addNonterm(((CSymbol)lapg_m[lapg_head-3].sym), ((String)lapg_m[lapg_head-2].sym), lapg_m[lapg_head-3].pos.line); addRule(((CRule)lapg_m[lapg_head-0].sym),((CSymbol)lapg_m[lapg_head-3].sym)); 
							break;
						case 25:
							 addRule(((CRule)lapg_m[lapg_head-0].sym),((CSymbol)lapg_gg.sym)); 
							break;
						case 26:
							 ((List<CSymbol>)lapg_gg.sym).add(((CSymbol)lapg_m[lapg_head-0].sym)); 
							break;
						case 27:
							 lapg_gg.sym = new ArrayList<CSymbol>(); ((List<CSymbol>)lapg_gg.sym).add(((CSymbol)lapg_m[lapg_head-0].sym)); 
							break;
						case 30:
							 lapg_gg.sym = new CRule(((List<CSymbol>)lapg_m[lapg_head-2].sym), ((CAction)lapg_m[lapg_head-1].sym), ((CSymbol)lapg_m[lapg_head-0].sym), inputId, lapg_m[lapg_head-2].pos.line); 
							break;
						case 31:
							 lapg_gg.sym = new CRule(null, ((CAction)lapg_m[lapg_head-1].sym), ((CSymbol)lapg_m[lapg_head-0].sym), inputId, lapg_m[lapg_head-1].pos.line); 
							break;
						case 32:
							 addRuleSymbol(((List<CSymbol>)lapg_gg.sym),((CAction)lapg_m[lapg_head-1].sym),((CSymbol)lapg_m[lapg_head-0].sym)); 
							break;
						case 33:
							 lapg_gg.sym = new ArrayList<CSymbol>(); addRuleSymbol(((List<CSymbol>)lapg_gg.sym),((CAction)lapg_m[lapg_head-1].sym),((CSymbol)lapg_m[lapg_head-0].sym)); 
							break;
						case 34:
							 lapg_gg.sym = ((CSymbol)lapg_m[lapg_head-0].sym); 
							break;
						case 37:
							 lapg_gg.sym = new CAction(rawData(lapg_m[lapg_head-2].pos.offset+1,lapg_m[lapg_head-0].pos.offset), inputId, lapg_m[lapg_head-2].pos.line); 
							break;
						case 41:
							 lapg_gg.sym = getSymbol(((String)lapg_m[lapg_head-0].sym), lapg_m[lapg_head-0].pos.line); 
							break;
					}
					for( int e = lapg_rlen[lapg_i]; e > 0; e-- ) 
						lapg_m[lapg_head--] = null;
					lapg_m[++lapg_head] = lapg_gg;
					lapg_m[lapg_head].state = lapg_state_sym( lapg_m[lapg_head-1].state, lapg_gg.lexem );
				} else if( lapg_i == -1 ) {
					lapg_m[++lapg_head] = lapg_n;
					lapg_m[lapg_head].state = lapg_state_sym( lapg_m[lapg_head-1].state, lapg_n.lexem );
					if( DEBUG_SYNTAX )
						System.out.println( MessageFormat.format( "shift: {0} ({1})", lapg_syms[lapg_n.lexem], new String(token,0,lapg_size) ) );
				}

			} while( lapg_i >= 0 && lapg_m[lapg_head].state != -1 );

			if( (lapg_i == -2 || lapg_m[lapg_head].state == -1) && lapg_n.lexem != 0 ) {
				break;
			}

		} while( lapg_n.lexem != 0 );

		if( lapg_m[lapg_head].state != 67-1 ) {
			error( MessageFormat.format( "syntax error before line {0}", lapg_n.pos.line ) );
			return false;
		};
		return true;
	}
}

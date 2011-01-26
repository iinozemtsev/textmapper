/**
 * Copyright 2002-2010 Evgeny Gryaznov
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
package org.textway.lapg.parser.ast;

import org.textway.lapg.parser.LapgTree.TextSource;

import java.util.List;

public class AstRule extends AstNode {

	private final List<AstRuleSymbol> list;
	private final AstCode action;
	private final AstRuleAttribute attribute;
	private final AstAnnotations annotations;
	private final String ruleAlias;
	private final AstError error;

	public AstRule(AstRulePrefix prefix, List<AstRuleSymbol> list, AstCode action, AstRuleAttribute attr, TextSource source,
				   int offset, int endoffset) {
		super(source, offset, endoffset);
		this.list = list;
		this.action = action;
		this.attribute = attr;
		if (prefix != null) {
			this.annotations = prefix.getAnnotations();
			this.ruleAlias = prefix.getAlias();
		} else {
			this.annotations = null;
			this.ruleAlias = null;
		}
		this.error = null;
	}

	public AstRule(AstError err) {
		super(err.getInput(), err.getOffset(), err.getEndOffset());
		this.list = null;
		this.action = null;
		this.attribute = null;
		this.ruleAlias = null;
		this.annotations = null;
		this.error = err;
	}

	public boolean hasSyntaxError() {
		return error != null;
	}

	public List<AstRuleSymbol> getList() {
		return list;
	}

	public AstCode getAction() {
		return action;
	}

	public AstRuleAttribute getAttribute() {
		return attribute;
	}

	public String getAlias() {
		return ruleAlias;
	}

	public AstAnnotations getAnnotations() {
		return annotations;
	}

	public void accept(AbstractVisitor v) {
		if (error != null) {
			v.visit(error);
			return;
		}
		if (!v.visit(this)) {
			return;
		}
		if (annotations != null) {
			annotations.accept(v);
		}
		if (list != null) {
			for (AstRuleSymbol rsym : list) {
				rsym.accept(v);
			}
		}
		if (action != null) {
			action.accept(v);
		}
		if (attribute != null) {
			attribute.accept(v);
		}
	}
}

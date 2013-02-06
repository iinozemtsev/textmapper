/**
 * Copyright 2002-2013 Evgeny Gryaznov
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
package org.textmapper.lapg.builder;

import org.textmapper.lapg.api.Nonterminal;
import org.textmapper.lapg.api.Rule;
import org.textmapper.lapg.api.SourceElement;
import org.textmapper.lapg.api.rule.RhsPart;

import java.util.ArrayList;
import java.util.List;

/**
 * evgeny, 10/27/12
 */
public class LiNonterminal extends LiSymbol implements Nonterminal {

	private RhsPart definition;
	private boolean isNullable;
	private final List<Rule> rules = new ArrayList<Rule>();

	public LiNonterminal(String name, String type, SourceElement origin) {
		super(name, type, origin);
	}

	@Override
	public RhsPart getDefinition() {
		return definition;
	}

	@Override
	public Iterable<Rule> getRules() {
		return rules;
	}

	@Override
	public boolean isNullable() {
		return isNullable;
	}

	void setDefinition(LiRhsRoot part) {
		if (definition != null) {
			throw new IllegalStateException("non-terminal is sealed");
		}
		this.definition = part;
		part.setLeft(this);
	}

	void addRule(LiRhsPart part) {
		if (definition == null) {
			definition = new LiRootRhsChoice(this);
		} else if (!(definition instanceof LiRootRhsChoice)) {
			throw new IllegalStateException("non-terminal is sealed");
		}
		((LiRootRhsChoice) definition).addRule(part);
	}

	void rewriteDefinition(RhsPart old, RhsPart new_) {
		if (old == definition) {
			definition = new_;
		}
	}

	void addRule(LiRule rule) {
		rules.add(rule);
	}

	void setNullable(boolean nullable) {
		isNullable = nullable;
	}
}

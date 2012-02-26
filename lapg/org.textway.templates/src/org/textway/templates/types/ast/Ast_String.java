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
package org.textway.templates.types.ast;

import org.textway.templates.types.TypesTree.TextSource;

public class Ast_String extends AstNode {

	private String identifier;
	private String scon;

	public Ast_String(String identifier, String scon, TextSource input, int start, int end) {
		super(input, start, end);
		this.identifier = identifier;
		this.scon = scon;
	}

	public String getIdentifier() {
		return identifier;
	}
	public String getScon() {
		return scon;
	}
	public void accept(AstVisitor v) {
		if (!v.visit(this)) {
			return;
		}

		// TODO for identifier
		// TODO for scon
	}
}

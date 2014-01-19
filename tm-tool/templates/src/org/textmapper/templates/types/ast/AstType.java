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
package org.textmapper.templates.types.ast;

import java.util.List;
import org.textmapper.templates.types.TypesTree.TextSource;

public class AstType extends AstNode {

	private final AstType.AstKindKind kind;
	private final boolean isReference;
	private final boolean isClosure;
	private final List<String> name;
	private final List<AstTypeEx> parameters;

	public AstType(AstType.AstKindKind kind, boolean isReference, boolean isClosure, List<String> name, List<AstTypeEx> parameters, TextSource source, int offset, int endoffset) {
		super(source, offset, endoffset);
		this.kind = kind;
		this.isReference = isReference;
		this.isClosure = isClosure;
		this.name = name;
		this.parameters = parameters;
	}

	public AstType.AstKindKind getKind() {
		return kind;
	}

	public boolean getIsReference() {
		return isReference;
	}

	public boolean getIsClosure() {
		return isClosure;
	}

	public List<String> getName() {
		return name;
	}

	public List<AstTypeEx> getParameters() {
		return parameters;
	}

	public void accept(AstVisitor v) {
		if (!v.visit(this)) {
			return;
		}
		if (parameters != null) {
			for (AstTypeEx it : parameters) {
				it.accept(v);
			}
		}
	}

	public enum AstKindKind {
		LBOOL,
		LSTRING,
		LINT,
	}
}
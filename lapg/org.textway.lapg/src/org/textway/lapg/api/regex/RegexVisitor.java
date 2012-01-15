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
package org.textway.lapg.api.regex;

public abstract class RegexVisitor {

	public abstract void visit(RegexAny c);

	public abstract void visit(RegexChar c);

	public abstract void visit(RegexExpand c);

	public abstract void visitBefore(RegexList c);

	public abstract void visitAfter(RegexList c);

	public abstract void visitBefore(RegexOr c);

	public abstract void visitBetween(RegexOr c);

	public abstract void visitAfter(RegexOr c);

	public abstract void visitBefore(RegexQuantifier c);

	public abstract void visitAfter(RegexQuantifier c);

	public boolean visit(RegexSet c) {
		return false;
	}

	public void visit(RegexRange c) {
	}

	public void visit(RegexEmpty c) {
	}
}

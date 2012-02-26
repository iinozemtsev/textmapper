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
package org.textway.templates.ast;

import org.textway.templates.api.EvaluationContext;
import org.textway.templates.api.EvaluationException;
import org.textway.templates.api.IEvaluationStrategy;
import org.textway.templates.ast.TemplatesTree.TextSource;

public abstract class ExpressionNode extends Node {

	protected ExpressionNode(TextSource source, int offset, int endoffset) {
		super(source, offset, endoffset);
	}

	@Override
	protected void emit(StringBuilder sb, EvaluationContext context, IEvaluationStrategy env) {
		try {
			sb.append(env.toString(env.evaluate(this, context, false), this));
		} catch (EvaluationException ex) {
			/* already handled, ignore */
		}
	}

	public abstract Object evaluate(EvaluationContext context, IEvaluationStrategy env) throws EvaluationException;

	@Override
	public final String toString() {
		StringBuilder sb = new StringBuilder();
		toString(sb);
		return sb.toString();
	}

	public abstract void toString(StringBuilder sb);
}
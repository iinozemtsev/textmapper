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
package org.textway.templates.ast;

import java.util.List;

import org.textway.templates.api.EvaluationContext;
import org.textway.templates.api.EvaluationException;
import org.textway.templates.api.IEvaluationStrategy;
import org.textway.templates.ast.AstTree.TextSource;

public class ListNode extends ExpressionNode {

	private final ExpressionNode[] expressions;

	public ListNode(List<ExpressionNode> expressions, TextSource source, int offset, int endoffset) {
		super(source, offset, endoffset);
		this.expressions = expressions != null && expressions.size() > 0 ? (ExpressionNode[]) expressions
				.toArray(new ExpressionNode[expressions.size()]) : null;
	}

	@Override
	public Object evaluate(EvaluationContext context, IEvaluationStrategy env) throws EvaluationException {
		Object[] result = null;
		if( expressions != null ) {
			result = new Object[expressions.length];
			for( int i = 0; i < expressions.length; i++ ) {
				result[i] = env.evaluate(expressions[i], context, false);
			}
		} else {
			result = new Object[0];
		}
		return result;
	}

	@Override
	public void toString(StringBuilder sb) {
		sb.append('[');
		if( expressions != null ) {
			for( int i = 0; i < expressions.length; i++ ) {
				if( i > 0) {
					sb.append(",");
				}
				expressions[i].toString(sb);
			}
		}
		sb.append(']');
	}
}

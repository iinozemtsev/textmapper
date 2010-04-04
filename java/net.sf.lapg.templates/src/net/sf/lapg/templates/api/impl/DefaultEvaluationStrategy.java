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
package net.sf.lapg.templates.api.impl;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import net.sf.lapg.templates.api.EvaluationContext;
import net.sf.lapg.templates.api.EvaluationException;
import net.sf.lapg.templates.api.IEvaluationCache;
import net.sf.lapg.templates.api.IEvaluationStrategy;
import net.sf.lapg.templates.api.ILocatedEntity;
import net.sf.lapg.templates.api.INamedEntity;
import net.sf.lapg.templates.api.INavigationStrategy;
import net.sf.lapg.templates.api.IBundleEntity;
import net.sf.lapg.templates.api.IQuery;
import net.sf.lapg.templates.api.ITemplate;
import net.sf.lapg.templates.ast.AstParser;
import net.sf.lapg.templates.ast.ExpressionNode;
import net.sf.lapg.templates.ast.AstLexer.ErrorReporter;

public class DefaultEvaluationStrategy implements IEvaluationStrategy {

	private final TemplatesFacade templatesFacade;
	private final TemplatesRegistry registry;
	private final INavigationStrategy.Factory navigationFactory;
	private IEvaluationCache myCache;

	public DefaultEvaluationStrategy(TemplatesFacade facade, INavigationStrategy.Factory factory, TemplatesRegistry registry) {
		this.templatesFacade = facade;
		this.navigationFactory = factory;
		this.registry = registry;
		factory.setEvaluationStrategy(this);
	}

	@SuppressWarnings("unchecked")
	public Object callMethod(Object obj, String methodName, Object[] args) throws EvaluationException {
		INavigationStrategy strategy = navigationFactory.getStrategy(obj);
		return strategy.callMethod(obj, methodName, args);
	}

	@SuppressWarnings("unchecked")
	public Object getByIndex(Object obj, Object index) throws EvaluationException {
		INavigationStrategy strategy = navigationFactory.getStrategy(obj);
		return strategy.getByIndex(obj, index);
	}

	@SuppressWarnings("unchecked")
	public Object getProperty(Object obj, String id) throws EvaluationException {
		INavigationStrategy strategy = navigationFactory.getStrategy(obj);
		return strategy.getProperty(obj, id);
	}

	public boolean toBoolean(Object o) {
		if( o instanceof Boolean ) {
			return ((Boolean)o).booleanValue();
		} else if( o instanceof String ) {
			return ((String)o).trim().length() > 0;
		}
		return o != null;
	}

	public String toString(Object o, ExpressionNode referer) throws EvaluationException {
		if( o instanceof Collection<?> || o instanceof Object[] ) {
			String message = "Evaluation of `"+referer.toString()+"` results in collection, cannot convert to String";
			EvaluationException ex = new HandledEvaluationException(message);
			fireError(referer, message);
			throw ex;
		}
		return o.toString();
	}

	public Object evaluate(ExpressionNode expr, EvaluationContext context, boolean permitNull) throws EvaluationException {
		try {
			Object result = expr.evaluate(context, this);
			if( result == null && !permitNull ) {
				String message = "Evaluation of `"+expr.toString()+"` failed for " + getTitle(context.getThisObject()) + ": null";
				EvaluationException ex = new HandledEvaluationException(message);
				fireError(expr, message);
				throw ex;
			}
			return result;
		} catch( HandledEvaluationException ex ) {
			throw ex;
		} catch( Throwable th ) {
			Throwable cause = th.getCause() != null ? th.getCause() : th;
			String message = "Evaluation of `"+expr.toString()+"` failed for " + getTitle(context.getThisObject()) + ": " + cause.getMessage();
			EvaluationException ex = new HandledEvaluationException(message);
			fireError(expr, message);
			throw ex;
		}
	}

	public String getTitle(Object object) {
		if( object == null ) {
			return "<unknown>";
		}
		if( object instanceof INamedEntity ) {
			return ((INamedEntity)object).getTitle();
		}
		return object.getClass().getCanonicalName();
	}

	private static class HandledEvaluationException extends EvaluationException {

		private static final long serialVersionUID = -718162932392225590L;

		public HandledEvaluationException(String message) {
			super(message);
		}
	}

	public IBundleEntity loadEntity(String qualifiedName, int kind, ILocatedEntity referer) {
		return registry.loadEntity(qualifiedName, kind, referer);
	}

	public String evaluate(ITemplate t, EvaluationContext context, Object[] arguments, ILocatedEntity referer) {
		if (t == null) {
			return "";
		}
		try {
			return t.apply(new EvaluationContext(context != null ? context.getThisObject() : null, context, t), this, arguments);
		} catch (EvaluationException ex) {
			fireError(t, ex.getMessage());
			return "";
		}
	}

	public Object evaluate(IQuery t, EvaluationContext context, Object[] arguments, ILocatedEntity referer) throws EvaluationException {
		return t.invoke(new EvaluationContext(context != null ? context.getThisObject() : null, context, t), this, arguments);
	}

	public String eval(ILocatedEntity referer, String template, String templateId, EvaluationContext context) {
		final String inputName = templateId != null ? templateId : referer.getLocation();
		AstParser p = new AstParser(new ErrorReporter() {
			public void error(int start, int end, int line, String s) {
				DefaultEvaluationStrategy.this.fireError(null, inputName + ":" + s);
			}
		});
		IBundleEntity[] loaded = null;
		if (!p.parseBody(template, "syntax", inputName)) {
			loaded = new ITemplate[0];
		} else {
			loaded = p.getResult();
		}

		ITemplate t = loaded != null && loaded.length == 1
				&& loaded[0].getKind() == IBundleEntity.KIND_TEMPLATE
				&& loaded[0].getName().equals("inline")
					? (ITemplate)loaded[0] : null;
		if (t == null) {
			return "";
		}
		try {
			return t.apply(context, this, null);
		} catch (EvaluationException ex) {
			fireError(t, ex.getMessage());
			return "";
		}
	}

	public Iterator<?> getCollectionIterator(Object o) {
		if (o instanceof Collection<?>) {
			return ((Collection<?>) o).iterator();
		}
		if (o instanceof Object[]) {
			return new ArrayIterator((Object[]) o);
		}
		return null;
	}

	private static class ArrayIterator implements Iterator<Object> {

		Object[] elements;

		int index;

		int lastElement;

		public ArrayIterator(Object[] elements) {
			this(elements, 0, elements.length - 1);
		}

		public ArrayIterator(Object[] elements, int firstElement, int lastElement) {
			this.elements = elements;
			index = firstElement;
			this.lastElement = lastElement;
		}

		public boolean hasNext() {
			return elements != null && index <= lastElement;
		}

		public Object next() throws NoSuchElementException {
			if (!hasNext()) {
				throw new NoSuchElementException();
			}
			return elements[index++];
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	public void fireError(ILocatedEntity referer, String error) {
		templatesFacade.fireError(referer, error);
	}

	public final void createFile(String name, String contents) {
		templatesFacade.createFile(name,contents);
	}

	public IEvaluationCache getCache() {
		if(myCache == null) {
			myCache = new DefaultEvaluationCache();
		}
		return myCache;
	}
}

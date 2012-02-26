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
package org.textway.templates.types;

import org.textway.templates.api.types.IClass;
import org.textway.templates.api.types.IMethod;
import org.textway.templates.api.types.IType;

public class TiMethod implements IMethod {

	private final String name;
	private TiClass declaringClass;
	private IType returnType;
	private IType[] parameterTypes;

	public TiMethod(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public IClass getDeclaringClass() {
		return declaringClass;
	}

	public IType getReturnType() {
		return returnType;
	}

	public IType[] getParameterTypes() {
		return parameterTypes;
	}

	void setType(IType type) {
		this.returnType = type;
	}

	void setParameterTypes(IType[] parameterTypes) {
		this.parameterTypes = parameterTypes;
	}

	void setDeclaringClass(TiClass declaringClass) {
		this.declaringClass = declaringClass;
	}
}

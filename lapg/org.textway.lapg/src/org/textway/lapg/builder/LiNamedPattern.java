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
package org.textway.lapg.builder;

import org.textway.lapg.api.NamedPattern;
import org.textway.lapg.api.SourceElement;
import org.textway.lapg.api.DerivedSourceElement;
import org.textway.lapg.api.regex.RegexPart;

/**
 * Gryaznov Evgeny, 6/23/11
 */
class LiNamedPattern implements NamedPattern, DerivedSourceElement {

	private final String name;
	private final RegexPart regexp;
	private final SourceElement origin;

	public LiNamedPattern(String name, RegexPart regexp, SourceElement origin) {
		this.name = name;
		this.regexp = regexp;
		this.origin = origin;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public RegexPart getRegexp() {
		return regexp;
	}

	public String getTitle() {
		return "Pattern `" + name + "`";
	}

	@Override
	public SourceElement getOrigin() {
		return origin;
	}
}

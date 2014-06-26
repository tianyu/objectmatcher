package org.hamcrest;

import java.util.Arrays;
import java.util.Iterator;

import org.hamcrest.internal.SelfDescribingValueIterator;

public abstract class ObjectMatcher<T> extends BaseMatcher<T> {

	public static <T> TypeMatcher<T> a(final Class<T> clazz) {
		return new TypeMatcher<T>(clazz);
	}

	public static <T> TypeMatcher<T> an(final Class<T> clazz) {
		return new TypeMatcher<T>(clazz);
	}

	private final Class<T> _clazz;

	protected ObjectMatcher(final Class<T> clazz) {
		_clazz = clazz;
	}

	protected ObjectMatcher(final ObjectMatcher<T> objectMatcher) {
		_clazz = objectMatcher.getObjectClass();
	}

	protected Class<T> getObjectClass() {
		return _clazz;
	}

	protected abstract GetterMatcher<T> where(final String name, final Matcher<?> matcher);

	protected abstract void describeTo(final MultilineDescription description);

	protected abstract void describeMismatch(final Object item, final MultilineDescription description);

	@Override
	public final void describeTo(final Description description) {
		describeTo(new MultilineDescription(description));
	}

	@Override
	public final void describeMismatch(final Object item, final Description description) {
		describeMismatch(item, new MultilineDescription(description));
	}

	protected static class MultilineDescription implements Description {
		private final Description _base;
		private final int _level;

		public MultilineDescription(final Description base) {
			if (base instanceof MultilineDescription) {
				final MultilineDescription multiline = (MultilineDescription) base;
				_base = multiline._base;
				_level = multiline._level + 1;
			} else {
				_base = base;
				_level = 1;
			}
		}

		public MultilineDescription appendNewline() {
			_base.appendText(System.lineSeparator());
			for (int i = 0; i < _level; ++i) {
				_base.appendText("    ");
			}
			return this;
		}

		@Override
		public MultilineDescription appendText(final String text) {
			_base.appendText(text);
			return this;
		}

		@Override
		public MultilineDescription appendDescriptionOf(final SelfDescribing value) {
			value.describeTo(this);
			return this;
		}

		@Override
		public MultilineDescription appendValue(final Object value) {
			_base.appendValue(value);
			return this;
		}

		@SafeVarargs
		@Override
		public final <T> MultilineDescription appendValueList(final String start, final String separator, final String end, final T... values) {
			return appendValueList(start, separator, end, Arrays.asList(values));
		}

		@Override
		public <T> MultilineDescription appendValueList(final String start, final String separator, final String end, final Iterable<T> values) {
			return appendList(start, separator, end, new SelfDescribingValueIterator<T>(values.iterator()));
		}

		@Override
		public MultilineDescription appendList(final String start, final String separator, final String end, final Iterable<? extends SelfDescribing> values) {
			return appendList(start, separator, end, values.iterator());
		}

		private MultilineDescription appendList(final String start, final String separator, final String end, final Iterator<? extends SelfDescribing> values) {
			appendText(start);
			try {
				if (!values.hasNext()) return this;

				appendDescriptionOf(values.next());
				while (values.hasNext()) {
					appendText(separator);
					appendDescriptionOf(values.next());
				}

				return this;
			} finally {
				appendText(end);
			}
		}
	}
}

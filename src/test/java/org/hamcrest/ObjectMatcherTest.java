package org.hamcrest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.ObjectMatcher.a;
import static org.hamcrest.ObjectMatcher.an;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Iterator;

import org.hamcrest.internal.SelfDescribingValueIterator;
import org.junit.Test;

public class ObjectMatcherTest {

	private static class Foo {
		private final int _id;
		private final Object _bar;

		public Foo(int id) {
			this(id, null);
		}

		public Foo(int id, Object bar) {
			_id = id;
			_bar = bar;
		}

		public int getId() {
			return _id;
		}

		public Object getBar() {
			return _bar;
		}
	}

	@Test
	public void testMatchType() {
		assertMatches(new Object(), an(Object.class));
		assertMismatch(new Object(), a(Foo.class), "a Foo", "was an Object");
	}

	@Test
	public void testMatchGetter() {
		final Foo foo = new Foo(1);
		assertMatches(foo, a(Foo.class).where("id", is(1)));

		assertMismatch(foo, a(Foo.class).where("id", is(200))
			, "a Foo\n    where id is 200"
			, "was a Foo\n    where id was 1"
		);

		assertMatches(foo, a(Foo.class)
				.where("id", is(1))
				.where("bar", is(nullValue()))
		);

		assertMismatch(foo, a(Foo.class)
				.where("id", is(200))
				.where("bar", is(not(nullValue())))
			, "a Foo\n    where id is 200\n    where bar is not null"
			, "was a Foo\n    where id was 1\n    where bar was null"
		);

		assertMatches(foo, a(Foo.class)
				.where("id", is(1))
				.where("bar", is(nullValue()))
				.where("id", is(1))
		);

		assertMismatch(foo, a(Foo.class)
				.where("id", is(1))
				.where("bar", is(not(nullValue())))
				.where("id", is(1))
			, "a Foo\n    where bar is not null"
			, "was a Foo\n    where bar was null"
		);

		assertMismatch(foo, a(Foo.class)
				.where("id", is(1))
				.where("bar", is(nullValue()))
				.where("id", is(200))
			, "a Foo\n    where id is 200"
			, "was a Foo\n    where id was 1"
		);
	}

	@Test
	public void testMatchMultilevel() {
		final Foo bar = new Foo(2);
		final Foo foo = new Foo(1, bar);

		assertMatches(foo, a(Foo.class)
			.where("id", is(1))
			.where("bar", is(a(Foo.class)
				.where("id", is(2))
				.where("bar", is(nullValue()))
			))
		);

		assertMismatch(foo, a(Foo.class)
			.where("id", is(200))
			.where("bar", is(a(Foo.class)
				.where("id", is(201))
				.where("bar", is(not(nullValue())))
			))
			, "a Foo"
			+ "\n    where id is 200"
			+ "\n    where bar is a Foo"
			+ "\n        where id is 201"
			+ "\n        where bar is not null"
			, "was a Foo"
			+ "\n    where id was 1"
			+ "\n    where bar was a Foo"
			+ "\n        where id was 2"
			+ "\n        where bar was null"
		);
	}

	@Test
	public void testSingleInvocation() {
		final Foo foo = mock(Foo.class);
		when(foo.getId()).thenReturn(1);
		final Matcher<Foo> matcher = a(Foo.class).where("id", is(200));

		matcher.matches(foo);
		getDescription(matcher);
		getMismatchDescription(matcher, foo);

		verify(foo, times(1)).getId();
	}

	@Test
	public void testSingleInvocation_nested() {
		final Foo foo = mock(Foo.class);
		final Foo bar = mock(Foo.class);
		when(foo.getBar()).thenReturn(bar);
		when(bar.getId()).thenReturn(2);

		final Matcher<Foo> matcher = a(Foo.class)
			.where("bar", is(a(Foo.class)
			.where("id", is(200))
		));

		matcher.matches(foo);
		getDescription(matcher);
		getMismatchDescription(matcher, foo);

		verify(bar, times(1)).getId();
	}

	private void assertMatches(Object item, Matcher<?> matcher) {
		assertThat(matcher.matches(item), is(true));
	}

	private void assertMismatch(Object item, Matcher<?> matcher, String description, String mismatchDescription) {
		assertThat(matcher.matches(item), is(false));
		assertThat(getDescription(matcher), equalTo(description));
		assertThat(getMismatchDescription(matcher, item), equalTo(mismatchDescription));
	}

	private static String getDescription(Matcher<?> matcher) {
		return new TestDescription()
			.appendDescriptionOf(matcher)
			.toString();
	}

	private static String getMismatchDescription(Matcher<?> matcher, Object item) {
		final Description description = new TestDescription();
		matcher.describeMismatch(item, description);
		return description.toString();
	}

	private static class TestDescription implements Description {
		private final StringBuilder _builder = new StringBuilder();

		@Override
		public Description appendText(final String text) {
			_builder.append(text);
			return this;
		}

		@Override
		public Description appendDescriptionOf(final SelfDescribing value) {
			value.describeTo(this);
			return this;
		}

		@Override
		public Description appendValue(final Object value) {
			_builder.append(value);
			return this;
		}

		@SafeVarargs
		@Override
		public final <T> Description appendValueList(final String start, final String separator, final String end, final T... values) {
			return appendValueList(start, separator, end, Arrays.asList(values));
		}

		@Override
		public <T> Description appendValueList(final String start, final String separator, final String end, final Iterable<T> values) {
			return appendList(start, separator, end, new SelfDescribingValueIterator<T>(values.iterator()));
		}

		@Override
		public Description appendList(final String start, final String separator, final String end, final Iterable<? extends SelfDescribing> values) {
			return appendList(start, separator, end, values.iterator());
		}

		@Override
		public String toString() {
			return _builder.toString();
		}

		private Description appendList(final String start, final String separator, final String end, final Iterator<? extends SelfDescribing> values) {
			_builder.append(start);
			try {
				if (!values.hasNext()) return this;

				appendDescriptionOf(values.next());

				while (values.hasNext()) {
					appendDescriptionOf(values.next());
				}

				return this;
			} finally {
				_builder.append(end);
			}
		}
	}
}

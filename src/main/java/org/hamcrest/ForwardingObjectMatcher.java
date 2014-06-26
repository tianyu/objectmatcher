package org.hamcrest;

public abstract class ForwardingObjectMatcher<T> extends BaseMatcher<T> {
	private final ObjectMatcher<T> _objectMatcher;

	public ForwardingObjectMatcher(final ObjectMatcher<T> objectMatcher) {
		_objectMatcher = objectMatcher;
	}

	protected GetterMatcher<T> where(final String name, final Matcher<?> matcher) {
		return _objectMatcher.where(name, matcher);
	}

	@Override
	public boolean matches(final Object item) {
		return _objectMatcher.matches(item);
	}

	@Override
	public void describeMismatch(final Object item, final Description mismatchDescription) {
		_objectMatcher.describeMismatch(item, mismatchDescription);
	}

	@Override
	public void describeTo(final Description description) {
		_objectMatcher.describeTo(description);
	}
}

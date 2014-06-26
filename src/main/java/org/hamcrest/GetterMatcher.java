package org.hamcrest;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class GetterMatcher<T> extends ObjectMatcher<T> {
	private final ObjectMatcher<T> _objectMatcher;
	private final String _getterName;
	private final Method _getter;
	private final Matcher<?> _getterMatcher;

	private enum Result { MATCH, MISMATCH, COULD_NOT_INVOKE }
	private Result _lastResult = Result.MATCH;
	private Object _lastReturnValue = null;
	private Exception _lastException = null;

	public GetterMatcher(
		final ObjectMatcher<T> objectMatcher
		, final String getterName
		, final Matcher<?> getterMatcher
	) {
		super(objectMatcher);
		_objectMatcher = objectMatcher;
		_getterName = getterName;
		_getter = getGetter(getObjectClass(), getterName);
		_getterMatcher = getterMatcher;
	}

	@Override
	public GetterMatcher<T> where(final String name, final Matcher<?> matcher) {
		return new GetterMatcher<T>(this, name, matcher);
	}

	private boolean test(final Object item) {
		try {
			_lastReturnValue = _getter.invoke(item);
		} catch (IllegalAccessException e) {
			_lastResult = Result.COULD_NOT_INVOKE;
			_lastException = e;
			return false;
		} catch (InvocationTargetException e) {
			_lastResult = Result.COULD_NOT_INVOKE;
			_lastException = e;
			return false;
		}
		if (_getterMatcher.matches(_lastReturnValue)) {
			_lastResult = Result.MATCH;
			return true;
		} else {
			_lastResult = Result.MISMATCH;
			return false;
		}
	}

	@Override
	public boolean matches(final Object item) {
		// Perform both the objectMatcher and the getterMatcher
		// since we want to store the result for all matchers later.
		final boolean objectMatches = _objectMatcher.matches(item);
		final boolean getterMatches = test(item);
		return objectMatches && getterMatches;
	}

	@Override
	protected void describeTo(final MultilineDescription description) {
		_objectMatcher.describeTo(description);
		switch (_lastResult) {
		case MISMATCH:
		case COULD_NOT_INVOKE:
			description
				.appendNewline()
				.appendText("where ")
				.appendText(_getterName)
				.appendText(" ")
				.appendDescriptionOf(_getterMatcher);
			break;
		default:
			// Don't write to description.
		}
	}

	@Override
	protected void describeMismatch(final Object item, final MultilineDescription description) {
		_objectMatcher.describeMismatch(item, description);
		switch (_lastResult) {
		case MISMATCH:
			description
				.appendNewline()
				.appendText("where ")
				.appendText(_getterName)
				.appendText(" ");
			_getterMatcher.describeMismatch(_lastReturnValue, description);
			break;
		case COULD_NOT_INVOKE:
			description
				.appendNewline()
				.appendText("where ")
				.appendText(item.getClass().getSimpleName())
				.appendText(".").appendText(_getter.getName())
				.appendText("() could not be invoked: ")
				.appendText(_lastException.getMessage());
			break;
		default:
			// Don't write to description
		}
	}

	private static Method getGetter(final Class<?> clazz, String getterName) {
		try {
			return clazz.getMethod(methodNameOf(getterName));
		} catch (NoSuchMethodException e) {
			throw new IllegalArgumentException(
				String.format("No such getter in %s by the name of %s"
					, clazz.getSimpleName()
					, getterName
				)
				, e
			);
		}
	}

	private static String methodNameOf(final String getterName) {
		if (getterName.isEmpty()) return "get";
		return String.format("get%s%s"
			, Character.toUpperCase(getterName.charAt(0))
			, getterName.substring(1)
		);
	}
}

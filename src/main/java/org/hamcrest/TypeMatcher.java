package org.hamcrest;

public class TypeMatcher<T> extends ObjectMatcher<T> {
	private final String _objectName;
	private final String _prefix;

	public TypeMatcher(final Class<T> clazz) {
		super(clazz);
		_objectName = clazz.getSimpleName();
		_prefix = getPrefix(_objectName);
	}

	@Override
	public GetterMatcher<T> where(final String name, final Matcher<?> matcher) {
		return new GetterMatcher<T>(this, name, matcher);
	}

	@Override
	public boolean matches(final Object item) {
		return item != null && getObjectClass().isInstance(item);
	}

	@Override
	protected void describeTo(final MultilineDescription description) {
		description.appendText(_prefix).appendText(" ").appendText(_objectName);
	}

	@Override
	protected void describeMismatch(final Object item, final MultilineDescription description) {
		if (item == null) {
			description.appendText("was null");
			return;
		}

		final String itemClassName = item.getClass().getSimpleName();
		description
			.appendText("was ")
			.appendText(getPrefix(itemClassName))
			.appendText(" ")
			.appendText(itemClassName);
	}

	private static String getPrefix(String objectName) {
		switch (Character.toUpperCase(objectName.charAt(0))) {
		case 'A':
		case 'E':
		case 'I':
		case 'O':
		case 'U':
			return "an";
		default:
			return "a";
		}
	}
}

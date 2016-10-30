package org.diylc.core.annotations;

public enum KeywordPolicy {
  /**
   * The component is represented with its type.
   */
  SHOW_TYPE_NAME,
  /**
   * The component is represented with its value.
   */
  SHOW_VALUE,
  /**
   * The component is not important enough and will not be shown in the keywords.
   */
  NEVER_SHOW,
  /**
   * The component is represented with the value of {@link ComponentDescriptor#keywordTag()}
   */
  SHOW_TAG,
  /**
   * The component is represented with both the value of {@link ComponentDescriptor#keywordTag()}
   * and the actual component value
   */
  SHOW_TAG_AND_VALUE,
}

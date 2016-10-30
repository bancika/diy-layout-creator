package org.diylc.utils;

import java.util.HashSet;
import java.util.Set;

import org.diylc.common.ComponentType;
import org.diylc.core.IDIYComponent;
import org.diylc.core.Project;
import org.diylc.core.annotations.KeywordPolicy;
import org.diylc.presenter.ComponentProcessor;

public class KeywordExtractor {

  private static KeywordExtractor instance;

  public static KeywordExtractor getInstance() {
    if (instance == null) {
      instance = new KeywordExtractor();
    }
    return instance;
  }

  private KeywordExtractor() {}

  @SuppressWarnings("unchecked")
  public String extractKeywords(Project project) {
    Set<String> words = new HashSet<String>();
    for (IDIYComponent<?> c : project.getComponents()) {
      ComponentType cType =
          ComponentProcessor.getInstance().extractComponentTypeFrom((Class<? extends IDIYComponent<?>>) c.getClass());
      if (cType.getKeywordPolicy() == KeywordPolicy.SHOW_TYPE_NAME)
        words.add(cType.getName().toLowerCase());
      if ((cType.getKeywordPolicy() == KeywordPolicy.SHOW_VALUE || cType.getKeywordPolicy() == KeywordPolicy.SHOW_TAG_AND_VALUE)
          && c.getValueForDisplay() != null && c.getValueForDisplay().trim().length() > 0)
        words.add(c.getValueForDisplay().trim().toLowerCase());
      if ((cType.getKeywordPolicy() == KeywordPolicy.SHOW_TAG || cType.getKeywordPolicy() == KeywordPolicy.SHOW_TAG_AND_VALUE)
          && cType.getKeywordTag() != null && cType.getKeywordTag().length() > 0)
        words.add(cType.getKeywordTag().trim().toLowerCase());
    }
    StringBuilder sb = new StringBuilder();
    for (String w : words)
      sb.append(w).append(",");
    if (sb.length() > 0)
      sb.deleteCharAt(sb.length() - 1);
    return sb.toString();
  }
}

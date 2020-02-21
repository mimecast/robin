package com.mimecast.robin.assertion;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Assert external group.
 *
 * <p>Container class for pattern groups used for external logs assertion.
 *
 * @see AssertExternal
 */
@SuppressWarnings("UnusedReturnValue")
public class AssertExternalGroup {

    /**
     * Compiled regex patterns.
     */
    private List<Pattern> patterns = new ArrayList<>();

    /**
     * Matched regex patterns.
     */
    private final List<Pattern> matched = new ArrayList<>();

    /**
     * Gets unmatched.
     *
     * @return Patterns list.
     */
    public List<Pattern> getUnmatched() {
        List<Pattern> unmatched = new ArrayList<>();
        for (Pattern pattern : patterns) {
            if (!matched.contains(pattern)) {
                unmatched.add(pattern);
            }
        }
        return unmatched;
    }

    /**
     * Gets matched.
     *
     * @return Patterns list.
     */
    public List<Pattern> getMatched() {
        return matched;
    }

    /**
     * Adds unmatched.
     *
     * @param pattern Pattern.
     * @return Self.
     */
    public AssertExternalGroup addMatched(Pattern pattern) {
        if (!matched.contains(pattern)) {
            matched.add(pattern);
        }
        return this;
    }

    /**
     * Gets patterns.
     *
     * @return Patterns list.
     */
    public List<Pattern> getPatterns() {
        return patterns;
    }

    /**
     * Sets patterns.
     *
     * @param list Patterns list.
     * @return Self.
     */
    public AssertExternalGroup setPatterns(List<Pattern> list) {
        patterns = list;
        return this;
    }

    /**
     * Has matched.
     *
     * @return Boolean.
     */
    public Boolean hasMatched() {
        return matched.size() == patterns.size();
    }
}

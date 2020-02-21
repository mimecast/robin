package com.mimecast.robin.assertion;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Assert group.
 * <p>Container class for pattern groups used for MTA assertion.
 *
 * @see AssertExternal
 */
@SuppressWarnings("UnusedReturnValue")
public class AssertGroup {

    /**
     * Regex rules to match against.
     */
    private List<String> rules = new ArrayList<>();

    /**
     * Compiled regex patterns.
     */
    private List<Pattern> patterns = new ArrayList<>();

    /**
     * Matched regex patterns.
     */
    private final List<Pattern> matched = new ArrayList<>();

    /**
     * Gets rules.
     *
     * @return Regex list.
     */
    public List<String> getRules() {
        return rules;
    }

    /**
     * Sets rules.
     *
     * @param list Regex list.
     * @return Self.
     */
    public AssertGroup setRules(List<String> list) {
        rules = list;
        return this;
    }

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
    public AssertGroup addMatched(Pattern pattern) {
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
    public AssertGroup setPatterns(List<Pattern> list) {
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

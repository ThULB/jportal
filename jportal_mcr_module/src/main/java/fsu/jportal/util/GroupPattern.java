package fsu.jportal.util;

import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GroupPattern {
    private final Pattern pattern;
    private final Set<String> groupNames;

    public GroupPattern(String regex) {
        this.pattern = Pattern.compile(regex);
        this.groupNames = getNamedGroupCandidates(regex);
    }

    public HashMap<String, String> parse(String input){
        Matcher matcher = pattern.matcher(input);

        HashMap<String, String> groupMatch = new HashMap<>();
        while (matcher.find()) {
            groupNames.forEach(name -> groupMatch.put(name, matcher.group(name)));
        }

        return groupMatch;
    }

    private Set<String> getNamedGroupCandidates(String regex) {
        Set<String> namedGroups = new TreeSet<>();

        Matcher m = Pattern.compile("\\(\\?<([a-zA-Z][a-zA-Z0-9]*)>").matcher(regex);

        while (m.find()) {
            namedGroups.add(m.group(1));
        }

        return namedGroups;
    }
}

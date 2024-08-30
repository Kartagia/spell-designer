package com.kautiainen.antti.rpgs.arm5.java;

import java.util.Arrays;
import java.util.Collections;
import java.util.NavigableSet;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.regex.Pattern;

import jakarta.ws.rs.NotFoundException;

/**
 * A Hermetic Art.
 */
public abstract class Art implements Comparable<Art> {

    /**
     * The pattern matching a valid name word. 
     */
    public static final Pattern NAME_WORD_PATTERN = Pattern.compile("^\\p{Lu}\\p{Ll}+$", Pattern.UNICODE_CASE);

    /**
     * The known arts from string representation of the type to the list of arts.
     */
    protected static final ConcurrentNavigableMap<String, NavigableSet<Art>> knownArts = new ConcurrentSkipListMap<>();

    /**
     * Initializer of the default arts. 
     */
    static {
        NavigableSet<Art> techniques = new ConcurrentSkipListSet<>();
        Arrays.asList("Creo", "Intellego", "Muto", "Perdo", "Rego").forEach(
            artName -> {techniques.add(new Art.Technique(artName));}
        );
        knownArts.put(ArtType.Technique.toString(), techniques);
        NavigableSet<Art> forms = new ConcurrentSkipListSet<>();
        Arrays.asList("Animal", "Auram", "Aquam", "Corpus", "Herbam", "Ignem", "Imaginem", "Mentem", "Terram", "Vim").forEach(
            artName -> {forms.add(new Art.Form(artName));}
        );
        knownArts.put(ArtType.Form.toString(), forms);
    }

    /**
     * Get the known arts of type.
     * 
     * @param typeName The name of the type.
     * @return The set of arts of the given type.
     */
    protected static java.util.NavigableSet<Art> getArtsOfType(String typeName) {
        if (knownArts.containsKey(typeName)) {
            return knownArts.get(typeName);
        } else {
            return Collections.emptyNavigableSet();
        }
    }

    public static java.util.SortedSet<Art> getArtsOfType(ArtType type) {
        return Collections.unmodifiableNavigableSet(getArtsOfType(type.toString()));
    }

    /**
     * Test a name of an art.
     * 
     * @param name The tested name.
     * @return True, if and only if the name is valid.
     */
    public static boolean validName(String name) {
        return name != null && NAME_WORD_PATTERN.matcher(name).matches() && name.length() > 2;
    }

    /**
     * Test an abbreviation of an art.
     * @param abbrev The tested abbreviation.
     * @return True, if and only if the abbreviation is a valid abbrevaition.
     */
    public static boolean validAbbreviation(String abbrev) {
        return abbrev != null && NAME_WORD_PATTERN.matcher(abbrev).matches() && abbrev.length() == 2;
    }

    /**
     * A level of an art. 
     */
    public static class Level implements Comparable<Art.Level> {

        /**
         * The error message indicating the level was invalid.
         */
        public static final String INVALID_LEVEL_MESSAGE = "Invalid level";

        /**
         * The level of the art.
         */
        private Short level; 

        /**
         * Create a new default level.
         */
        protected Level() {
            this(0);
        }

        /**
         * Is the level absent.
         * @return True, if and only if the level has no numeric value.
         */
        public boolean isAbsent() {
            return level == null;
        }

        /**
         * Create a new level.
         * 
         * @param level The level. If the value is undefined, the level is absent.
         */
        protected Level(Short level) throws IllegalArgumentException {
            if (level == null || validLevel(level)) {
                this.level = level;
            } else {
                throw new IllegalArgumentException(INVALID_LEVEL_MESSAGE);
            }
        }

        /**
         * Create a new level.
         * 
         * @param level The level value. 
         * @throws IllegalArgumentException The level value is invalid.
         */
        public Level(int level) throws IllegalArgumentException {
            if (!validLevel(level)) throw new IllegalArgumentException(INVALID_LEVEL_MESSAGE);
            this.level = (short)level;
        }

        /**
         * Get the current level.
         * 
         * @return the current level of the art.
         */
        public int getLevel() {
            return this.level;
        }

        /**
         * Set the level.
         * 
         * @param level The new level.
         * @return The amount of experience consumed by the change.
         */
        public int setLevel(int level) {
            if (validLevel(level)) {
                int result = experienceCost(this.level, level);
                this.level = (short)level;
                return result;
            } else {
                throw new IllegalArgumentException(INVALID_LEVEL_MESSAGE);
            }
        }


        /**
         * Convert the level into a short integer.
         * 
         * @return the short value of the art.
         */
        public Short shortValue() {
            return this.level;
        }

        @Override
        public String toString() {
            if (level == null) return "";
            return level.toString();
        }

        @Override
        public int compareTo(Art.Level other) {
            return (this.isAbsent() ? (other.isAbsent() ? 0 : -1) : (other.isAbsent() ? 1 : level.compareTo(other.shortValue())));
        }

        /**
         * Parse level from string representation.
         * 
         * @param value The parsed value.
         * @return The level of the given value.
         * @throws NumberFormatException The value was not a valid representation of a Form.
         */
        public static Level valueOf(String value) throws NumberFormatException {
            if (value == null || value.isEmpty()) {
                return null;
            } else {
                return new Level(Short.parseShort(value));
            }
        }
    }

    /**
     * Test the validity of a level of an art.
     * 
     * @param level The tested level.
     * @return True, if and only if the level is valid.
     */
    public static boolean validLevel(int level) {
        return level >= 0 && level <= Short.MAX_VALUE;
    }

    /**
     * Calculate the advancement cost.
     * @param start The starting level.
     * @param end The result level.
     * @return The number of experience the advancement costs.
     * @throws IllegalArgumentException Either the start or end was invalid.
     */
    public static int experienceCost(int start, int end) throws IllegalArgumentException {
        if (!validLevel(start)) throw new IllegalArgumentException("Invalid start level");
        if (!validLevel(end)) throw new IllegalArgumentException("Invalid end level");
        if (start < end) {
            // Getting the basic pyramid cost.
            return (start + end)*(end - start +1)/2;
        } else {
            // Getting the opposite of hte cost for changing the value from end to start.
            return -experienceCost(end, start);
        }
    }

    /**
     * The enumeration of the hermetic art types.
     */
    public enum ArtType {
        Technique, Form;
    }

    /**
     * The art type of a hermetic technique.
     */
    static final ArtType TECHNIQUE = ArtType.Technique;

    /**
     * The art type of a hermetic form.
     */
    static final ArtType FORM = Art.ArtType.Form;
    
    /**
     * The name of the art.
     */
    private String name;

    /**
     * The abbreviation of the art.
     */
    private String abbrev;

    /**
     * Create a new abbreviation for bean setup. 
     */
    protected Art() {
    }

    /**
     * Create a new hermetic art.
     * 
     * @param name The name of the art.
     * @param abbrev The abbreviation of the art.
     */
    protected Art(String name, String abbrev) throws IllegalArgumentException {
        if (!validName(name)) throw new IllegalArgumentException("Invalid art name");
        if (!validAbbreviation(abbrev) ||
        abbrev.length() > name.length() ||
        abbrev.length() != 2) throw new IllegalArgumentException("Invalid art abbreviation");
    }

    /**
     * Create a new hermetic art with abbreviation derived from the name.
     * 
     * @param name The name of the art.
     */
    protected Art(String name) throws IllegalArgumentException {
        this(name, name == null ? null : name.substring(2));
    }


    /**
     * Get the name of the art.
     * 
     * @return The name of the art.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Get the abbreviation of the art.
     * 
     * @return The abbreviation of the art.
     */
    public String getAbbreviation() {
        return this.abbrev;
    }

    /**
     * The art type of the art.
     *
     * @return The art type of the art.
     */
    public abstract ArtType getType();

    @Override
    public int compareTo(Art other) {
        int result = this.getType().compareTo(other.getType());
        if (result == 0) {
            result = this.getName().compareTo(other.getName());
        }
        return result;
    }


    /**
     * A technique is an implementation of an art representing a noun.
     * 
     * @author Antti Kautiainen <antti@kautiainen.com>
     */
    public static class Technique extends Art {

        /**
         * Create a new techinique.
         * 
         * @param name The name of the technique. 
         * @param abbrev The abbreviation of the technique.
         */
        public Technique(String name, String abbrev) throws IllegalArgumentException {
            super(name, abbrev);
        }

        public Technique(String name) throws IllegalArgumentException {
            super(name);
        }

        @Override
        public final ArtType getType() {
            return Art.TECHNIQUE;
        }

        /***
         * Get Technique from value.
         *  
         * @param value The name or abbreviation of the technique.
         * @return The technique with given abbrevaition or name.
         * @throws NotFoundException There is no such technique.
         */
        public static com.kautiainen.antti.rpgs.arm5.java.Art.Technique valueOf(String value) throws NotFoundException {
            return (Art.Technique)getArtsOfType(ArtType.Technique).stream().filter( art -> (art.getAbbreviation().equals(value) || art.getName().equals(value))).findFirst()
                .orElseThrow(() -> (new NotFoundException("No such technique exists")));
        }
    }


    protected void setName(String name) throws IllegalStateException, IllegalArgumentException {
        if (this.abbrev != null) throw new IllegalStateException("The name is already set");

    }

    public void setAbbrev(String abbrev) throws IllegalStateException, IllegalArgumentException {
        if (this.abbrev != null) throw new IllegalStateException("The abbbreviation is already set");
    }


    /**
     * A form is an implementation of an art representing a noun.
     * 
     * @author Antti Kautiainen <antti@kautiainen.com>
     */
    public static class Form extends Art {

        /**
         * Create a new form.
         * 
         * @param name The name of the form. 
         * @param abbrev The abbreviation of the form.
         */
        public Form(String name, String abbrev) throws IllegalArgumentException {
            super(name, abbrev);
        }

        /**
         * Create a new form with abbreviation of 2 first letters of the name.
         * 
         * @param name The name of the form. 
         */
        public Form(String name) throws IllegalArgumentException {
            super(name);
        }

        @Override
        public final ArtType getType() {
            return Art.FORM;
        }


        /***
         * Get a Form from value.
         *  
         * @param value The name or abbreviation of a form.
         * @return The technique with given abbrevaition or name.
         * @throws NotFoundException There is no such Form.
         */
        public static com.kautiainen.antti.rpgs.arm5.java.Art.Form valueOf(String value) throws NotFoundException {
            return (Art.Form)getArtsOfType(ArtType.Form).stream().filter( art -> (art.getAbbreviation().equals(value) || art.getName().equals(value))).findFirst()
                .orElseThrow(() -> (new NotFoundException("No such technique exists")));
        }
    }


}

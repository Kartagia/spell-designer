package com.kautiainen.antti.rpgs.arm5.java.spelldesigner;

import java.util.Collections;
import java.util.Comparator;
import java.util.NavigableSet;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.regex.Pattern;

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
    protected static final ConcurrentNavigableMap<ArtType, NavigableSet<Art>> knownArts = new ConcurrentSkipListMap<>();

    /**
     * Add known art tyep.
     *
     * @param artType The new art type.
     * @return True, if and only if the art was added.
     */
    protected static final boolean addKnownArtType(Art.ArtType artType) {
        if (artType == null || knownArts.containsKey(artType)) {
            return false;
        } else {
            knownArts.put(artType, new ConcurrentSkipListSet<>());
            return true;
        }
    }

    /**
     * Add known art. 
     *
     * @param art The added art.
     * @return True, if and only if the art was added.
     */
    protected static final boolean addKnownArt(Art art) {
        addKnownArtType(art.getType());
        return knownArts.get(art.getType()).add(art);
    }

    /**
     * Get the known arts of type.
     * 
     * @param typeName The name of the type.
     * @return The set of arts of the given type.
     */
    public static java.util.NavigableSet<Art> getArtsOfType(String typeName) {
        ArtType type = knownArts.keySet().stream().filter( current -> (current.toString().equals(typeName))).findFirst().orElse(null);
        if (type == null) {
            return Collections.emptyNavigableSet();
        } else {
            return Collections.unmodifiableNavigableSet(knownArts.get(type));
        }
    }

    public static java.util.SortedSet<Art> getArtsOfType(ArtType type) {
        if (type != null && knownArts.containsKey(type)) {
            return Collections.unmodifiableNavigableSet(knownArts.get(type));
        } else {
            return Collections.emptyNavigableSet();
        }
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
     * The interface of the art type.
     */
    public interface ArtType {

        /**
         * The comaprator of the art types.
         */
        static final Comparator<ArtType> COMPARATOR = (ArtType compared, ArtType comparee) -> {
            int result = compared.getKey().compareTo(comparee.getKey());
            // Techniues before Forms before neither.
            if (result == 0) {
                if (compared.isTechnique()) {
                    result = (compared.isTechnique() ? 0 : -1);
                } else if (comparee.isTechnique()) {
                    result = 1;
                } else if (compared.isForm()) {
                    return comparee.isForm() ? 0 : -1;
                } else if (comparee.isForm()) {
                    return -1;
                }
            }
            if (result == 0) {
                return compared.getName().compareTo(comparee.getName());
            }
            return result;
        };

        /**
         * The key value of the art type.
         */
        String getKey();

        /**
         * The name of the art type.
         */
        String getName();

        /**
         * Is the art type technique used as a verb.
         * 
         * @return True, if and only if the art type is technique.
         */
        default boolean isTechnique() { return false; } 

        /**
         * Is the art type form used as a noun.
         * 
         * @reutrn True, if and only if the art type is a form.
         */
        default boolean isForm() {return !isTechnique(); }

        /**
         * Natural order comparison by the Art Type.
         * @param other The art type compared iwth.
         * @return The result of the {@link java.util.Comparator#compare}.
         * @throws NullPointerException The other was undefined.
         */
        default int compareTo(ArtType other) throws NullPointerException {
            return COMPARATOR.compare(this, other);
        }
    };

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
        int result = ArtType.COMPARATOR.compare(this.getType(), other.getType());
        if (result == 0) {
            result = this.getName().compareTo(other.getName());
        }
        return result;
    }


    /**
     * The ininial setting of the art name.
     * @param name The name of the art.
     * @throws IllegalStateException The name has already been set.
     * @throws IllegalArgumentException The name was invalid.
     */
    protected void setName(String name) throws IllegalStateException, IllegalArgumentException {
        if (this.name != null) throw new IllegalStateException("The name is already set");
        if (validName(name)) {
            this.name = name;
        } else {
            throw new IllegalArgumentException("Invalid name");
        }
    }

    /**
     * The ininial setting of the abbreviation of the art.
     * @param abbrev The abbreviation of the art.
     * @throws IllegalStateException The abbreviation has already been set.
     * @throws IllegalArgumentException The abbreviation was invalid.
     */
    public void setAbbrev(String abbrev) throws IllegalStateException, IllegalArgumentException {
        if (this.abbrev != null) throw new IllegalStateException("The abbbreviation is already set");
        if (validAbbreviation(abbrev)) {
            this.abbrev = abbrev;
        } else {
            throw new IllegalArgumentException("Invalid abbreviation");
        }
    }
}

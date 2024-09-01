/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.kautiainen.antti.rpgs.arm5.java.spelldesigner;

import java.util.Optional;

import jakarta.resource.spi.IllegalStateException;


/**
 * A class representing a spell guideline.
 * 
 * @author Antti Kautiainen <antti@kautiainen.com>
 */
public class SpellGuideline<TECHNIQUE_TYPE extends TechniqueArtType,
TECHNIQUE extends TechniqueInterface<TECHNIQUE_TYPE>, 
FORM_TYPE extends FormArtType, 
FORM extends FormInterface<FORM_TYPE>> {

  static short levelToMagnitude(short level) {
    if (level > 4) {
      return (short)(level / 5 + (level % 5 == 0 ? 0 : 1));
    } else if (level < 0) {
      return (short)(-4 - levelToMagnitude((short)(-level)));
    } else {
      return (short)(level-4);
    }
  }

  static short magnitudeToLevel(short magnitude) {
    if (magnitude <= -8) {
      return (short)(-5*magnitudeToLevel((short)(9-magnitude)));
    } else if (magnitude < 1) {
      return (short)(4+magnitude);
    } else {
      return (short)(magnitude*5);
    }
  }

  /**
   * A sequence containing a reference.
   */
  public static interface ReferenceSequence extends CharSequence  {

    @Override
    default char charAt(int index) {
      return toString().charAt(index);
    }

    @Override
    default int length() {
      return toString().length();
    }

    @Override
    default CharSequence subSequence(int start, int end) {
      return toString().subSequence(start, end);
    }

    /**
     * The string representatin with reference replaced with a value.
     * 
     * @param replacement The string replacing the reference. If the replacement is
     * not defined, the replacement description is used instead.
     * @return The string with reference replaced either with the replacemetn description,
     * or the given replcement string.
     */
    public String toString(String replacement);
  }

  /**
   * A reference containing replacement with level value.
   */
  public static interface LevelReference extends ReferenceSequence {


    /**
     * Create string representation of the level reference with a referred value.
     * 
     * @param level The replacement level. An undefined value indicates
     * the generic description is used.
     */
    default String toString(Short level) {
      return level == null ? toString() : toString(String.valueOf(level));
    }
  };

  /**
   * Magnitude reference sequence.
   */
  public static class MagnitudeReference implements LevelReference {

    private Integer modifier = null;

    public MagnitudeReference() {
      this(0);
    }

    @SuppressWarnings("")
    public MagnitudeReference(int magnitudeModifier) {
      try {
        setMagnitudeModifier(modifier);
      } catch(IllegalStateException ase){
        throw new Error("The magntiude modifier initialzied before construction");
      }
    }

    /**
     * Test validity of the modifier.
     * 
     * @param modifier The tested modifier.
     * @returns True, if and only if the modifier is a vlaid magnitud emodifier.
     * @implNote The method is called during constructor. Do not use internal state.
     */
    public boolean validModifier(int modifier) {
      return (Short.MIN_VALUE+8)/5 <= modifier && modifier < Short.MAX_VALUE/5;
    }

    /**
     * Set the magnitude modifier.
     * @implNote The method is called in constructor, thus subclass implementation should not
     * rely on internal state of the implementing instance.
     */
    protected void setMagnitudeModifier(int modifier) throws IllegalStateException, IllegalArgumentException {
      if (this.modifier != null) throw new IllegalStateException("The modifier has already been set");
      if (validModifier(modifier)) {
        this.modifier = modifier;
      } else {
        throw new IllegalArgumentException("Invalid magnitude modifier");
      }
    }

    @Override
    public String toString() {
      return toString(String.format("(level %s %d)", modifier < 0 ? "-" : "+", Math.abs(modifier)));
    }

    @Override
    public String toString(Short level) {
      if (level == null) return toString();
      else return toString(level.shortValue());
    }

    public String toString(short level) {
      return String.format("%d", magnitudeToLevel((short)(levelToMagnitude(level) + modifier)));
    }

    @Override
    public String toString(String replacement) {
      if (replacement == null) {
        return toString();
      } else {
        return replacement;
      }
    }

    public String toString(Art.Level level) {
      if (level.isAbsent()) {
        return toString();
      } else {
        return toString(level.shortValue());
      }
    }
  }

  public static class SequenceWithMagnitudeReference extends MagnitudeReference {

    private String prefix = "";

    private String suffix = "";

    public SequenceWithMagnitudeReference(String prefix, short modifier, String suffix) {
      super(modifier);
      this.prefix = (prefix == null ? "" : prefix);
      this.suffix = (suffix == null ? "" : suffix);
    }

    /**
     * Create a sequence with magnitude reference of a prefix followed by a magnitude modifier.
     *
     * @param prefix The prefix. An undefined value defaults to empty string.
     * @param modifier The magnitude modifier value.
     * @return The sequence with magntiude modifier. 
     */
    public static SequenceWithMagnitudeReference prefixed(String prefix, short modifier) {
      return new SequenceWithMagnitudeReference(prefix, modifier, null);
    }

    /**
     * Create a sequence with a magnitude modifier followed by a suffix.
     *
     * @param modifier The magnitude modifier value.
     * @param suffx The suffix of the modifier. An undefined value defaults to empty string.
     * @return The sequence with magntiude modifier. 
     */
    public static SequenceWithMagnitudeReference suffixed(short modifier, String suffix) {
      return new SequenceWithMagnitudeReference(null, modifier, suffix);
    }

    /**
     * Create descriptoin with reference replacing reference with generic description.
     * 
     * @return Return the string representation with relative magnitude reference value.
     */
    @Override
    public String toString() {
      return toString((Short)null);
    }

    /**
     * Create description with reference replaced with the actual level.
     * 
     * @param level The placeholded leve.
     */
    public String toString(GuidelineLevel level) {
      return toString(super.toString(level));
    }

    /**
     * Create description with reference replaced with the actual level.
     * 
     * @param level The placeholded leve.
     */
    @Override
    public String toString(Short level) {
      return toString(super.toString(level));      
    }

    @Override
    public String toString(String replacement) {
      if (replacement == null) return toString();
      else 
        return String.format("%s%s%s", prefix, replacement, suffix);
    }
  }

  /**
   * A spell guideline level includes value of Generic, which is not numeric.
   */
  public static class GuidelineLevel extends Art.Level {

    protected GuidelineLevel() {
      super();
    }

    public GuidelineLevel(Short level) {
      super(level);
    }

    @Override
    public String toString() {
        if (isAbsent()) return "Generic";
        return super.toString();
    }

    /**
     * Parse level from string representation.
     * 
     * @param value The parsed value.
     * @return The level of the given value.
     * @throws NumberFormatException The value was not a valid representation of a Form.
     */
    public static GuidelineLevel valueOf(String value) throws NumberFormatException {
        if (value == null || value.isEmpty() || "Generic".equalsIgnoreCase(value)) {
            return new GuidelineLevel(null);
        } else {
            return new GuidelineLevel(Short.valueOf(value));
        }
    }
  }


  /**
   * The error message that the name was already initialized.
   */
  public static final String NAME_ALREADY_SET_EXCEPTION = "Name already initialized";

  /**
   * The error message that the name was invalid.
   */
  public static final String INVALID_NAME_EXCEPTION = "Invalid name";


  /**
   * The error message that the description was already initialized.
   */
  public static final String DESCRIPTION_ALREADY_SET_EXCEPTION = "Description already initialized";

  /**
   * The error message that the description was invalid.
   */
  public static final String INVALID_DESCRIPTION_EXCEPTION = "Invalid description";

  /**
   * The error message that the level was already initialized.
   */
  public static final String LEVEL_ALREADY_SET_EXCEPTION = "Level already initialized";

  /**
   * The error message that the level was invalid.
   */
  public static final String INVALID_LEVEL_EXCEPTION = "Invalid level";

  /**
   * The error message that the form was already initialized.
   */
  public static final String FORM_ALREADY_SET_EXCEPTION = "Form already initialized";

  /**
   * The error message that the form was invalid.
   */
  public static final String FORM_LEVEL_EXCEPTION = "Invalid form";

  /**
   * The error message that the technique was already initialized.
   */
  public static final String TECHNIQUE_ALREADY_SET_EXCEPTION = "Technique already initialized";

  /**
   * The error message that the technique was invalid.
   */
  public static final String INVALID_TECHNIQUE_EXCEPTION = "Invalid technique";


  /**
   * The technique of the guideline.
   */
  private TECHNIQUE technique; 

  /**
   * The art of the guideline.
   */
  private FORM form; 

  /**
   * The level of the guideline.
   */
  private GuidelineLevel level;

  /**
   * The name of the guideline. 
   * 
   * The name may contain the defining syllable of the gudieline without the ending period.
   */
  private CharSequence name;

  /**
   * The optional description of the guideline without name.
   * 
   * The optional description contains the further sentences of the description.
   */
  private CharSequence description;

  /**
   * Create a spell guideline without values.
   */
  protected SpellGuideline() {
  }

  public String getName() {
    return name.toString();
  }

  /**
   * Initialize the name of the spell guideline.
   * 
   * @param name The name of the spell guideline.
   * @throws IllegalStateException The name has already been set.
   * @throws IllegalArgumentException The name of the guideline is invalid.
   */
  public void setName(CharSequence name) throws IllegalArgumentException, IllegalStateException {
    if (this.name != null) throw new IllegalStateException(NAME_ALREADY_SET_EXCEPTION);
    if (!validName(name)) throw new IllegalArgumentException(INVALID_NAME_EXCEPTION);
    this.name = name;
  }

  /**
   * Test validity of a guidline name.
   * @param name The name of the guideline.
   * @return True, if and only if the guideline is a valid guideline.
   */
  public boolean validName(CharSequence name) {
    if (name == null) return false;
    if (name instanceof String nameString) {
      return !nameString.isEmpty() && nameString.trim().equals(nameString) && !nameString.contains(".");
    } else if (name instanceof LevelReference LevelReference) {
      return validName(LevelReference.toString(this.level.shortValue()));
    } else {
      return validName(name.toString());
    }
  }

  /**
   * Get the description of the spell guideline.
   * 
   * @return The long description, if any exists.
   */
  public Optional<String> getDescription() {
    return Optional.ofNullable(this.description.toString());
  }

  /**
   * Initialize the description of the spell guideline.
   * 
   * @param description The description of the spell guideline.
   * @throws IllegalStateException The description has already been set.
   * @throws IllegalArgumentException The description of the guideline is invalid.
   */
  public void setDescription(CharSequence description) throws IllegalArgumentException, IllegalStateException {
    if (this.description != null) throw new IllegalStateException(DESCRIPTION_ALREADY_SET_EXCEPTION);
    if (!validDescription(description)) throw new IllegalArgumentException(INVALID_DESCRIPTION_EXCEPTION);
    this.description = description;
  }

  /**
   * Test validity of a description.
   * @param description The tested description.
   * @return True, if and only if the description is valid.
   */
  public boolean validDescription(CharSequence description) {
    return description == null || description.length() != 0;
  }

  /**
   * Get the guideline level.
   * 
   * @return The guideline level. This value is always defined for a valid guideline.
   */
  public GuidelineLevel getLevel() {
    return this.level;
  }

  /**
   * Initialize the level of hte spell guideline.
   * 
   * @param level The new level of the guideline.
   * @throws IllegalStateException The level has already been set.
   * @throws IllegalArgumentException The level of the guideline is invalid.
   */
  public void setLevel(GuidelineLevel level) throws IllegalArgumentException, IllegalStateException {
    if (this.level != null) throw new IllegalStateException(LEVEL_ALREADY_SET_EXCEPTION);
    this.level = level;
  }

  /**
   * get the technique of the guideline.
   * 
   * @return The technique of the guideline.
   */
  public TechniqueInterface<? extends TechniqueArtType> getTechnique() {
    return this.technique;
  }

  /**
   * Inititalize the technique of the guideline.
   * 
   * @param technique The technique of the guideline.
   * @throws IllegalStateException The technique has already been set.
   * @throws IllegalArgumentException The technique is invalid.
   */
  public void setTechnique(TECHNIQUE technique) throws IllegalStateException {
    if (this.technique != null) throw new IllegalStateException(TECHNIQUE_ALREADY_SET_EXCEPTION);
    this.technique = technique;
  }

  /**
   * get the form of the guideline.
   * 
   * @return The form of the guideline.
   */
  public FormInterface<? extends FormArtType> getForm() {
    return this.form;
  }

  /**
   * Inititalize the form of the guideline.
   * 
   * @param form The form of the guideline.
   * @throws IllegalStateException The form has already been set.
   * @throws IllegalArgumentException The form is invalid.
   */
  public void setForm(FORM form) throws IllegalStateException {
    if (this.form != null) throw new IllegalStateException(FORM_ALREADY_SET_EXCEPTION);
    this.form = form;
  }


  @Override
  public String toString() {
    return String.format(
      "%s%s%s: %s.%s",
      this.getTechnique().getAbreviation().orElse(technique.getName()), 
      this.getForm().getAbreviation().orElse(form.getName()),
      this.getLevel().toString(),
      this.getName(), 
      this.getDescription().orElse("")
    );
  }


}

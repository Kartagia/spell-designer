/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.kautiainen.antti.rpgs.arm5.java.spelldesigner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NavigableSet;
import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.function.Supplier;

/**
 * The class representing Hermeitc Arts.
 *
 * @author Antti Kautiainen <antti@kautiainen.com>
 */
public class HermeticArts {


  /**
   * The technique type.
   */
  public static final TechniqueArtType TECHNIQUE_TYPE = new TechniqueArtType() {

    @Override
    public String getKey() {
      return HermeticArtType.Technique.getKey();
    }

    @Override
    public String getName() {
      return HermeticArtType.Technique.getName();
    }
    
  };

  /**
   * The technique type.
   */
  public static final FormArtType FORM_TYPE = new FormArtType() {

    @Override
    public String getKey() {
      return HermeticArtType.Form.getKey();
    }

    @Override
    public String getName() {
      return HermeticArtType.Form.getName();
    }
    
  };

  /**
   * The enumeration of the Hermetic Techniques.
   */
  public static enum HermeticArtType implements Art.ArtType {
    Technique("Technique", true), Form("Form", false);

    final boolean isTechnique;

    final String name;

    /**
     * Create a new hermetic Art Type.
     * 
     * @param name The name of the type.
     * @param isTechnique Is the type technique.
     */
    HermeticArtType(String name, boolean isTechnique) {
      if (Art.validName(name)) {
        this.name = name;
      } else {
        throw new IllegalArgumentException("Invalid art type name");
      }
      this.isTechnique = isTechnique;
    }



    @Override
    public String getKey() {
      return "Hermetic";
    }

    @Override
    public String getName() {
      return this.name;
    }

    @Override
    public boolean isTechnique() {
      return isTechnique;
    }
    
  }

  /**
   * The default techinique naems.
   */
  public static final java.util.List<String> DEFAULT_TECHNIQUE_NAMES = Arrays.asList("Creo", "Intellego", "Muto", "Perdo", "Rego");

  /**
   * The default form naems.
   */
  public static final java.util.List<String> DEFAULT_FORM_NAMES = Arrays.asList(
    "Animal", "Auram", "Aquam", "Corpus", "Herbam", 
    "Ignem", "Imaginem", "Mentem", "Terram", "Vim"
  );

  /**
   * The default art names.
   */
  public static final java.util.List<String> DEFAULT_ART_NAMES = Collections.unmodifiableList(
    ((Supplier<List<String>>) (() -> {
      ArrayList<String> result = new ArrayList<>(DEFAULT_TECHNIQUE_NAMES);
      result.addAll(DEFAULT_FORM_NAMES);
      return result;
    })).get()
  );

  /**
   * The default Hermetic arts.
   */
  public static final HermeticArts DEFAULT_ARTS = new HermeticArts();

  /**
   * The techniques of the arts.
   */
  private NavigableSet<Art> technqiues;

  /**
   * The forms of the arts.
   */
  private NavigableSet<Art> forms;

  /**
   * Create Hermetic Arts with default names.
   */
  public HermeticArts() {
    this(DEFAULT_TECHNIQUE_NAMES, DEFAULT_FORM_NAMES);
  }

  /**
   * Create a new art with abbreviation cut 2 first letters of the name.
   * 
   * @param type The type of the art.
   * @param name The name of the art.
   * @return The created art.
   * @throws IllegalArgumentException The name, or type was invalid.
   */
  public Art createArt(Art.ArtType type, String name) throws IllegalArgumentException {
    return createArt(type, name, Optional.ofNullable(name).orElseThrow( () -> (
      new IllegalArgumentException("Invalid art name")
    )).substring(2));
  }

  /**
   * Create a new art.
   * 
   * @param type The type of the art.
   * @param name The name of the art.
   * @param abbrev The abbreviation of the art.
   * @return The created art.
   * @throws IllegalArgumentException The name, type, or abbrevaition was invalid.
   */
  public Art createArt(Art.ArtType type, String name, String abbrev) throws IllegalArgumentException {
    if (type == null) throw new IllegalArgumentException("Invalid art type");
    if (HermeticArtType.Technique.equals(type)) {
      return new HermeticTechnique(name, abbrev);
    } else if (HermeticArtType.Form.equals(type)) {
      return new HermeticForm(name, abbrev);
    } else {
      return new Art(name, abbrev) {

        @Override
        public ArtType getType() {
          return type;
        }
      
      };
    }
  }

  /**
   * Create a new Hermeitc Arts with custon manes.
   * @param techniqueNames The technique names.
   * @param formNames The form names.
   */
  public HermeticArts(List<String> techniqueNames, List<String> formNames) {
    this(HermeticArtType.Technique, techniqueNames, HermeticArtType.Form, formNames);
  }

  /**
   * Create a new Hermetic Arts with custom technique type and names, and form type and form names.
   * 
   * @param techniqueType The custon type of the techniques.
   */
  @SuppressWarnings("")
  protected HermeticArts(Art.ArtType techniqueType,List<String> techniqueNames, Art.ArtType formType, List<String> formNames) {
    this.technqiues = new ConcurrentSkipListSet<>();
    for (int i=0, end = techniqueNames.size(); i < end; i++) {
      technqiues.add(createArt(techniqueType, techniqueNames.get(i)));      
    }
    this.forms = new ConcurrentSkipListSet<>();
    for (int i=0, end = formNames.size(); i < end; i++) {
      forms.add(createArt(formType, formNames.get(i)));      
    }
  }


  /**
   * Class representing a hermetic technique.
   */
  public static class HermeticTechnique extends Art implements TechniqueInterface<TechniqueArtType> {

    public HermeticTechnique(String name) {
      super(name);
    }

    public HermeticTechnique(String name, String abbreviation) throws IllegalArgumentException {
      super(name, abbreviation);
    }

    @Override
    public final TechniqueArtType getType() {
      return HermeticArts.TECHNIQUE_TYPE;
    }

    @Override
    public Optional<String> getAbreviation() {
      return super.getAbbreviation();
    }

  }

  public static class HermeticForm extends Art implements FormInterface<FormArtType> {

    public HermeticForm(String name) {
      super(name);
    }

    public HermeticForm(String name, String abbreviation) throws IllegalArgumentException {
      super(name, abbreviation);
    }

    @Override
    public final FormArtType getType() {
      return HermeticArts.FORM_TYPE;
    }

    @Override
    public Optional<String> getAbreviation() {
      return super.getAbbreviation();
    }
  }


  /**
   * Get technique from the arts.
   *
   * @param techniqueName The name of the technique.
   * @return The technique with given name, if the arts has one.
   */
  public Optional<HermeticTechnique> getTechnique(String techniqueName) {
    return Optional.ofNullable(
      (HermeticTechnique)this.technqiues.stream().filter( current -> (current.getName().equals(techniqueName))).findFirst().orElse(null)
    );
  }

  /**
   * Get form from the arts.
   *
   * @param formName The name of the form.
   * @return The form with given name, if the arts has one.
   */
  public Optional<HermeticForm> getForm(String formName) {
    return Optional.ofNullable(
      (HermeticForm)this.forms.stream().filter( current -> (current.getName().equals(formName))).findFirst().orElse(null)
    );
  }
}

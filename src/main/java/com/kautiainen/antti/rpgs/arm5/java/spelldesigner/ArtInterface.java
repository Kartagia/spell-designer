package com.kautiainen.antti.rpgs.arm5.java.spelldesigner;

import java.util.Optional;

/**
 * An interface representing an art.
 */
public interface ArtInterface<TYPE extends Art.ArtType> {

  /**
   * The name of hte art. 
   * @return The name of the art.
   */
  String getName();

  /**
   * The type of the art.
   */
  TYPE getType();

  /**
   * The optional abbreviation of the art.
   * 
   * @return The abbreviation of the art.
   */
  Optional<String> getAbreviation();
}
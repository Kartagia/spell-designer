package com.kautiainen.antti.rpgs.arm5.java.spelldesigner;

/**
 * An art type subinterface for technique types.
 */
public interface TechniqueArtType extends Art.ArtType {

  @Override
  default boolean isTechnique() {
    return true;
  }
}
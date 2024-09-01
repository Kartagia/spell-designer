package com.kautiainen.antti.rpgs.arm5.java.spelldesigner;

/**
 * The art types, which represnts forms.
 */
public interface FormArtType extends Art.ArtType {

  @Override
  default boolean isTechnique() {
    return false;
  }
}
package com.kautiainen.antti.rpgs.arm5.java.spelldesigner;

/**
 * The art types which represents neither technique nor form.
 */
public interface SpecialArtType extends Art.ArtType {
  @Override
  default boolean isTechnique() {
    return false;
  }

  @Override
  default boolean isForm() {
    return false;
  }
}
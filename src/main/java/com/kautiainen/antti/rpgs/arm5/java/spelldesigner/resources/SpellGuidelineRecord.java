package com.kautiainen.antti.rpgs.arm5.java.spelldesigner.resources;

import java.util.Optional;

import com.kautiainen.antti.rpgs.arm5.java.spelldesigner.Art;
import com.kautiainen.antti.rpgs.arm5.java.spelldesigner.FormArtType;
import com.kautiainen.antti.rpgs.arm5.java.spelldesigner.FormInterface;
import com.kautiainen.antti.rpgs.arm5.java.spelldesigner.SpellGuideline;
import com.kautiainen.antti.rpgs.arm5.java.spelldesigner.TechniqueArtType;
import com.kautiainen.antti.rpgs.arm5.java.spelldesigner.TechniqueInterface;



/**
 * The immutable record of a spell guideline.
 */
public record SpellGuidelineRecord(
  TechniqueInterface<TechniqueArtType> tech,
  FormInterface<FormArtType> form, 
  SpellGuideline.GuidelineLevel level,
  String name,
  String description) implements Comparable<SpellGuidelineRecord> {
  
  @Override
  public final int compareTo(SpellGuidelineRecord other) {
    int result = tech().getName().compareTo(other.tech().getName());
    if (result == 0) {
      result = form().getName().compareTo(other.form().getName());
    }
    if (result == 0) {
      Art.Level myLevel = this.level(), otherLevel = other.level();
      if (myLevel == null) {
        result = otherLevel == null ? 0 : -1;
      } else if (otherLevel == null) {
        result = 1;
      } else {
        result = Short.compare(myLevel.shortValue(), otherLevel.shortValue());
      }
    }
    return result;
  }

  @Override
  public final boolean equals(Object other) {
    if (other == this || other == null) return other != null;
    if (other instanceof SpellGuidelineRecord sgRecord) {
      return compareTo(sgRecord) == 0;
    } else {
      return false;
    }
  }

  @Override
  public final int hashCode() {
    return java.util.Objects.hash(tech(), form());
  }

  @Override
  public final String toString() {
    return String.format("%2.2s%2.2s%s: %s. %s", tech().getAbreviation().orElse(tech().getName()), 
    form().getAbreviation().orElse(form().getName()),
    Optional.ofNullable((Object)level()).orElse("Generic"), 
    name(), Optional.ofNullable(description()).orElse(""));
  }
}

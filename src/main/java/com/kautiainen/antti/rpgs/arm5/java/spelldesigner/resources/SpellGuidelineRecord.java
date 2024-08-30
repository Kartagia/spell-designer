package com.kautiainen.antti.rpgs.arm5.java.spelldesigner.resources;

import java.util.Optional;

import com.kautiainen.antti.rpgs.arm5.java.Art;
import com.kautiainen.antti.rpgs.arm5.java.Art.Form;
import com.kautiainen.antti.rpgs.arm5.java.Art.Technique;
import com.kautiainen.antti.rpgs.arm5.java.SpellGuideline;


/**
 * The immutable record of a spell guideline.
 */
public record SpellGuidelineRecord(Technique tech, Form form, SpellGuideline.GuidelineLevel level, String name, String description) implements Comparable<SpellGuidelineRecord> {
  
  @Override
  public final int compareTo(SpellGuidelineRecord other) {
    int result = tech().compareTo(other.tech());
    if (result == 0) {
      result = form().compareTo(form);
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
    return String.format("%2.2s%2.2s%s: %s. %s", tech().getAbbreviation(), form().getAbbreviation(), Optional.ofNullable((Object)level()).orElse("Generic"), 
    name(), Optional.ofNullable(description()).orElse(""));
  }
}

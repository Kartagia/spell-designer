/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.kautiainen.antti.rpgs.arm5.java.spelldesigner;

/**
 * A class representing a spell guideline.
 * 
 * @author Antti Kautiainen <antti@kautiainen.com>
 */
public class SpellGuideline {

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

}

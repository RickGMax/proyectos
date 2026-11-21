package cl.uchile.dcc.model.potions

import cl.uchile.dcc.model.units.Character

/** Represents a potion that increases current defense by 15 percent. */
class StrengthPotion(name: String)
  extends Potion(name, PotionType.Strength) {

  /** Increases the character's current defense. */
  override def applyTo(character: Character): Unit = {
    character.increaseDefense(15.0)
  }
}
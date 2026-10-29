package cl.uchile.dcc.model.potions

import cl.uchile.dcc.model.units.Character

/** Represents a potion that restores 20 percent of maximum health. */
class HealingPotion(name: String)
  extends Potion(name, PotionType.Heal) {

  /** Restores the character's health up to its maximum. */
  override def applyTo(character: Character): Unit = {
    character.restoreLifePoints(character.maxLifePoints * 0.2)
  }
}

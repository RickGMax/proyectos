package cl.uchile.dcc.model.potions

import cl.uchile.dcc.model.units.Character

/** Represents a potion that restores 30 percent of maximum mana. */
class ManaPotion(name: String)
  extends Potion(name, PotionType.Mana) {

  /** Dispatches the effect according to the character receiving it. */
  override def applyTo(character: Character): Unit = {
    character.receiveManaPotion(this)
  }
}


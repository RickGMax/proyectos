package cl.uchile.dcc.model.potions

import cl.uchile.dcc.model.units.Character

/** Represents a potion that boosts the next spell's magical damage. */
class MagicForcePotion(name: String)
  extends Potion(name, PotionType.MagicForce) {

  /** Dispatches the effect according to the character receiving it. */
  override def applyTo(character: Character): Unit = {
    character.receiveMagicForcePotion(this)
  }
}
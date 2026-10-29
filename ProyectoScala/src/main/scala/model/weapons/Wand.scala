package cl.uchile.dcc.model.weapons

import cl.uchile.dcc.model.units.Character

/** Represents a wand owned by a playable character. */
class Wand(
            name: String,
            attackPoints: Int,
            magicAttack: Int,
            weight: Double,
            owner: Character
          ) extends MagicWeapon(
  name, attackPoints, magicAttack, weight, owner, WeaponType.Wand
) {

  /** Checks the character's permission to equip this wand. */
  override def canBeEquippedBy(character: Character): Boolean =
    character.canEquip(this)
}
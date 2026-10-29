package cl.uchile.dcc.model.weapons

import cl.uchile.dcc.model.units.Character

/** Represents a staff owned by a playable character. */
class Staff(
             name: String,
             attackPoints: Int,
             magicAttack: Int,
             weight: Double,
             owner: Character
           ) extends MagicWeapon(
  name, attackPoints, magicAttack, weight, owner, WeaponType.Staff
) {

  /** Checks the character's permission to equip this staff. */
  override def canBeEquippedBy(character: Character): Boolean =
    character.canEquip(this)
}
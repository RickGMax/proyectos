package cl.uchile.dcc.model.units

import cl.uchile.dcc.model.weapons.{Bow, Sword, Wand, Weapon}
import cl.uchile.dcc.model.items.Usable

/** Represents an archer. */
class Archer(
              name: String,
              lifePoints: Double,
              defense: Int,
              weight: Double,
              weapon: Option[Weapon] = None,
              inventory: Array[Usable] = Array.empty[Usable]
            ) extends AbstractCharacter(
  name, lifePoints, defense, weight, weapon, inventory
) {
  /** Allows equipping swords. */
  override def canEquip(sword: Sword): Boolean = true

  /** Allows equipping bows. */
  override def canEquip(bow: Bow): Boolean = true

  /** Allows equipping wands. */
  override def canEquip(wand: Wand): Boolean = true
}
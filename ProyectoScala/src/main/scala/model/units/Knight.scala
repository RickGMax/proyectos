package cl.uchile.dcc.model.units

import cl.uchile.dcc.model.weapons.{Bow, Dagger, Sword, Weapon}
import cl.uchile.dcc.model.items.Usable

/** Represents a knight. */
class Knight(
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

  /** Allows equipping daggers. */
  override def canEquip(dagger: Dagger): Boolean = true

  /** Allows equipping bows. */
  override def canEquip(bow: Bow): Boolean = true
}
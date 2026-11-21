package cl.uchile.dcc.model.items

import cl.uchile.dcc.model.potions.Potion
import cl.uchile.dcc.model.weapons.Weapon

/** Visits the supported kinds of usable items. */
trait UsableVisitor {
  /** Processes a weapon. */
  def visitWeapon(weapon: Weapon): Unit

  /** Processes a potion. */
  def visitPotion(potion: Potion): Unit
}
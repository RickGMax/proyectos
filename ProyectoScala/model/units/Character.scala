package cl.uchile.dcc.model.units
import cl.uchile.dcc.model.actions.{Action, Attack, Target}
import cl.uchile.dcc.model.weapons.Weapon
import cl.uchile.dcc.model.items.Usable
import cl.uchile.dcc.model.actions.Source


/** Common contract implemented by playable characters. */
trait Character extends GameUnit with Source {

  /** Returns this character's equipped weapon, if any. */
  def weapon: Option[Weapon]

  /** Equips the given weapon. */
  def equipWeapon(newWeapon: Weapon): Unit

  /** Removes the currently equipped weapon. */
  def unequipWeapon(): Unit

  /** Returns a copy of this character's inventory. */
  def inventory: Array[Usable]

  /** Adds an item to this character's inventory. */
  def addToInventory(item: Usable): Unit

  /** Returns the equipped weapon's physical attack points. */
  def attackPoints: Int
}
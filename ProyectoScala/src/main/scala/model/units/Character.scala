package cl.uchile.dcc.model.units

import cl.uchile.dcc.model.actions.{
  Action, Attack, Healing, Meteorite, Purification, Target, Thunder
}

import cl.uchile.dcc.model.weapons.{Bow, Dagger, Staff, Sword, Wand, Weapon}
import cl.uchile.dcc.model.items.Usable
import cl.uchile.dcc.model.actions.Source
import cl.uchile.dcc.model.potions.{MagicForcePotion, ManaPotion, Potion}



/** Common contract implemented by playable characters. */
trait Character extends GameUnit with Source {
  /** Returns whether this character may equip a sword. */
  def canEquip(sword: Sword): Boolean = false

  /** Returns whether this character may equip a dagger. */
  def canEquip(dagger: Dagger): Boolean = false

  /** Returns whether this character may equip a bow. */
  def canEquip(bow: Bow): Boolean = false

  /** Returns whether this character may equip a wand. */
  def canEquip(wand: Wand): Boolean = false

  /** Returns whether this character may equip a staff. */
  def canEquip(staff: Staff): Boolean = false

  /** Rejects Meteorite unless the character supports dark magic. */
  def castMeteorite(action: Meteorite, target: Target): Unit = {
    throw new UnsupportedOperationException(
      "This character cannot cast Meteorite."
    )
  }

  /** Rejects Purification unless the character supports white magic. */
  def castPurification(action: Purification, target: Target): Unit = {
    throw new UnsupportedOperationException(
      "This character cannot cast Purification."
    )
  }

  /** Rejects Healing unless the character supports white magic. */
  def castHealing(action: Healing, target: Target): Unit = {
    throw new UnsupportedOperationException(
      "This character cannot cast Healing."
    )
  }

  /** Rejects Thunder unless the character supports dark magic. */
  def castThunder(action: Thunder, target: Target): Unit = {
    throw new UnsupportedOperationException(
      "This character cannot cast Thunder."
    )
  }

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

  /** Applies an owned potion and removes one occurrence after success. */
  def consumePotion(potion: Potion): Unit

  /** Returns the equipped weapon's physical attack points. */
  def attackPoints: Int

  /** Rejects mana potions for characters without mana. */
  def receiveManaPotion(potion: ManaPotion): Unit = {
    throw new UnsupportedOperationException(
      "This character cannot use mana potions."
    )
  }

  /** Rejects magic force potions for characters without magic. */
  def receiveMagicForcePotion(potion: MagicForcePotion): Unit = {
    throw new UnsupportedOperationException(
      "This character cannot use magic force potions."
    )
  }
}
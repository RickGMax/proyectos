package cl.uchile.dcc.model.items

import cl.uchile.dcc.model.potions.*
import cl.uchile.dcc.model.units.Character
import cl.uchile.dcc.model.weapons.*
import scala.util.Random

/** Creates the items used to initialize a battle. */
object ItemFactory {

  /** Creates a new potion of the requested type. */
  def createPotion(potionType: PotionType): Potion =
    potionType match {
      case PotionType.Heal       => new HealingPotion("Healing Potion")
      case PotionType.Strength   => new StrengthPotion("Strength Potion")
      case PotionType.Mana       => new ManaPotion("Mana Potion")
      case PotionType.MagicForce => new MagicForcePotion("Magic Force Potion")
    }

  /** Creates a weapon for its owner and rolls its initial stats. */
  def createWeapon(
                    weaponType: WeaponType,
                    owner: Character,
                    random: Random
                  ): Weapon = {
    val name = weaponType.toString
    val attackPoints = random.between(10, 26)
    val weight = random.between(1, 9).toDouble

    weaponType match {
      case WeaponType.Sword  => new Sword(name, attackPoints, weight, owner)
      case WeaponType.Dagger => new Dagger(name, attackPoints, weight, owner)
      case WeaponType.Bow    => new Bow(name, attackPoints, weight, owner)
      case WeaponType.Wand =>
        new Wand(name, attackPoints, random.between(10, 26), weight, owner)
      case WeaponType.Staff =>
        new Staff(name, attackPoints, random.between(10, 26), weight, owner)
    }
  }
}
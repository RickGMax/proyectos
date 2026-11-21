package cl.uchile.dcc.model.items

import cl.uchile.dcc.model.potions.*
import cl.uchile.dcc.model.units.{BlackMage, Knight}
import cl.uchile.dcc.model.weapons.*
import munit.FunSuite
import scala.util.Random

class ItemFactoryTest extends FunSuite {

  test("creates a new potion of each requested type") {
    val cases = Seq(
      PotionType.Heal       -> classOf[HealingPotion],
      PotionType.Strength   -> classOf[StrengthPotion],
      PotionType.Mana       -> classOf[ManaPotion],
      PotionType.MagicForce -> classOf[MagicForcePotion]
    )

    cases.foreach { case (potionType, expectedClass) =>
      val potion = ItemFactory.createPotion(potionType)

      assert(expectedClass.isInstance(potion), s"Wrong class for $potionType")
      assertEquals(potion.types, potionType)
      assert(potion ne ItemFactory.createPotion(potionType))
    }
  }

  test("creates each weapon with its owner and valid stats") {
    val knight = new Knight("Knight", 100.0, 10, 50.0)
    val mage = new BlackMage("Mage", 100.0, 10, 50.0, 80.0)
    val random = new Random(42L)

    val cases = Seq(
      (WeaponType.Sword, classOf[Sword], knight),
      (WeaponType.Dagger, classOf[Dagger], knight),
      (WeaponType.Bow, classOf[Bow], knight),
      (WeaponType.Wand, classOf[Wand], mage),
      (WeaponType.Staff, classOf[Staff], mage)
    )

    cases.foreach { case (weaponType, expectedClass, owner) =>
      val weapon = ItemFactory.createWeapon(weaponType, owner, random)

      assert(expectedClass.isInstance(weapon), s"Wrong class for $weaponType")
      assertEquals(weapon.types, weaponType)
      assert(weapon.owner eq owner)
      assert((10 to 25).contains(weapon.attackPoints))
      assert(weapon.weight >= 1.0 && weapon.weight <= 8.0)

      weapon match {
        case _: MagicWeapon =>
          assert(weapon.spellPower.exists(power => power >= 10.0 && power <= 25.0))
        case _ =>
          assert(weapon.spellPower.isEmpty)
      }
    }
  }
}
package cl.uchile.dcc.model.units

import munit.FunSuite
import scala.util.Random

class CharacterFactoryTest extends FunSuite {

  private def assertStartingStats(unit: GameUnit, expectedName: String): Unit = {
    assertEquals(unit.name, expectedName)
    assert(unit.lifePoints >= 80.0 && unit.lifePoints <= 120.0)
    assertEquals(unit.lifePoints, unit.maxLifePoints)
    assert(unit.defense >= 3.0 && unit.defense <= 8.0)
    assert(unit.weight >= 40.0 && unit.weight <= 80.0)
  }

  test("creates each playable class with valid starting stats") {
    val random = new Random(42L)
    val cases = Seq(
      CharacterType.Knight    -> classOf[Knight],
      CharacterType.Archer    -> classOf[Archer],
      CharacterType.Thief     -> classOf[Thief],
      CharacterType.BlackMage -> classOf[BlackMage],
      CharacterType.WhiteMage -> classOf[WhiteMage]
    )

    cases.foreach { case (kind, expectedClass) =>
      val name = s"Test $kind"
      val character = CharacterFactory.createCharacter(kind, name, random)

      assert(expectedClass.isInstance(character), s"Wrong class for $kind")
      assertStartingStats(character, name)

      character match {
        case mage: MagicCharacter =>
          assert(mage.manaPoints >= 50.0 && mage.manaPoints <= 100.0)
          assertEquals(mage.manaPoints, mage.maxManaPoints)
        case _ => ()
      }
    }
  }

  test("creates an enemy with valid starting stats and attack") {
    val enemy = CharacterFactory.createEnemy("Enemy 1", new Random(42L))

    assertStartingStats(enemy, "Enemy 1")
    assert((10 to 25).contains(enemy.attackPoints))
  }
}
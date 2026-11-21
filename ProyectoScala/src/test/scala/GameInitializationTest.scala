package cl.uchile.dcc.controller

import cl.uchile.dcc.model.actions.Source
import cl.uchile.dcc.model.potions.{Potion, PotionType}
import cl.uchile.dcc.model.units.{Character, Enemy, MagicCharacter}
import munit.FunSuite
import scala.util.Random

/** Checks that the factories and controller work together. */
class GameInitializationTest extends FunSuite {

  test("creates two sides with compatible starting equipment") {
    Seq(42L, 2L).foreach { seed =>
      val game = GameController.initializeGame(new Random(seed))

      assertEquals(game.players.map(_.units.size), Seq(3, 3))
      assert(game.players(1).units.forall(_.isInstanceOf[Enemy]))

      val characters = game.players.head.units.collect {
        case character: Character => character
      }
      assertEquals(characters.size, 3)

      characters.foreach { character =>
        val weapon = character.weapon.getOrElse(fail(s"${character.name} has no weapon"))
        val inventory = character.inventory
        val potions = inventory.collect { case potion: Potion => potion }

        assert(weapon.owner eq character)
        assert(weapon.canBeEquippedBy(character))
        assert(inventory.exists(_ eq weapon))
        assertEquals(potions.length, 1)

        character match {
          case mage: MagicCharacter =>
            assert(mage.manaPoints > 0.0)
            assert(weapon.spellPower.nonEmpty)
          case _ =>
            val kind = potions.head.types
            assert(kind == PotionType.Heal || kind == PotionType.Strength)
        }
      }

      assert(game.nextTurn().nonEmpty)
    }
  }

  test("creates a connected grid with both sides in starting positions") {
    val game = GameController.initializeGame(new Random(42L))
    val coordinates = (for (x <- 1 to 3; y <- 1 to 3) yield (x, y)).toSet

    assertEquals(game.panels.size, 9)
    assertEquals(game.panels.map(panel => (panel.x, panel.y)).toSet, coordinates)
    game.panels.foreach { panel =>
      val neighbors = panel.adjacent.toVector
      val expected = coordinates.filter { case (x, y) =>
        math.abs(panel.x - x) + math.abs(panel.y - y) == 1
      }
      assertEquals(neighbors.size, expected.size)
      assertEquals(neighbors.map(neighbor => (neighbor.x, neighbor.y)).toSet, expected)
      assert(neighbors.forall(neighbor => game.panels.exists(_ eq neighbor)))
    }

    game.players.zip(Seq(1, 3)).foreach { case (player, column) =>
      val startingPositions = player.units.map { unit =>
        val panel = unit.currentPanel.getOrElse(fail(s"${unit.name} has no starting panel"))
        assert(game.panels.exists(_ eq panel))
        assert(panel.containsUnit(unit))
        (panel.x, panel.y)
      }
      assertEquals(startingPositions.toSet, coordinates.filter(_._1 == column))
    }
  }

  test("a unit can move immediately and reset restores its starting panel") {
    val game = GameController.initializeGame(new Random(42L))
    val unit = game.nextTurn().getOrElse(fail("No active unit after initialization"))
    val source = unit match {
      case source: Source => source
      case _ => fail(s"${unit.name} cannot perform actions")
    }
    val origin = unit.currentPanel.getOrElse(fail(s"${unit.name} has no starting panel"))
    val destination = origin.adjacent.find(_.x == 2).getOrElse(fail("Missing adjacent panel"))

    assert(game.availableActions(source).exists(_.name == "Move"))
    assertEquals(game.executeAction(source, "Move", destination), "Action 'Move' executed.")
    assertEquals(unit.currentPanel, Some(destination))
    assert(destination.containsUnit(unit))
    assert(!origin.containsUnit(unit))

    game.resetGame()

    assertEquals(unit.currentPanel, Some(origin))
    assert(origin.containsUnit(unit))
    assert(!destination.containsUnit(unit))
  }

  test("reset restores the equipment generated at initialization") {
    val game = GameController.initializeGame(new Random(42L))
    val character = game.players.head.units
      .collectFirst { case unit: Character => unit }
      .getOrElse(fail("Missing playable character"))

    val initialWeapon = character.weapon
    val initialInventory = character.inventory.toVector
    val potion = initialInventory
      .collectFirst { case item: Potion => item }
      .getOrElse(fail("Missing starting potion"))

    character.consumePotion(potion)
    character.unequipWeapon()
    assert(character.weapon.isEmpty)
    assertEquals(character.inventory.length, initialInventory.size - 1)

    game.resetGame()

    assertEquals(character.weapon, initialWeapon)
    assertEquals(character.inventory.toVector, initialInventory)
  }
}

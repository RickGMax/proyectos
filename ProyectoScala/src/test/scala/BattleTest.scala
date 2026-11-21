package cl.uchile.dcc.controller

import munit.FunSuite
import cl.uchile.dcc.model.actions.Attack
import cl.uchile.dcc.model.map.Panel
import cl.uchile.dcc.model.players.Player
import cl.uchile.dcc.model.units.{BlackMage, Enemy, GameUnit}
import cl.uchile.dcc.model.weapons.Staff

/** Verifies that player defeat ends a battle and reset restores it. */
class BattleEndTest extends FunSuite {
  private def newBattle(): (GameController, Enemy, Enemy) = {
    val first = new Enemy("First", 100.0, 150, 0, 40.0)
    val second = new Enemy("Second", 100.0, 150, 0, 50.0)
    val controller = new GameController(
      new Player(Seq(first)), new Player(Seq(second))
    )
    (controller, first, second)
  }

  test("a battle continues while both players have living units") {
    val first = new Enemy("First", 100.0, 20, 0, 40.0)
    val second = new Enemy("Second", 100.0, 20, 0, 50.0)
    val controller = new GameController(
      new Player(Seq(first)), new Player(Seq(second))
    )

    assert(!controller.isGameOver)
    assertEquals(controller.gameOverMessage, None)
    assertEquals(controller.nextTurn(), Some(first))
    val message = controller.executeAction(first, "Attack", second)
    assertEquals(message, "Action 'Attack' executed.")

    assertEquals(second.lifePoints, 80.0)
    assert(!controller.isGameOver)
    assertEquals(controller.currentUnit, Some(second))
  }

  test("defeating the last opposing unit ends turns and blocks further actions") {
    val (controller, first, second) = newBattle()
    assertEquals(controller.nextTurn(), Some(first))

    val message = controller.executeAction(first, "Attack", second)
    assertEquals(message, "Game over.")
    assertEquals(controller.gameOverMessage, Some(message))

    assertEquals(second.lifePoints, 0.0)
    assert(controller.isGameOver)
    assertEquals(controller.currentUnit, None)
    assertEquals(controller.nextTurn(), None)
    assert(controller.availableActions(first).isEmpty)
    intercept[IllegalArgumentException] {
      controller.executeAction(first, "Attack", second)
    }
    assertEquals(first.lifePoints, 100.0)
    assertEquals(second.lifePoints, 0.0)
  }

  test("a battle cannot start when a player has no living units") {
    val defeatedTeams: Seq[Seq[GameUnit]] = Seq(
      Seq.empty[GameUnit],
      Seq(new Enemy("Defeated", 0.0, 20, 0, 50.0))
    )

    defeatedTeams.foreach { units =>
      val living = new Enemy("Living", 100.0, 20, 0, 40.0)
      val controller = new GameController(
        new Player(Seq(living)), new Player(units)
      )

      assert(controller.isGameOver)
      assertEquals(controller.gameOverMessage, Some("Game over."))
      assertEquals(controller.nextTurn(), None)
      assertEquals(controller.currentUnit, None)
      assert(controller.availableActions(living).isEmpty)
    }
  }

  test("an external defeat hides the active turn and prevents further turns") {
    val (controller, first, second) = newBattle()
    assertEquals(controller.nextTurn(), Some(first))

    second.doAction(new Attack, first)
    assertEquals(controller.gameOverMessage, Some("Game over."))

    assert(controller.isGameOver)
    assertEquals(controller.currentUnit, None)
    assertEquals(controller.nextTurn(), None)
  }

  test("a player notifies observers only when the last living unit falls") {
    val attacker = new Enemy("Attacker", 100.0, 150, 0, 40.0)
    val first = new Enemy("First target", 100.0, 20, 0, 50.0)
    val second = new Enemy("Second target", 100.0, 20, 0, 60.0)
    val defendingPlayer = new Player(Seq(first, second))
    var defeats = 0
    defendingPlayer.addDefeatObserver(() => defeats += 1)
    val controller = new GameController(new Player(Seq(attacker)), defendingPlayer)
    assertEquals(controller.nextTurn(), Some(attacker))

    attacker.doAction(new Attack, first)
    assertEquals(defeats, 0)
    assertEquals(controller.gameOverMessage, None)
    assertEquals(controller.currentUnit, Some(attacker))

    attacker.doAction(new Attack, second)
    assertEquals(defeats, 1)
    assertEquals(controller.gameOverMessage, Some("Game over."))
    assertEquals(controller.currentUnit, None)
  }

  test("resetting a finished battle restores both players and the turn order") {
    val (controller, first, second) = newBattle()
    controller.nextTurn()
    controller.executeAction(first, "Attack", second)
    assert(controller.isGameOver)

    controller.resetGame()

    assertEquals(first.lifePoints, 100.0)
    assertEquals(second.lifePoints, 100.0)
    assert(!controller.isGameOver)
    assertEquals(controller.gameOverMessage, None)
    assertEquals(controller.currentUnit, None)
    assert(controller.availableActions(first).nonEmpty)
    assertEquals(controller.nextTurn(), Some(first))
    assertEquals(controller.executeAction(first, "Attack", second), "Game over.")
  }

  test("Meteorite ends the battle when both players lose their last unit") {
    val magician = new BlackMage("Mage", 30.0, 10, 40.0, 100.0)
    val enemy = new Enemy("Enemy", 30.0, 20, 5, 50.0)
    val staff = new Staff("Staff", 10, 40, 5.0, magician)
    val panel = new Panel(
      1, 1, Array[GameUnit](magician, enemy), Array.empty[Panel]
    )
    magician.equipWeapon(staff)
    val controller = new GameController(
      new Player(Seq(magician)), new Player(Seq(enemy))
    )
    assertEquals(controller.nextTurn(), Some(magician))

    val message = controller.executeAction(magician, "Meteorite", panel)

    assertEquals(magician.lifePoints, 0.0)
    assertEquals(enemy.lifePoints, 0.0)
    assertEquals(magician.manaPoints, 50.0)
    assertEquals(message, "Game over.")
    assertEquals(controller.currentUnit, None)
    assertEquals(controller.nextTurn(), None)
  }

  test("a battle rejects the same player on both sides") {
    val unit = new Enemy("Unit", 100.0, 20, 0, 40.0)
    val player = new Player(Seq(unit))

    intercept[IllegalArgumentException] {
      new GameController(player, player)
    }

    assertEquals(unit.lifePoints, 100.0)
    assertEquals(unit.player, Some(player))
  }

  test("a failed attack preserves the current turn and keeps the battle active") {
    val (controller, first, second) = newBattle()
    assertEquals(controller.nextTurn(), Some(first))

    intercept[IllegalArgumentException] {
      controller.executeAction(first, "Attack", first)
    }

    assert(!controller.isGameOver)
    assertEquals(controller.currentUnit, Some(first))
    assertEquals(first.lifePoints, 100.0)
    assertEquals(second.lifePoints, 100.0)
  }

  test("a battle rejects attacks on units outside its two sides") {
    val (controller, first, second) = newBattle()
    val unowned = new Enemy("Unowned", 100.0, 20, 0, 40.0)
    val thirdPlayer = new Player(Seq(new Enemy("Third party", 100.0, 20, 0, 40.0)))
    assertEquals(controller.nextTurn(), Some(first))

    (unowned +: thirdPlayer.units).foreach { outsider =>
      intercept[IllegalArgumentException] {
        controller.executeAction(first, "Attack", outsider)
      }
      assertEquals(outsider.lifePoints, 100.0)
      assertEquals(controller.currentUnit, Some(first))
      assert(!controller.isGameOver)
    }

    assertEquals(controller.executeAction(first, "Attack", second), "Game over.")
    assertEquals(second.lifePoints, 0.0)
  }
}

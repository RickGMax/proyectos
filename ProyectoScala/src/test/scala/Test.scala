package cl.uchile.dcc.controller

import munit.FunSuite
import cl.uchile.dcc.model.items.Usable
import cl.uchile.dcc.model.potions.{
  HealingPotion, StrengthPotion, ManaPotion, MagicForcePotion,
  Potion, PotionType
}
import cl.uchile.dcc.model.units.{
  Archer, BlackMage, Character, Enemy, GameUnit, Knight,
  MagicCharacter, Thief, WhiteMage
}
import cl.uchile.dcc.model.weapons.{
  Bow, Dagger, MagicWeapon, Staff, Sword, Wand, Weapon, WeaponType
}
import cl.uchile.dcc.model.actions.{
  Action, Attack, ConsumePotion, EquipWeapon, Healing, Meteorite,
  Move, Purification, Source, Target, Thunder, UsableAction
}
import cl.uchile.dcc.model.players.Player
import cl.uchile.dcc.model.map.Panel
import cl.uchile.dcc.model.turns.TurnScheduler


class EntitiesTest extends FunSuite:
  test("all character classes share the character abstraction") {
    val characters: Seq[Character] = Seq(
      new Knight("Knight", 100.0, 10, 50.0),
      new Archer("Archer", 100.0, 10, 50.0),
      new Thief("Thief", 100.0, 10, 50.0),
      new BlackMage("Black mage", 100.0, 10, 50.0, 80.0),
      new WhiteMage("White mage", 100.0, 10, 50.0, 100.0)
    )

    assertEquals(
      characters.map(_.name),
      Seq("Knight", "Archer", "Thief", "Black mage", "White mage")
    )
    assert(characters.forall(_.weapon.isEmpty))
    assert(characters.forall(_.inventory.isEmpty))
  }

  test("both mage classes preserve their mana points") {
    val magicians: Seq[MagicCharacter] = Seq(
      new BlackMage("V", 100.0, 10, 50.0, 80.0),
      new WhiteMage("Gandalf", 100.0, 10, 50.0, 150.0)
    )

    assertEquals(magicians.map(_.manaPoints), Seq(80.0, 150.0))
  }


  test("a character stores its attributes"):
    val character = new Knight(
      "Peter",
      20.0,
      10,
      75.0,
      None,
      Array.empty[Usable]
    )
    assertEquals(character.name,"Peter")
    assertEquals(character.lifePoints,20.0)
    assertEquals(character.defense, 10.0)
    assertEquals(character.weight,75.0)
    assertEquals(character.weapon,None)
    assertEquals(character.inventory.length, 0)


  test("a magic character stores mana points"):
    val magician = new WhiteMage(
      "Gandalf", 80.0, 8, 60.0, 150.0
    )

    assertEquals(magician.name, "Gandalf")
    assertEquals(magician.lifePoints, 80.0)
    assertEquals(magician.defense, 8.0)
    assertEquals(magician.weight, 60.0)
    assertEquals(magician.weapon, None)
    assertEquals(magician.manaPoints, 150.0)
    assertEquals(magician.inventory.length, 0)

    val character:Character = magician
    val unit:GameUnit = magician

    assertEquals(character.name, "Gandalf")
    assertEquals(unit.name, "Gandalf")

  test("a dagger stores its attributes and owner") {
    val owner = new Thief("Locke", 80.0, 8, 45.0)
    val dagger: Weapon = new Dagger("Assassin dagger", 20, 2.0, owner)

    assertEquals(dagger.name, "Assassin dagger")
    assertEquals(dagger.attackPoints, 20)
    assertEquals(dagger.weight, 2.0)
    assertEquals(dagger.owner, owner)
    assertEquals(dagger.types, WeaponType.Dagger)

    val usable: Usable = dagger
    assertEquals(usable.name, "Assassin dagger")
  }

  test("a bow stores its attributes and owner") {
    val owner = new Archer("Robin", 90.0, 10, 50.0)
    val bow: Weapon = new Bow("Longbow", 25, 4.0, owner)

    assertEquals(bow.name, "Longbow")
    assertEquals(bow.attackPoints, 25)
    assertEquals(bow.weight, 4.0)
    assertEquals(bow.owner, owner)
    assertEquals(bow.types, WeaponType.Bow)

    val usable: Usable = bow
    assertEquals(usable.name, "Longbow")
  }

  test("a enemy stores its attributes"):
    val enemy = new Enemy(
      "Demon",
      50.0,
      15,
      5,
      40.0

    )
    assertEquals(enemy.name, "Demon")
    assertEquals(enemy.lifePoints, 50.0)
    assertEquals(enemy.attackPoints, 15)
    assertEquals(enemy.defense, 5.0)
    assertEquals(enemy.weight, 40.0)

    val unit: GameUnit = enemy
    assertEquals(unit.name, "Demon")

  test("a potion stores its name and type"):
    val potion = new HealingPotion(
      "Healing potion"
    )
    assertEquals(potion.name, "Healing potion")
    assertEquals(potion.types, PotionType.Heal)
    val usable: Usable = potion
    assertEquals(usable.name, "Healing potion")

  test("all potion classes expose their names and can be used as items") {
    val potions: Seq[Potion] = Seq(
      new HealingPotion("Healing potion"),
      new StrengthPotion("Strength potion"),
      new ManaPotion("Mana potion"),
      new MagicForcePotion("Magic force potion")
    )

    val items: Seq[Usable] = potions

    assertEquals(
      items.map(_.name),
      Seq(
        "Healing potion",
        "Strength potion",
        "Mana potion",
        "Magic force potion"
      )
    )

    assertEquals(
      potions.map(_.types),
      Seq(
        PotionType.Heal,
        PotionType.Strength,
        PotionType.Mana,
        PotionType.MagicForce
      )
    )
  }

  test("a weapon stores its attributes and owner"):
    val owner = new Knight(
      "Dante",
      100.0,
      20,
      75.0,
      None,
      Array.empty[Usable]
    )

    val sword = new Sword(
      "Rebelion",
      30,
      5.0,
      owner,
    )
    assertEquals(sword.name, "Rebelion")
    assertEquals(sword.attackPoints, 30)
    assertEquals(sword.weight, 5.0)
    assertEquals(sword.owner, owner)
    assertEquals(sword.types, WeaponType.Sword)

    val usable:Usable = sword
    assertEquals(usable.name, "Rebelion")

  test("a magic weapon stores magic attack points"):
    val owner = new BlackMage(
      "V", 100.0, 20, 75.0, 150.0
    )

    val staff = new Staff(
      "V staff",
      30,
      40,
      3.0,
      owner
    )

    assertEquals(staff.name, "V staff")
    assertEquals(staff.attackPoints, 30)
    assertEquals(staff.magicAttack, 40)
    assertEquals(staff.weight, 3.0)
    assertEquals(staff.owner, owner)
    assertEquals(staff.types, WeaponType.Staff)

    val usable: Usable = staff
    val weapon: Weapon = staff
    assertEquals(usable.name, "V staff")
    assertEquals(weapon.name, "V staff")


  test("a wand stores physical and magical attack points") {
    val owner = new BlackMage("V", 100.0, 10, 50.0, 80.0)
    val wand: MagicWeapon =
      new Wand("Dark wand", 15, 35, 2.0, owner)

    assertEquals(wand.name, "Dark wand")
    assertEquals(wand.attackPoints, 15)
    assertEquals(wand.magicAttack, 35)
    assertEquals(wand.weight, 2.0)
    assertEquals(wand.owner, owner)
    assertEquals(wand.types, WeaponType.Wand)

    val usable: Usable = wand
    assertEquals(usable.name, "Dark wand")
  }

  test("a character weapon slot can contain a weapon"):
    val character = new Knight(
      "Dante",
      100.0,
      20,
      75.0,
      None,
      Array.empty[Usable]
    )

    val sword = new Sword(
      "Rebelion",
      30,
      5.0,
      character,
    )
    character.equipWeapon(sword)
    assertEquals(character.weapon, Some(sword))
    assertEquals(character.availableActions.map(_.name), Seq("Attack"))
    character.unequipWeapon()
    assertEquals(character.weapon, None)
    assert(character.availableActions.isEmpty)

  test("a character inventory can contain usable item"):
    val character = new Knight(
      "Dante",
      100.0,
      20,
      75.0,
      None,
      Array.empty[Usable]
    )

    val potion = new HealingPotion(
      "Healing potion"
    )

    val sword = new Sword(
      "Rebelion",
      30,
      5.0,
      character,
    )

    character.addToInventory(potion)
    character.addToInventory(sword)
    assertEquals(character.inventory.length, 2)
    assertEquals(character.inventory(0), potion)
    assertEquals(character.inventory(1), sword)

  test("a character protects its inventory from external changes") {
    val potion = new HealingPotion("Healing potion")
    val otherPotion = new ManaPotion("Mana potion")
    val initialItems = Array[Usable](potion)

    val character = new Knight(
      "Dante", 100.0, 20, 75.0,
      None, initialItems
    )

    initialItems(0) = otherPotion
    assertEquals(character.inventory(0), potion)

    val inventoryCopy = character.inventory
    inventoryCopy(0) = otherPotion
    assertEquals(character.inventory(0), potion)
  }

  test("a player is defeated only when all its units are defeated"):
    val character = new Knight(
      "Dante", 100.0, 20, 75.0
      ,None, Array.empty[Usable]
    )
    val enemy = new Enemy("Vergil", 80.0, 20, 10, 40.0)
    val attacker = new Enemy("Mundus", 100.0, 200, 10, 50.0)

    val player = new Player(Seq(character, enemy))

    assertEquals(player.units, Seq[GameUnit](character, enemy))
    assertEquals(player.defeated, false)

    attacker.doAction(new Attack, character)

    assertEquals(character.lifePoints, 0.0)
    assertEquals(player.defeated, false)

    attacker.doAction(new Attack, enemy)

    assertEquals(enemy.lifePoints, 0.0)
    assertEquals(player.defeated, true)


  test("a player without units is defeated"):
    val player = new Player(Seq.empty[GameUnit])

    assertEquals(player.units, Seq.empty[GameUnit])
    assertEquals(player.defeated, true)


  test("a Panel stores coordinates, units and adjacent panels"):
    val character = new Knight(
      "Dante",
      100.0,
      20,
      75.0,
      None,
      Array.empty[Usable]
    )
    val unit: GameUnit = character
    val panelMid = new Panel(2,3,Array(unit),Array.empty[Panel])
    val panelRight = new Panel(3,3,Array.empty[GameUnit],Array.empty[Panel])
    val panelLeft = new Panel(1,3,Array.empty[GameUnit],Array.empty[Panel])

    assertEquals(panelMid.x, 2)
    assertEquals(panelMid.y, 3)
    assertEquals(panelMid.units.length, 1)
    assertEquals(panelMid.units(0), unit)
    assert(panelMid.adjacent.isEmpty)
    panelMid.setAdjacentPanels(Array(panelLeft, panelRight))
    assertEquals(panelMid.adjacent.length,2)
    assertEquals(panelMid.adjacent(0),panelLeft)
    assertEquals(panelMid.adjacent(1),panelRight)


  test("a panel protects its collections from external changes") {
    val first: GameUnit = new Enemy("First", 100.0, 20, 5, 40.0)
    val second: GameUnit = new Enemy("Second", 100.0, 20, 5, 40.0)

    val right = new Panel(
      2, 1, Array.empty[GameUnit], Array.empty[Panel]
    )
    val below = new Panel(
      1, 2, Array.empty[GameUnit], Array.empty[Panel]
    )

    val initialUnits = Array(first)
    val initialAdjacent = Array(right)
    val panel = new Panel(1, 1, initialUnits, initialAdjacent)

    initialUnits(0) = second
    initialAdjacent(0) = below
    assertEquals(panel.units(0), first)
    assertEquals(panel.adjacent(0), right)

    val unitsCopy = panel.units
    val adjacentCopy = panel.adjacent
    unitsCopy(0) = second
    adjacentCopy(0) = below
    assertEquals(panel.units(0), first)
    assertEquals(panel.adjacent(0), right)

    panel.setUnits(initialUnits)
    panel.setAdjacentPanels(initialAdjacent)
    initialUnits(0) = first
    initialAdjacent(0) = right
    assertEquals(panel.units(0), second)
    assertEquals(panel.adjacent(0), below)
  }

  test("a Action stores name and attributes"):
    val attack: Attack = new Attack
    val action:Action = attack
    assertEquals(action.name,"Attack")

  test("a Move stores name and attributes"):
    val move: Move = new Move
    val action: Action = move
    assertEquals(action.name, "Move")

  test("Equip weapon stores name and possibleUsables"):
    val character = new Knight(
      "Dante",
      100.0,
      20,
      75.0,
      None,
      Array.empty[Usable]
    )
    val sword = new Sword(
      "Rebelion",
      30,
      5.0,
      character
    )
    val equipweap: EquipWeapon = new EquipWeapon(Array[Usable](sword))
    val usabaction: UsableAction = equipweap
    assertEquals(usabaction.name, "Equip a weapon")
    assertEquals(usabaction.possibleUsables(0), sword)
    assertEquals(usabaction.possibleUsables.length, 1)

  test("a usable action protects its possible items from external changes") {
    val potion = new HealingPotion("Healing potion")
    val otherPotion = new ManaPotion("Mana potion")
    val initialItems = Array[Usable](potion)

    val action: UsableAction = new ConsumePotion(initialItems)

    initialItems(0) = otherPotion
    assertEquals(action.possibleUsables(0), potion)

    val itemsCopy = action.possibleUsables
    itemsCopy(0) = otherPotion
    assertEquals(action.possibleUsables(0), potion)
  }

  test("Consume potion stores name and possibleUsables"):
    val potion = new HealingPotion(
      "Healing potion"
    )
    val consumep:ConsumePotion = new ConsumePotion(Array[Usable](potion))
    val usabaction:UsableAction = consumep
    assertEquals(usabaction.name,"Consume a potion")
    assertEquals(usabaction.possibleUsables(0),potion)
    assertEquals(usabaction.possibleUsables.length, 1)


  test("a Thunder stores name and attributes"):
    val thunder: Thunder = new Thunder
    val action: Action = thunder
    assertEquals(action.name, "Thunder")



  test("a Meteorite stores name and attributes"):
    val meteorite: Meteorite = new Meteorite
    val action: Action = meteorite
    assertEquals(action.name, "Meteorite")

  test("a Healing stores name and attributes"):
    val healing: Healing = new Healing
    val action: Action = healing
    assertEquals(action.name, "Healing")

  test("a Purification stores name and attributes"):
    val purification: Purification = new Purification
    val action: Action = purification
    assertEquals(action.name, "Purification")



  test("a character calculates its total weight"):
    val character = new Knight(
      "Dante",
      100.0,
      20,
      75.0,
      None,
      Array.empty[Usable]
    )
    assertEquals(character.totalWeight, 75.0)

    val sword = new Sword(
      "Rebelion",
      30,
      5.0,
      character
    )
    character.equipWeapon(sword)
    assertEquals(character.totalWeight, 80.0)

  test("an enemy calculates its total weight"):
    val enemy = new Enemy(
      "Vergil",
      100.0,
      30,
      10,
      50.0

    )
    assertEquals(enemy.totalWeight, 50.0)

  test("a turn scheduler starts empty"):
    val scheduler = new TurnScheduler

    assert(scheduler.units.isEmpty)


  test("a turn scheduler adds a unit"):
    val enemy = new Enemy(
      "Vergil",
      100.0,
      30,
      10,
      50.0

    )
    val scheduler = new TurnScheduler
    scheduler.addUnit(enemy)
    assertEquals(scheduler.units.length, 1)
    assertEquals(scheduler.units.head,enemy)
    assertEquals(scheduler.barAnalyze(enemy),Some(0.0))

  test("a turn scheduler removes a unit"):
    val enemy = new Enemy(
      "Vergil",
      100.0,
      30,
      10,
      50.0

    )
    val scheduler = new TurnScheduler

    scheduler.addUnit(enemy)
    assertEquals(scheduler.units.length, 1)
    scheduler.removeUnit(enemy)
    assert(scheduler.units.isEmpty)
    assertEquals(scheduler.barAnalyze(enemy),None)

  test("a turn scheduler increases every action bar"):
    val enemy1 = new Enemy(
      "Vergil",
      100.0,
      30,
      10,
      50.0

    )
    val enemy2 = new Enemy(
      "Virgilio",
      100.0,
      30,
      10,
      50.0

    )
    val scheduler = new TurnScheduler

    scheduler.addUnit(enemy1)
    scheduler.addUnit(enemy2)

    scheduler.increaseActionBars(15.0)

    assertEquals(scheduler.barAnalyze(enemy1),Some(15.0))
    assertEquals(scheduler.barAnalyze(enemy2),Some(15.0))


  test("a turn scheduler resets one action bar"):
    val enemy1 = new Enemy(
      "Vergil",
      100.0,
      30,
      10,
      50.0

    )
    val enemy2 = new Enemy(
      "Virgilio",
      100.0,
      30,
      10,
      50.0

    )
    val scheduler = new TurnScheduler

    scheduler.addUnit(enemy1)
    scheduler.addUnit(enemy2)

    scheduler.increaseActionBars(15.0)
    scheduler.resetActionBar(enemy1)
    

    assertEquals(scheduler.barAnalyze(enemy1), Some(0.0))
    assertEquals(scheduler.barAnalyze(enemy2), Some(15.0))


  test("a turn scheduler detects a completed action bar"):
    val enemy = new Enemy(
      "Vergil",
      100.0,
      30,
      10,
      50.0

    )

    val scheduler = new TurnScheduler
    scheduler.addUnit(enemy)
    scheduler.increaseActionBars(45.0)

    assertEquals(scheduler.hasCompletedBar(enemy),false)
    scheduler.increaseActionBars(10.0)
    assertEquals(scheduler.hasCompletedBar(enemy),true)


  test("completed units are ordered by excess"):
    val enemy1 = new Enemy(
      "Vergil",
      100.0,
      30,
      10,
      50.0

    )
    val enemy2 = new Enemy(
      "Virgilio",
      100.0,
      30,
      10,
      40.0

    )
    val enemy3 = new Enemy(
      "Mundus",
      100.0,
      40,
      20,
      100.0
    )

    val scheduler = new TurnScheduler
    scheduler.addUnit(enemy1)
    scheduler.addUnit(enemy2)
    scheduler.addUnit(enemy3)

    scheduler.increaseActionBars(60.0)

    val completed = scheduler.completedUnits

    assertEquals(completed.length,2)
    assertEquals(completed(0),enemy2)
    assertEquals(completed(1),enemy1)


  test("a turn scheduler selects unit with the mayor excess"):
    val enemy1 = new Enemy(
      "Vergil",
      100.0,
      30,
      10,
      50.0

    )
    val enemy2 = new Enemy(
      "Virgilio",
      100.0,
      30,
      10,
      40.0

    )
    val scheduler = new TurnScheduler

    scheduler.addUnit(enemy1)
    scheduler.addUnit(enemy2)
    assertEquals(scheduler.nextUnit,None)

    scheduler.increaseActionBars(60.0)

    assertEquals(scheduler.nextUnit,Some(enemy2))


  test("a turn scheduler calculates a maximum action bar"):
    val enemy = new Enemy(
      "Vergil",
      100.0,
      30,
      10,
      50.0

    )

    val scheduler = new TurnScheduler
    scheduler.addUnit(enemy)
    assertEquals(scheduler.maxActionBar(enemy),Some(50.0))


  test("an action bar is completed exactly at its maximum"):
    val enemy = new Enemy(
      "Vergil",
      100.0,
      30,
      10,
      50.0

    )

    val scheduler = new TurnScheduler
    scheduler.addUnit(enemy)
    scheduler.increaseActionBars(50.0)
    assertEquals(scheduler.hasCompletedBar(enemy),true)

  test("A controller selects and stores the next unit"):
    val heavy = new Enemy("Vergil",100.0,30,10, 50.0)
    val light = new Enemy("Mundus",100.0,20,5, 40.0)

    val controller = new GameController(Seq(heavy,light))

    assertEquals(controller.currentUnit, None)

    assertEquals(controller.nextTurn(),Some(light))
    assertEquals(controller.currentUnit,Some(light))

    assertEquals(controller.nextTurn(), Some(heavy))
    assertEquals(controller.currentUnit, Some(heavy))

  test("A controller without units not has next turn"):
    val controller = new GameController(Seq.empty[GameUnit])

    assertEquals(controller.nextTurn(),None)
    assertEquals(controller.currentUnit,None)

  test("A controller rejects a nonpositive total weight"):
    val enemy = new Enemy("Mundus",100.0,20,5, 0.0)
    val controller = new GameController(Seq(enemy))

    intercept[IllegalArgumentException]:
      controller.nextTurn()

    assertEquals(controller.currentUnit, None)

  test(" A controller rejects nonfinite total weight"):
    val enemy = new Enemy("Mundus",100.0,20,5, Double.PositiveInfinity)
    val controller = new GameController(Seq(enemy))

    intercept[IllegalArgumentException]:
      controller.nextTurn()


  test(" resetting the controller restores the initial turn progression"):
    val heavy = new Enemy("Vergil", 100.0, 30, 10, 50.0)
    val light = new Enemy("Mundus", 100.0, 20, 5, 40.0)

    val controller = new GameController(Seq(heavy,light))

    assertEquals(controller.nextTurn(),Some(light))

    controller.resetGame()

    assertEquals(controller.currentUnit, None)

    assertEquals(controller.nextTurn(),Some(light))
    assertEquals(controller.nextTurn(),Some(heavy))

  test(" resetting an empty controller keeps it without an active turn"):
    val controller = new GameController(Seq.empty[GameUnit])

    controller.resetGame()

    assertEquals(controller.currentUnit, None)
    assertEquals(controller.nextTurn(), None)

  test(" an enemy delegates an action with the correct source and target"):
    val enemy = new Enemy("Vergil",100.0,20,5, 50.0)
    val objective = new Enemy("Mundus",100.0,20,5, 40.0)

    var receivedSource: Option[Enemy] = None
    var receivedTarget: Option[Target] = None

    val action: Action = new Action("Recording action") {
      override def executeFromEnemy(source: Enemy,target: Target): Unit ={
        receivedSource = Some(source)
        receivedTarget = Some(target)
      }
    }

    val source:Source = enemy
    val target:Target = objective

    source.doAction(action, target)

    assertEquals(receivedSource,Some(enemy))
    assertEquals(receivedTarget,Some(target))


  test("an enemy rejects an action wihout enemy execution support"):
    val enemy = new Enemy("Vergil", 100.0, 20, 5, 50.0)
    val target = new Enemy("Mundus", 100.0, 20, 5, 40.0)

    val action = new Action("Unsupported action"){}

    intercept[UnsupportedOperationException]:
      enemy.doAction(action, target)


  test("action bars include half the equipped weapon weight"):
    val character = new Knight(
      "Dante", 100.0, 20, 75.0,
      None, Array.empty[Usable]
    )

    assertEquals(character.actionBarMaximum, 75.0)

    val sword =
      new Sword("Rebellion", 30, 5.0, character)

    character.equipWeapon(sword)

    assertEquals(character.totalWeight, 80.0)
    assertEquals(character.actionBarMaximum, 77.5)

    val enemy = new Enemy("Demon", 100.0, 20, 5, 78.0)
    val scheduler = new TurnScheduler

    scheduler.addUnit(character)
    scheduler.addUnit(enemy)

    assertEquals(scheduler.maxActionBar(character), Some(77.5))

    scheduler.increaseActionBars(77.0)
    assertEquals(scheduler.hasCompletedBar(character), false)

    scheduler.increaseActionBars(0.5)
    assertEquals(scheduler.hasCompletedBar(character), true)

    scheduler.increaseActionBars(0.5)

    assertEquals(scheduler.completedUnits, Seq[GameUnit](character, enemy))
    assertEquals(scheduler.nextUnit, Some(character))


  test("an enemy attack reduces a character's health"):
    val enemy = new Enemy("Vergil", 100.0, 30, 5, 50.0)
    val character = new Knight(
      "Dante", 100.0, 10, 75.0,
      None, Array.empty[Usable]
    )

    val source: Source = enemy
    val action: Action = new Attack
    val target: Target = character

    source.doAction(action, target)

    assertEquals(character.lifePoints, 80.0) // 100 - (30 - 10)
    assertEquals(enemy.lifePoints, 100.0)


  test("defense greater than attack prevents damage"):
    val enemy = new Enemy("Vergil", 100.0, 20, 5, 50.0)
    val target = new Enemy("Mundus", 80.0, 20, 40, 40.0)

    enemy.doAction(new Attack, target)

    assertEquals(target.lifePoints, 80.0)


  test("an attack cannot reduce health below zero"):
    val enemy = new Enemy("Vergil", 100.0, 50, 5, 50.0)
    val target = new Enemy("Mundus", 10.0, 20, 5, 40.0)

    enemy.doAction(new Attack, target)

    assertEquals(target.lifePoints, 0.0)


  test("a defeated enemy cannot perform an action"):
    val enemy = new Enemy("Vergil", 0.0, 30, 5, 50.0)
    val target = new Enemy("Mundus", 100.0, 20, 5, 40.0)

    intercept[IllegalArgumentException]:
      enemy.doAction(new Attack, target)

    assertEquals(target.lifePoints, 100.0)


  test("an attack rejects a defeated target"):
    val enemy = new Enemy("Vergil", 100.0, 30, 5, 50.0)
    val target = new Enemy("Mundus", 0.0, 20, 5, 40.0)

    intercept[IllegalArgumentException]:
      enemy.doAction(new Attack, target)

    assertEquals(target.lifePoints, 0.0)


  test("an attack rejects an unsupported target"):
    val enemy = new Enemy("Vergil", 100.0, 30, 5, 50.0)
    val target = new Target {}

    intercept[UnsupportedOperationException]:
      enemy.doAction(new Attack, target)

  test("an enemy exposes and finds its available actions"):
    val enemy = new Enemy("Vergil", 100.0, 20, 5, 50.0)
    val source: Source = enemy

    assertEquals(source.name, "Vergil")
    assertEquals(source.availableActions.map(_.name), Seq("Attack"))
    assertEquals(source.findAction("Attack"), source.availableActions.headOption)
    assertEquals(source.findAction("Unknown"), None)

  test("a defeated enemy has no available actions"):
    val enemy = new Enemy("Vergil", 0.0, 20, 5, 50.0)

    assertEquals(enemy.availableActions, Seq.empty[Action])
    assertEquals(enemy.findAction("Attack"), None)


  test("a controller lists actions for a unit outside its active turn"):
    val heavy = new Enemy("Vergil", 100.0, 30, 10, 50.0)
    val light = new Enemy("Mundus", 100.0, 20, 5, 40.0)
    val controller = new GameController(Seq(heavy, light))

    controller.nextTurn()

    assertEquals(
      controller.availableActions(heavy).map(_.name),
      Seq("Attack")
    )
    assertEquals(controller.currentUnit, Some(light))

  test("a controller reflects changes in available actions"):
    val enemy = new Enemy("Vergil", 100.0, 30, 10, 50.0)
    val controller = new GameController(Seq(enemy))

    assertEquals(
      controller.availableActions(enemy).map(_.name),
      Seq("Attack")
    )

    val attacker = new Enemy("Attacker", 100.0, 200, 0, 50.0)
    attacker.doAction(new Attack, enemy)

    assertEquals(
      controller.availableActions(enemy),
      Seq.empty[Action]
    )
    assertEquals(controller.currentUnit, None)


  test("an enemy defeated after creation cannot perform actions"):
    val enemy = new Enemy("Vergil", 100.0, 30, 5, 50.0)
    val target = new Enemy("Mundus", 100.0, 20, 10, 40.0)

    val attacker = new Enemy("Attacker", 100.0, 200, 0, 50.0)
    attacker.doAction(new Attack, enemy)

    intercept[IllegalArgumentException]:
      enemy.doAction(new Attack, target)

    assertEquals(target.lifePoints, 100.0)

  test("a controller rejects a nonpositive action bar maximum") {
    Seq(0.0, -1.0).foreach { maximum =>
      val enemy = new Enemy("Vergil", 100.0, 30, 10, 50.0) {
        override def actionBarMaximum: Double = maximum
      }

      val controller = new GameController(Seq(enemy))

      intercept[IllegalArgumentException] {
        controller.nextTurn()
      }

      assertEquals(controller.currentUnit, None)
    }
  }

  test("a controller executes the active character's attack") {
    val character = new Knight("Dante", 100.0, 10, 40.0)
    val enemy = new Enemy("Vergil", 100.0, 20, 10, 50.0)
    val sword = new Sword("Rebellion", 30, 5.0, character)

    character.equipWeapon(sword)

    val controller = new GameController(Seq(character, enemy))

    assertEquals(controller.nextTurn(), Some(character))

    controller.executeAction(character, "Attack", enemy)

    assertEquals(enemy.lifePoints, 80.0)
    assertEquals(character.lifePoints, 100.0)
    assertEquals(controller.currentUnit, Some(enemy))

    intercept[IllegalArgumentException] {
      controller.executeAction(character, "Attack", enemy)
    }

    assertEquals(enemy.lifePoints, 80.0)
  }

  test("a controller rejects actions before or outside the source's turn") {
    val heavy = new Enemy("Heavy", 100.0, 30, 5, 50.0)
    val light = new Enemy("Light", 100.0, 20, 5, 40.0)
    val controller = new GameController(Seq(heavy, light))

    intercept[IllegalArgumentException] {
      controller.executeAction(heavy, "Attack", light)
    }

    assertEquals(light.lifePoints, 100.0)
    assertEquals(controller.currentUnit, None)

    assertEquals(controller.nextTurn(), Some(light))

    intercept[IllegalArgumentException] {
      controller.executeAction(heavy, "Attack", light)
    }

    assertEquals(light.lifePoints, 100.0)
    assertEquals(controller.currentUnit, Some(light))
  }

  test("a controller rejects an unavailable action during an active turn") {
    val attacker = new Enemy("Attacker", 100.0, 30, 5, 40.0)
    val target = new Enemy("Target", 100.0, 20, 10, 50.0)
    val controller = new GameController(Seq(attacker, target))

    assertEquals(controller.nextTurn(), Some(attacker))

    intercept[IllegalArgumentException] {
      controller.executeAction(attacker, "Unknown", target)
    }

    assertEquals(target.lifePoints, 100.0)
    assertEquals(controller.currentUnit, Some(attacker))
  }

  test("an unarmed character cannot attack") {
    val character = new Knight("Dante", 100.0, 10, 75.0)
    val enemy = new Enemy("Vergil", 100.0, 20, 5, 50.0)

    intercept[IllegalArgumentException] {
      character.doAction(new Attack, enemy)
    }

    assertEquals(enemy.lifePoints, 100.0)
    assertEquals(character.lifePoints, 100.0)
    assert(character.availableActions.isEmpty)
  }

  test("a character defeated after creation cannot perform actions") {
    val character = new Knight("Dante", 100.0, 10, 75.0)
    val sword = new Sword("Rebellion", 30, 5.0, character)
    val enemy = new Enemy("Vergil", 100.0, 200, 5, 50.0)

    character.equipWeapon(sword)
    assertEquals(character.availableActions.map(_.name), Seq("Attack"))

    enemy.doAction(new Attack, character)

    assertEquals(character.lifePoints, 0.0)
    assert(character.availableActions.isEmpty)

    intercept[IllegalArgumentException] {
      character.doAction(new Attack, enemy)
    }

    assertEquals(enemy.lifePoints, 100.0)
  }

  test("a character equips a weapon through an action") {
    val character = new Knight("Dante", 100.0, 10, 75.0)
    val sword = new Sword("Rebellion", 30, 5.0, character)
    val action = new EquipWeapon(Array[Usable](sword))

    character.addToInventory(sword)
    assertEquals(character.weapon, None)

    character.doAction(action, sword)

    assertEquals(character.weapon, Some(sword))
    assertEquals(character.attackPoints, 30)
  }

  test("equipment rejects a target that is not a weapon") {
    val character = new Knight("Dante", 100.0, 10, 75.0)
    val sword = new Sword("Rebellion", 30, 5.0, character)
    val enemy = new Enemy("Vergil", 100.0, 20, 5, 50.0)
    val action = new EquipWeapon(Array[Usable](sword))

    intercept[UnsupportedOperationException] {
      character.doAction(action, enemy)
    }

    assertEquals(character.weapon, None)
    assertEquals(enemy.lifePoints, 100.0)
  }
  test("a controller offers and executes equipment from the inventory") {
    val character = new Knight("Dante", 100.0, 10, 40.0)
    val enemy = new Enemy("Vergil", 100.0, 20, 5, 50.0)
    val potion = new HealingPotion("Healing potion")
    val sword = new Sword("Rebellion", 30, 5.0, character)
    val controller = new GameController(Seq(character, enemy))

    character.addToInventory(potion)
    assertEquals(character.findAction("Equip a weapon"), None)

    character.addToInventory(sword)

    character.findAction("Equip a weapon") match {
      case Some(action: EquipWeapon) =>
        assertEquals(action.possibleUsables.toSeq, Seq[Usable](sword))
      case _ =>
        fail("Expected an equipment action")
    }

    assertEquals(controller.nextTurn(), Some(character))

    controller.executeAction(character, "Equip a weapon", sword)

    assertEquals(character.weapon, Some(sword))
    assert(character.findAction("Attack").nonEmpty)
    assertEquals(controller.currentUnit, Some(enemy))
  }

  test("a failed action preserves the current turn") {
    val character = new Knight("Dante", 100.0, 10, 40.0)
    val enemy = new Enemy("Vergil", 100.0, 20, 5, 50.0)
    val sword = new Sword("Rebellion", 30, 5.0, character)

    character.addToInventory(sword)

    val controller = new GameController(Seq(character, enemy))
    assertEquals(controller.nextTurn(), Some(character))

    intercept[UnsupportedOperationException] {
      controller.executeAction(character, "Equip a weapon", enemy)
    }

    assertEquals(controller.currentUnit, Some(character))
    assertEquals(character.weapon, None)
    assertEquals(enemy.lifePoints, 100.0)
  }

  test("taking damage preserves maximum life points") {
    val character = new Knight("Dante", 100.0, 10, 40.0)
    val enemy = new Enemy("Vergil", 100.0, 30, 5, 50.0)

    assertEquals(character.maxLifePoints, 100.0)

    enemy.doAction(new Attack, character)

    assertEquals(character.lifePoints, 80.0)
    assertEquals(character.maxLifePoints, 100.0)
  }

  test("restoring life points respects maximum health") {
    val character = new Knight("Dante", 100.0, 10, 40.0)
    val enemy = new Enemy("Vergil", 100.0, 30, 5, 50.0)

    enemy.doAction(new Attack, character)
    assertEquals(character.lifePoints, 80.0)

    character.restoreLifePoints(10.0)
    assertEquals(character.lifePoints, 90.0)

    character.restoreLifePoints(50.0)
    assertEquals(character.lifePoints, 100.0)
    assertEquals(character.maxLifePoints, 100.0)
  }

  test("restoring life points cannot revive a defeated unit") {
    val character = new Knight("Dante", 100.0, 10, 40.0)
    val enemy = new Enemy("Vergil", 100.0, 200, 5, 50.0)

    enemy.doAction(new Attack, character)
    assertEquals(character.lifePoints, 0.0)

    intercept[IllegalArgumentException] {
      character.restoreLifePoints(20.0)
    }

    assertEquals(character.lifePoints, 0.0)
  }

  test("invalid recovery amounts preserve life points") {
    val character = new Knight("Dante", 100.0, 10, 40.0)
    val enemy = new Enemy("Vergil", 100.0, 30, 5, 50.0)

    enemy.doAction(new Attack, character)

    Seq(-1.0, Double.NaN, Double.PositiveInfinity, Double.NegativeInfinity)
      .foreach { amount =>
        intercept[IllegalArgumentException] {
          character.restoreLifePoints(amount)
        }
        assertEquals(character.lifePoints, 80.0)
      }
  }

  test("consuming a healing potion restores twenty percent of maximum health") {
    val character = new Knight("Dante", 200.0, 10, 40.0)
    val enemy = new Enemy("Vergil", 100.0, 90, 5, 50.0)
    val potion = new HealingPotion("Healing potion")
    val sword = new Sword("Rebellion", 30, 5.0, character)

    character.addToInventory(sword)
    character.addToInventory(potion)
    enemy.doAction(new Attack, character)

    assertEquals(character.lifePoints, 120.0)

    character.consumePotion(potion)

    assertEquals(character.lifePoints, 160.0)
    assertEquals(character.inventory.toSeq, Seq[Usable](sword))
  }

  test("a healing potion cannot exceed maximum health") {
    val character = new Knight("Dante", 100.0, 10, 40.0)
    val enemy = new Enemy("Vergil", 100.0, 15, 5, 50.0)
    val potion = new HealingPotion("Healing potion")

    character.addToInventory(potion)
    enemy.doAction(new Attack, character)
    assertEquals(character.lifePoints, 95.0)

    character.consumePotion(potion)

    assertEquals(character.lifePoints, 100.0)
    assert(character.inventory.isEmpty)
  }

  test("a character cannot consume a potion outside its inventory") {
    val character = new Knight("Dante", 100.0, 10, 40.0)
    val ownedPotion = new HealingPotion("Healing potion")
    val otherPotion = new HealingPotion("Healing potion")

    character.addToInventory(ownedPotion)

    intercept[IllegalArgumentException] {
      character.consumePotion(otherPotion)
    }

    assertEquals(character.lifePoints, 100.0)
    assertEquals(character.inventory.toSeq, Seq[Usable](ownedPotion))
  }

  test("consuming a potion removes only one inventory occurrence") {
    val character = new Knight("Dante", 100.0, 10, 40.0)
    val potion = new HealingPotion("Healing potion")

    character.addToInventory(potion)
    character.addToInventory(potion)

    character.consumePotion(potion)
    assertEquals(character.inventory.toSeq, Seq[Usable](potion))

    character.consumePotion(potion)
    assert(character.inventory.isEmpty)

    intercept[IllegalArgumentException] {
      character.consumePotion(potion)
    }
  }

  test("a defeated character cannot consume a potion") {
    val character = new Knight("Dante", 0.0, 10, 40.0)
    val potion = new HealingPotion("Healing potion")

    character.addToInventory(potion)

    intercept[IllegalArgumentException] {
      character.consumePotion(potion)
    }

    assertEquals(character.lifePoints, 0.0)
    assertEquals(character.inventory.toSeq, Seq[Usable](potion))
  }

  test("a rejected potion effect preserves the inventory") {
    val character = new Knight("Dante", 100.0, 10, 40.0)
    val potion = new Potion("Unsupported potion", PotionType.Heal) {}

    character.addToInventory(potion)

    intercept[UnsupportedOperationException] {
      character.consumePotion(potion)
    }

    assertEquals(character.lifePoints, 100.0)
    assertEquals(character.inventory.toSeq, Seq[Usable](potion))
  }


  test("the controller consumes an inventory potion and advances the turn") {
    val character = new Knight("Dante", 100.0, 10, 40.0)
    val enemy = new Enemy("Vergil", 100.0, 50, 5, 50.0)
    val potion = new HealingPotion("Healing potion")
    val sword = new Sword("Rebellion", 30, 5.0, character)

    character.addToInventory(sword)
    assertEquals(character.findAction("Consume a potion"), None)

    character.addToInventory(potion)
    enemy.doAction(new Attack, character)
    assertEquals(character.lifePoints, 60.0)

    character.findAction("Consume a potion") match {
      case Some(action: ConsumePotion) =>
        assertEquals(action.possibleUsables.toSeq, Seq[Usable](potion))
      case _ =>
        fail("Expected a potion consumption action")
    }

    val controller = new GameController(Seq(character, enemy))
    assertEquals(controller.nextTurn(), Some(character))

    controller.executeAction(character, "Consume a potion", potion)

    assertEquals(character.lifePoints, 80.0)
    assertEquals(character.inventory.toSeq, Seq[Usable](sword))
    assertEquals(character.findAction("Consume a potion"), None)
    assertEquals(controller.currentUnit, Some(enemy))
  }

  test("consuming an unowned potion preserves the current turn") {
    val character = new Knight("Dante", 100.0, 10, 40.0)
    val enemy = new Enemy("Vergil", 100.0, 20, 5, 50.0)
    val ownedPotion = new HealingPotion("Healing potion")
    val otherPotion = new HealingPotion("Healing potion")

    character.addToInventory(ownedPotion)

    val controller = new GameController(Seq(character, enemy))
    assertEquals(controller.nextTurn(), Some(character))

    intercept[IllegalArgumentException] {
      controller.executeAction(character, "Consume a potion", otherPotion)
    }

    assertEquals(character.lifePoints, 100.0)
    assertEquals(character.inventory.toSeq, Seq[Usable](ownedPotion))
    assertEquals(controller.currentUnit, Some(character))
  }

  test("consuming an unsupported target preserves the current turn") {
    val character = new Knight("Dante", 100.0, 10, 40.0)
    val enemy = new Enemy("Vergil", 100.0, 20, 5, 50.0)
    val potion = new HealingPotion("Healing potion")

    character.addToInventory(potion)

    val controller = new GameController(Seq(character, enemy))
    assertEquals(controller.nextTurn(), Some(character))

    intercept[UnsupportedOperationException] {
      controller.executeAction(character, "Consume a potion", enemy)
    }

    assertEquals(character.lifePoints, 100.0)
    assertEquals(enemy.lifePoints, 100.0)
    assertEquals(character.inventory.toSeq, Seq[Usable](potion))
    assertEquals(controller.currentUnit, Some(character))
  }

  test("a rejected potion effect preserves the current turn") {
    val character = new Knight("Dante", 100.0, 10, 40.0)
    val enemy = new Enemy("Vergil", 100.0, 20, 5, 50.0)
    val potion = new Potion("Unsupported potion", PotionType.Heal) {}

    character.addToInventory(potion)

    val controller = new GameController(Seq(character, enemy))
    assertEquals(controller.nextTurn(), Some(character))

    intercept[UnsupportedOperationException] {
      controller.executeAction(character, "Consume a potion", potion)
    }

    assertEquals(character.lifePoints, 100.0)
    assertEquals(character.inventory.toSeq, Seq[Usable](potion))
    assertEquals(controller.currentUnit, Some(character))
  }

  test("the controller consumes a strength potion and advances the turn") {
    val character = new Knight("Dante", 100.0, 10, 40.0)
    val enemy = new Enemy("Vergil", 100.0, 20, 5, 50.0)
    val potion = new StrengthPotion("Strength potion")

    character.addToInventory(potion)

    val controller = new GameController(Seq(character, enemy))
    assertEquals(controller.nextTurn(), Some(character))

    controller.executeAction(character, "Consume a potion", potion)

    assertEquals(character.defense, 11.5)
    assertEquals(character.lifePoints, 100.0)
    assert(character.inventory.isEmpty)
    assertEquals(controller.currentUnit, Some(enemy))
  }

  test("a strength potion reduces subsequent physical damage") {
    val character = new Knight("Dante", 100.0, 10, 40.0)
    val enemy = new Enemy("Vergil", 100.0, 30, 5, 50.0)
    val potion = new StrengthPotion("Strength potion")

    character.addToInventory(potion)
    character.consumePotion(potion)
    enemy.doAction(new Attack, character)

    assertEquals(character.defense, 11.5)
    assertEquals(character.lifePoints, 81.5)
  }

  test("strength potions increase the current defense cumulatively") {
    val character = new Knight("Dante", 100.0, 20, 40.0)
    val firstPotion = new StrengthPotion("First potion")
    val secondPotion = new StrengthPotion("Second potion")

    character.addToInventory(firstPotion)
    character.addToInventory(secondPotion)

    character.consumePotion(firstPotion)
    assertEquals(character.defense, 23.0)

    character.consumePotion(secondPotion)
    assertEqualsDouble(character.defense, 26.45, 0.000001)
    assert(character.inventory.isEmpty)
  }

  test("a defeated unit cannot increase its defense") {
    val character = new Knight("Dante", 0.0, 10, 40.0)

    intercept[IllegalArgumentException] {
      character.increaseDefense(15.0)
    }

    assertEquals(character.defense, 10.0)
  }

  test("invalid defense increases preserve the current defense") {
    val character = new Knight("Dante", 100.0, 10, 40.0)

    Seq(-1.0, Double.NaN, Double.PositiveInfinity, Double.NegativeInfinity)
      .foreach { percentage =>
        intercept[IllegalArgumentException] {
          character.increaseDefense(percentage)
        }

        assertEquals(character.defense, 10.0)
      }
  }

  test("spending mana preserves maximum mana points") {
    val magician = new BlackMage("V", 100.0, 10, 40.0, 100.0)

    magician.spendManaPoints(40.0)

    assertEquals(magician.manaPoints, 60.0)
    assertEquals(magician.maxManaPoints, 100.0)
  }

  test("restoring mana respects maximum mana points") {
    val magician = new WhiteMage("Gandalf", 100.0, 10, 40.0, 100.0)

    magician.spendManaPoints(80.0)
    assertEquals(magician.manaPoints, 20.0)

    magician.restoreManaPoints(30.0)
    assertEquals(magician.manaPoints, 50.0)

    magician.restoreManaPoints(100.0)
    assertEquals(magician.manaPoints, 100.0)
    assertEquals(magician.maxManaPoints, 100.0)
  }

  test("insufficient mana preserves the current mana points") {
    val magician = new BlackMage("V", 100.0, 10, 40.0, 100.0)

    magician.spendManaPoints(30.0)

    intercept[IllegalArgumentException] {
      magician.spendManaPoints(80.0)
    }

    assertEquals(magician.manaPoints, 70.0)
  }

  test("invalid mana amounts preserve the current mana points") {
    val magician = new WhiteMage("Gandalf", 100.0, 10, 40.0, 100.0)

    magician.spendManaPoints(40.0)

    Seq(-1.0, Double.NaN, Double.PositiveInfinity, Double.NegativeInfinity)
      .foreach { amount =>
        intercept[IllegalArgumentException] {
          magician.spendManaPoints(amount)
        }
        assertEquals(magician.manaPoints, 60.0)

        intercept[IllegalArgumentException] {
          magician.restoreManaPoints(amount)
        }
        assertEquals(magician.manaPoints, 60.0)
      }
  }

  test("a defeated magician cannot spend or restore mana") {
    val magician = new BlackMage("V", 100.0, 10, 40.0, 100.0)
    val enemy = new Enemy("Vergil", 100.0, 200, 5, 50.0)

    enemy.doAction(new Attack, magician)
    assertEquals(magician.lifePoints, 0.0)

    intercept[IllegalArgumentException] {
      magician.spendManaPoints(10.0)
    }

    intercept[IllegalArgumentException] {
      magician.restoreManaPoints(10.0)
    }

    assertEquals(magician.manaPoints, 100.0)
  }

  test("both mage classes recover thirty percent of maximum mana") {
    val magicians: Seq[MagicCharacter] = Seq(
      new BlackMage("V", 100.0, 10, 40.0, 200.0),
      new WhiteMage("Gandalf", 100.0, 10, 40.0, 150.0)
    )

    magicians.foreach { magician =>
      val potion = new ManaPotion("Mana potion")
      magician.addToInventory(potion)
      magician.spendManaPoints(magician.maxManaPoints)

      assertEquals(magician.manaPoints, 0.0)

      magician.consumePotion(potion)

      assertEqualsDouble(
        magician.manaPoints,
        magician.maxManaPoints * 0.3,
        0.000001
      )
      assert(magician.inventory.isEmpty)
    }
  }

  test("the controller consumes a mana potion without exceeding maximum mana") {
    val magician = new WhiteMage("Gandalf", 100.0, 10, 40.0, 100.0)
    val enemy = new Enemy("Vergil", 100.0, 20, 5, 50.0)
    val potion = new ManaPotion("Mana potion")

    magician.addToInventory(potion)
    magician.spendManaPoints(10.0)
    assertEquals(magician.manaPoints, 90.0)

    val controller = new GameController(Seq(magician, enemy))
    assertEquals(controller.nextTurn(), Some(magician))

    controller.executeAction(magician, "Consume a potion", potion)

    assertEquals(magician.manaPoints, 100.0)
    assert(magician.inventory.isEmpty)
    assertEquals(controller.currentUnit, Some(enemy))
  }

  test("common characters reject mana potions and preserve their turn") {
    val characters: Seq[Character] = Seq(
      new Knight("Dante", 100.0, 10, 40.0),
      new Archer("Robin", 100.0, 10, 40.0),
      new Thief("Locke", 100.0, 10, 40.0)
    )

    characters.foreach { character =>
      val enemy = new Enemy("Vergil", 100.0, 20, 5, 50.0)
      val potion = new ManaPotion("Mana potion")

      character.addToInventory(potion)

      val controller = new GameController(Seq(character, enemy))
      assertEquals(controller.nextTurn(), Some(character))

      intercept[UnsupportedOperationException] {
        controller.executeAction(character, "Consume a potion", potion)
      }

      assertEquals(character.lifePoints, 100.0)
      assertEquals(character.inventory.toSeq, Seq[Usable](potion))
      assertEquals(controller.currentUnit, Some(character))
    }
  }

  test("both mage classes consume magic force potions and preserve the bonus") {
    val magicians: Seq[MagicCharacter] = Seq(
      new BlackMage("V", 100.0, 10, 40.0, 100.0),
      new WhiteMage("Gandalf", 100.0, 10, 40.0, 100.0)
    )

    magicians.foreach { magician =>
      val enemy = new Enemy("Vergil", 100.0, 20, 5, 50.0)
      val potion = new MagicForcePotion("Magic force potion")

      assertEquals(magician.magicDamageMultiplier, 1.0)
      magician.addToInventory(potion)

      val controller = new GameController(Seq(magician, enemy))
      assertEquals(controller.nextTurn(), Some(magician))

      controller.executeAction(magician, "Consume a potion", potion)

      assertEquals(magician.magicDamageMultiplier, 1.5)
      assertEquals(magician.manaPoints, 100.0)
      assert(magician.inventory.isEmpty)
      assertEquals(controller.currentUnit, Some(enemy))
    }
  }

  test("common characters reject magic force potions and preserve their turn") {
    val characters: Seq[Character] = Seq(
      new Knight("Dante", 100.0, 10, 40.0),
      new Archer("Robin", 100.0, 10, 40.0),
      new Thief("Locke", 100.0, 10, 40.0)
    )

    characters.foreach { character =>
      val enemy = new Enemy("Vergil", 100.0, 20, 5, 50.0)
      val potion = new MagicForcePotion("Magic force potion")

      character.addToInventory(potion)

      val controller = new GameController(Seq(character, enemy))
      assertEquals(controller.nextTurn(), Some(character))

      intercept[UnsupportedOperationException] {
        controller.executeAction(character, "Consume a potion", potion)
      }

      assertEquals(character.lifePoints, 100.0)
      assertEquals(character.inventory.toSeq, Seq[Usable](potion))
      assertEquals(controller.currentUnit, Some(character))
    }
  }

  test("a defeated magician cannot receive a magic force bonus") {
    val magician = new BlackMage("V", 100.0, 10, 40.0, 100.0)
    val enemy = new Enemy("Vergil", 100.0, 200, 5, 50.0)
    val potion = new MagicForcePotion("Magic force potion")

    magician.addToInventory(potion)
    enemy.doAction(new Attack, magician)

    intercept[IllegalArgumentException] {
      potion.applyTo(magician)
    }

    assertEquals(magician.magicDamageMultiplier, 1.0)
    assertEquals(magician.inventory.toSeq, Seq[Usable](potion))
  }

  test("spell damage is unavailable without a magical weapon") {
    val magician = new BlackMage("V", 100.0, 10, 40.0, 100.0)
    val dagger = new Dagger("Dagger", 15, 2.0, magician)

    assertEquals(magician.magicDamage, None)

    magician.equipWeapon(dagger)

    assertEquals(magician.magicDamage, None)
  }

  test("spell damage follows the currently equipped magical weapon") {
    val magicians: Seq[MagicCharacter] = Seq(
      new BlackMage("V", 100.0, 10, 40.0, 100.0),
      new WhiteMage("Gandalf", 100.0, 10, 40.0, 100.0)
    )

    magicians.foreach { magician =>
      val staff = new Staff("Staff", 10, 40, 5.0, magician)
      val wand = new Wand("Wand", 10, 35, 2.0, magician)

      magician.equipWeapon(staff)
      assertEquals(magician.magicDamage, Some(40.0))

      magician.equipWeapon(wand)
      assertEquals(magician.magicDamage, Some(35.0))

      magician.unequipWeapon()
      assertEquals(magician.magicDamage, None)
    }
  }

  test("querying boosted spell damage preserves the pending bonus") {
    val magician = new BlackMage("V", 100.0, 10, 40.0, 100.0)
    val staff = new Staff("Staff", 10, 35, 5.0, magician)
    val potion = new MagicForcePotion("Magic force potion")

    magician.equipWeapon(staff)
    magician.addToInventory(potion)
    magician.consumePotion(potion)

    val firstReading = magician.magicDamage
    val secondReading = magician.magicDamage

    assertEquals(firstReading, Some(52.5))
    assertEquals(secondReading, firstReading)
    assertEquals(magician.magicDamageMultiplier, 1.5)
  }

  test("the controller casts Thunder and advances the turn") {
    val magician = new BlackMage("V", 100.0, 10, 40.0, 100.0)
    val enemy = new Enemy("Vergil", 100.0, 20, 100, 50.0)
    val staff = new Staff("Staff", 10, 40, 5.0, magician)

    magician.equipWeapon(staff)
    assert(magician.findAction("Thunder").nonEmpty)

    val controller = new GameController(Seq(magician, enemy))
    assertEquals(controller.nextTurn(), Some(magician))

    controller.executeAction(magician, "Thunder", enemy)

    assertEquals(enemy.lifePoints, 60.0)
    assertEquals(magician.manaPoints, 80.0)
    assertEquals(controller.currentUnit, Some(enemy))
  }

  test("Thunder consumes the magical bonus only once and clamps health at zero") {
    val magician = new BlackMage("V", 100.0, 10, 40.0, 100.0)
    val enemy = new Enemy("Vergil", 200.0, 20, 100, 50.0)
    val character = new Knight("Dante", 30.0, 100, 50.0)
    val staff = new Staff("Staff", 10, 35, 5.0, magician)
    val potion = new MagicForcePotion("Magic force potion")

    magician.equipWeapon(staff)
    magician.addToInventory(potion)
    magician.consumePotion(potion)

    magician.doAction(new Thunder, enemy)

    assertEquals(enemy.lifePoints, 147.5)
    assertEquals(magician.manaPoints, 80.0)
    assertEquals(magician.magicDamageMultiplier, 1.0)
    assertEquals(magician.magicDamage, Some(35.0))

    magician.doAction(new Thunder, character)

    assertEquals(character.lifePoints, 0.0)
    assertEquals(magician.manaPoints, 60.0)
  }

  test("invalid Thunder targets preserve mana bonus and current turn") {
    val magician = new BlackMage("V", 100.0, 10, 40.0, 100.0)
    val enemy = new Enemy("Vergil", 100.0, 20, 5, 50.0)
    val defeated = new Enemy("Defeated", 0.0, 20, 5, 50.0)
    val staff = new Staff("Staff", 10, 40, 5.0, magician)
    val potion = new MagicForcePotion("Magic force potion")

    magician.equipWeapon(staff)
    magician.addToInventory(potion)
    magician.consumePotion(potion)

    val controller = new GameController(Seq(magician, enemy))
    assertEquals(controller.nextTurn(), Some(magician))

    intercept[IllegalArgumentException] {
      controller.executeAction(magician, "Thunder", defeated)
    }

    intercept[UnsupportedOperationException] {
      controller.executeAction(magician, "Thunder", new Target {})
    }

    assertEquals(magician.manaPoints, 100.0)
    assertEquals(magician.magicDamageMultiplier, 1.5)
    assertEquals(enemy.lifePoints, 100.0)
    assertEquals(defeated.lifePoints, 0.0)
    assertEquals(controller.currentUnit, Some(magician))
  }

  test("Thunder requires a magical weapon and at least twenty mana points") {
    val magician = new BlackMage("V", 100.0, 10, 40.0, 20.0)
    val enemy = new Enemy("Vergil", 100.0, 20, 5, 50.0)
    val dagger = new Dagger("Dagger", 15, 2.0, magician)
    val staff = new Staff("Staff", 10, 40, 5.0, magician)

    assertEquals(magician.findAction("Thunder"), None)
    intercept[IllegalArgumentException] {
      magician.doAction(new Thunder, enemy)
    }

    magician.equipWeapon(dagger)
    assertEquals(magician.findAction("Thunder"), None)
    intercept[IllegalArgumentException] {
      magician.doAction(new Thunder, enemy)
    }

    magician.equipWeapon(staff)
    assert(magician.findAction("Thunder").nonEmpty)

    magician.spendManaPoints(1.0)
    assertEquals(magician.findAction("Thunder"), None)
    intercept[IllegalArgumentException] {
      magician.doAction(new Thunder, enemy)
    }

    assertEquals(magician.manaPoints, 19.0)
    assertEquals(enemy.lifePoints, 100.0)

    magician.restoreManaPoints(1.0)
    magician.doAction(new Thunder, enemy)

    assertEquals(enemy.lifePoints, 60.0)
    assertEquals(magician.manaPoints, 0.0)
    assertEquals(magician.findAction("Thunder"), None)
  }

  test("only black mages can cast Thunder") {
    val whiteMage = new WhiteMage("Gandalf", 100.0, 10, 40.0, 100.0)
    val staff = new Staff("Staff", 10, 40, 5.0, whiteMage)
    whiteMage.equipWeapon(staff)

    val sources: Seq[Source] = Seq(
      new Knight("Dante", 100.0, 10, 40.0),
      new Archer("Robin", 100.0, 10, 40.0),
      new Thief("Locke", 100.0, 10, 40.0),
      whiteMage,
      new Enemy("Attacker", 100.0, 20, 5, 50.0)
    )
    val target = new Enemy("Vergil", 100.0, 20, 5, 50.0)

    sources.foreach { source =>
      assertEquals(source.findAction("Thunder"), None)

      intercept[UnsupportedOperationException] {
        source.doAction(new Thunder, target)
      }
    }

    assertEquals(target.lifePoints, 100.0)
    assertEquals(whiteMage.manaPoints, 100.0)
  }

  test("the controller casts Healing and advances the turn") {
    val magician = new WhiteMage("Gandalf", 100.0, 10, 40.0, 100.0)
    val character = new Knight("Dante", 200.0, 10, 70.0)
    val enemy = new Enemy("Vergil", 100.0, 110, 5, 50.0)
    val staff = new Staff("Staff", 10, 40, 5.0, magician)

    magician.equipWeapon(staff)
    enemy.doAction(new Attack, character)
    assertEquals(character.lifePoints, 100.0)

    val controller = new GameController(Seq(magician, enemy, character))
    assertEquals(controller.nextTurn(), Some(magician))

    controller.executeAction(magician, "Healing", character)

    assertEquals(character.lifePoints, 150.0)
    assertEquals(magician.manaPoints, 85.0)
    assertEquals(controller.currentUnit, Some(enemy))
  }

  test("Healing consumes the magical bonus once and respects maximum health") {
    val magician = new WhiteMage("Gandalf", 100.0, 10, 40.0, 100.0)
    val character = new Knight("Dante", 200.0, 10, 70.0)
    val enemy = new Enemy("Vergil", 100.0, 110, 5, 50.0)
    val staff = new Staff("Staff", 10, 40, 5.0, magician)
    val potion = new MagicForcePotion("Magic force potion")

    magician.equipWeapon(staff)
    magician.addToInventory(potion)
    magician.consumePotion(potion)
    enemy.doAction(new Attack, character)

    magician.doAction(new Healing, character)

    assertEquals(character.lifePoints, 155.0)
    assertEquals(magician.manaPoints, 85.0)
    assertEquals(magician.magicDamageMultiplier, 1.0)

    magician.doAction(new Healing, character)

    assertEquals(character.lifePoints, 200.0)
    assertEquals(magician.manaPoints, 70.0)
  }

  test("invalid Healing targets preserve mana bonus and current turn") {
    val magician = new WhiteMage("Gandalf", 100.0, 10, 40.0, 100.0)
    val enemy = new Enemy("Vergil", 100.0, 20, 5, 50.0)
    val defeated = new Knight("Defeated", 0.0, 10, 70.0)
    val staff = new Staff("Staff", 10, 40, 5.0, magician)
    val potion = new MagicForcePotion("Magic force potion")

    magician.equipWeapon(staff)
    magician.addToInventory(potion)
    magician.consumePotion(potion)

    val controller = new GameController(Seq(magician, enemy))
    assertEquals(controller.nextTurn(), Some(magician))

    intercept[UnsupportedOperationException] {
      controller.executeAction(magician, "Healing", enemy)
    }

    intercept[IllegalArgumentException] {
      controller.executeAction(magician, "Healing", defeated)
    }

    assertEquals(magician.manaPoints, 100.0)
    assertEquals(magician.magicDamageMultiplier, 1.5)
    assertEquals(enemy.lifePoints, 100.0)
    assertEquals(defeated.lifePoints, 0.0)
    assertEquals(controller.currentUnit, Some(magician))
  }

  test("Healing requires at least fifteen mana points") {
    val magician = new WhiteMage("Gandalf", 100.0, 10, 40.0, 15.0)
    val character = new Knight("Dante", 100.0, 10, 70.0)
    val enemy = new Enemy("Vergil", 100.0, 30, 5, 50.0)
    val staff = new Staff("Staff", 10, 40, 5.0, magician)

    magician.equipWeapon(staff)
    enemy.doAction(new Attack, character)
    assert(magician.findAction("Healing").nonEmpty)

    magician.spendManaPoints(1.0)
    assertEquals(magician.findAction("Healing"), None)

    intercept[IllegalArgumentException] {
      magician.doAction(new Healing, character)
    }

    assertEquals(magician.manaPoints, 14.0)
    assertEquals(character.lifePoints, 80.0)

    magician.restoreManaPoints(1.0)
    magician.doAction(new Healing, character)

    assertEquals(character.lifePoints, 100.0)
    assertEquals(magician.manaPoints, 0.0)
    assertEquals(magician.findAction("Healing"), None)
  }

  test("only white mages can cast Healing") {
    val blackMage = new BlackMage("V", 100.0, 10, 40.0, 100.0)
    val staff = new Staff("Staff", 10, 40, 5.0, blackMage)
    blackMage.equipWeapon(staff)

    val sources: Seq[Source] = Seq(
      new Knight("Dante", 100.0, 10, 40.0),
      new Archer("Robin", 100.0, 10, 40.0),
      new Thief("Locke", 100.0, 10, 40.0),
      blackMage,
      new Enemy("Attacker", 100.0, 20, 5, 50.0)
    )
    val target = new Knight("Target", 100.0, 10, 70.0)

    sources.foreach { source =>
      assertEquals(source.findAction("Healing"), None)

      intercept[UnsupportedOperationException] {
        source.doAction(new Healing, target)
      }
    }

    assertEquals(target.lifePoints, 100.0)
    assertEquals(blackMage.manaPoints, 100.0)
  }

  test("Purification uses current enemy health and advances the turn") {
    val magician = new WhiteMage("Gandalf", 100.0, 10, 40.0, 100.0)
    val enemy = new Enemy("Vergil", 200.0, 20, 100, 50.0)
    val attacker = new Enemy("Attacker", 100.0, 200, 5, 60.0)
    val staff = new Staff("Staff", 10, 40, 5.0, magician)

    magician.equipWeapon(staff)
    attacker.doAction(new Attack, enemy)
    assertEquals(enemy.lifePoints, 100.0)

    val controller = new GameController(Seq(magician, enemy))
    assertEquals(controller.nextTurn(), Some(magician))

    controller.executeAction(magician, "Purification", enemy)

    assertEquals(enemy.lifePoints, 65.0)
    assertEquals(magician.manaPoints, 75.0)
    assertEquals(controller.currentUnit, Some(enemy))
  }

  test("Purification consumes the magical bonus only once") {
    val magician = new WhiteMage("Gandalf", 100.0, 10, 40.0, 100.0)
    val enemy = new Enemy("Vergil", 200.0, 20, 100, 50.0)
    val staff = new Staff("Staff", 10, 40, 5.0, magician)
    val potion = new MagicForcePotion("Magic force potion")

    magician.equipWeapon(staff)
    magician.addToInventory(potion)
    magician.consumePotion(potion)

    magician.doAction(new Purification, enemy)

    assertEquals(enemy.lifePoints, 140.0)
    assertEquals(magician.manaPoints, 75.0)
    assertEquals(magician.magicDamageMultiplier, 1.0)

    magician.doAction(new Purification, enemy)

    assertEqualsDouble(enemy.lifePoints, 99.0, 0.000001)
    assertEquals(magician.manaPoints, 50.0)
  }

  test("invalid Purification targets preserve mana bonus and current turn") {
    val magician = new WhiteMage("Gandalf", 100.0, 10, 40.0, 100.0)
    val enemy = new Enemy("Vergil", 100.0, 20, 5, 50.0)
    val character = new Knight("Dante", 100.0, 10, 70.0)
    val defeated = new Enemy("Defeated", 0.0, 20, 5, 50.0)
    val staff = new Staff("Staff", 10, 40, 5.0, magician)
    val potion = new MagicForcePotion("Magic force potion")

    magician.equipWeapon(staff)
    magician.addToInventory(potion)
    magician.consumePotion(potion)

    val controller = new GameController(Seq(magician, enemy))
    assertEquals(controller.nextTurn(), Some(magician))

    intercept[UnsupportedOperationException] {
      controller.executeAction(magician, "Purification", character)
    }

    intercept[IllegalArgumentException] {
      controller.executeAction(magician, "Purification", defeated)
    }

    assertEquals(character.lifePoints, 100.0)
    assertEquals(defeated.lifePoints, 0.0)
    assertEquals(magician.manaPoints, 100.0)
    assertEquals(magician.magicDamageMultiplier, 1.5)
    assertEquals(controller.currentUnit, Some(magician))
  }

  test("white spells become available according to their individual mana costs") {
    val magician = new WhiteMage("Gandalf", 100.0, 10, 40.0, 25.0)
    val enemy = new Enemy("Vergil", 100.0, 20, 5, 50.0)
    val staff = new Staff("Staff", 10, 40, 5.0, magician)

    magician.equipWeapon(staff)
    assert(magician.findAction("Purification").nonEmpty)

    magician.spendManaPoints(1.0)

    assertEquals(magician.findAction("Purification"), None)
    assert(magician.findAction("Healing").nonEmpty)

    intercept[IllegalArgumentException] {
      magician.doAction(new Purification, enemy)
    }

    assertEquals(enemy.lifePoints, 100.0)
    assertEquals(magician.manaPoints, 24.0)

    magician.restoreManaPoints(1.0)
    magician.doAction(new Purification, enemy)

    assertEquals(enemy.lifePoints, 65.0)
    assertEquals(magician.manaPoints, 0.0)
    assertEquals(magician.findAction("Purification"), None)
    assertEquals(magician.findAction("Healing"), None)
  }

  test("only white mages can cast Purification") {
    val blackMage = new BlackMage("V", 100.0, 10, 40.0, 100.0)
    val staff = new Staff("Staff", 10, 40, 5.0, blackMage)
    blackMage.equipWeapon(staff)

    val sources: Seq[Source] = Seq(
      new Knight("Dante", 100.0, 10, 40.0),
      new Archer("Robin", 100.0, 10, 40.0),
      new Thief("Locke", 100.0, 10, 40.0),
      blackMage,
      new Enemy("Attacker", 100.0, 20, 5, 50.0)
    )
    val target = new Enemy("Vergil", 100.0, 20, 5, 50.0)

    sources.foreach { source =>
      assertEquals(source.findAction("Purification"), None)

      intercept[UnsupportedOperationException] {
        source.doAction(new Purification, target)
      }
    }

    assertEquals(target.lifePoints, 100.0)
    assertEquals(blackMage.manaPoints, 100.0)
  }

  test("the controller casts Meteorite against all units in the target panel") {
    val magician = new BlackMage("V", 100.0, 10, 40.0, 100.0)
    val enemy = new Enemy("Vergil", 100.0, 20, 100, 50.0)
    val character = new Knight("Dante", 100.0, 100, 70.0)
    val outside = new Enemy("Outside", 100.0, 20, 5, 80.0)
    val staff = new Staff("Staff", 10, 40, 5.0, magician)
    val panel = new Panel(
      1, 1, Array[GameUnit](enemy, character), Array.empty[Panel]
    )

    magician.equipWeapon(staff)

    val controller =
      new GameController(Seq(magician, enemy, character, outside))
    assertEquals(controller.nextTurn(), Some(magician))

    controller.executeAction(magician, "Meteorite", panel)

    assertEquals(enemy.lifePoints, 60.0)
    assertEquals(character.lifePoints, 60.0)
    assertEquals(outside.lifePoints, 100.0)
    assertEquals(magician.manaPoints, 50.0)
    assertEquals(controller.currentUnit, Some(enemy))
  }

  test("Meteorite applies one magical bonus to the entire panel") {
    val magician = new BlackMage("V", 100.0, 10, 40.0, 100.0)
    val enemy = new Enemy("Vergil", 200.0, 20, 100, 50.0)
    val character = new Knight("Dante", 200.0, 100, 70.0)
    val staff = new Staff("Staff", 10, 40, 5.0, magician)
    val potion = new MagicForcePotion("Magic force potion")
    val panel = new Panel(
      1, 1, Array[GameUnit](enemy, character), Array.empty[Panel]
    )

    magician.equipWeapon(staff)
    magician.addToInventory(potion)
    magician.consumePotion(potion)

    magician.doAction(new Meteorite, panel)

    assertEquals(enemy.lifePoints, 140.0)
    assertEquals(character.lifePoints, 140.0)
    assertEquals(magician.manaPoints, 50.0)
    assertEquals(magician.magicDamageMultiplier, 1.0)

    magician.doAction(new Meteorite, panel)

    assertEquals(enemy.lifePoints, 100.0)
    assertEquals(character.lifePoints, 100.0)
    assertEquals(magician.manaPoints, 0.0)
  }

  test("invalid Meteorite targets preserve health mana bonus and turn") {
    val magician = new BlackMage("V", 100.0, 10, 40.0, 100.0)
    val enemy = new Enemy("Vergil", 100.0, 20, 5, 50.0)
    val defeated = new Enemy("Defeated", 0.0, 20, 5, 50.0)
    val staff = new Staff("Staff", 10, 40, 5.0, magician)
    val potion = new MagicForcePotion("Magic force potion")
    val panel = new Panel(
      1, 1, Array[GameUnit](enemy, defeated), Array.empty[Panel]
    )

    magician.equipWeapon(staff)
    magician.addToInventory(potion)
    magician.consumePotion(potion)

    val controller = new GameController(Seq(magician, enemy))
    assertEquals(controller.nextTurn(), Some(magician))

    intercept[UnsupportedOperationException] {
      controller.executeAction(magician, "Meteorite", enemy)
    }

    intercept[IllegalArgumentException] {
      controller.executeAction(magician, "Meteorite", panel)
    }

    assertEquals(enemy.lifePoints, 100.0)
    assertEquals(defeated.lifePoints, 0.0)
    assertEquals(magician.manaPoints, 100.0)
    assertEquals(magician.magicDamageMultiplier, 1.5)
    assertEquals(controller.currentUnit, Some(magician))
  }

  test("dark spells become available according to their individual mana costs") {
    val magician = new BlackMage("V", 100.0, 10, 40.0, 50.0)
    val enemy = new Enemy("Vergil", 100.0, 20, 5, 50.0)
    val staff = new Staff("Staff", 10, 40, 5.0, magician)
    val panel = new Panel(
      1, 1, Array[GameUnit](enemy), Array.empty[Panel]
    )

    magician.equipWeapon(staff)
    assert(magician.findAction("Meteorite").nonEmpty)

    magician.spendManaPoints(1.0)

    assertEquals(magician.findAction("Meteorite"), None)
    assert(magician.findAction("Thunder").nonEmpty)

    intercept[IllegalArgumentException] {
      magician.doAction(new Meteorite, panel)
    }

    assertEquals(enemy.lifePoints, 100.0)
    assertEquals(magician.manaPoints, 49.0)

    magician.restoreManaPoints(1.0)
    magician.doAction(new Meteorite, panel)

    assertEquals(enemy.lifePoints, 60.0)
    assertEquals(magician.manaPoints, 0.0)
    assertEquals(magician.findAction("Meteorite"), None)
    assertEquals(magician.findAction("Thunder"), None)
  }

  test("only black mages can cast Meteorite") {
    val whiteMage = new WhiteMage("Gandalf", 100.0, 10, 40.0, 100.0)
    val staff = new Staff("Staff", 10, 40, 5.0, whiteMage)
    whiteMage.equipWeapon(staff)

    val sources: Seq[Source] = Seq(
      new Knight("Dante", 100.0, 10, 40.0),
      new Archer("Robin", 100.0, 10, 40.0),
      new Thief("Locke", 100.0, 10, 40.0),
      whiteMage,
      new Enemy("Attacker", 100.0, 20, 5, 50.0)
    )
    val target = new Enemy("Vergil", 100.0, 20, 5, 50.0)
    val panel = new Panel(
      1, 1, Array[GameUnit](target), Array.empty[Panel]
    )

    sources.foreach { source =>
      assertEquals(source.findAction("Meteorite"), None)

      intercept[UnsupportedOperationException] {
        source.doAction(new Meteorite, panel)
      }
    }

    assertEquals(target.lifePoints, 100.0)
    assertEquals(whiteMage.manaPoints, 100.0)
  }

  test("Meteorite completes when the caster is hit and processes each unit once") {
    val magician = new BlackMage("V", 30.0, 10, 40.0, 100.0)
    val enemy = new Enemy("Vergil", 100.0, 20, 100, 50.0)
    val staff = new Staff("Staff", 10, 40, 5.0, magician)
    val potion = new MagicForcePotion("Magic force potion")
    val panel = new Panel(
      1, 1, Array[GameUnit](magician, magician, enemy), Array.empty[Panel]
    )

    magician.equipWeapon(staff)
    magician.addToInventory(potion)
    magician.consumePotion(potion)

    magician.doAction(new Meteorite, panel)

    assertEquals(magician.lifePoints, 0.0)
    assertEquals(enemy.lifePoints, 40.0)
    assertEquals(magician.manaPoints, 50.0)
    assertEquals(magician.magicDamageMultiplier, 1.0)
    assert(magician.availableActions.isEmpty)
  }

  test("the controller skips units already defeated before the first turn") {
    val defeated = new Enemy("Defeated", 0.0, 20, 5, 10.0)
    val living = new Enemy("Living", 100.0, 20, 5, 40.0)
    val controller = new GameController(Seq(defeated, living))

    assertEquals(controller.nextTurn(), Some(living))
    assertEquals(controller.nextTurn(), Some(living))
  }

  test("defeating a unit prevents it from receiving the next turn") {
    val character = new Knight("Dante", 100.0, 10, 40.0)
    val defeated = new Enemy("Vergil", 20.0, 20, 5, 50.0)
    val survivor = new Enemy("Survivor", 100.0, 20, 5, 70.0)
    val sword = new Sword("Rebellion", 30, 5.0, character)

    character.equipWeapon(sword)

    val controller = new GameController(Seq(character, defeated, survivor))
    assertEquals(controller.nextTurn(), Some(character))

    controller.executeAction(character, "Attack", defeated)

    assertEquals(defeated.lifePoints, 0.0)
    assertEquals(controller.currentUnit, Some(survivor))
    assertEquals(controller.nextTurn(), Some(character))
  }

  test("a caster defeated by Meteorite receives no further turns") {
    val magician = new BlackMage("V", 30.0, 10, 40.0, 100.0)
    val enemy = new Enemy("Vergil", 100.0, 20, 5, 50.0)
    val staff = new Staff("Staff", 10, 40, 5.0, magician)
    val panel = new Panel(
      1, 1, Array[GameUnit](magician, enemy), Array.empty[Panel]
    )

    magician.equipWeapon(staff)

    val controller = new GameController(Seq(magician, enemy))
    assertEquals(controller.nextTurn(), Some(magician))

    controller.executeAction(magician, "Meteorite", panel)

    assertEquals(magician.lifePoints, 0.0)
    assertEquals(enemy.lifePoints, 60.0)
    assertEquals(magician.manaPoints, 50.0)
    assertEquals(controller.currentUnit, Some(enemy))
    assertEquals(controller.nextTurn(), Some(enemy))
  }

  test("the controller has no active turn when Meteorite defeats every unit") {
    val magician = new BlackMage("V", 30.0, 10, 40.0, 100.0)
    val enemy = new Enemy("Vergil", 30.0, 20, 5, 50.0)
    val staff = new Staff("Staff", 10, 40, 5.0, magician)
    val panel = new Panel(
      1, 1, Array[GameUnit](magician, enemy), Array.empty[Panel]
    )

    magician.equipWeapon(staff)

    val controller = new GameController(Seq(magician, enemy))
    assertEquals(controller.nextTurn(), Some(magician))

    controller.executeAction(magician, "Meteorite", panel)

    assertEquals(magician.lifePoints, 0.0)
    assertEquals(enemy.lifePoints, 0.0)
    assertEquals(magician.manaPoints, 50.0)
    assertEquals(controller.currentUnit, None)
    assertEquals(controller.nextTurn(), None)
  }

  test("defeated units with zero weight do not prevent turn selection") {
    val character = new Knight("Defeated knight", 0.0, 10, 0.0)
    val enemy = new Enemy("Defeated enemy", 0.0, 20, 5, 0.0)
    val controller = new GameController(Seq(character, enemy))

    assertEquals(controller.nextTurn(), None)
    assertEquals(controller.currentUnit, None)
  }

  test("a panel adds units without duplicating them") {
    val character = new Knight("Dante", 100.0, 10, 40.0)
    val enemy = new Enemy("Vergil", 100.0, 20, 5, 50.0)
    val panel = new Panel(
      1, 1, Array.empty[GameUnit], Array.empty[Panel]
    )

    assert(!panel.containsUnit(character))

    panel.addUnit(character)
    panel.addUnit(character)
    panel.addUnit(enemy)

    assert(panel.containsUnit(character))
    assert(panel.containsUnit(enemy))
    assertEquals(
      panel.units.toSeq,
      Seq[GameUnit](character, enemy)
    )
  }

  test("a panel removes only the requested unit") {
    val first = new Enemy("First", 100.0, 20, 5, 40.0)
    val second = new Enemy("Second", 100.0, 20, 5, 50.0)
    val absent = new Enemy("Absent", 100.0, 20, 5, 60.0)
    val panel = new Panel(
      1, 1, Array[GameUnit](first, second), Array.empty[Panel]
    )

    panel.removeUnit(first)

    assert(!panel.containsUnit(first))
    assert(panel.containsUnit(second))
    assertEquals(panel.units.toSeq, Seq[GameUnit](second))

    panel.removeUnit(absent)
    panel.removeUnit(first)
    assertEquals(panel.units.toSeq, Seq[GameUnit](second))

    panel.removeUnit(second)
    assert(panel.units.isEmpty)
  }

  test("creating a panel assigns its units a current position") {
    val enemy = new Enemy("Vergil", 100.0, 20, 5, 50.0)

    assertEquals(enemy.currentPanel, None)

    val panel = new Panel(
      1, 1, Array[GameUnit](enemy), Array.empty[Panel]
    )

    assertEquals(enemy.currentPanel, Some(panel))
    assert(panel.containsUnit(enemy))
  }

  test("placing a unit in another panel updates both panels") {
    val enemy = new Enemy("Vergil", 100.0, 20, 5, 50.0)
    val origin = new Panel(
      1, 1, Array[GameUnit](enemy), Array.empty[Panel]
    )
    val destination = new Panel(
      2, 1, Array.empty[GameUnit], Array.empty[Panel]
    )

    destination.addUnit(enemy)

    assert(origin.units.isEmpty)
    assertEquals(enemy.currentPanel, Some(destination))
    assertEquals(destination.units.toSeq, Seq[GameUnit](enemy))

    origin.removeUnit(enemy)
    assertEquals(enemy.currentPanel, Some(destination))

    destination.addUnit(enemy)
    assertEquals(destination.units.toSeq, Seq[GameUnit](enemy))
  }

  test("replacing panel units updates their current positions") {
    val first = new Enemy("First", 100.0, 20, 5, 40.0)
    val second = new Enemy("Second", 100.0, 20, 5, 50.0)
    val panel = new Panel(
      1, 1, Array[GameUnit](first), Array.empty[Panel]
    )

    panel.setUnits(Array[GameUnit](second))

    assertEquals(first.currentPanel, None)
    assertEquals(second.currentPanel, Some(panel))

    panel.setUnits(Array.empty[GameUnit])

    assertEquals(second.currentPanel, None)
    assert(panel.units.isEmpty)
  }

  test("a physical attack removes a defeated unit from its panel") {
    val character = new Knight("Dante", 20.0, 5, 40.0)
    val survivor = new Knight("Survivor", 100.0, 10, 60.0)
    val enemy = new Enemy("Vergil", 100.0, 30, 5, 50.0)
    val panel = new Panel(
      1, 1, Array[GameUnit](character, survivor), Array.empty[Panel]
    )

    enemy.doAction(new Attack, character)

    assertEquals(character.lifePoints, 0.0)
    assertEquals(character.currentPanel, None)
    assertEquals(survivor.currentPanel, Some(panel))
    assertEquals(panel.units.toSeq, Seq[GameUnit](survivor))
  }

  test("Meteorite removes defeated units while keeping survivors in the panel") {
    val magician = new BlackMage("V", 30.0, 10, 40.0, 100.0)
    val enemy = new Enemy("Vergil", 100.0, 20, 5, 50.0)
    val staff = new Staff("Staff", 10, 40, 5.0, magician)
    val panel = new Panel(
      1, 1, Array[GameUnit](magician, enemy), Array.empty[Panel]
    )

    magician.equipWeapon(staff)
    magician.doAction(new Meteorite, panel)

    assertEquals(magician.lifePoints, 0.0)
    assertEquals(magician.currentPanel, None)
    assertEquals(enemy.lifePoints, 60.0)
    assertEquals(enemy.currentPanel, Some(panel))
    assertEquals(panel.units.toSeq, Seq[GameUnit](enemy))
    assertEquals(magician.manaPoints, 50.0)
  }

  test("all character classes can move through the controller") {
    val characters: Seq[Character] = Seq(
      new Knight("Dante", 100.0, 10, 40.0),
      new Archer("Robin", 100.0, 10, 40.0),
      new Thief("Locke", 100.0, 10, 40.0),
      new BlackMage("V", 100.0, 10, 40.0, 100.0),
      new WhiteMage("Gandalf", 100.0, 10, 40.0, 100.0)
    )

    characters.foreach { character =>
      val enemy = new Enemy("Vergil", 100.0, 20, 5, 50.0)
      val origin = new Panel(
        1, 1, Array[GameUnit](character), Array.empty[Panel]
      )
      val destination = new Panel(
        2, 1, Array.empty[GameUnit], Array.empty[Panel]
      )
      origin.setAdjacentPanels(Array(destination))

      val controller = new GameController(Seq(character, enemy))
      assertEquals(controller.nextTurn(), Some(character))

      controller.executeAction(character, "Move", destination)

      assert(origin.units.isEmpty)
      assertEquals(character.currentPanel, Some(destination))
      assertEquals(destination.units.toSeq, Seq[GameUnit](character))
      assertEquals(controller.currentUnit, Some(enemy))
    }
  }

  test("an enemy can move through the controller") {
    val enemy = new Enemy("Vergil", 100.0, 20, 5, 40.0)
    val character = new Knight("Dante", 100.0, 10, 50.0)
    val origin = new Panel(
      1, 1, Array[GameUnit](enemy), Array.empty[Panel]
    )
    val destination = new Panel(
      2, 1, Array.empty[GameUnit], Array.empty[Panel]
    )
    origin.setAdjacentPanels(Array(destination))

    val controller = new GameController(Seq(enemy, character))
    assertEquals(controller.nextTurn(), Some(enemy))

    controller.executeAction(enemy, "Move", destination)

    assert(origin.units.isEmpty)
    assertEquals(enemy.currentPanel, Some(destination))
    assertEquals(destination.units.toSeq, Seq[GameUnit](enemy))
    assertEquals(controller.currentUnit, Some(character))
  }

  test("invalid movement destinations preserve position and current turn") {
    val character = new Knight("Dante", 100.0, 10, 40.0)
    val enemy = new Enemy("Vergil", 100.0, 20, 5, 50.0)
    val origin = new Panel(
      1, 1, Array[GameUnit](character), Array.empty[Panel]
    )
    val adjacent = new Panel(
      2, 1, Array.empty[GameUnit], Array.empty[Panel]
    )
    val remote = new Panel(
      3, 1, Array.empty[GameUnit], Array.empty[Panel]
    )
    origin.setAdjacentPanels(Array(adjacent, origin))

    val controller = new GameController(Seq(character, enemy))
    assertEquals(controller.nextTurn(), Some(character))

    intercept[IllegalArgumentException] {
      controller.executeAction(character, "Move", remote)
    }

    intercept[IllegalArgumentException] {
      controller.executeAction(character, "Move", origin)
    }

    assertEquals(character.currentPanel, Some(origin))
    assertEquals(origin.units.toSeq, Seq[GameUnit](character))
    assert(adjacent.units.isEmpty)
    assert(remote.units.isEmpty)
    assertEquals(controller.currentUnit, Some(character))
  }

  test("unsupported movement targets preserve position and current turn") {
    val character = new Knight("Dante", 100.0, 10, 40.0)
    val enemy = new Enemy("Vergil", 100.0, 20, 5, 50.0)
    val origin = new Panel(
      1, 1, Array[GameUnit](character), Array.empty[Panel]
    )
    val destination = new Panel(
      2, 1, Array.empty[GameUnit], Array.empty[Panel]
    )
    origin.setAdjacentPanels(Array(destination))

    val controller = new GameController(Seq(character, enemy))
    assertEquals(controller.nextTurn(), Some(character))

    intercept[UnsupportedOperationException] {
      controller.executeAction(character, "Move", enemy)
    }

    assertEquals(character.currentPanel, Some(origin))
    assertEquals(origin.units.toSeq, Seq[GameUnit](character))
    assert(destination.units.isEmpty)
    assertEquals(controller.currentUnit, Some(character))
  }

  test("movement availability follows placement and adjacent destinations") {
    val enemy = new Enemy("Vergil", 100.0, 20, 5, 40.0)
    val destination = new Panel(
      2, 1, Array.empty[GameUnit], Array.empty[Panel]
    )

    assertEquals(enemy.findAction("Move"), None)

    intercept[IllegalArgumentException] {
      enemy.doAction(new Move, destination)
    }

    assertEquals(enemy.currentPanel, None)
    assert(destination.units.isEmpty)

    val origin = new Panel(
      1, 1, Array[GameUnit](enemy), Array.empty[Panel]
    )
    assertEquals(enemy.findAction("Move"), None)

    origin.setAdjacentPanels(Array(origin))
    assertEquals(enemy.findAction("Move"), None)

    origin.setAdjacentPanels(Array(destination))
    assert(enemy.findAction("Move").nonEmpty)

    enemy.doAction(new Move, destination)

    assert(origin.units.isEmpty)
    assertEquals(enemy.currentPanel, Some(destination))
    assertEquals(enemy.findAction("Move"), None)
  }

  test("defeated units cannot move even when placed on a panel") {
    val enemy = new Enemy("Defeated", 0.0, 20, 5, 40.0)
    val origin = new Panel(
      1, 1, Array[GameUnit](enemy), Array.empty[Panel]
    )
    val destination = new Panel(
      2, 1, Array.empty[GameUnit], Array.empty[Panel]
    )
    origin.setAdjacentPanels(Array(destination))

    assertEquals(enemy.findAction("Move"), None)

    intercept[IllegalArgumentException] {
      enemy.moveTo(destination)
    }

    intercept[IllegalArgumentException] {
      enemy.doAction(new Move, destination)
    }

    assertEquals(enemy.currentPanel, Some(origin))
    assert(origin.containsUnit(enemy))
    assert(destination.units.isEmpty)
  }

  test("resetting restores health defense and defeated participants") {
    val enemy = new Enemy("Vergil", 100.0, 200, 5, 40.0)
    val character = new Knight("Dante", 100.0, 10, 50.0)
    val controller = new GameController(Seq(enemy, character))

    character.increaseDefense(15.0)
    assertEquals(controller.nextTurn(), Some(enemy))

    controller.executeAction(enemy, "Attack", character)
    assertEquals(character.lifePoints, 0.0)

    controller.resetGame()

    assertEquals(character.lifePoints, 100.0)
    assertEquals(character.defense, 10.0)
    assertEquals(controller.currentUnit, None)
    assertEquals(controller.nextTurn(), Some(enemy))
    assertEquals(controller.nextTurn(), Some(character))
  }

  test("resetting restores original panel positions and turn progression") {
    val character = new Knight("Dante", 100.0, 10, 40.0)
    val enemy = new Enemy("Vergil", 100.0, 20, 5, 50.0)
    val origin = new Panel(
      1, 1, Array[GameUnit](character), Array.empty[Panel]
    )
    val destination = new Panel(
      2, 1, Array.empty[GameUnit], Array.empty[Panel]
    )
    origin.setAdjacentPanels(Array(destination))

    val controller = new GameController(Seq(character, enemy))
    assertEquals(controller.nextTurn(), Some(character))

    controller.executeAction(character, "Move", destination)
    assertEquals(character.currentPanel, Some(destination))

    controller.resetGame()

    assertEquals(character.currentPanel, Some(origin))
    assertEquals(origin.units.toSeq, Seq[GameUnit](character))
    assert(destination.units.isEmpty)
    assertEquals(controller.currentUnit, None)
    assertEquals(controller.nextTurn(), Some(character))
  }

  test("repeated resets restore equipment and consumed inventory items") {
    val character = new Knight("Dante", 100.0, 10, 40.0)
    val original = new Sword("Original", 30, 5.0, character)
    val replacement = new Sword("Replacement", 40, 8.0, character)
    val potion = new HealingPotion("Healing potion")

    character.addToInventory(original)
    character.addToInventory(replacement)
    character.addToInventory(potion)
    character.equipWeapon(original)

    val controller = new GameController(Seq(character))

    (1 to 2).foreach { _ =>
      character.consumePotion(potion)
      character.equipWeapon(replacement)
      character.addToInventory(new HealingPotion("Extra potion"))

      controller.resetGame()

      assertEquals(character.weapon, Some(original))
      assertEquals(
        character.inventory.toSeq,
        Seq[Usable](original, replacement, potion)
      )
      assertEquals(controller.currentUnit, None)
    }
  }

  test("resetting restores mana magical bonuses and potion inventory") {
    val magician = new BlackMage("V", 100.0, 10, 40.0, 100.0)
    val enemy = new Enemy("Vergil", 200.0, 20, 5, 50.0)
    val staff = new Staff("Staff", 10, 40, 5.0, magician)
    val firstPotion = new MagicForcePotion("First potion")
    val secondPotion = new MagicForcePotion("Second potion")

    magician.equipWeapon(staff)
    magician.addToInventory(firstPotion)
    magician.addToInventory(secondPotion)

    val controller = new GameController(Seq(magician, enemy))

    magician.consumePotion(firstPotion)
    magician.doAction(new Thunder, enemy)
    magician.consumePotion(secondPotion)

    assertEquals(magician.manaPoints, 80.0)
    assertEquals(magician.magicDamageMultiplier, 1.5)

    controller.resetGame()

    assertEquals(magician.manaPoints, 100.0)
    assertEquals(magician.magicDamageMultiplier, 1.0)
    assertEquals(enemy.lifePoints, 200.0)
    assertEquals(
      magician.inventory.toSeq,
      Seq[Usable](firstPotion, secondPotion)
    )
  }

  test("resetting restores the state captured at controller creation") {
    val enemy = new Enemy("Vergil", 100.0, 20, 5, 40.0)
    val attacker = new Enemy("Attacker", 100.0, 30, 5, 60.0)

    attacker.doAction(new Attack, enemy)
    assertEquals(enemy.lifePoints, 75.0)

    val controller = new GameController(Seq(enemy))
    val panel = new Panel(
      1, 1, Array[GameUnit](enemy), Array.empty[Panel]
    )

    enemy.increaseDefense(15.0)
    attacker.doAction(new Attack, enemy)

    controller.resetGame()

    assertEquals(enemy.lifePoints, 75.0)
    assertEquals(enemy.maxLifePoints, 100.0)
    assertEquals(enemy.defense, 5.0)
    assertEquals(enemy.currentPanel, None)
    assert(panel.units.isEmpty)
    assertEquals(controller.currentUnit, None)
  }

  test("character weapon permissions follow the equipment table") {
    // Order: sword, dagger, bow, wand, staff.
    val cases: Seq[(Character, Seq[Boolean])] = Seq(
      (
        new Knight("Knight", 100.0, 10, 40.0),
        Seq(true, true, true, false, false)
      ),
      (
        new Archer("Archer", 100.0, 10, 40.0),
        Seq(true, false, true, true, false)
      ),
      (
        new Thief("Thief", 100.0, 10, 40.0),
        Seq(true, true, false, false, false)
      ),
      (
        new BlackMage("Black mage", 100.0, 10, 40.0, 100.0),
        Seq(false, true, false, true, true)
      ),
      (
        new WhiteMage("White mage", 100.0, 10, 40.0, 100.0),
        Seq(false, false, true, true, true)
      )
    )

    cases.foreach { case (character, expected) =>
      val actual = Seq(
        character.canEquip(new Sword("Sword", 10, 5.0, character)),
        character.canEquip(new Dagger("Dagger", 10, 2.0, character)),
        character.canEquip(new Bow("Bow", 10, 3.0, character)),
        character.canEquip(new Wand("Wand", 10, 35, 2.0, character)),
        character.canEquip(new Staff("Staff", 10, 40, 5.0, character))
      )

      assertEquals(actual, expected)
    }
  }

  test("equipment enforces permissions for every character and weapon") {
    // Order: sword, dagger, bow, wand, staff.
    val cases: Seq[(Character, Seq[Boolean])] = Seq(
      (
        new Knight("Knight", 100.0, 10, 40.0),
        Seq(true, true, true, false, false)
      ),
      (
        new Archer("Archer", 100.0, 10, 40.0),
        Seq(true, false, true, true, false)
      ),
      (
        new Thief("Thief", 100.0, 10, 40.0),
        Seq(true, true, false, false, false)
      ),
      (
        new BlackMage("Black mage", 100.0, 10, 40.0, 100.0),
        Seq(false, true, false, true, true)
      ),
      (
        new WhiteMage("White mage", 100.0, 10, 40.0, 100.0),
        Seq(false, false, true, true, true)
      )
    )

    cases.foreach { case (character, expected) =>
      val weapons: Seq[Weapon] = Seq(
        new Sword("Sword", 10, 5.0, character),
        new Dagger("Dagger", 10, 2.0, character),
        new Bow("Bow", 10, 3.0, character),
        new Wand("Wand", 10, 35, 2.0, character),
        new Staff("Staff", 10, 40, 5.0, character)
      )

      weapons.zip(expected).foreach { case (weapon, allowed) =>
        val previousWeapon = character.weapon

        if (allowed) {
          character.equipWeapon(weapon)
          assertEquals(character.weapon, Some(weapon))
        } else {
          val error = intercept[IllegalArgumentException] {
            character.equipWeapon(weapon)
          }

          assert(error.getMessage.contains("not compatible"))
          assertEquals(character.weapon, previousWeapon)
        }
      }
    }
  }

  test("incompatible equipment is filtered and preserves the current turn") {
    val character = new Knight("Dante", 100.0, 10, 40.0)
    val enemy = new Enemy("Vergil", 100.0, 20, 5, 50.0)
    val sword = new Sword("Rebellion", 30, 5.0, character)
    val staff = new Staff("Staff", 10, 40, 5.0, character)

    character.equipWeapon(sword)
    character.addToInventory(staff)
    assertEquals(character.findAction("Equip a weapon"), None)

    character.addToInventory(sword)

    character.findAction("Equip a weapon") match {
      case Some(action: EquipWeapon) =>
        assertEquals(action.possibleUsables.toSeq, Seq[Usable](sword))
      case _ =>
        fail("Expected an equipment action")
    }

    val controller = new GameController(Seq(character, enemy))
    assertEquals(controller.nextTurn(), Some(character))

    val error = intercept[IllegalArgumentException] {
      controller.executeAction(character, "Equip a weapon", staff)
    }

    assert(error.getMessage.contains("not compatible"))
    assertEquals(character.weapon, Some(sword))
    assertEquals(character.inventory.toSeq, Seq[Usable](staff, sword))
    assertEquals(controller.currentUnit, Some(character))
  }

  test("an incompatible initial weapon is rejected") {
    val owner = new WhiteMage("Gandalf", 100.0, 10, 40.0, 100.0)
    val staff = new Staff("Staff", 10, 40, 5.0, owner)

    val error = intercept[IllegalArgumentException] {
      new Knight("Dante", 100.0, 10, 40.0, Some(staff))
    }

    assert(error.getMessage.contains("not compatible"))
  }

  test("a defeated character cannot equip a compatible weapon") {
    val character = new Knight("Dante", 0.0, 10, 40.0)
    val sword = new Sword("Rebellion", 30, 5.0, character)

    val error = intercept[IllegalArgumentException] {
      character.equipWeapon(sword)
    }

    assert(error.getMessage.contains("defeated"))
    assertEquals(character.weapon, None)
  }

  test("insufficient mana reports the required and available amounts") {
    val magician = new BlackMage("V", 100.0, 10, 40.0, 20.0)
    val enemy = new Enemy("Vergil", 100.0, 20, 5, 50.0)
    val staff = new Staff("Staff", 10, 40, 5.0, magician)
    val potion = new MagicForcePotion("Magic force potion")

    magician.equipWeapon(staff)
    magician.addToInventory(potion)
    magician.consumePotion(potion)
    magician.spendManaPoints(1.0)

    val error = intercept[IllegalArgumentException] {
      magician.doAction(new Thunder, enemy)
    }

    assert(error.getMessage.contains("Not enough mana"))
    assert(error.getMessage.contains("20.0"))
    assert(error.getMessage.contains("19.0"))
    assertEquals(magician.manaPoints, 19.0)
    assertEquals(magician.magicDamageMultiplier, 1.5)
    assertEquals(enemy.lifePoints, 100.0)
  }

  test("casting with a common weapon reports the magical weapon requirement") {
    val magician = new BlackMage("V", 100.0, 10, 40.0, 100.0)
    val enemy = new Enemy("Vergil", 100.0, 20, 5, 50.0)
    val dagger = new Dagger("Dagger", 15, 2.0, magician)
    val potion = new MagicForcePotion("Magic force potion")

    magician.equipWeapon(dagger)
    magician.addToInventory(potion)
    magician.consumePotion(potion)

    val error = intercept[IllegalArgumentException] {
      magician.doAction(new Thunder, enemy)
    }

    assert(error.getMessage.contains("magical weapon"))
    assertEquals(magician.manaPoints, 100.0)
    assertEquals(magician.magicDamageMultiplier, 1.5)
    assertEquals(enemy.lifePoints, 100.0)
  }

  test("a defeated caster reports its state without consuming resources") {
    val magician = new BlackMage("V", 100.0, 10, 40.0, 100.0)
    val enemy = new Enemy("Vergil", 100.0, 200, 5, 50.0)
    val staff = new Staff("Staff", 10, 40, 5.0, magician)
    val potion = new MagicForcePotion("Magic force potion")

    magician.equipWeapon(staff)
    magician.addToInventory(potion)
    magician.consumePotion(potion)
    enemy.doAction(new Attack, magician)

    val error = intercept[IllegalArgumentException] {
      magician.castThunder(new Thunder, enemy)
    }

    assert(error.getMessage.contains("defeated"))
    assertEquals(magician.manaPoints, 100.0)
    assertEquals(magician.magicDamageMultiplier, 1.5)
    assertEquals(enemy.lifePoints, 100.0)
  }

  test("equipment options cannot bypass inventory membership") {
    val character = new Knight("Dante", 100.0, 10, 40.0)
    val sword = new Sword("External sword", 30, 5.0, character)
    val action = new EquipWeapon(Array[Usable](sword))

    val error = intercept[IllegalArgumentException] {
      character.doAction(action, sword)
    }

    assert(error.getMessage.contains("inventory"))
    assertEquals(character.weapon, None)
    assert(character.inventory.isEmpty)
  }

  test("unowned equipment preserves the turn and can be equipped after acquisition") {
    val character = new Knight("Dante", 100.0, 10, 40.0)
    val enemy = new Enemy("Vergil", 100.0, 20, 5, 50.0)
    val original = new Sword("Original", 30, 5.0, character)
    val external = new Sword("External", 40, 8.0, character)

    character.addToInventory(original)
    character.equipWeapon(original)

    val controller = new GameController(Seq(character, enemy))
    assertEquals(controller.nextTurn(), Some(character))

    val error = intercept[IllegalArgumentException] {
      controller.executeAction(character, "Equip a weapon", external)
    }

    assert(error.getMessage.contains("inventory"))
    assertEquals(character.weapon, Some(original))
    assertEquals(character.inventory.toSeq, Seq[Usable](original))
    assertEquals(controller.currentUnit, Some(character))

    character.addToInventory(external)
    controller.executeAction(character, "Equip a weapon", external)

    assertEquals(character.weapon, Some(external))
    assertEquals(controller.currentUnit, Some(enemy))
  }

  test("physical attacks against allies preserve health and the current turn") {
    val character = new Knight("Dante", 100.0, 10, 40.0)
    val enemy = new Enemy("Ally", 100.0, 30, 5, 50.0)
    val sword = new Sword("Rebellion", 30, 5.0, character)

    character.equipWeapon(sword)
    val owner = new Player(Seq(character, enemy))

    assertEquals(character.player, Some(owner))
    assertEquals(enemy.player, Some(owner))

    val controller = new GameController(owner.units)
    assertEquals(controller.nextTurn(), Some(character))

    val error = intercept[IllegalArgumentException] {
      controller.executeAction(character, "Attack", enemy)
    }

    assert(error.getMessage.contains("same player"))

    intercept[IllegalArgumentException] {
      enemy.doAction(new Attack, character)
    }

    assertEquals(character.lifePoints, 100.0)
    assertEquals(enemy.lifePoints, 100.0)
    assertEquals(controller.currentUnit, Some(character))
  }

  test("physical attacks are allowed between different players") {
    val attacker = new Knight("Attacker", 100.0, 10, 40.0)
    val target = new Knight("Target", 100.0, 10, 50.0)
    val sword = new Sword("Sword", 30, 5.0, attacker)

    attacker.equipWeapon(sword)

    val firstPlayer = new Player(Seq(attacker))
    val secondPlayer = new Player(Seq(target))
    val controller =
      new GameController(firstPlayer.units ++ secondPlayer.units)

    assertEquals(controller.nextTurn(), Some(attacker))

    controller.executeAction(attacker, "Attack", target)

    assertEquals(target.lifePoints, 80.0)
    assertEquals(controller.currentUnit, Some(target))
  }

  test("units cannot physically attack themselves") {
    val character = new Knight("Dante", 100.0, 10, 40.0)
    val enemy = new Enemy("Vergil", 100.0, 30, 5, 50.0)
    val sword = new Sword("Rebellion", 30, 5.0, character)

    character.equipWeapon(sword)

    val cases: Seq[(Source, GameUnit)] = Seq(
      (character, character),
      (enemy, enemy)
    )

    cases.foreach { case (source, target) =>
      val error = intercept[IllegalArgumentException] {
        source.doAction(new Attack, target)
      }

      assert(error.getMessage.contains("itself"))
      assertEquals(target.lifePoints, 100.0)
    }
  }

  test("assigning units to two players is rejected without partial changes") {
    val first = new Enemy("First", 100.0, 20, 5, 40.0)
    val second = new Enemy("Second", 100.0, 20, 5, 50.0)
    val originalPlayer = new Player(Seq(first))

    intercept[IllegalArgumentException] {
      new Player(Seq(second, first))
    }

    assertEquals(first.player, Some(originalPlayer))
    assertEquals(second.player, None)
  }

























  
  
  
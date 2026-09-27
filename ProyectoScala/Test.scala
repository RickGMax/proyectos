package cl.uchile.dcc.controller

import munit.FunSuite

class EntitiesTest extends FunSuite:

  test("a character stores its attributes"):
    val character = new Character(
      "Peter",
      20.0,
      10,
      75.0,
      Clase.Knight,
      None,
      Array.empty[Usable]
    )
    assertEquals(character.name,"Peter")
    assertEquals(character.lifepoints,20.0)
    assertEquals(character.defense,10)
    assertEquals(character.weight,75.0)
    assertEquals(character.classes,Clase.Knight)
    assertEquals(character.weapon,None)
    assertEquals(character.inventory.length, 0)


  test("a magic character stores mana points"):
    val magician = new Magic_character(
      "Gandalf",
      80.0,
      8,
      60.0,
      Clase.WhiteMagician,
      None,
      150.0,
      Array.empty[Usable]
    )

    assertEquals(magician.name, "Gandalf")
    assertEquals(magician.lifepoints, 80.0)
    assertEquals(magician.defense, 8)
    assertEquals(magician.weight, 60.0)
    assertEquals(magician.classes, Clase.WhiteMagician)
    assertEquals(magician.weapon, None)
    assertEquals(magician.manapoints, 150.0)
    assertEquals(magician.inventory.length, 0)

    val character:Character = magician
    val unit:Unity = magician

    assertEquals(character.name, "Gandalf")
    assertEquals(unit.name, "Gandalf")

  test("a enemy stores its attributes"):
    val enemy = new Enemy(
      "Demon",
      50.0,
      15,
      5,
      40.0

    )
    assertEquals(enemy.name, "Demon")
    assertEquals(enemy.lifepoints, 50.0)
    assertEquals(enemy.attackpoints, 15)
    assertEquals(enemy.defense, 5)
    assertEquals(enemy.weight, 40.0)

    val unit: Unity = enemy
    assertEquals(unit.name, "Demon")

  test("a potion stores its name and type"):
    val potion = new Potion(
      "Healing potion",
      Typepotion.Heal
    )
    assertEquals(potion.name, "Healing potion")
    assertEquals(potion.types, Typepotion.Heal)
    val usable: Usable = potion
    assertEquals(usable.name, "Healing potion")

  test("a weapon stores its attributes and owner"):
    val owner = new Character(
      "Dante",
      100.0,
      20,
      75.0,
      Clase.Knight,
      None,
      Array.empty[Usable]
    )

    val sword = new Weapon(
      "Rebelion",
      30,
      5.0,
      owner,
      Typeweapon.Sword
    )
    assertEquals(sword.name, "Rebelion")
    assertEquals(sword.attackpoints, 30)
    assertEquals(sword.weight, 5.0)
    assertEquals(sword.owner, owner)
    assertEquals(sword.types, Typeweapon.Sword)

    val usable:Usable = sword
    assertEquals(usable.name, "Rebelion")

  test("a magic weapon stores magic attack points"):
    val owner = new Character(
      "V",
      100.0,
      20,
      75.0,
      Clase.BlackMagician,
      None,
      Array.empty[Usable]
    )

    val staff = new Magicweapon(
      "V staff",
      30,
      40,
      Typeweapon.Staff,
      3.0,
      owner
    )

    assertEquals(staff.name, "V staff")
    assertEquals(staff.attackpoints, 30)
    assertEquals(staff.magicattack, 40)
    assertEquals(staff.weight, 3.0)
    assertEquals(staff.owner, owner)
    assertEquals(staff.types, Typeweapon.Staff)

    val usable: Usable = staff
    val weapon: Weapon = staff
    assertEquals(usable.name, "V staff")
    assertEquals(weapon.name, "V staff")

  test("a character weapon slot can contain a weapon"):
    val character = new Character(
      "Dante",
      100.0,
      20,
      75.0,
      Clase.Knight,
      None,
      Array.empty[Usable]
    )

    val sword = new Weapon(
      "Rebelion",
      30,
      5.0,
      character,
      Typeweapon.Sword
    )
    character.weapon = Some(sword)
    assertEquals(character.weapon, Some(sword))

  test("a character inventory can contain usable item"):
    val character = new Character(
      "Dante",
      100.0,
      20,
      75.0,
      Clase.Knight,
      None,
      Array.empty[Usable]
    )

    val potion = new Potion(
      "Healing potion",
      Typepotion.Heal
    )

    val sword = new Weapon(
      "Rebelion",
      30,
      5.0,
      character,
      Typeweapon.Sword
    )

    character.inventory = Array(potion,sword)
    assertEquals(character.inventory.length, 2)
    assertEquals(character.inventory(0), potion)
    assertEquals(character.inventory(1), sword)

  test("a player stores its units"):
    val character = new Character(
      "Dante",
      100.0,
      20,
      75.0,
      Clase.Knight,
      None,
      Array.empty[Usable]
    )

    val enemy = new Enemy(
      "Demon",
      50.0,
      15,
      5,
      40.0

    )

    val player = new Player(
      false,
      Array(character,enemy)
    )
    assertEquals(player.units.length, 2)
    assertEquals(player.units(0), character)
    assertEquals(player.units(1), enemy)
    assertEquals(player.defeated, false)
    player.defeated = true
    assertEquals(player.defeated, true)


  test("a Panel stores coordinates, units and adjacent panels"):
    val character = new Character(
      "Dante",
      100.0,
      20,
      75.0,
      Clase.Knight,
      None,
      Array.empty[Usable]
    )
    val unit: Unity = character
    val panelMid = new Panel(2,3,Array(unit),Array.empty[Panel])
    val panelRight = new Panel(3,3,Array.empty[Unity],Array.empty[Panel])
    val panelLeft = new Panel(1,3,Array.empty[Unity],Array.empty[Panel])

    assertEquals(panelMid.x, 2)
    assertEquals(panelMid.y, 3)
    assertEquals(panelMid.units.length, 1)
    assertEquals(panelMid.units(0), unit)
    assert(panelMid.adjacent.isEmpty)
    panelMid.adjacent = Array(panelLeft,panelRight)
    assertEquals(panelMid.adjacent.length,2)
    assertEquals(panelMid.adjacent(0),panelLeft)
    assertEquals(panelMid.adjacent(1),panelRight)

  test("a Action stores name and attributes"):
    val attack: Attack = new Attack
    val action:Action = attack
    assertEquals(action.name,"Attack")

  test("a Move stores name and attributes"):
    val move: Move = new Move
    val action: Action = move
    assertEquals(action.name, "Move")

  test("Equip weapon stores name and possibleUsables"):
    val character = new Character(
      "Dante",
      100.0,
      20,
      75.0,
      Clase.Knight,
      None,
      Array.empty[Usable]
    )
    val sword = new Weapon(
      "Rebelion",
      30,
      5.0,
      character,
      Typeweapon.Sword
    )
    val equipweap: EquipWeapon = new EquipWeapon(Array[Usable](sword))
    val usabaction: UsableAction = equipweap
    assertEquals(usabaction.name, "Equip a weapon")
    assertEquals(usabaction.possibleUsables(0), sword)
    assertEquals(usabaction.possibleUsables.length, 1)


  test("Consume potion stores name and possibleUsables"):
    val potion = new Potion(
      "Healing potion",
      Typepotion.Heal
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
    val character = new Character(
      "Dante",
      100.0,
      20,
      75.0,
      Clase.Knight,
      None,
      Array.empty[Usable]
    )
    assertEquals(character.totalWeight, 75.0)

    val sword = new Weapon(
      "Rebelion",
      30,
      5.0,
      character,
      Typeweapon.Sword
    )
    character.weapon = Some(sword)
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
































  
  
  
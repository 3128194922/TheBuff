ItemEvents.rightClicked('minecraft:stick', event => {
  const player = event.player

  if (player.isShiftKeyDown()) {
    TheBuff.removeAll(player)
    player.tell('已清除全部特殊状态')
    return
  }

  TheBuff.applyPermanent(player, 'thebuff:burn', 1, player)
  TheBuff.apply(player, 'thebuff:frost', 200, 0, player)
  player.tell('已附着永久灼烧与 10 秒霜冻')

  const list = TheBuff.getAll(player)
  for (let i = 0; i < list.size(); i++) {
    const buff = list.get(i)
    console.info(`${buff.id} Lv.${buff.amplifier + 1} ${buff.permanent ? '永久' : buff.duration}`)
  }
})

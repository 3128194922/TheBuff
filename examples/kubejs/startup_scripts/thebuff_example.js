StartupEvents.registry('thebuff:buff', event => {
  event.create('burn')
    .name('灼烧')
    .color(0xFF6600)
    .category('harmful')
    .maxLevel(5)
    .damage(1.0, 20)
    .damageType('thebuff:buff')
    .onApply(ctx => {
      if (ctx.newlyApplied) {
        ctx.entity.tell('你被灼烧了')
      }
    })
    .onTick(ctx => {
      // 永久状态也会每 tick 调用这里
    })
    .onExpire(ctx => {
      console.info(`灼烧结束：${ctx.reason}`)
    })

  event.create('frost')
    .name('霜冻')
    .color(0x66CCFF)
    .category('harmful')
    .maxLevel(3)
    .attribute('minecraft:generic.movement_speed', -0.15, 'multiply_total')
    .damage(0.5, 40)
    .onTick(ctx => {
      if (ctx.entity.tickCount % 100 === 0) {
        ctx.entity.tell(`霜冻剩余：${ctx.permanent ? '永久' : ctx.remaining}`)
      }
    })
})

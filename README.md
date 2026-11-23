# The Buff

Minecraft 1.20.1 / Forge 47.x 的特殊状态（Buff）系统。

本模组不继承原版 `MobEffect`，也不调用 `LivingEntity#addEffect`，而是通过 Capability 直接附着状态。
因此，**通过重写 `addEffect` 来实现药水免疫的实体仍然会被本模组附着状态**。

## 特性

- 定时状态与永久状态：
  - 定时：`duration > 0`，每 tick 递减，到期自动移除。
  - 永久：`duration = -1`，不进行倒计时、不做过期判断，减少每 tick 运算。
  - 永久状态重复附着时不会被定时附着降级；需要转回定时请调用 `setDuration`。
- 状态等级（amplifier，0 起）。
- 属性修饰符：每级增加/减少指定 attribute。
- 周期伤害：可配置每级伤害、伤害间隔、伤害类型。
- 伤害归属：周期伤害的 `DamageSource` 会带上附着者实体/玩家；附着者离线时死亡消息仍保留其名字。
- 完整增删 API：`apply` / `applyPermanent` / `remove` / `removeAll` / `removeBySource` / `removeCategory` / `setDuration` / `setAmplifier` / `refresh`。
- 玩家 HUD：右上角纯文本显示 `◆ 名称 Lv.N: 时间`，无图标、无粒子。
- Jade：独立“特殊状态”区块，使用 `◆` 前缀和分类颜色，与原版药水效果区分。
- KubeJS：`StartupEvents.registry('thebuff:buff', ...)` + `TheBuff` 全局 API。

## 依赖

- 必需：Minecraft 1.20.1、Forge 47.x。
- 可选：KubeJS 2001.6.5+。
- 可选：Jade 11.x。

## 命令

```text
/thebuff apply <targets> <buff> [duration] [amplifier]
/thebuff applyPermanent <targets> <buff> [amplifier]
/thebuff remove <targets> <buff>
/thebuff clear <targets>
/thebuff list <target>
```

`duration = -1` 表示永久状态。命令需要权限等级 2。
`list` 会在末尾显示附着者名字（例如 `<- PlayerName`）。

## Java API

```java
BuffApi.apply(target, new ResourceLocation("thebuff", "test_burn"), 200, 1, sourceEntity);
BuffApi.applyPermanent(target, new ResourceLocation("thebuff", "test_burn"), 2, sourceEntity);
BuffApi.remove(target, new ResourceLocation("thebuff", "test_burn"));
BuffApi.removeAll(target);
BuffApi.setDuration(target, id, 600);
BuffApi.setAmplifier(target, id, 3);
boolean has = BuffApi.has(target, id);
int level = BuffApi.getAmplifier(target, id);
int remaining = BuffApi.getDuration(target, id);
boolean permanent = BuffApi.isPermanent(target, id);
```

完整 API 见 `com.thebuff.api.BuffApi`。

## KubeJS

启动脚本：

```js
StartupEvents.registry('thebuff:buff', event => {
  event.create('burn')
    .name('灼烧')
    .color(0xFF6600)
    .category('harmful')
    .maxLevel(5)
    .damage(1.0, 20)
    .damageType('thebuff:buff')
    .onTick(ctx => {
      // ctx.entity / ctx.amplifier / ctx.remaining / ctx.source
    })
})
```

服务端脚本：

```js
TheBuff.apply(player, 'thebuff:burn', 200, 1, player)
TheBuff.applyPermanent(player, 'thebuff:burn', 2, player)
TheBuff.remove(player, 'thebuff:burn')

const list = TheBuff.getAll(player)
for (let i = 0; i < list.size(); i++) {
  const b = list.get(i)
  console.info(`${b.id} ${b.amplifier} ${b.duration}`)
}
```

详细说明见 `KUBEJS_API.md`。

## 限制

- 只对 `LivingEntity` 生效。
- 未加载的实体不会 tick。
- 牛奶、`/effect clear`、`removeAllEffects` 不会移除本模组状态；请使用 `/thebuff clear` 或 `BuffApi.removeAll`。
- 伤害最终仍通过 `hurt` 结算，完全无敌/创造模式等仍可能免伤。本模组解决的是“药水附着免疫”，不保证“伤害免疫”。
- 属性修饰符是 transient 的，实体重载、复活、换维度时会重新挂载。
- 永久状态不做倒计时，但仍会执行你配置的 `onTick` 和周期伤害。
- 永久状态不会触发 HUD 的定期同步（没有倒计时需要校正）。

## 构建

```text
gradlew.bat build
```

构建产物在 `build/libs/`。

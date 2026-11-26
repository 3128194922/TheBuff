# The Buff KubeJS API

适用：Minecraft 1.20.1、Forge 47.x、KubeJS 2001.6.5+。

## 1. 注册状态

在 `kubejs/startup_scripts/` 中：

```js
StartupEvents.registry('thebuff:buff', event => {
  event.create('burn')
    .name('灼烧')
    .color(0xFF6600)
    .category('harmful')
    .maxLevel(5)
    .attribute('minecraft:generic.movement_speed', -0.1, 'multiply_total')
    .damage(1.0, 20)
    .damageType('thebuff:buff')
    .ignoreInvulnerability(true)
    .applyImmediately(true)
    .showInHud(true)
    .onApply(ctx => {})
    .onTick(ctx => {})
    .onExpire(ctx => {})
})
```

### Builder 方法

| 方法 | 说明 |
|---|---|
| `name(String)` | 显示名 |
| `color(int)` / `color(String)` | HUD/Jade 颜色，例如 `0xFF6600` 或 `"#FF6600"` |
| `category(String)` | `beneficial` / `harmful` / `neutral` |
| `maxLevel(int)` | 最大等级，默认 256 级 |
| `attribute(id, amountPerLevel)` | 每级增加 attribute，默认 `addition` |
| `attribute(id, amountPerLevel, operation)` | `addition` / `multiply_base` / `multiply_total` |
| `damage(amountPerLevel, intervalTicks)` | 每级伤害与伤害间隔；`intervalTicks` 为 0 表示不造成周期伤害 |
| `damageType(String)` | 伤害类型，默认 `thebuff:buff`；真实伤害可用 `thebuff:buff_true` |
| `ignoreInvulnerability(boolean)` | 是否无视无敌帧，默认 `true` |
| `applyImmediately(boolean)` | 附着时是否立刻造成一次伤害，默认 `true` |
| `showInHud(boolean)` | 是否在右上角显示，默认 `true` |
| `onApply(fn)` | 新附着或重复附着时调用 |
| `onTick(fn)` | 每 tick 调用（永久状态也会调用） |
| `onExpire(fn)` | 到期或被移除时调用 |

### 回调上下文 `ctx`

| 属性/方法 | 说明 |
|---|---|
| `ctx.entity` | 被附着的 `LivingEntity` |
| `ctx.type` | `BuffType` |
| `ctx.instance` | `BuffInstance` |
| `ctx.amplifier` | 等级（0 起） |
| `ctx.remaining` | 剩余 tick；永久状态为 `-1` |
| `ctx.permanent` | 是否永久 |
| `ctx.elapsedTicks` | 已存在 tick 数 |
| `ctx.source` | 附着者实体，可能为 `null` |
| `ctx.sourceName` | 附着者名字，可能为 `null` |
| `ctx.extra` | 额外 NBT `CompoundTag` |
| `ctx.reason` | 移除原因（仅 `onExpire`） |
| `ctx.newlyApplied` | 是否是新附着（仅 `onApply`） |
| `ctx.damage(amount)` | 用当前状态的伤害类型造成一次伤害 |

## 2. 附着与移除

在 `kubejs/server_scripts/` 中：

```js
// 定时
TheBuff.apply(player, 'thebuff:burn', 200, 1, sourceEntity)

// 永久
TheBuff.applyPermanent(player, 'thebuff:burn', 2, sourceEntity)

// 移除
TheBuff.remove(player, 'thebuff:burn')
TheBuff.removeAll(player)
TheBuff.removeBySource(player, sourceEntity)
TheBuff.removeCategory(player, 'harmful')

// 修改
TheBuff.setDuration(player, 'thebuff:burn', 600)
TheBuff.setAmplifier(player, 'thebuff:burn', 3)
TheBuff.refresh(player, 'thebuff:burn', 200)

// 查询
TheBuff.has(player, 'thebuff:burn')
TheBuff.getLevel(player, 'thebuff:burn')
TheBuff.getDuration(player, 'thebuff:burn') // -1 = 永久
TheBuff.isPermanent(player, 'thebuff:burn')
TheBuff.getInstance(player, 'thebuff:burn')

const list = TheBuff.getAll(player)
for (let i = 0; i < list.size(); i++) {
  const b = list.get(i)
  console.info(`${b.id} Lv.${b.amplifier + 1} ${b.permanent ? '永久' : b.duration}`)
}
```

`b` 是 `BuffInstance`，可用：`b.id`、`b.type`、`b.amplifier`、`b.duration`、`b.permanent`、`b.sourceUuid`、`b.sourceName`、`b.extra`。解析后的附着者实体请用 `ctx.source`。

## 3. 伤害归属

`TheBuff.apply(...)` / `applyPermanent(...)` 的最后一个参数是附着者实体。

- 周期伤害的 `DamageSource` 会以该实体作为 `causingEntity`。
- 击杀、死亡消息、`lastHurtByMob` 等会归到附着者。
- 附着者离线时，`BuffDamageSource` 会用保存的名字生成死亡消息。

建议所有会造成伤害的状态都传入附着者；不传时仍可附着，但伤害没有实体归属。

## 4. 移除与到期事件

在 `kubejs/server_scripts/` 中监听。两个事件都只在服务端触发，载荷为只读快照（状态此时已移除）。

```js
// 任何原因被移除：manual（remove / clear 命令 / removeAll / removeCategory）、
// death（死亡清空）、replaced、expired（到期）
TheBuffEvents.removed(event => {
  console.info(`${event.buffId} 从 ${event.entity} 移除，原因 ${event.reasonName}`)
})

// 仅定时状态自然到期；同时也会触发上面的 removed
TheBuffEvents.expired(event => {
  console.info(`${event.buffId} Lv.${event.amplifier + 1} 在 ${event.entity} 上到期`)
})
```

### 事件字段

| 属性/方法 | 说明 |
|---|---|
| `event.entity` | 被附着的 `LivingEntity` |
| `event.player` | 玩家实体，非玩家为 `null` |
| `event.instance` / `event.buff` | 被移除的 `BuffInstance` 快照 |
| `event.type` | `BuffType` |
| `event.id` | `ResourceLocation` |
| `event.buffId` | 状态 id 字符串，例如 `thebuff:burn` |
| `event.amplifier` | 等级（0 起） |
| `event.duration` | 移除时的剩余 tick；永久状态为 `-1` |
| `event.permanent` | 是否永久 |
| `event.elapsedTicks` | 已存在 tick 数 |
| `event.reason` | `BuffRemovalReason` 枚举 |
| `event.reasonName` | 小写原因字符串：`removed` / `cleared` / `expired` / `death` / `replaced` |
| `event.expired` | 是否到期 |
| `event.manual` | 是否手动移除（`removed` 或 `cleared`） |
| `event.source` | 附着者实体，未加载时为 `null` |
| `event.sourceName` | 附着者名字，可能为 `null` |

### 触发来源

| 操作 | `reasonName` | 是否触发 `expired` |
|---|---|---|
| `/thebuff remove`、`TheBuff.remove`、`removeBySource`、`removeCategory` | `removed` | 否 |
| `/thebuff clear`、`TheBuff.removeAll` | `cleared` | 否 |
| 时长归零 | `expired` | 是 |
| 死亡清空（`clearOnDeath`） | `death` | 否 |

## 5. 示例文件

参考 `examples/kubejs/` 下的启动脚本和服务端脚本。

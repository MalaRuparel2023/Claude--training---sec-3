<!--
  TEMPLATE: IoT / connected-device project (mobile app ↔ physical device ↔ cloud).
  Derived from ClaudeTraining/CLAUDE.md, specialized for device integration, real-time sync,
  and offline-first. Copy to <project-root>/CLAUDE.md, replace <PLACEHOLDER>s, delete N/A sections.
  Guidance: docs/claude-md-templates/README.md.  Delete this comment when done.
-->

# CLAUDE.md

Guidance for Claude Code in **<PROJECT_NAME>** — <what devices, what the system does>.

## Project

<STACK.> Talks to **<device type>** over **<BLE | Wi-Fi/mDNS | MQTT | Zigbee/Thread | USB/serial>**,
backed by **<cloud: Firebase/AWS IoT/custom MQTT broker>**.

Modules / layers:
- `:app` — UI, ViewModels, connection orchestration.
- `:domain` — pure Kotlin: device models, `DeviceRepository`/`SyncEngine` interfaces, use cases.
- `<:transport / :data>` — protocol adapters (BLE/MQTT), cloud client, local store.

### Build & test commands

```bash
<compile / assemble / unit-test / instrumented-test / lint / coverage commands>
```
<Note any hardware-in-the-loop vs simulator test modes and required permissions/entitlements.>

## Architecture

`:app` → `:domain` ← `<:transport>`; `:domain` is protocol-agnostic (no BLE/MQTT types leak in).
The **device is modeled as a repository/stream behind a domain interface** so transport can be
swapped (real radio ↔ simulator) and unit-tested.

## Device integration

- **Discovery & pairing**: <scan → pair → bond flow; where identity/keys are stored; re-pair path.>
- **Connection lifecycle**: <who owns the connection (a foreground/bound Service? a scoped manager?);
  connect on <event>, disconnect on <event>; state machine: Disconnected→Connecting→Connected→…>.
  State-guard connects (no-op if already connecting/connected).
- **Protocol adapter**: <GATT services/characteristics | MQTT topics | message schema>; encode/decode
  lives in `<:transport>`, mapped to domain models at the boundary. Treat every device payload as
  **untrusted & nullable** — validate before use.
- **Permissions/OS**: <BLUETOOTH_CONNECT/SCAN, location, foreground-service type, background limits,
  Doze/battery-optimization caveats, iOS background modes>.
- **Reliability**: retry/backoff on connect; command timeouts; idempotent commands; firmware/version
  negotiation. **Never block the main thread on IO** — all transport is suspend/async.

## Real-time sync

- **Inbound**: device/cloud → app as a `Flow`/subscription; <debounce/conflate high-frequency
  telemetry; backpressure strategy>. Mirror connection events into an observable `connectionState`
  that drives a UI "disconnected" banner.
- **Outbound**: commands → device/cloud; confirm via ack/echo; surface pending/failed state.
- **Conflict resolution**: <last-write-wins | version vectors | server-authoritative>; define the
  tiebreaker and the clock source (device clocks drift — prefer server or monotonic timestamps).
- **Freshness**: <how the UI shows stale vs live data; heartbeat/keepalive interval>.

## Offline-first patterns

- **Single source of truth**: the **local store** (<Room/DataStore>). UI reads local; sync writes local.
- **Sync engine**: <where reconciliation lives>; remote→local on connect, local→remote flush of a
  **durable outbound queue** (survives process death). Reads never block on the network.
- **Queueing & replay**: commands issued offline are persisted and replayed on reconnect, in order,
  idempotently (dedupe by client-generated id).
- **Reconciliation**: <merge rule when local & remote diverge; how deletes/tombstones are handled>.
- **Test the offline path explicitly**: airplane-mode → act → reconnect → assert convergence.

## Key modules

| Path | Role |
|------|------|
| `<domain/.../DeviceRepository.kt>` | device contract (framework-free) |
| `<transport/.../BleDeviceSource.kt>` | protocol adapter |
| `<transport/.../<Fake>DeviceSource.kt>` | simulator/fake for tests & demos |
| `<domain/.../SyncEngine.kt>` + impl | reconciliation + outbound queue |
| `<app/.../ui/viewmodel/DeviceViewModel.kt>` | connection + device state |

## Testing approach

- Unit-test domain + sync logic against a **fake device source** and an in-memory store — no radio.
- Contract-test the protocol adapter against recorded/simulated device payloads (incl. malformed).
- Instrumented/HIL tests for real-radio paths where feasible; mark them separately (they're flaky/slow).
- Coverage gate ≥ <N>%; transport SDK wrappers excluded with justification, sync/domain logic is not.
- Cover: connect/disconnect races, timeout, offline-queue replay, conflict resolution, reconnect.

## Conventions & gotchas

<Battery/foreground-service rules, teardown to avoid leaked GATT/subscriptions, thread confinement of
the radio, firmware-version branches, permission-denied UX, emulator-can't-do-BLE notes.>

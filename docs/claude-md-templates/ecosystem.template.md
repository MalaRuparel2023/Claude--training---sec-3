<!--
  TEMPLATE: Multi-app ecosystem (several apps/services sharing backends, contracts, design system).
  This is the ROOT CLAUDE.md for the workspace/monorepo. Each app ALSO has its own CLAUDE.md
  (use android-app.template.md / iot-project.template.md for those). This root explains the SYSTEM
  and cross-cutting rules; per-app files explain each app.
  Copy to <workspace-root>/CLAUDE.md, replace <PLACEHOLDER>s, delete N/A sections.
  Guidance: docs/claude-md-templates/README.md.  Delete this comment when done.
-->

# CLAUDE.md

Root guidance for the **<ECOSYSTEM_NAME>** workspace — <what the system does end-to-end>.
Each sub-project has its own `CLAUDE.md`; this file covers the system and shared rules.

## System overview

<One paragraph: the apps, who uses each, how they fit together.>

| App / service | Path | Platform | Purpose | Owner |
|---------------|------|----------|---------|-------|
| <Consumer app> | `<apps/consumer>` | Android | | <team> |
| <Admin/recruiter app> | `<apps/admin>` | <Web/Next.js> | | <team> |
| <Device firmware/gateway> | `<devices/…>` | <embedded> | | <team> |
| <Backend/API> | `<services/api>` | <Node/Kotlin> | | <team> |

## Workspace layout

<Monorepo vs polyrepo. If monorepo: package manager/build tool (Gradle composite, Nx, Turborepo,
Bazel), where shared modules live, how apps reference them. If polyrepo: how repos relate, versioning.>

```
<tree of top-level dirs with one-line purpose each>
```

## Shared contracts (the glue — change these carefully)

- **API / data schema**: <OpenAPI/proto/GraphQL location>; **single source of truth**; codegen story.
  A change here fans out to every consumer — enumerate them and update together.
- **Design system**: <shared UI kit/tokens location>; who owns it; how apps consume it.
- **Auth & identity**: <shared auth provider, token format, user id semantics across apps>.
- **Shared domain modules**: <cross-app Kotlin/TS libs; their stability guarantees>.
- **Events / messaging**: <event bus/topics/queue contracts between services>.

> **Rule for Claude**: before editing a shared contract, list its consumers (grep/graph) and change
> them in the same PR, or the build breaks downstream. Prefer additive, backward-compatible changes.

## Per-app CLAUDE.md relationship

- This root file: system map, shared contracts, cross-cutting conventions, release coordination.
- Each app's `CLAUDE.md`: that app's stack, build/test, architecture, gotchas.
- **Don't duplicate**: system-wide facts live here and are linked from apps, not copied.

## Cross-cutting

- **Build/test all**: `<command to build & test the whole workspace>`; per-app commands live in each app.
- **CI/CD**: <mono-pipeline with affected-only builds? per-app pipelines? how releases are coordinated;
  which changes require a coordinated multi-app release>.
- **Versioning & compatibility**: <how app↔API↔device versions are kept compatible; deprecation policy;
  feature flags for staged rollout across apps>.
- **Env & secrets**: shared config strategy; **never** hardcode; per-env matrices.
- **Observability**: shared analytics event taxonomy, crash/perf, correlation ids across apps.

## Integration points & ownership

| Boundary | Producer | Consumer(s) | Contract | On change… |
|----------|----------|-------------|----------|-----------|
| <API vX> | <api> | <consumer, admin> | <schema ref> | <coordinate release> |
| <Realtime channel> | <service> | <apps> | <topic/schema> | |
| <Device protocol> | <firmware> | <gateway app> | <spec ref> | |

## Conventions & gotchas

<Cross-repo traps: contract drift, version skew, "update codegen after schema change", shared-module
publishing flow, monorepo task-graph quirks, which changes need sign-off from another team.>

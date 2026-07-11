import test from "node:test";
import assert from "node:assert/strict";
import { seedForDate, gameSet } from "./index.js";

test("daily seeds are deterministic", () => assert.equal(seedForDate("2026-07-11"), seedForDate("2026-07-11")));
test("daily run selects three unique games", () => { const games = gameSet(42); assert.equal(games.length, 3); assert.equal(new Set(games).size, 3); });

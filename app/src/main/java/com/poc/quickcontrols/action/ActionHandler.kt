package com.poc.quickcontrols.action

import android.content.Context

/**
 * Contract every feature (torch, DND, VPN, volume, dark mode, ...) implements.
 *
 * Each handler declares which QuickActions it owns and how to execute them.
 * Keep the implementation self-contained so adding a new feature is just:
 *   1. Add enum entries to [QuickAction].
 *   2. Implement an ActionHandler in its own package (e.g. dnd/DndActionHandler.kt).
 *   3. Register the handler in [ActionDispatcher.handlers].
 */
interface ActionHandler {
    /** QuickActions this handler is responsible for. */
    val supported: Set<QuickAction>

    /** Perform [action]. Return a short user-facing confirmation string. */
    fun handle(context: Context, action: QuickAction): Result

    data class Result(val success: Boolean, val message: String)
}

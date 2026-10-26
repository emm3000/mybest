package com.emm.mybest.domain.repository

/**
 * Relaunches MyBest in a fresh process, so every screen reopens the database after a backup restore.
 */
interface AppRestarter {
    fun restart()
}

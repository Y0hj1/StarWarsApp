package ru.urfu.starwars.presentation.common

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Общий для экранов списка и фильтров кэш: показывать ли бейдж на кнопке фильтра.
 * Хранится только в памяти, создаётся Hilt-ом как синглтон.
 */
@Singleton
class FilterBadgeCache @Inject constructor() {

    private val _showBadge = MutableStateFlow(false)
    val showBadge: StateFlow<Boolean> = _showBadge.asStateFlow()

    fun setFiltersApplied(applied: Boolean) {
        _showBadge.value = applied
    }
}

package com.grupox.wololo.model.helpers

import com.grupox.wololo.model.User
import com.grupox.wololo.model.RepoUsers

fun getFilters(filterString: String): Boolean = filterString === "filter"

fun getOrders(orderString: String): Boolean = orderString === "orderBy"

// TODO make it generic for model and repos type
fun queryParamsSearch(
        queryParams: Map<String, String>
): List<User> = RepoUsers.find(
        queryParams.filter { getFilters(it.key) }
                .map{it.value}
        ,
        queryParams.filter { getOrders(it.key) }
                .map{it.value}
)
package com.github.imeszaros.vb2buxfer

class TagMapper(private val mapping: Map<String, String>) {

    fun map(description: String) = mapping.keys
            .find { description.contains(it, true) }
            ?.let(mapping::get)
            ?.split(",")
            ?.map { it.trim() }
            ?.toList() ?: emptyList()
}
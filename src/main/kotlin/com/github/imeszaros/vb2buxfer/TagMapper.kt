package com.github.imeszaros.vb2buxfer

class TagMapper(private val mapping: MutableMap<String, String>) {

    fun map(description: String) = mapping.keys
            .find { description.contains(it, true) }
            ?.let(mapping::get)
            ?.split(",")
            ?.map { it.trim() }
            ?.toMutableList() ?: mutableListOf()

    fun update(description: String, tags: String) {
        mapping[description] = tags
    }
}
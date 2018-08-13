package com.github.imeszaros.vb2buxfer

import com.github.imeszaros.vb2buxfer.gui.GUI

fun main(args: Array<String>) {
    GUI.init(Configuration()).join()
}
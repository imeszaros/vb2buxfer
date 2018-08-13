package com.github.imeszaros.vb2buxfer.gui

import org.eclipse.swt.SWT
import org.eclipse.swt.layout.GridData
import org.eclipse.swt.widgets.*

fun Shell.center(monitor: Monitor = this.display.primaryMonitor) = setLocation(
        monitor.bounds.width / 2 - size.x / 2,
        monitor.bounds.height / 2 - size.y / 2)

fun Control.gridData(
        minimumWidth: Int? = null,
        minimumHeight: Int? = null,
        widthHint: Int? = null,
        heightHint: Int? = null,
        horizontalAlignment: Int? = null,
        verticalAlignment: Int? = null,
        grabExcessHorizontalSpace: Boolean? = null,
        grabExcessVerticalSpace: Boolean? = null,
        horizontalSpan: Int? = null,
        verticalSpan: Int? = null,
        horizontalIndent: Int? = null,
        verticalIndent: Int? = null,
        exclude: Boolean? = null): GridData {

    if (layoutData !is GridData) {
        layoutData = GridData()
    }

    return with (layoutData as GridData) {
        minimumWidth?.let { this.minimumWidth = it }
        minimumHeight?.let { this.minimumHeight = it }
        widthHint?.let { this.widthHint = it }
        heightHint?.let { this.heightHint = it }
        horizontalAlignment?.let { this.horizontalAlignment = it }
        verticalAlignment?.let { this.verticalAlignment = it }
        grabExcessHorizontalSpace?.let { this.grabExcessHorizontalSpace = it }
        grabExcessVerticalSpace?.let { this.grabExcessVerticalSpace = it }
        horizontalSpan?.let { this.horizontalSpan = it }
        verticalSpan?.let { this.verticalSpan = it }
        horizontalIndent?.let { this.horizontalIndent = it }
        verticalIndent?.let { this.verticalIndent = it }
        exclude?.let { this.exclude = it }

        this
    }
}

fun Shell.showError(msg: String) = MessageBox(this, SWT.ICON_ERROR or SWT.OK).apply {
    text = "Error"
    message = msg
    open()
}

fun Shell.showError(t: Throwable) = showError(t.message ?: t.javaClass.simpleName)
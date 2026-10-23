package com.emm.mybest.data.capture

internal fun bodyOf(bigText: CharSequence?, text: CharSequence?): String? = (bigText ?: text)?.toString()

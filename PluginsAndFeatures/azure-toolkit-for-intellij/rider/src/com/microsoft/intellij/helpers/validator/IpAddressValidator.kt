package com.microsoft.intellij.helpers.validator

import com.intellij.openapi.ui.InputValidator

class IpAddressInputValidator : InputValidator {
    companion object {
        val instance = IpAddressInputValidator()
    }

    override fun checkInput(input: String?): Boolean {
        return !input.isNullOrEmpty() && validateIpV4Address(input)
    }

    override fun canClose(input: String?): Boolean {
        return checkInput(input)
    }

    fun validateIpV4Address(address: String): Boolean {
        val regex = Regex("^(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})$")
        val match = regex.matchEntire(address) ?: return false

        return match.groupValues.drop(1).all { value ->
            val intValue = value.toIntOrNull()
                    ?: return@all false

            if (value.length > 1 && value.startsWith('0'))
                return@all false

            intValue in 0..255
        }
    }
}
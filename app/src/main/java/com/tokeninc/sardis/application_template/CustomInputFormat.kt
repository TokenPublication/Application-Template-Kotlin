package com.tokeninc.sardis.application_template

import com.token.uicomponents.CustomInput.EditTextInputType
import com.token.uicomponents.CustomInput.InputValidator
import org.jetbrains.annotations.Nullable

/**
 *
 * @param hint: Title to be shown above edittext.
 * @param type: Input format for edittext.
 * @param maxLength: Maximum length for input string. If null, maximum length will not be set.
 * @param invalidMessage: Warning message to be shown under edit text in case input is not valid.
 * @param validator: An object that is a type of InputValidator. Required if related input must be validated.
 *                 If validator is null, input will always be valid no matter if it's empty or not.
 */
public class CustomInputFormat(
    private val hint: String,
    private val type: EditTextInputType,
    private val maxLength: Integer?,
    private val invalidMessage: String?,
    private val validator: InputValidator?
){
    //önce fragment oluşturup input alacaksın sonra burayı yaparsın
    fun setText(writingData: String){

    }
}
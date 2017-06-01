package com.kotlinandroidbot.ui.layouts

import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import com.jakewharton.rxbinding2.widget.indeterminate
import com.kotlinandroidbot.R
import com.kotlinandroidbot.ui.activities.LoginActivity
import org.jetbrains.anko.*
import org.jetbrains.anko.design.textInputLayout
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.sdk25.coroutines.textChangedListener

/**
 * Created by serhii_slobodyanuk on 5/30/17.
 */

class LoginActivityUI : AnkoComponent<LoginActivity> {

    lateinit var username: EditText
    lateinit var password: EditText
    lateinit var login: Button

    override fun createView(ui: AnkoContext<LoginActivity>) = with(ui) {

        linearLayout {

            padding = dip(30)
            gravity = Gravity.CENTER
            orientation = LinearLayout.VERTICAL

            relativeLayout {
                id = R.id.progress
                progressBar().indeterminate()
                visibility = View.GONE
            }.lparams(width = matchParent, height = matchParent)

            textInputLayout {
                username = editText {
                    id = R.id.edit_login
                    hint = ui.ctx.getString(R.string.login)
                }
            }

            textInputLayout {
                password = editText {
                    id = R.id.edit_password
                    hint = ui.ctx.getString(R.string.password)
                }
            }

            login = button(R.string.login) {
                id = R.id.button_login

                handleLogin()

                onClick {
                    ui.owner.onLoginClick(username.text.toString(), password.text.toString())
                }
            }.lparams {
                topMargin = dip(30)
            }
        }
    }

    fun handleLogin() {
        val editTexts = listOf(username, password)
        editTexts.forEach {
            it.textChangedListener {
                onTextChanged { _, _, _, _ -> login.isClickable = isValidate() }
                beforeTextChanged { _, _, _, _ -> login.isClickable = isValidate() }
                afterTextChanged { _ -> login.isClickable = isValidate() }
            }
        }
    }

    fun isValidate(): Boolean {
        return when {
            username.editableText.isNotBlank() and password.editableText.isNotBlank() -> true
            else -> false
        }
    }

}
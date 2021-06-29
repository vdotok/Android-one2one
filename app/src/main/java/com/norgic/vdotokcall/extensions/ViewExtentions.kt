package com.norgic.vdotokcall.extensions
import android.app.Activity
import android.content.Context
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Patterns
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.databinding.BindingAdapter
import com.google.android.material.snackbar.Snackbar
import com.norgic.vdotokcall.R


fun Activity.hideKeyboard() {
    val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    //Find the currently focused view, so we can grab the correct window token from it.
    var view: View? = this.currentFocus

    if (view == null) {
        view = View(this)
    }
    imm.hideSoftInputFromWindow(view.windowToken, 0)

//    (getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(view.windowToken, 0)
}


fun View.hide() {
    this.visibility = View.GONE
}

fun View.show() {
    this.visibility = View.VISIBLE
}

fun View.invisible() {
    this.visibility = View.INVISIBLE
}

fun View.toggleVisibility() {
    if (this.visibility == View.VISIBLE) this.hide()
    else this.show()
}

fun View.showSnackBar(message: String?) {
    message?.let { Snackbar.make(this, it, Snackbar.LENGTH_LONG).show() }
}

fun View.showSnackBar(stringId: Int) {
    Snackbar.make(this, stringId, Snackbar.LENGTH_LONG).show()
}

fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }
    })
}


@BindingAdapter(value = ["email", "showErrorMsg"])
fun View.checkedEmail(email: String, showErrorMsg: Boolean = false): Boolean {
    return if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
        if(showErrorMsg) this.showSnackBar(this.context.getString(R.string.invalid_email))
        false
    } else {
        true
    }
}

@BindingAdapter(value = ["username", "showErrorMsg"])
fun View.checkedUserName(username: String, showErrorMsg: Boolean = false): Boolean {
    return if (username.containsNonAlphaNumericName()|| username.length < 4 || username.length > 20 || username.isEmpty() || TextUtils.isDigitsOnly(username)) {
        if(showErrorMsg) this.showSnackBar(this.context.getString(R.string.invalid_username))
        false
    } else {
        true
    }
}

@BindingAdapter("password")
fun View.checkedPassword(password: String): Boolean {
    return if (password.containsNonAlphaNumeric() || password.length < 8 || password.isEmpty()) {
        this.showSnackBar(this.context.getString(R.string.invalid_password))
        false
    } else {
        true
    }
}


@BindingAdapter("inputText")
fun View.checkInputTextIsEmailType(inputText: String): Boolean {
    return inputText.contains("@") && inputText.contains(".com")
}
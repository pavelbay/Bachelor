package com.pavel.augmented.util

import android.app.Activity
import android.content.pm.PackageManager
import android.support.annotation.IdRes
import android.support.annotation.StringRes
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.support.v4.content.ContextCompat
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.widget.Toast


fun AppCompatActivity.replaceFragmentInActivity(fragment: Fragment, @IdRes frameId: Int) {
    supportFragmentManager.transact {
        replace(frameId, fragment)
    }
}

/**
 * The `fragment` is added to the container view with tag. The operation is
 * performed by the `fragmentManager`.
 */
fun AppCompatActivity.addFragmentToActivity(fragment: Fragment, tag: String) {
    supportFragmentManager.transact {
        add(fragment, tag)
    }
}

fun AppCompatActivity.setupActionBar(@IdRes toolbarId: Int, action: ActionBar.() -> Unit) {
    setSupportActionBar(findViewById(toolbarId))
    supportActionBar?.run {
        action()
    }
}

fun Activity.askForPermissions(permissions: Array<out String>, requestCode: Int): Boolean {
    val permissionsToBeRequested = ArrayList<String>()
    permissions
            .filter { ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED &&
                        !shouldShowRequestPermissionRationale(it)}
            .forEach { permissionsToBeRequested.add(it)}
    return if (permissionsToBeRequested.isNotEmpty()) {
        requestPermissions(permissionsToBeRequested.toTypedArray(), requestCode)
        true
    } else false
}

fun FragmentActivity.showToast(text: String) {
    runOnUiThread { Toast.makeText(this, text, Toast.LENGTH_SHORT).show() }
}

fun FragmentActivity.showToast(@StringRes res: Int) {
    runOnUiThread { Toast.makeText(this, getString(res), Toast.LENGTH_SHORT).show() }
}

/**
 * Runs a FragmentTransaction, then calls commit().
 */
private inline fun FragmentManager.transact(action: FragmentTransaction.() -> Unit) {
    beginTransaction().apply {
        action()
    }.commit()
}
package com.arduinocontroller

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager

class NavigationController(val fragmentManager: FragmentManager) {
    val fragmentList: ArrayList<String> = arrayListOf()

    fun setInitial(fragment: Fragment) {
        fragmentList.clear()
        fragmentList.add(fragment.javaClass.name)
        fragmentManager.beginTransaction()
                .add(R.id.container, fragment, fragment.javaClass.name)
                .disallowAddToBackStack()
                .commitNow()
    }

    fun goNext(fragment: Fragment) {
        val transaction = fragmentManager.beginTransaction().disallowAddToBackStack()
        transaction.add(R.id.container, fragment, fragment.javaClass.name)
        fragmentList.forEach {
            transaction.detach(fragmentManager.findFragmentByTag(it)!!)
        }
        fragmentList.add(fragment.javaClass.name)
        transaction.commitNow()
    }

    fun goBack(): Boolean {
        if (fragmentList.size > 1) {
            fragmentManager.beginTransaction()
                    .remove(fragmentManager.findFragmentByTag(fragmentList.last())!!)
                    .attach(fragmentManager.findFragmentByTag(fragmentList.get(fragmentList.lastIndex - 1))!!)
                    .disallowAddToBackStack()
                    .commitNow()
            fragmentList.remove(fragmentList.last())
            return true
        } else {
            return false
        }
    }
}
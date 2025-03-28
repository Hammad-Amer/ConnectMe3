package com.example.connectme

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.auth.FirebaseAuth
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent

@RunWith(AndroidJUnit4::class)
class LoginTest {

    @get:Rule
    var activityRule = ActivityScenarioRule(LoginPage::class.java)

    @Before
    fun setUp() {
        Intents.init()
        FirebaseAuth.getInstance().signOut()
    }


    @Test
    fun testLoginWithValidCredentials() {

        onView(withId(R.id.LoginButton_login)).check(matches(isDisplayed()))

        onView(withId(R.id.Username_login))
            .perform(typeText("affan@email.com"), closeSoftKeyboard())
        onView(withId(R.id.Password_login))
            .perform(typeText("123456789"), closeSoftKeyboard())

        onView(withId(R.id.LoginButton_login)).perform(click())
        Thread.sleep(3000)
        intended(hasComponent(MainFeedScreen::class.java.name))
    }
    @After
    fun tearDown() {
        Intents.release()
    }

}
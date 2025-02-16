package com.example.connectme

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavigationTest {

    @Before
    fun setUp() {
        Intents.init()
        ActivityScenario.launch(LoginPage::class.java)
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    @Test
    fun testLoginButtonNavigatesToMainFeedScreen() {

        onView(withId(R.id.LoginButton_login)).perform(click())


        Intents.intended(hasComponent(MainFeedScreen::class.java.name))
    }

    @Test
    fun testRegisterTextNavigatesToRegisterPage() {

        onView(withId(R.id.Register_login)).perform(click())

        Intents.intended(hasComponent(RegisterPage::class.java.name))
    }
}
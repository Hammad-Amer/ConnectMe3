package com.example.connectme

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
class MainFeedButtonTest {

    @Before
    fun setUp() {
        Intents.init()
        ActivityScenario.launch(MainFeedScreen::class.java)
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    @Test
    fun testDMButtonNavigatesToDMs() {

        onView(withId(R.id.connectme_dm)).perform(click())


        Intents.intended(hasComponent(DMs::class.java.name))
    }
}

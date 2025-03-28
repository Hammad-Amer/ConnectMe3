package com.example.connectme

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.auth.FirebaseAuth
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.espresso.intent.Intents
import org.junit.After
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class MessageTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(DMs::class.java)
    private val testEmail = "skc@email.com"
    private val testPassword = "skc123"
    @Before
    fun setUp() {

        Intents.init()

        FirebaseAuth.getInstance().signOut()

        val authLatch = CountDownLatch(1)
        FirebaseAuth.getInstance().signInWithEmailAndPassword(testEmail, testPassword)
            .addOnCompleteListener { authLatch.countDown() }
        authLatch.await(5, TimeUnit.SECONDS)

        Thread.sleep(3000)
    }

    @Test
    fun testSendMessage() {
        onView(withId(R.id.recyclerView_Dms)).check(matches(isDisplayed()))

        onView(withId(R.id.recyclerView_Dms))
            .perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))

        onView(withId(R.id.etMessage)).check(matches(isDisplayed()))

        onView(withId(R.id.etMessage))
            .perform(typeText("Hello, how are you?"), closeSoftKeyboard())

        onView(withId(R.id.btnSend_vanish)).perform(click())

    }
    @After
    fun tearDown() {
        FirebaseAuth.getInstance().signOut()
        Intents.release()

    }
}


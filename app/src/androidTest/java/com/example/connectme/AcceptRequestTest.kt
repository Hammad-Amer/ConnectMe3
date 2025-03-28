package com.example.connectme

import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import org.hamcrest.Matchers.allOf
import androidx.test.espresso.matcher.ViewMatchers.hasSibling
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class AcceptRequestTest {

    private val testRequesterId = "test_requester_123"
    private val testCurrentUserId = "7VJfGfN5ijNBE3i7747KDtkcdrG3"
    private val testEmail = "affan@email.com"
    private val testPassword = "123456789"

    @Before
    fun setup() {
        val latch = CountDownLatch(1)
        FirebaseAuth.getInstance().signOut()

        FirebaseDatabase.getInstance().reference.apply {
            child("Users").child(testRequesterId).child("username").setValue("Test Requester")
            child("Users").child(testRequesterId).child("profileImage").setValue("test_image_url")
        }

        FirebaseDatabase.getInstance().reference.child("Requests")
            .child(testCurrentUserId)
            .child("pending")
            .child(testRequesterId)
            .setValue(mapOf(
                "username" to "Test Requester",
                "profileImageUrl" to "test_image_url"
            )).addOnCompleteListener { latch.countDown() }

        latch.await(5, TimeUnit.SECONDS)

        val authLatch = CountDownLatch(1)
        FirebaseAuth.getInstance().signInWithEmailAndPassword(testEmail, testPassword)
            .addOnCompleteListener { authLatch.countDown() }
        authLatch.await(5, TimeUnit.SECONDS)
    }

    @Test
    fun testAcceptFollowRequest() {
        ActivityScenario.launch(ContactsRequests::class.java)
        Thread.sleep(10000)
        onView(withId(R.id.requestsRecyclerView))
            .perform(RecyclerViewActions.scrollToPosition<RecyclerView.ViewHolder>(0))

        onView(allOf(
            withId(R.id.acceptButton),
            hasSibling(withText("Test Requester"))
        )).perform(click())

        val verificationLatch = CountDownLatch(1)
        FirebaseDatabase.getInstance().reference
            .child("Users").child(testCurrentUserId).child("followers").child(testRequesterId)
            .get().addOnSuccessListener {
                assert(it.exists())
                verificationLatch.countDown()
            }
        verificationLatch.await(5, TimeUnit.SECONDS)
    }

    @After
    fun cleanup() {
        FirebaseDatabase.getInstance().reference.apply {
            child("Users").child(testRequesterId).removeValue()
            child("Users").child(testCurrentUserId).removeValue()
            child("Requests").child(testCurrentUserId).removeValue()
        }
        FirebaseAuth.getInstance().signOut()
    }
}
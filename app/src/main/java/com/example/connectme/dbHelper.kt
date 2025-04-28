package com.example.connectme

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class dbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "ConnectMeDMs.db"
        private const val DATABASE_VERSION = 1
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
       CREATE TABLE cached_dms (
           user_id TEXT PRIMARY KEY,
           username TEXT,
           pfp      TEXT
       );
        """)


        db.execSQL("""
            CREATE TABLE cached_stories (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER,
                profileImage TEXT,
                media_type TEXT,
                media_data TEXT,
                timestamp INTEGER
            );
        """)
        db.execSQL("""
    CREATE TABLE cached_posts (
        post_id TEXT PRIMARY KEY,
        user_id TEXT,
        username TEXT,
        image TEXT,
        post TEXT,
        caption TEXT,
        timestamp INTEGER,
        like_count INTEGER,
        is_liked INTEGER
);
""")

        db.execSQL("""
        CREATE TABLE cached_messages (
            id TEXT PRIMARY KEY,
            sender_id TEXT,
            receiver_id TEXT,
            message TEXT,
            timestamp INTEGER
        );
    """)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS cached_dms")
        db.execSQL("DROP TABLE IF EXISTS cached_messages")
        db.execSQL("DROP TABLE IF EXISTS cached_stories")
        db.execSQL("DROP TABLE IF EXISTS cached_posts")
        onCreate(db)
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        Log.w("DBHelper", "Downgrading database from $oldVersion to $newVersion. Data will be lost.")
        db.execSQL("DROP TABLE IF EXISTS cached_dms")
        db.execSQL("DROP TABLE IF EXISTS cached_messages")
        onCreate(db)
    }

    fun cacheStory(userId: Int, profileImage: String, mediaType: String, mediaData: String, timestamp: Long) {
        try {
            val db = writableDatabase
            val values = ContentValues().apply {
                put("user_id", userId)           // Store userId as integer
                put("profileImage", profileImage) // Store profile image (String)
                put("media_type", mediaType)     // Store media type (String)
                put("media_data", mediaData)     // Store media data (String)
                put("timestamp", timestamp)      // Store timestamp (Long)
            }

            // Insert or replace the cached story into the database
            val result = db.insertWithOnConflict("cached_stories", null, values, SQLiteDatabase.CONFLICT_REPLACE)

            // Log error if the insert fails
            if (result == -1L) {
                Log.e("DBHelper", "Failed to insert story into cache.")
            }
        } catch (e: Exception) {
            Log.e("DBHelper", "Error caching story", e)
        }
    }

    fun getCachedStories(): List<ModelStory> {
        val storyList = mutableListOf<ModelStory>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM cached_stories", null)

        if (cursor.moveToFirst()) {
            do {
                val userId = cursor.getInt(cursor.getColumnIndexOrThrow("user_id"))
                val profileImage = cursor.getString(cursor.getColumnIndexOrThrow("profileImage"))
                val mediaType = cursor.getString(cursor.getColumnIndexOrThrow("media_type"))
                val mediaData = cursor.getString(cursor.getColumnIndexOrThrow("media_data"))
                val timestamp = cursor.getLong(cursor.getColumnIndexOrThrow("timestamp"))

                storyList.add(ModelStory(
                    type = AdapterStory.OTHER_STORY,
                    userId = userId,
                    profileImage = profileImage,
                    mediaData = mediaData,
                    mediaType = mediaType,
                    timestamp = timestamp,
                    hasActiveStory = true
                ))
            } while (cursor.moveToNext())
        }

        cursor.close()
        return storyList
    }

    fun getCachedStoryByUser(userId: Int): ModelStory? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM cached_stories WHERE user_id = ?", arrayOf(userId.toString()))

        var story: ModelStory? = null

        if (cursor.moveToFirst()) {
            val profileImage = cursor.getString(cursor.getColumnIndexOrThrow("profileImage"))
            val mediaType = cursor.getString(cursor.getColumnIndexOrThrow("media_type"))
            val mediaData = cursor.getString(cursor.getColumnIndexOrThrow("media_data"))
            val timestamp = cursor.getLong(cursor.getColumnIndexOrThrow("timestamp"))

            story = ModelStory(
                type = AdapterStory.OTHER_STORY,
                userId = userId,
                profileImage = profileImage,
                mediaData = mediaData,
                mediaType = mediaType,
                timestamp = timestamp,
                hasActiveStory = true
            )
        }

        cursor.close()
        return story
    }

    // Clear cached stories
    fun clearCachedStories() {
        writableDatabase.delete("cached_stories", null, null)
    }


    fun cacheFeedPosts(posts: List<ModelFeedPosts>) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            for (post in posts) {
                val values = ContentValues().apply {
                    put("post_id", post.postId)
                    put("user_id", post.userId)
                    put("username", post.username)
                    put("image", post.image)
                    put("post", post.post)
                    put("caption", post.caption)
                    put("timestamp", post.timestamp)
                    put("like_count", post.likeCount)
                    put("is_liked", if (post.isLiked) 1 else 0)
                }
                db.insertWithOnConflict("cached_posts", null, values, SQLiteDatabase.CONFLICT_REPLACE)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun getCachedFeedPosts(): List<ModelFeedPosts> {
        val posts = mutableListOf<ModelFeedPosts>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM cached_posts", null)

        if (cursor.moveToFirst()) {
            do {
                posts.add(ModelFeedPosts(
                    postId = cursor.getString(cursor.getColumnIndexOrThrow("post_id")),
                    userId = cursor.getString(cursor.getColumnIndexOrThrow("user_id")),
                    username = cursor.getString(cursor.getColumnIndexOrThrow("username")),
                    image = cursor.getString(cursor.getColumnIndexOrThrow("image")),
                    post = cursor.getString(cursor.getColumnIndexOrThrow("post")),
                    caption = cursor.getString(cursor.getColumnIndexOrThrow("caption")),
                    timestamp = cursor.getLong(cursor.getColumnIndexOrThrow("timestamp")),
                    likeCount = cursor.getInt(cursor.getColumnIndexOrThrow("like_count")),
                    isLiked = cursor.getInt(cursor.getColumnIndexOrThrow("is_liked")) == 1
                ))
            } while (cursor.moveToNext())
        }

        cursor.close()
        return posts
    }

    fun clearCachedPosts() {
        writableDatabase.delete("cached_posts", null,null)
       }
    fun cacheDM(userId: String, username: String, pfp: String?) {
        try {
            val db = writableDatabase
            val values = ContentValues().apply {
                put("user_id", userId)
                put("username", username)
                put("pfp", pfp)
            }
            db.insertWithOnConflict("cached_dms", null, values, SQLiteDatabase.CONFLICT_REPLACE)
        } catch (e: Exception) {
            Log.e("DBHelper", "Error caching DM", e)
        }
    }

    fun getCachedDMs(): List<ModelDMs> {
        val dmList = mutableListOf<ModelDMs>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM cached_dms", null)

        if (cursor.moveToFirst()) {
            do {
                val userId = cursor.getString(cursor.getColumnIndexOrThrow("user_id"))
                val username = cursor.getString(cursor.getColumnIndexOrThrow("username"))
                val pfp      = cursor.getString(cursor.getColumnIndexOrThrow("pfp"))
                dmList.add( ModelDMs(
                                      image        = R.drawable.pf6,
                                   name         = username,
                                       userId       = userId,
                                   profileImage = if (pfp.isNullOrEmpty()) null else pfp
                               ))
            } while (cursor.moveToNext())
        }

        cursor.close()
        return dmList
    }

    fun clearCachedDMs() {
        writableDatabase.delete("cached_dms", null, null)
    }


    fun cacheMessage(msg: ModelChat) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("id", msg.id)
            put("sender_id", msg.senderId)
            put("receiver_id", msg.receiverId)
            put("message", msg.message)
            put("timestamp", msg.timestamp)
        }
        db.insertWithOnConflict(
            "cached_messages",
            null,
            values,
            SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    /** Retrieve cached messages between two users */
    fun getCachedMessages(userA: String, userB: String): List<ModelChat> {
        val list = mutableListOf<ModelChat>()
        val db = readableDatabase
        val sql = "SELECT * FROM cached_messages WHERE " +
                "(sender_id = ? AND receiver_id = ?) OR (sender_id = ? AND receiver_id = ?) " +
                "ORDER BY timestamp ASC"
        val cursor = db.rawQuery(
            sql,
            arrayOf(userA, userB, userB, userA)
        )
        cursor.use {
            if (it.moveToFirst()) {
                do {
                    list.add(
                        ModelChat(
                            id         = it.getString(it.getColumnIndexOrThrow("id")),
                            message    = it.getString(it.getColumnIndexOrThrow("message")),
                            timestamp  = it.getLong(it.getColumnIndexOrThrow("timestamp")),
                            senderId   = it.getString(it.getColumnIndexOrThrow("sender_id")),
                            receiverId = it.getString(it.getColumnIndexOrThrow("receiver_id")),
                            vanish     = false
                        )
                    )
                } while (it.moveToNext())
            }
        }
        return list
    }
    fun clearCachedMessages(userA: String? = null, userB: String? = null) {
        val db = writableDatabase
        if (userA != null && userB != null) {
            db.delete(
                "cached_messages",
                "(sender_id = ? AND receiver_id = ?) OR (sender_id = ? AND receiver_id = ?)",
                arrayOf(userA, userB, userB, userA)
            )
        } else {
            db.delete("cached_messages", null, null)
        }
    }
}

package com.queentylion.sibitranslator.database

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.queentylion.sibitranslator.types.Users

class UsersRepository(
    private val database: DatabaseReference
) {
    private val TAG = "UsersRepository"

    fun writeNewUsers(userId: String? = null, username: String? = null) {
        if (userId == null) {
            Log.e(TAG, "userId is null")
            return
        }

        val userQuery = database.child("users").orderByChild("userId").equalTo(userId)

        userQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    // User with the same userId already exists, handle accordingly
                    Log.w(TAG, "User with userId $userId already exists.")
                    // You can choose to update existing data or take other actions here
                } else {
                    val user = Users(userId, username) // Assuming Users class has userId field
                    val userValues = user.toMap()

                    val childUpdates = HashMap<String, Any>()
                    childUpdates["/users/$userId"] = userValues

                    database.updateChildren(childUpdates)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e(TAG, "Database error: ${databaseError.message}")
            }
        })
    }
}
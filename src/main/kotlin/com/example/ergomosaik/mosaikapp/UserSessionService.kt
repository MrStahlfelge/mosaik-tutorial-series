package com.example.ergomosaik.mosaikapp

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class UserSessionService {
    // this is a very basic user session service that holds some data for an anon user, identified
    // simply by a UUID. This is absolutely not how you would do that, it doesn't scale when using
    // multiple backends and the data is destroyed when server has a black out.
    // But we do this here in the example for simplicity

    private val userDataMap: HashMap<String, UserData> = HashMap()
    private val logger = LoggerFactory.getLogger(UserSessionService::class.java)
    fun getUserData(sessionId: String): UserData {
        synchronized(userDataMap) {
            val userData =
                if (userDataMap.containsKey(sessionId)) userDataMap[sessionId]!!
                else {
                    val newUser = UserData()
                    userDataMap[sessionId] = UserData()
                    newUser
                }
            userData.setActiveNow()
            return userData
        }
    }

    @Scheduled(fixedRate = 1000 * 60 * 60)
    private fun deleteInactiveUserData() {
        // deletes all user entries inactive for half an hour, runs every hour
        synchronized(userDataMap) {
            val userSet = userDataMap.entries
            val iterator = userSet.iterator()
            while (iterator.hasNext()) {
                val (key, user) = iterator.next()
                if (user.lastActiveMs < System.currentTimeMillis() - 1000L * 60 * 30) {
                    logger.info("Removing user data for session $key")
                    iterator.remove()
                }
            }
        }
    }
}

class UserData {
    var lastActiveMs: Long = 0 // used to delete the data after some time
        private set
    var p2pkAddress: String? = null
    fun setActiveNow() {
        lastActiveMs = System.currentTimeMillis()
    }
}

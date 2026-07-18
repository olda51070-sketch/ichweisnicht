package com.example.data

import kotlinx.coroutines.flow.Flow

class PracticeRepository(private val dao: PracticeSessionDao) {
    val allSessions: Flow<List<PracticeSession>> = dao.getAllSessions()

    suspend fun insert(session: PracticeSession): Long {
        return dao.insertSession(session)
    }

    suspend fun delete(session: PracticeSession) {
        dao.deleteSession(session)
    }

    suspend fun deleteById(id: Int) {
        dao.deleteSessionById(id)
    }
}

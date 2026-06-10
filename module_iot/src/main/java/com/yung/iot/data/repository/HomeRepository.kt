package com.yung.iot.data.repository

import android.content.Context
import com.yung.iot.data.db.IotDatabase
import com.yung.iot.data.db.RoomEntity
import com.yung.iot.data.mapper.toModel
import com.yung.iot.data.model.IotHome
import com.yung.iot.data.model.IotRoom
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class HomeRepository private constructor(
    context: Context,
) {
    private val roomDao = IotDatabase.get(context).roomDao()

    val defaultHome = IotHome(
        homeId = DEFAULT_HOME_ID,
        name = "我的家",
    )

    fun observeRooms(homeId: String = DEFAULT_HOME_ID): Flow<List<IotRoom>> {
        return roomDao.observeByHome(homeId).map { entities ->
            entities.map { it.toModel() }
        }
    }

    suspend fun ensureSeedData() {
        roomDao.insertAll(
            listOf(
                RoomEntity("room_living", DEFAULT_HOME_ID, "客厅", 0),
                RoomEntity("room_bedroom", DEFAULT_HOME_ID, "卧室", 1),
                RoomEntity("room_kitchen", DEFAULT_HOME_ID, "厨房", 2),
                RoomEntity("room_balcony", DEFAULT_HOME_ID, "阳台", 3),
            ),
        )
    }

    companion object {
        const val DEFAULT_HOME_ID = "home_default"

        @Volatile
        private var instance: HomeRepository? = null

        fun init(context: Context) {
            if (instance == null) {
                synchronized(this) {
                    if (instance == null) {
                        instance = HomeRepository(context.applicationContext)
                    }
                }
            }
        }

        fun getInstance(): HomeRepository {
            return instance ?: error("HomeRepository.init() must be called first")
        }
    }
}

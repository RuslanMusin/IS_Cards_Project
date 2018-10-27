package com.summer.itis.cardsproject.repository.database.base

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.summer.itis.cardsproject.model.common.Identified
import io.reactivex.Single

interface BaseRepository<Entity: Identified> {

    //CRUD
    fun findById(id: String): Single<Entity>

    fun createEntity(entity: Entity): Single<Boolean>

    fun updateEntity(entity: Entity): Single<Boolean>

    fun deleteEntity(entity: Entity): Single<Boolean>

    //other
    fun getKey(): String?

    fun setKey(entity: Entity)

    fun getMapValues(entity: Entity): HashMap<String, Any?>

    fun getValueFromSnapshot(dataSnapshot: DataSnapshot): Entity?

    fun findAll(): Single<List<Entity>>

    fun findEntitiesByIds(ids: List<String>): Single<List<Entity>>

    fun findAllOfReference(reference: DatabaseReference): Single<List<Entity>>

    fun findEntityByFieldValue(field: String, value: String): Single<List<Entity>>

    fun findByQueryField(field: String, userQuery: String): Single<List<Entity>>

    fun createEntityValues(values: HashMap<String, Any?>): Single<Boolean>

    fun createEntityOfReference(reference: DatabaseReference, entity: Entity): Single<Boolean>

    fun createEntityValuesOfReference(reference: DatabaseReference, values: HashMap<String, Any?>): Single<Boolean>
}
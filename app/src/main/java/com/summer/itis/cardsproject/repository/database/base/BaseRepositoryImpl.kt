package com.summer.itis.cardsproject.repository.database.base

import android.util.Log
import com.google.firebase.database.*
import com.summer.itis.cardsproject.model.common.Identified
import com.summer.itis.cardsproject.repository.database.base.BaseRepository
import com.summer.itis.cardsproject.utils.Const
import com.summer.itis.cardsproject.utils.Const.TAG_LOG
import com.summer.itis.cardsproject.utils.RxUtils
import io.reactivex.Observable
import io.reactivex.Single

abstract class BaseRepositoryImpl<Entity: Identified>(): BaseRepository<Entity> {

    abstract val TABLE_NAME: String

    abstract val databaseReference: DatabaseReference

    abstract protected var createReference: DatabaseReference

    abstract override fun getValueFromSnapshot(dataSnapshot: DataSnapshot): Entity?

    abstract override fun getMapValues(entity: Entity): HashMap<String, Any?>

    override fun getKey(): String? {
        return databaseReference.push().key
    }

    override fun setKey(entity: Entity) {
        getKey()?.let {
            entity.id = it
        }
    }

    override fun findById(id: String): Single<Entity> {
        val query: Query = databaseReference.child(id)
        val single : Single<Entity> =  Single.create { e ->
            query.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if(dataSnapshot.exists()) {
                        val entity: Entity? = getValueFromSnapshot(dataSnapshot)
                        entity?.let { e.onSuccess(entity) }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })

        }
        return single.compose(RxUtils.asyncSingle())
    }

    override fun findEntitiesByIds(ids: List<String>): Single<List<Entity>> {
        val single: Single<List<Entity>> = Single.create{e ->
            Observable
                    .fromIterable(ids)
                    .flatMap {
                        this.findById(it).toObservable()
                    }
                    .toList()
                    .subscribe{entities ->
                        e.onSuccess(entities)
                    }
        }
        return single.compose(RxUtils.asyncSingle())
    }

    override fun findAll(): Single<List<Entity>> {
       return findAllOfReference(databaseReference)
    }

    override fun findAllOfReference(reference: DatabaseReference): Single<List<Entity>> {
        val single: Single<List<Entity>> = Single.create{e ->
            reference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val list: MutableList<Entity> = ArrayList()
                    for(snap in dataSnapshot.children) {
                        val entity: Entity? = getValueFromSnapshot(snap)
                        entity?.let { list.add(entity) }
                    }
                    e.onSuccess(list)
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })

        }
        return single.compose(RxUtils.asyncSingle())
    }

    override fun createEntity(entity: Entity): Single<Boolean> {
       return createEntityValues(getMapValues(entity))
    }

    override fun createEntityValues(values: HashMap<String, Any?>): Single<Boolean> {
        return createEntityValuesOfReference(createReference, values)
    }

    override fun createEntityOfReference(reference: DatabaseReference, entity: Entity): Single<Boolean> {
        return createEntityValuesOfReference(reference, getMapValues(entity))

    }

    override fun createEntityValuesOfReference(reference: DatabaseReference, values: HashMap<String, Any?>): Single<Boolean> {
        val single: Single<Boolean> = Single.create { e ->
            reference.updateChildren(values).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG_LOG,"created")
                    e.onSuccess(true)
                } else {
                    e.onSuccess(false)
                }
            }

        }
        return single.compose(RxUtils.asyncSingle())
    }

    override fun updateEntity(entity: Entity): Single<Boolean> {
        val single: Single<Boolean> = Single.create { e ->
            databaseReference.child(entity.id).setValue(entity).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    e.onSuccess(true)
                } else {
                    e.onSuccess(false)
                }
            }
        }
        return single.compose(RxUtils.asyncSingle())
    }

    override fun deleteEntity(entity: Entity): Single<Boolean> {
        val single: Single<Boolean> = Single.create { e ->
            databaseReference.child(entity.id).removeValue().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    e.onSuccess(true)
                } else {
                    e.onSuccess(false)
                }
            }
        }
        return single.compose(RxUtils.asyncSingle())
    }

    override fun findEntityByFieldValue(field: String, value: String): Single<List<Entity>> {
        val query: Query = databaseReference.orderByChild(field).equalTo(value)
        val single: Single<List<Entity>> = Single.create { e ->
            query.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                }

                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val list: MutableList<Entity> = ArrayList()
                    for (snapshot in dataSnapshot.children) {
                        val card = getValueFromSnapshot(snapshot)
                        card?.let { list.add(it) }
                    }
                    e.onSuccess(list)
                }
            })
        }
        return single.compose(RxUtils.asyncSingle())
    }

    override fun findByQueryField(field: String, userQuery: String): Single<List<Entity>> {
        val single: Single<List<Entity>> = Single.create { e ->
            val queryPart = userQuery.toLowerCase()
            val query = databaseReference.orderByChild(field).startAt(queryPart).endAt(queryPart + Const.QUERY_END)
            query.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val list: MutableList<Entity> = ArrayList()
                    for (snapshot in dataSnapshot.children) {
                        val card = getValueFromSnapshot(snapshot)
                        card?.let { list.add(it) }

                    }
                    e.onSuccess(list)
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
        }
        return single.compose(RxUtils.asyncSingle())
    }

}
package com.summer.itis.cardsproject.repository.database.base

import com.google.firebase.database.*
import com.summer.itis.cardsproject.model.common.Identified
import com.summer.itis.cardsproject.repository.database.base.BaseRepositoryImpl
import com.summer.itis.cardsproject.model.common.ElementId
import com.summer.itis.cardsproject.model.common.Relation
import com.summer.itis.cardsproject.utils.RxUtils
import io.reactivex.Single

abstract class RelationalRepository<Entity: Identified>(): BaseRepositoryImpl<Entity>() {

    private val FIELD_ID = "id"
    private val FIELD_RELATION = "relation"

    fun setIdRelation(id: String): HashMap<String, Any> {
        val result = HashMap<String, Any>()
        result[FIELD_ID] = id
        return result
    }

    fun setFullRelation(relation: Relation?): Map<String, Any>? {
        var result: Map<String, Any>? = null
        if(relation != null) {
            result = HashMap<String, Any>()
            result[FIELD_ID] = relation.id
            result[FIELD_RELATION] = relation.relation
        }
        return result
    }

    fun createRelation(reference: DatabaseReference, id: String, status: String): Relation {
        val relation = Relation()
        relation.id = id
        relation.relation = status
        reference.setValue(relation)
        return relation
    }

    fun findRelativeIds(id: String, tableName: String): Single<List<String>> {
        return findRelativeIdsByRef(databaseReference.root.child(tableName).child(id))
    }

    fun findRelativeIdsByRef(reference: Query): Single<List<String>> {
        val single: Single<List<String>> =  Single.create { e ->
            reference.addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val elementIds: MutableList<String> = ArrayList()
                    for (snapshot in dataSnapshot.children) {
                        val elementId = snapshot.getValue(ElementId::class.java)
                        elementId?.let { elementIds.add(it.id) }
                    }
                    e.onSuccess(elementIds.toList())

                }

                override fun onCancelled(p0: DatabaseError) {
                }
            })
        }
        return single.compose(RxUtils.asyncSingle())
    }

    fun findRelations(id: String, tableName: String): Single<List<Relation>> {
        val single: Single<List<Relation>> = Single.create { e ->
            val query: Query = databaseReference.root.child(tableName).child(id)
            query.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val relations: MutableList<Relation> = ArrayList()
                    for (snapshot in dataSnapshot.children) {
                        val relation = snapshot.getValue(Relation::class.java)
                        relation?.let { relations.add(relation) }
                    }
                    e.onSuccess(relations)
                }
            })
        }
        return single.compose(RxUtils.asyncSingle())
    }

    fun findRelationsIdByValues(id: String, tableName: String, values: List<String>, isAny: Boolean): Single<List<String>> {
        val single: Single<List<String>> = Single.create { e ->
            findRelations(id, tableName).subscribe{ relations ->
                val list: MutableList<String> = java.util.ArrayList()
                for(rel in relations) {
                    if(isAny && values.contains(rel.relation)) {
                        list.add(rel.id)
                    } else if(!isAny) {
                        list.add(rel.id)
                        for(value in values) {
                            if(!rel.relation.equals(value)) {
                                list.remove(rel.id)
                                break
                            }
                        }
                    }
                }
                e.onSuccess(list)
            }
        }
        return single.compose(RxUtils.asyncSingle())
    }

}
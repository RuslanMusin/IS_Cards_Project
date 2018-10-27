package com.summer.itis.cardsproject.repository.database.base

import com.summer.itis.cardsproject.model.common.Identified
import com.summer.itis.cardsproject.repository.database.base.RelationalRepository
import com.summer.itis.cardsproject.utils.Const.SEP

abstract class ChildRelativeRepository<Entity: Identified>(): RelationalRepository<Entity>() {

    fun setRelativeMap(commonMap: HashMap<String, Any>, entity: Entity) {
        commonMap[TABLE_NAME + SEP + entity.id] = getMapValues(entity)
    }

}
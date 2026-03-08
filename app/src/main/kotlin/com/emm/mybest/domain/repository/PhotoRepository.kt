package com.emm.mybest.domain.repository

import com.emm.mybest.domain.models.ProgressPhoto
import kotlinx.coroutines.flow.Flow

interface PhotoRepository {
    fun getAllPhotos(): Flow<List<ProgressPhoto>>
}

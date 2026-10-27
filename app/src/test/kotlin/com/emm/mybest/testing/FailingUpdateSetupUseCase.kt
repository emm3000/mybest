package com.emm.mybest.testing

import com.emm.mybest.domain.usecase.UpdateSetupUseCase
import io.mockk.coEvery
import io.mockk.mockk

fun failingUpdateSetupUseCase(error: Throwable): UpdateSetupUseCase {
    val update = mockk<UpdateSetupUseCase>()
    coEvery { update(any()) } throws error
    return update
}

package com.unilumin.smartapp.ui.viewModel.pages

import androidx.paging.PagingSource
import androidx.paging.PagingState

/**
 * 通用分页加载器
 * @param T 数据模型类型
 * @param requestBlock 执行具体请求的 Lambda 表达式，接收 (page, pageSize)
 */
class GenericPagingSource<T : Any>(
    private val requestBlock: suspend (Int, Int) -> List<T>
) : PagingSource<Int, T>() {

    override fun getRefreshKey(state: PagingState<Int, T>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, T> {
        val page = params.key ?: 1
        val pageSize = params.loadSize
        return try {
            val responseData = requestBlock(page, pageSize)
            LoadResult.Page(
                data = responseData,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (responseData.isEmpty() || responseData.size < pageSize) null else page + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}
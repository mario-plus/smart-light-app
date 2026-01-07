package com.unilumin.smartapp.ui.viewModel.pages

import android.content.Context
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.unilumin.smartapp.client.UniCallbackService
import com.unilumin.smartapp.client.data.PageResponse
import com.unilumin.smartapp.client.data.SiteInfo
import com.unilumin.smartapp.client.service.SiteService
/**
 * 站点分页数据
 * */
class SitePagingSource(
    private val siteService: SiteService,
    private val context: Context,
    private val roadId: String?,
    private val keyword: String?
) : PagingSource<Int, SiteInfo>() {

    override fun getRefreshKey(state: PagingState<Int, SiteInfo>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, SiteInfo> {
        val page = params.key ?: 1 // 默认第一页
        val pageSize = params.loadSize
        return try {
            // 调用 API
            val rawResponse = siteService.getSiteList(
                curPage = page,
                pageSize = pageSize,
                roadIdList = roadId?.toLongOrNull()?.let { listOf(it) },
                tagCondition = "or",
                keyword = keyword.toString()
            )
            var parseDataNewSuspend =
                UniCallbackService<PageResponse<SiteInfo>>().parseDataNewSuspend(
                    rawResponse,
                    context
                )
            val data: List<SiteInfo>? = parseDataNewSuspend?.list

            val prevKey = if (page > 1) page - 1 else null
            val nextKey = if (data?.isNotEmpty() != false) page + 1 else null
            LoadResult.Page(
                data = data as List<SiteInfo>,
                prevKey = prevKey,
                nextKey = nextKey
            )
        } catch (e: Exception) {
            e.printStackTrace()
            LoadResult.Error(e)
        }
    }
}